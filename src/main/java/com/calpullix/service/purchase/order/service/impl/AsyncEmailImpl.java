package com.calpullix.service.purchase.order.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.calpullix.service.purchase.order.service.AsyncEmail;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AsyncEmailImpl implements AsyncEmail {

	private static final String LOGO_MAIl = "logo_CalpulliX.png";
	
	private static final String EMAIL_TEMPLATE = "emailTemplate.html";

	private static final String EMAIL_FROM = "no-reply@calpullix.com";
	
	private static final String PURCHASE_ORDER_ATTACH = "ordenCompra.pdf";

	private static final String PURCHASE_ORDER_TEMPLATE = "${purchaseOrder}";
	
	private static final String NAME_KEY_TEMPLATE = "${name}";

	private static final String DATE_KEY_TEMPLATE = "${date}";

	@Autowired
	private JavaMailSender sender;
	
	@Value("${app.subject-email}")
	private String subjectEmail;

	@Value("${app.id-image}")
	private String idImage;

	
	@Override
	@Async
	@Timed(value = "calpullix.service.email.purchaseorder.metrics", 
	description = "Email purchase order")
	public void sendEmail(
			ByteArrayOutputStream baos,
			String date,
			String purchaseOrder, 
			String name, 
			String email) throws MessagingException {
		log.info(":: Sending email restart password ");
		final MimeMessage message = sender.createMimeMessage();
		final MimeMessageHelper helper = new MimeMessageHelper(message, Boolean.TRUE);

		final ClassPathResource htmlFile = new ClassPathResource(EMAIL_TEMPLATE);
		String htmlContent = "";
		try {
			final Map<String, String> values = new HashMap<>();
			values.put(NAME_KEY_TEMPLATE, name);
			values.put(DATE_KEY_TEMPLATE, date);
			values.put(PURCHASE_ORDER_TEMPLATE, purchaseOrder);
			htmlContent = readFile(htmlFile.getFile(), values);
			log.info(":: HTML {} ", htmlContent);
		} catch (Exception e) {
			log.error(":: Error File Mail ", e);
			e.printStackTrace();
		}
		helper.setFrom(EMAIL_FROM);
		helper.setTo(email);
		helper.setText(htmlContent, true);
		helper.setSubject(subjectEmail);
			
		
		try {
			baos.close();
			final InputStream inputStream = new ByteArrayInputStream(baos.toByteArray()); 
			inputStream.close();
			helper.addAttachment(PURCHASE_ORDER_ATTACH, new ByteArrayResource(
					IOUtils.toByteArray(inputStream)));
		} catch (IOException e) {
			log.error(":: Error purchase order email", e);
		}
		

		final ClassPathResource file = new ClassPathResource(LOGO_MAIl);
		helper.addInline(idImage, file);

		sender.send(message);
		
		log.info(":: Change password email has been sended ");
	}
	
	private String readFile(File file, Map<String, String> values) throws IOException {
		String result = new String();
		final Path path = Paths.get(file.getAbsolutePath());
		final List<String> lines = Files.readAllLines(path);
		for (final String line : lines) {
			result+=line;
		}
		for (final String key : values.keySet()) {
			if (result.contains(key)) {
				result = result.replace(key, values.get(key));
			} 
		}
		return result;
	}


}
