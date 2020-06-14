package com.calpullix.service.purchase.order.service;

import java.io.ByteArrayOutputStream;

import javax.mail.MessagingException;

public interface AsyncEmail {

	void sendEmail(ByteArrayOutputStream baos, String date, String purchaseOrder, String name, String email)
			throws MessagingException;

}
