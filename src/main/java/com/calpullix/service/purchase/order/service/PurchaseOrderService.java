package com.calpullix.service.purchase.order.service;

import com.calpullix.service.purchase.order.model.PurchaseListOrderResponseDTO;
import com.calpullix.service.purchase.order.model.PurchaseOrderDetailResponseDTO;
import com.calpullix.service.purchase.order.model.PurchaseOrderRequestDTO;

public interface PurchaseOrderService {

	PurchaseListOrderResponseDTO getPurchaseOrder(PurchaseOrderRequestDTO request);
	
	PurchaseOrderDetailResponseDTO getPurchaseOrderDetail(PurchaseOrderRequestDTO request);
	
	PurchaseListOrderResponseDTO getPurchaseOrderStatus();
	
	PurchaseOrderRequestDTO updatePurchaseOrder(PurchaseOrderRequestDTO request);
	
	byte[] getPurchaseOrderPdf(Integer idPurchaseOrder);
	
}
