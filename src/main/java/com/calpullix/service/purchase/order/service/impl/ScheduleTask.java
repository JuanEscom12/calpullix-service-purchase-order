package com.calpullix.service.purchase.order.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.calpullix.db.process.branch.model.Branch;
import com.calpullix.db.process.branch.repository.BranchRepository;
import com.calpullix.db.process.catalog.model.ProductBranchStatus;
import com.calpullix.db.process.catalog.model.PurchaseOrderStatus;
import com.calpullix.db.process.product.model.Product;
import com.calpullix.db.process.product.repository.ProductBranchRepository;
import com.calpullix.db.process.product.repository.ProductRepository;
import com.calpullix.db.process.purchaseorder.model.Purchaseorder;
import com.calpullix.db.process.purchaseorder.repository.PurchaseOrderRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScheduleTask {

	private static final String SECHEDULING_EXPRESSION = "${app.scheduling-expression}";
	
	private static final int MAX_LENGTH_DESCRIPTION  = 50;
	
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductBranchRepository productBranchRepository;

	@Autowired
	private PurchaseOrderRepository purchaseOrderRepository; 
	
	@Autowired
	private BranchRepository branchRepository;

	private SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");

	@Value("${app.purchase-order-descrption}")
	private String purchaseOrderDescrption;

	@Value("${app.number-extra-products}")
	private int numberExtraProducts;

	@Scheduled(cron = SECHEDULING_EXPRESSION)
	public void processPurchaseOrders() {
		log.info(":: Process purchase orders ");
		final List<Branch> branches = branchRepository.findAll();
		final List<Product> products = productRepository.findAll();
		int totalPoductsBranch;
		Purchaseorder purchaseOrder;
		Calendar date = Calendar.getInstance();
		String description;
		for (final Branch branch : branches) {
			for (final Product product : products) {
				totalPoductsBranch = productBranchRepository.getNumberProductsByIdbranchAndIdproductAndStatus(product,
						branch, ProductBranchStatus.ON_SALE.getId());
				if (totalPoductsBranch < product.getQuantitylowerlimit()) {
					purchaseOrder = new Purchaseorder();
					purchaseOrder.setIdproduct(product);
					purchaseOrder.setCreationdate(formatDate.format(date.getTime()));
					description = purchaseOrderDescrption + ": " + branch.getName() + ", " + product.getName();
					purchaseOrder.setDescription(description.length() <= MAX_LENGTH_DESCRIPTION ? description : 
						description.substring(0, MAX_LENGTH_DESCRIPTION));
					purchaseOrder.setIdbranch(branch);
					purchaseOrder
							.setQuantity(product.getQuantitylowerlimit() - totalPoductsBranch + numberExtraProducts);
					purchaseOrder.setStatus(PurchaseOrderStatus.CREATED);
					purchaseOrderRepository.save(purchaseOrder);
				}
			}
		}
		log.info(":: Ends process purchase orders ");
	}
}
