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
public class PurchaseOrderStatusDTO {
	
	private Integer id;
	
	private Integer value;
	
	private String name;

}
