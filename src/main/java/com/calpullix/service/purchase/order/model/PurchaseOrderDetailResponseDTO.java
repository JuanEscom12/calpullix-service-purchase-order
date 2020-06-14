package com.calpullix.service.purchase.order.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PurchaseOrderDetailResponseDTO {
	
	private Integer idPurchaseOrder;
	
	private String rfc;
	
	private String brand;
	
	private String pdfUrl;

	private BigDecimal unitPrice;
	
	private String vendor;
	
	private String vendorContact;
	
	private String telephone;
	
	private BigDecimal totalPrice;
	
	private Integer quantityItems;
	
	private String mapsQuery;
	
	private byte[] picture;
	
	private Boolean isActive;
	
}
