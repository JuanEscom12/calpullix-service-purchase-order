package com.calpullix.service.purchase.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calpullix.service.purchase.order.model.PurchaseListOrderResponseDTO;
import com.calpullix.service.purchase.order.model.PurchaseOrderDetailResponseDTO;
import com.calpullix.service.purchase.order.model.PurchaseOrderRequestDTO;
import com.calpullix.service.purchase.order.service.PurchaseOrderService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/calpullix")
@Slf4j
public class PurchaseOrderController {

	private static final String PATH_RETRIEVE_PURCHASE_ORDER = "${app.path-retrieve-purchaseorder}";

	private static final String PATH_RETRIEVE_PURCHASE_ORDER_DETAIL = "${app.path-retrieve-purchaseorder-detail}";

	private static final String PATH_UPDATE_PURCHASE_ORDER = "${app.path-update-purchaseorder}";

	private static final String PATH_RETRIEVE_PURCHASE_ORDER_STATUS = "${app.path-retrieve-purchaseorder-status}";

	private static final String PATH_RETRIEVE_PURCHASE_ORDER_PDF = "${app.path-retrieve-purchaseorder-pdf}";

	@Autowired
	private PurchaseOrderService purchaseOrderService;

	
	@PostMapping(value = PATH_RETRIEVE_PURCHASE_ORDER, produces = "application/json")
	public ResponseEntity<PurchaseListOrderResponseDTO> getPurchaseOrder(@RequestBody PurchaseOrderRequestDTO request) {
		log.info(":: Retrieve Purchase Order Controller {} ", request);
		return new ResponseEntity<>(purchaseOrderService.getPurchaseOrder(request), HttpStatus.OK);
	}

	
	@PostMapping(value = PATH_RETRIEVE_PURCHASE_ORDER_DETAIL, produces = "application/json")
	public ResponseEntity<PurchaseOrderDetailResponseDTO> getPurchaseOrderDetail(
			@RequestBody PurchaseOrderRequestDTO request) {
		log.info(":: Retrieve Purchase Order Detail Handler {} ", request);
		return new ResponseEntity<>(purchaseOrderService.getPurchaseOrderDetail(request), HttpStatus.OK);
	}

	
	@PostMapping(value = PATH_UPDATE_PURCHASE_ORDER, produces = "application/json")
	public ResponseEntity<PurchaseOrderRequestDTO> updatePurchaseOrder(@RequestBody PurchaseOrderRequestDTO request) {
		log.info(":: Update Purchase Order Handler {} ", request);
		return new ResponseEntity<>(purchaseOrderService.updatePurchaseOrder(request), HttpStatus.OK);
	}
	

	
	@PostMapping(value = PATH_RETRIEVE_PURCHASE_ORDER_STATUS, produces = "application/json")
	public ResponseEntity<PurchaseListOrderResponseDTO> getPurchaseOrderStatus(
			@RequestBody PurchaseOrderRequestDTO request) {
		log.info(":: Get Purchase Order Status Handler {} ", request);
		return new ResponseEntity<>( purchaseOrderService.getPurchaseOrderStatus(), HttpStatus.OK);
	}
	
	
	
	@GetMapping(value = PATH_RETRIEVE_PURCHASE_ORDER_PDF, produces = "application/pdf")
	public ResponseEntity<byte[]> getPurchaseOrderPdf(
			@PathVariable(name = "idPurchaseOrder") Integer idPurchaseOrder) {
		log.info(":: Get Purchase Order Status Handler {} ", idPurchaseOrder);
		
		return new ResponseEntity<>(purchaseOrderService.getPurchaseOrderPdf(idPurchaseOrder), HttpStatus.OK);
	}

}
