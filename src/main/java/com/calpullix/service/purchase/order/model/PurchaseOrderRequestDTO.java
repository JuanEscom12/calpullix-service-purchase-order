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
public class PurchaseOrderRequestDTO {
	
	private Integer id;
	
	private Integer idPurchaseOrder;
	
	private Integer branchId;
	
	private String date;
	
	private String endDate;
	
	private Integer page;
	
	private Integer quantityItems;
	
	private BigDecimal totalPrice;
	
	private boolean updated;
	
	private Integer purchaseOrderStatus;

	
}
