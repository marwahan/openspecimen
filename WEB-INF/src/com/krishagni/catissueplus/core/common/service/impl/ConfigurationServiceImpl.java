package com.krishagni.catissueplus.core.common.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;

import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.domain.ConfigErrorCode;
import com.krishagni.catissueplus.core.common.domain.ConfigProperty;
import com.krishagni.catissueplus.core.common.domain.ConfigSetting;
import com.krishagni.catissueplus.core.common.domain.Module;
import com.krishagni.catissueplus.core.common.events.ConfigSettingDetail;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.service.ConfigChangeListener;
import com.krishagni.catissueplus.core.common.service.ConfigurationService;
import com.krishagni.catissueplus.core.common.util.AuthUtil;
import com.krishagni.catissueplus.core.common.util.Status;
import com.krishagni.catissueplus.core.common.util.Utility;

public class ConfigurationServiceImpl implements ConfigurationService, InitializingBean {
	
	private Map<String, List<ConfigChangeListener>> changeListeners = 
			new ConcurrentHashMap<String, List<ConfigChangeListener>>();
	
	private Map<String, Map<String, ConfigSetting>> configSettings;
	
	private DaoFactory daoFactory;
	
	private MessageSource messageSource;
		
	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}
	
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	@PlusTransactional
	public ResponseEvent<List<ConfigSettingDetail>> getSettings(RequestEvent<String> req) {
		String module = req.getPayload();
		Map<String, ConfigSetting> moduleSettings = configSettings.get(module);
		if (moduleSettings == null) {
			moduleSettings = Collections.emptyMap();
		}
		
		return ResponseEvent.response(ConfigSettingDetail.from(moduleSettings.values()));
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<ConfigSettingDetail> saveSetting(RequestEvent<ConfigSettingDetail> req) {
		ConfigSettingDetail detail = req.getPayload();
		
		String module = detail.getModule();
		Map<String, ConfigSetting> moduleSettings = configSettings.get(module);
		if (moduleSettings == null || moduleSettings.isEmpty()) {
			return ResponseEvent.userError(ConfigErrorCode.MODULE_NOT_FOUND);
		}
		
		String prop = detail.getName();
		ConfigSetting existing = moduleSettings.get(prop);
		if (existing == null) {
			return ResponseEvent.userError(ConfigErrorCode.SETTING_NOT_FOUND);
		}
		
		String setting = detail.getValue();
		if (!isValidSetting(existing.getProperty(), setting)) {
			return ResponseEvent.userError(ConfigErrorCode.INVALID_SETTING_VALUE);
		}
		
		ConfigSetting newSetting = new ConfigSetting();
		newSetting.setProperty(existing.getProperty());
		newSetting.setActivatedBy(AuthUtil.getCurrentUser());
		newSetting.setActivationDate(Calendar.getInstance().getTime());
		newSetting.setActivityStatus(Status.ACTIVITY_STATUS_ACTIVE.getStatus());
		newSetting.setValue(setting);
				
		existing.setActivityStatus(Status.ACTIVITY_STATUS_DISABLED.getStatus());
		
		daoFactory.getConfigSettingDao().saveOrUpdate(existing);
		daoFactory.getConfigSettingDao().saveOrUpdate(newSetting);		
		moduleSettings.put(prop, newSetting);
		
		notifyListeners(module, prop, setting);
		return ResponseEvent.response(ConfigSettingDetail.from(newSetting));
	}
	
	@Override
	@PlusTransactional
	public Integer getIntSetting(String module, String name, Integer... defValue) {
		String value = getStrSetting(module, name, (String)null);
		if (StringUtils.isBlank(value)) {
			return defValue != null && defValue.length > 0 ? defValue[0] : null;
		}
		
		return Integer.parseInt(value);
	}

	@Override
	@PlusTransactional
	public Double getFloatSetting(String module, String name, Double... defValue) {
		String value = getStrSetting(module, name, (String)null);
		if (StringUtils.isBlank(value)) {
			return defValue != null && defValue.length > 0 ? defValue[0] : null;
		}
		
		return Double.parseDouble(value);
	}

	@Override
	@PlusTransactional
	public String getStrSetting(String module, String name,	String... defValue) {
		Map<String, ConfigSetting> moduleSettings = configSettings.get(module);
		
		String value = null;
		if (moduleSettings != null) {
			ConfigSetting setting = moduleSettings.get(name);
			if (setting != null) {
				value = setting.getValue();
			}
		}
		
		if (StringUtils.isBlank(value)) {
			value = defValue != null && defValue.length > 0 ? defValue[0] : null;
		} 
		
		return value;
	}
	
	@Override
	@PlusTransactional
	public void reload() {
		Map<String, Map<String, ConfigSetting>> settingsMap = new ConcurrentHashMap<String, Map<String, ConfigSetting>>();
		
		List<ConfigSetting> settings = daoFactory.getConfigSettingDao().getAllSettings();
		for (ConfigSetting setting : settings) {
			ConfigProperty prop = setting.getProperty();			
			Hibernate.initialize(prop.getAllowedValues()); // pre-init
						
			Module module = prop.getModule();
			
			Map<String, ConfigSetting> moduleSettings = settingsMap.get(module.getName());
			if (moduleSettings == null) {
				moduleSettings = new ConcurrentHashMap<String, ConfigSetting>();
				settingsMap.put(module.getName(), moduleSettings);
			}
			
			moduleSettings.put(prop.getName(), setting);			
		}
		
		this.configSettings = settingsMap;
		
		for (List<ConfigChangeListener> listeners : changeListeners.values()) {
			for (ConfigChangeListener listener : listeners) {
				listener.onConfigChange(null, null);
			}			
		}
	}

	@Override
	public void registerChangeListener(String module, ConfigChangeListener callback) {
		List<ConfigChangeListener> listeners = changeListeners.get(module);
		if (listeners == null) {
			listeners = new ArrayList<ConfigChangeListener>();
			changeListeners.put(module, listeners);
		}
		
		listeners.add(callback);
	}
	
	@Override
	public Map<String, Object> getLocaleSettings() {
		Map<String, Object> result = new HashMap<String, Object>();
		
		Locale locale = Locale.getDefault();
		result.put("locale", locale.toString());
		result.put("dateFmt", messageSource.getMessage("common_date_fmt", null, locale));
		result.put("timeFmt", messageSource.getMessage("common_time_fmt", null, locale));
		result.put("deFeDateFmt", messageSource.getMessage("common_de_fe_date_fmt", null, locale));
		result.put("deBeDateFmt", messageSource.getMessage("common_de_be_date_fmt", null, locale));
		result.put("utcOffset", Utility.getTimezoneOffset());
		return result;
	}
		
	@Override
	public String getDeDateFormat() {		
		return messageSource.getMessage("common_de_be_date_fmt", null, Locale.getDefault());
	}

	@Override
	public String getTimeFormat() {
		return messageSource.getMessage("common_time_fmt", null, Locale.getDefault());
	}

	@Override
	public String getDeDateTimeFormat() {
		return getDeDateFormat() + " " + getTimeFormat();
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		reload();
		
		setLocale();
		registerChangeListener("common", new ConfigChangeListener() {			
			@Override
			public void onConfigChange(String name, String value) {				
				if (name.equals("locale")) {
					setLocale();
				}
			}
		});
	}
	
	private boolean isValidSetting(ConfigProperty property, String setting) {
		if (StringUtils.isBlank(setting)) {
			return true;
		}
		
		Set<String> allowedValues = property.getAllowedValues();
		if (CollectionUtils.isNotEmpty(allowedValues) && !allowedValues.contains(setting)) {
			return false;
		}
				
		try {
			switch (property.getDataType()) {
				case BOOLEAN:
					Boolean.parseBoolean(setting);
					break;
					
				case FLOAT:
					Double.parseDouble(setting);
					break;
					
				case INT:
					Integer.parseInt(setting);
					break;
					
				default:
					break;
			}
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private void notifyListeners(String module, String property, String setting) {
		List<ConfigChangeListener> listeners = changeListeners.get(module);
		if (listeners == null) {
			return;
		}
		
		for (ConfigChangeListener listener : listeners) {
			listener.onConfigChange(property, setting);
		}
	}
	
	private void setLocale() {		
		String setting = getStrSetting("common", "locale", "en_US");
		Locale newLocale = LocaleUtils.toLocale(setting);
		Locale existingLocale = Locale.getDefault();
		
		if (!existingLocale.equals(newLocale)) {
			Locale.setDefault(newLocale);
		}
	}
}
