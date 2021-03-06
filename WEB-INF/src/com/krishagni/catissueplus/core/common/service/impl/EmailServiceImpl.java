package com.krishagni.catissueplus.core.common.service.impl;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.krishagni.catissueplus.core.common.domain.Email;
import com.krishagni.catissueplus.core.common.service.ConfigChangeListener;
import com.krishagni.catissueplus.core.common.service.ConfigurationService;
import com.krishagni.catissueplus.core.common.service.EmailService;
import com.krishagni.catissueplus.core.common.service.TemplateService;

public class EmailServiceImpl implements EmailService, ConfigChangeListener, InitializingBean {
	private static Log LOGGER = LogFactory.getLog(EmailServiceImpl.class);
	
	private static final String MODULE = "email";
	
	private static final String TEMPLATE_SOURCE = "email-templates/";
	
	private static final String BASE_TMPL = "baseTemplate.vm";
	
	private static final String FOOTER_TMPL = "footer.vm";
	
	private static String subjectPrefix;
	
	private JavaMailSender mailSender;
	
	private TemplateService templateService;
	
	private MessageSource messageSource;
	
	private ThreadPoolTaskExecutor taskExecutor;
	
	private ConfigurationService cfgSvc;

	public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
	
	public void setCfgSvc(ConfigurationService cfgSvc) {
		this.cfgSvc = cfgSvc;
	}
	
	@Override
	public void onConfigChange(String name, String value) {
		initializeMailSender();		
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initializeMailSender();
		cfgSvc.registerChangeListener(MODULE, this);		
	}
	
	private void initializeMailSender() {
		try {
			JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
			mailSender.setUsername(getAccountId());
			mailSender.setPassword(getAccountPassword());
			mailSender.setHost(getMailServerHost());
			mailSender.setPort(getMailServerPort());

			String startTlsEnabled = getStartTlsEnabled();
			String authEnabled = getAuthEnabled();
			if (StringUtils.isNotBlank(startTlsEnabled) && StringUtils.isNotBlank(authEnabled)) {
				Properties props = new Properties();
				props.put("mail.smtp.starttls.enable", startTlsEnabled);
				props.put("mail.smtp.auth", authEnabled);
				mailSender.setJavaMailProperties(props);
			}
			
			this.mailSender = mailSender;
		} catch (Exception e) {
			e.printStackTrace();
			new RuntimeException("Error while initialising java mail sender", e);
		}
	}
	
	@Override
	public boolean sendEmail(String emailTmplKey, String[] to, Map<String, Object> props) {
		return sendEmail(emailTmplKey, to, null, props);
	}
	
	@Override
	public boolean sendEmail(String emailTmplKey, String[] to, File[] attachments, Map<String, Object> props) {
		String adminEmailId = getAdminEmailId();
		
		props.put("template", getEmailTmpl(emailTmplKey));
		props.put("footer", getFooterTmpl());
		props.put("appUrl", getAppUrl());
		props.put("adminEmailAddress", adminEmailId);
		props.put("adminPhone", "1234567890");//TODO: will be replaced by property file
		String subject = getSubject(emailTmplKey, (String[]) props.get("$subject"));
		String content = templateService.render(getBaseTmpl(), props);
		
		Email email = new Email();
		email.setSubject(subject);
		email.setBody(content);
		email.setToAddress(to);
		email.setCcAddress(new String[] {adminEmailId});
		email.setAttachments(attachments);
		
		return sendEmail(email);
	}

	@Override
	public boolean sendEmail(Email mail) {
		try {
			final MimeMessage mimeMessage = mailSender.createMimeMessage();
			final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false, "UTF-8"); // true = multipart
			message.setSubject(mail.getSubject());
			message.setTo(mail.getToAddress());
			
			if (mail.getBccAddress() != null) {
				message.setBcc(mail.getBccAddress());
			}
			
			if (mail.getCcAddress() != null) {
				message.setCc(mail.getCcAddress());
			}
			
			message.setText(mail.getBody(), true); // true = isHtml
			message.setFrom(getAccountId());
			
			if (mail.getAttachments() != null) {
				for (File attachment: mail.getAttachments()) {
					FileSystemResource file = new FileSystemResource(attachment);
					message.addAttachment(file.getFilename(), file);
				}
			}
			
			taskExecutor.submit(new SendMailTask(mimeMessage));
			return true;
		} catch (Exception e) {
			LOGGER.error("Error sending e-mail", e);
			return false;
		}
		
	}
	
	private String getSubject(String emailTmplKey, String[] subjParams) {
		if (subjectPrefix == null) {
			subjectPrefix = messageSource.getMessage("email_subject_prefix", null, Locale.getDefault());
		}
		
		return subjectPrefix + messageSource.getMessage(emailTmplKey.toLowerCase() + "_subj", subjParams, Locale.getDefault());
	}
	
	private String getEmailTmpl(String emailTmplKey) {
		return getTmplSource() + emailTmplKey + ".vm";
	}
	
	private class SendMailTask implements Runnable {
		private MimeMessage mimeMessage;
		
		public SendMailTask(MimeMessage mimeMessage) {
			this.mimeMessage = mimeMessage;
		}
		
		public void run() {
			try{
				mailSender.send(mimeMessage);
			} catch(Exception e) {
				e.printStackTrace();
				new RuntimeException("Error while sending Email", e);
			}
		}
	}
	
	private String getTmplSource() {
		return TEMPLATE_SOURCE + Locale.getDefault().toString() + "/";
	}
	
	private String getBaseTmpl() {
		return getTmplSource() + BASE_TMPL;
	}
	
	private String getFooterTmpl() {
		return getTmplSource() + FOOTER_TMPL;
	}
	
	/**
	 *  Config helper methods
	 */
	private String getAccountId() {
		return cfgSvc.getStrSetting(MODULE, "account_id");
	}
	
	private String getAccountPassword() {
		return cfgSvc.getStrSetting(MODULE, "account_password");
	}
	
	private String getMailServerHost() {
		return cfgSvc.getStrSetting(MODULE, "server_host");
	}
	
	private Integer getMailServerPort() {
		return cfgSvc.getIntSetting(MODULE, "server_port", 25);
	}
	
	private String getStartTlsEnabled() {
		return cfgSvc.getStrSetting(MODULE, "starttls_enabled");
	}
	
	private String getAuthEnabled() {
		return cfgSvc.getStrSetting(MODULE, "auth_enabled");
	}
	
	private String getAdminEmailId() {
		return cfgSvc.getStrSetting(MODULE, "admin_email_id");
	}	
	
	private String getAppUrl() {
		return cfgSvc.getStrSetting("common", "app_url");
	}
}
