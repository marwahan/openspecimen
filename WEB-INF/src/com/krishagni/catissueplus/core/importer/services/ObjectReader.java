package com.krishagni.catissueplus.core.importer.services;

import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.util.CsvFileReader;
import com.krishagni.catissueplus.core.common.util.CsvReader;
import com.krishagni.catissueplus.core.common.util.Utility;
import com.krishagni.catissueplus.core.importer.domain.ImportJobErrorCode;
import com.krishagni.catissueplus.core.importer.domain.ObjectSchema;
import com.krishagni.catissueplus.core.importer.domain.ObjectSchema.Field;
import com.krishagni.catissueplus.core.importer.domain.ObjectSchema.Record;

public class ObjectReader implements Closeable {
	private static final String CLEAR_FIELD = "##os_clear##";
	
	private CsvReader csvReader;
	
	private ObjectSchema schema;
	
	private Class<?> objectClass;
	
	private String[] currentRow;
	
	private String dateFmt;
	
	private String timeFmt;
			
	public ObjectReader(String filePath, ObjectSchema schema, String dateFmt, String timeFmt) {
		try {
			this.csvReader = CsvFileReader.createCsvFileReader(filePath, true);
			this.schema = schema;
			if (StringUtils.isNotBlank(schema.getRecord().getName())) {
				this.objectClass = Class.forName(schema.getRecord().getName());
			}			
			this.dateFmt = dateFmt;
			this.timeFmt = timeFmt;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Object next() {
		if (csvReader.next()) {
			currentRow = csvReader.getRow();
			return parseObject();
		} else {
			currentRow = null;
			return null;
		}
	}
	
	public List<String> getCsvColumnNames() {
		return new ArrayList<String>(Arrays.asList(csvReader.getColumnNames()));
	}
	
	public List<String> getCsvRow() {
		return new ArrayList<String>(Arrays.asList(currentRow));
	}
	
	@Override
	public void close() throws IOException {
		csvReader.close();
	}
	
	public static String getSchemaFields(ObjectSchema schema) {
		List<String> columnNames = getSchemaFields(schema.getRecord(), "");
		return Utility.stringListToCsv(columnNames);
	}
			
	private Object parseObject() {
		try {
			Map<String, Object> objectProps = parseObject(schema.getRecord(), "");
			if (objectClass == null) {
				return objectProps;
			} else {
				return new ObjectMapper().convertValue(objectProps, objectClass);
			}			
		} catch (Exception e) {
			e.printStackTrace();
			throw OpenSpecimenException.userError(ImportJobErrorCode.RECORD_PARSE_ERROR, e.getLocalizedMessage());
		}
	}
	
	private Map<String, Object> parseObject(Record record, String prefix) 
	throws Exception {
		Map<String, Object> props = new HashMap<String, Object>();
		props.putAll(parseFields(record, prefix));
		
		if (record.getSubRecords() == null) {
			return props;
		}
		
		for (Record subRec : record.getSubRecords()) {
			Object subObjProps = parseSubObjects(subRec, prefix);
			if (isEmpty(subObjProps)) {
				continue;
			}
			
			props.put(subRec.getAttribute(), subObjProps);
		}
		
		return props;
	}
	
	private Map<String, Object> parseFields(Record record, String prefix) 
	throws Exception {
		Map<String, Object> props = new HashMap<String, Object>();
		
		for (Field field : record.getFields()) {
			if (field.isMultiple()) {
				List<Object> values = new ArrayList<Object>();
				for (int idx = 1; true; ++idx) {
					String columnName = prefix + field.getCaption() + "#" + idx;
					Object value = getValue(field, columnName);
					if (isClearField(value)) {
						values.clear();
						break;
					} else if (value == null) {
						break;
					}
					
					values.add(value);
				}
				
				props.put(field.getAttribute(), values);				
			} else {
				String columnName = prefix + field.getCaption();
				Object value = getValue(field, columnName);
				if (value != null) {
					props.put(field.getAttribute(), isClearField(value) ? null : value);
				}				
			}
		}
		
		return props;		
	}
	
	private Object parseSubObjects(Record record, String prefix) 
	throws Exception {		
		String newPrefix = prefix;
		if (StringUtils.isNotBlank(record.getCaption())) {
			newPrefix += record.getCaption() + "#";
		}
		
		Object result = null;
		if (record.isMultiple()) {
			List<Map<String, Object>> subObjects = new ArrayList<Map<String, Object>>();
			for (int idx = 1; true; ++idx) {
				Map<String, Object> subObject = parseObject(record, newPrefix + idx + "#");
				if (subObject.isEmpty()) {
					break;
				}
				
				subObjects.add(subObject);
			}			
			result = subObjects;
		} else {
			result = parseObject(record, newPrefix);
		}	
		
		return result;
	}
	
	private Object getValue(Field field, String columnName) 
	throws Exception {
		if (!csvReader.isColumnPresent(columnName)) {
			return null;
		}
		
		String value = csvReader.getColumn(columnName);
		boolean isBlank = StringUtils.isBlank(value);
		if (isBlank) {
			return CLEAR_FIELD;
		} else if (field.getType() != null && field.getType().equals("date")) {
			return new SimpleDateFormat(dateFmt).parse(value);
		} else if (field.getType() != null && field.getType().equals("datetime")) {
			return new SimpleDateFormat(dateFmt + " " + timeFmt).parse(value);
		} else {
			return value;
		}
	}
	
	private boolean isEmpty(Object obj) {
		if (obj instanceof List<?>) {
			List<?> list = (List<?>)obj;
			return list.isEmpty();
		} else if (obj instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>)obj;
			return map.isEmpty();
		}
		
		return false;
	}
	
	private static List<String> getSchemaFields(Record record, String prefix) {
		List<String> columnNames = new ArrayList<String>();
		
		for (Field field : record.getFields()) {
			String columnName = prefix + field.getCaption();
			if (field.isMultiple()) {
				columnNames.add(columnName + "#1");
				columnNames.add(columnName + "#2");
			} else {
				columnNames.add(columnName);
			}
		}
		
		if (record.getSubRecords() == null) {
			return columnNames;
		}
		
		for (Record subRecord : record.getSubRecords()) {
			String newPrefix = prefix;
			if (StringUtils.isNotBlank(subRecord.getCaption())) {
				newPrefix += subRecord.getCaption() + "#";
			}
			
			if (subRecord.isMultiple()) {
				columnNames.addAll(getSchemaFields(subRecord, newPrefix + "1#"));
				columnNames.addAll(getSchemaFields(subRecord, newPrefix + "2#"));
			} else {
				columnNames.addAll(getSchemaFields(subRecord, newPrefix));
			}			
		}
		
		return columnNames;				
	}
	
	private boolean isClearField(Object value) {
		return value instanceof String && ((String)value).trim().equals(CLEAR_FIELD);
	}
			
	public static void main(String[] args) 
	throws Exception {
		ObjectSchema containerSchema = ObjectSchema.parseSchema("/home/vpawar/work/ka/catp/os/WEB-INF/resources/com/krishagni/catissueplus/core/administrative/schema/container.xml");
		System.err.println("Container: " + ObjectReader.getSchemaFields(containerSchema));
		
		ObjectSchema instituteSchema = ObjectSchema.parseSchema("/home/vpawar/work/ka/catp/os/WEB-INF/resources/com/krishagni/catissueplus/core/administrative/schema/institute.xml");
		System.err.println("Institute: " + ObjectReader.getSchemaFields(instituteSchema));
		
		ObjectSchema siteSchema = ObjectSchema.parseSchema("/home/vpawar/work/ka/catp/os/WEB-INF/resources/com/krishagni/catissueplus/core/administrative/schema/site.xml");
		System.err.println("Site: " + ObjectReader.getSchemaFields(siteSchema));
		
		ObjectSchema userSchema = ObjectSchema.parseSchema("/home/vpawar/work/ka/catp/os/WEB-INF/resources/com/krishagni/catissueplus/core/administrative/schema/user.xml");
		System.err.println("User: " + ObjectReader.getSchemaFields(userSchema));

		ObjectSchema userRolesSchema = ObjectSchema.parseSchema("/home/vpawar/work/ka/catp/os/WEB-INF/resources/com/krishagni/catissueplus/core/administrative/schema/user-roles.xml");
		System.err.println("User Roles: " + ObjectReader.getSchemaFields(userRolesSchema));
		
	}
}