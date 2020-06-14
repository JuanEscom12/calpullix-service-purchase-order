package com.calpullix.service.purchase.order.model;

import java.util.List;

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
public class PurchaseListOrderResponseDTO {
	
	int totalRows;
	
	private List<PurchaseOrderResponseDTO> purchaseOrder;
	
	private List<PurchaseOrderStatusDTO> purchaseOrderStatus;

}
