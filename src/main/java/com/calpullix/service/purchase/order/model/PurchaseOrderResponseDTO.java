package com.calpullix.service.purchase.order.model;

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
public class PurchaseOrderResponseDTO {
	
	private Integer idProduct;
	
	private Integer idPurchaseOrder;
	
	private String date;
	
	private String name;
	
	private String description;
	
	private String branchName;
	
	private String status;
	
	private Integer numberStockProducts;
	
	private Integer numberShelfProducts;
	
	private Integer suggestedNumberItems;
	
	private Integer totalRows;
	
	private Boolean enabled;
	
	private Integer index;
}
