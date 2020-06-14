package com.calpullix.service.purchase.order.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.mail.MessagingException;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.calpullix.db.process.branch.model.Branch;
import com.calpullix.db.process.catalog.model.ProductLocation;
import com.calpullix.db.process.catalog.model.ProductStatus;
import com.calpullix.db.process.catalog.model.PurchaseOrderStatus;
import com.calpullix.db.process.product.model.Product;
import com.calpullix.db.process.product.repository.ProductBranchRepository;
import com.calpullix.db.process.product.repository.ProductHistoryRepository;
import com.calpullix.db.process.provider.model.Provider;
import com.calpullix.db.process.purchaseorder.model.Purchaseorder;
import com.calpullix.db.process.purchaseorder.repository.PurchaseOrderRepository;
import com.calpullix.service.purchase.order.model.PurchaseListOrderResponseDTO;
import com.calpullix.service.purchase.order.model.PurchaseOrderDetailResponseDTO;
import com.calpullix.service.purchase.order.model.PurchaseOrderRequestDTO;
import com.calpullix.service.purchase.order.model.PurchaseOrderResponseDTO;
import com.calpullix.service.purchase.order.model.PurchaseOrderStatusDTO;
import com.calpullix.service.purchase.order.service.AsyncEmail;
import com.calpullix.service.purchase.order.service.PurchaseOrderService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

	private static final String[] HEADERS = { "Cantidad", "Descripción", "Unidades", "Costo unitario" };

	private static final String PURCHASE_ORDER_IMG = "purchaseOrder.pdf";

	private static final String LOGO_IMG = "logo_CalpulliX.png";

	private static final String PURCHASE_ORDER_LABEL = "Orden de compra \n\n";

	private static final String DATE_LABEL = "Fecha:  ";

	private static final String NUMBER_PURCHASE_ORDER_LABEL = "No. Orden de compra: ";

	private static final String PROVIDER_LABEL = "Proveedor: ";

	private static final String TOTAL_LABEL = "\n Total:    ";

	private static final String PREPARE_FOR_LABEL = "\n Preparado por: ";

	private static final String SIGNATURE_LABEL = "\n\n Firma: ";

	private static final String HTTP_PROTOCOL = "http://";

	private static final String PORT = ":9100";

	@Autowired
	private PurchaseOrderRepository purchaseOrderRepository;

	@Autowired
	private ProductBranchRepository productBranchRepository;

	@Autowired
	private ProductHistoryRepository productHistoryRepository;

	@Autowired
	private AsyncEmail asyncEmail;

	private SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");

	@Value("${app.path-retrieve-pdf}")
	private String pathRetrievePurchaseOrderPdf;

	@Value("${app.pagination-size}")
	private Integer paginationSize;
	

	@SuppressWarnings("unused")
	private PurchaseListOrderResponseDTO defaultGetPurchaseOrder(PurchaseOrderRequestDTO request) {
		log.info(":::: Fallback Method Get purchase order service {} ", request);
		final PurchaseListOrderResponseDTO result = new PurchaseListOrderResponseDTO();
		result.setPurchaseOrder(new ArrayList<>());
		return result;
	}

//	@HystrixCommand(fallbackMethod = "defaultGetPurchaseOrder")
	@Override
	public PurchaseListOrderResponseDTO getPurchaseOrder(PurchaseOrderRequestDTO request) {
		log.info(":: Get purchase order service {} ", request);
//		if (true) {
//			try {
//				TimeUnit.SECONDS.sleep(3);
//				return new PurchaseListOrderResponseDTO();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		Calendar currentDate = Calendar.getInstance();
		if (isAfterDate(currentDate, request.getDate())) {
			request.setDate(formatDate.format(currentDate.getTime()));
			request.setEndDate(formatDate.format(currentDate.getTime()));
		} else if (isAfterDate(currentDate, request.getEndDate())) {
			request.setEndDate(formatDate.format(currentDate.getTime()));
		}
		final PurchaseListOrderResponseDTO result = new PurchaseListOrderResponseDTO();
		final Page<Purchaseorder> purchaseOrderList = getPurchaseorderList(request, result);
		final List<PurchaseOrderResponseDTO> list = new ArrayList<>();
		PurchaseOrderResponseDTO response;
		int index = 0;
		if (purchaseOrderList.hasContent()) {
			for (Purchaseorder item : purchaseOrderList) {
				response = new PurchaseOrderResponseDTO();
				response.setBranchName(item.getIdbranch().getName());
				response.setDescription(item.getDescription());
				response.setDate(item.getStatus().equals(PurchaseOrderStatus.CREATED) ? item.getCreationdate()
						: item.getDeliverydate());
				response.setIdProduct(item.getIdproduct().getId());
				response.setName(item.getIdproduct().getName());
				response.setNumberShelfProducts(productBranchRepository.getCountByIdproductAndIdbranchAndLocation(
						item.getIdproduct(), item.getIdbranch(), ProductLocation.SHELF.getId()));
				response.setNumberStockProducts(productBranchRepository.getCountByIdproductAndIdbranchAndLocation(
						item.getIdproduct(), item.getIdbranch(), ProductLocation.CELLAR.getId()));
				response.setSuggestedNumberItems(item.getQuantity());
				response.setIndex(index);
				response.setIdPurchaseOrder(item.getId());
				response.setStatus(item.getStatus().getDescription());
				index++;
				list.add(response);
			}
			result.setPurchaseOrder(list);
		}
		return result;
	}

	private boolean isAfterDate(Calendar currentDate, String requestDate) {
		boolean result;
		Date date = parseDate(requestDate);
		result = date == null ? false : date.after(currentDate.getTime());
		return result;
	}

	private Date parseDate(String requestDate) {
		Date date;
		try {
			date = formatDate.parse(requestDate);
		} catch (Exception e) {
			log.info(":: Error parse date ", e);
			date = null;
		}
		return date;
	}

	private Page<Purchaseorder> getPurchaseorderList(PurchaseOrderRequestDTO request,
			PurchaseListOrderResponseDTO result) {
		final Pageable pagination = PageRequest.of(request.getPage() - 1, paginationSize);
		Page<Purchaseorder> purchaseOrderList;
		int totalRows;
		Branch idbranch;
		if (BooleanUtils.negate(request.getBranchId() == null) && BooleanUtils.negate(request.getDate() == null)
				&& BooleanUtils.negate(request.getPurchaseOrderStatus() == null)) {
			idbranch = new Branch();
			idbranch.setId(request.getBranchId());
			if (BooleanUtils.negate(request.getPurchaseOrderStatus() == null)
					&& request.getPurchaseOrderStatus().equals(PurchaseOrderStatus.ON_PROCESS.getId())) {
				purchaseOrderList = purchaseOrderRepository.findAllByIdbranchAndDeliverydateAndStatus(idbranch,
						request.getDate(), request.getEndDate(), request.getPurchaseOrderStatus(), pagination);
				totalRows = purchaseOrderRepository.getPurchaseorderByIdbranchAndDeliverydateCount(idbranch,
						request.getDate(), request.getEndDate(), request.getPurchaseOrderStatus());
			} else {
				purchaseOrderList = purchaseOrderRepository.findAllByIdbranchAndCreationdateAndStatus(idbranch,
						request.getDate(), request.getEndDate(), request.getPurchaseOrderStatus(), pagination);
				totalRows = purchaseOrderRepository.getPurchaseorderByIdbranchAndCreationDateCount(idbranch,
						request.getDate(), request.getEndDate(), request.getPurchaseOrderStatus());
			}
		} else if (BooleanUtils.negate(request.getDate() == null)
				&& BooleanUtils.negate(request.getPurchaseOrderStatus() == null)) {
			if (request.getPurchaseOrderStatus().equals(PurchaseOrderStatus.ON_PROCESS.getId())) {
				purchaseOrderList = purchaseOrderRepository.findAllByDeliverydateAndStatus(request.getDate(),
						request.getEndDate(), request.getPurchaseOrderStatus(), pagination);
				totalRows = purchaseOrderRepository.getPurchaseorderByDeliverydateAndStatusCount(request.getDate(),
						request.getEndDate(), request.getPurchaseOrderStatus());
			} else {
				purchaseOrderList = purchaseOrderRepository.findAllCreationDateAndStatus(request.getDate(),
						request.getEndDate(), request.getPurchaseOrderStatus(), pagination);
				totalRows = purchaseOrderRepository.getPurchaseorderByCreationDateAndStatusCount(request.getDate(),
						request.getEndDate(), request.getPurchaseOrderStatus());
			}
		} else if (BooleanUtils.negate(request.getBranchId() == null)
				&& BooleanUtils.negate(request.getPurchaseOrderStatus() == null)) {
			idbranch = new Branch();
			idbranch.setId(request.getBranchId());
			purchaseOrderList = purchaseOrderRepository.findAllByIdbranchAndStatus(idbranch,
					request.getPurchaseOrderStatus(), pagination);
			totalRows = purchaseOrderRepository.getPurchaseorderByIdbranchAndStatusCount(idbranch,
					request.getPurchaseOrderStatus());
		} else if (BooleanUtils.negate(request.getBranchId() == null)
				&& BooleanUtils.negate(request.getDate() == null)) {
			idbranch = new Branch();
			idbranch.setId(request.getBranchId());
			purchaseOrderList = purchaseOrderRepository.findAllByIdbranchAndCreationdate(idbranch, request.getDate(),
					request.getEndDate(), pagination);
			totalRows = purchaseOrderRepository.getPurchaseorderByIdbranchAndCreationdateCount(idbranch,
					request.getDate(), request.getEndDate());
		} else if (BooleanUtils.negate(request.getBranchId() == null)) {
			idbranch = new Branch();
			idbranch.setId(request.getBranchId());
			purchaseOrderList = purchaseOrderRepository.findAllByIdbranch(idbranch, pagination);
			totalRows = purchaseOrderRepository.getPurchaseorderByIdbranchCount(idbranch);
		} else if (BooleanUtils.negate(request.getPurchaseOrderStatus() == null)) {
			purchaseOrderList = purchaseOrderRepository.findAllByStatus(request.getPurchaseOrderStatus(), pagination);
			totalRows = purchaseOrderRepository.getPurchaseorderByStatusCount(request.getPurchaseOrderStatus());
		} else if (BooleanUtils.negate(request.getDate() == null)) {
			purchaseOrderList = purchaseOrderRepository.findAllByCreationdate(request.getDate(), request.getEndDate(),
					pagination);
			totalRows = purchaseOrderRepository.getPurchaseorderByCreationdateCount(request.getDate(),
					request.getEndDate());
		} else {
			purchaseOrderList = purchaseOrderRepository.findAll(pagination);
			totalRows = purchaseOrderRepository.getPurchaseorderCount();
		}
		result.setTotalRows(totalRows);
		return purchaseOrderList;
	}

	@Override
	public PurchaseListOrderResponseDTO getPurchaseOrderStatus() {
		log.info(":: Get purchase order status service ");
		final PurchaseListOrderResponseDTO result = new PurchaseListOrderResponseDTO();
		final List<PurchaseOrderStatusDTO> purchaseOrderStatus = new ArrayList<>();
		PurchaseOrderStatusDTO status;
		for (final PurchaseOrderStatus item : PurchaseOrderStatus.values()) {
			status = new PurchaseOrderStatusDTO();
			status.setId(item.getId());
			status.setValue(item.getId());
			status.setName(item.getDescription());
			purchaseOrderStatus.add(status);
		}
		result.setPurchaseOrderStatus(purchaseOrderStatus);
		return result;
	}

	@Override
	public PurchaseOrderDetailResponseDTO getPurchaseOrderDetail(PurchaseOrderRequestDTO request) {
		final PurchaseOrderDetailResponseDTO result = new PurchaseOrderDetailResponseDTO();
		final Optional<Purchaseorder> model = purchaseOrderRepository.findById(request.getId());
		if (model.isPresent()) {
			final Product product = model.get().getIdproduct();
			final Provider provider = product.getProvider();
			final BigDecimal unitPrice = productHistoryRepository.findPurchasePriceByIdproduct(product,
					ProductStatus.ACTIVE.getId());
			result.setBrand(product.getBrand().getDescription());
			result.setIdPurchaseOrder(model.get().getId());
			result.setIsActive(model.get().getStatus().equals(PurchaseOrderStatus.CREATED));
			result.setPicture(product.getImage());
			result.setQuantityItems(model.get().getQuantity());
			result.setUnitPrice(unitPrice);
			result.setTotalPrice(
					unitPrice.multiply(new BigDecimal(result.getQuantityItems()).setScale(2, RoundingMode.HALF_UP)));
			result.setVendor(provider.getName());
			result.setVendorContact(provider.getContact());
			result.setTelephone(provider.getTelephone());
			result.setMapsQuery(provider.getLatitude() + "," + provider.getLongitude());
			try {
				final InetAddress inetAddress = InetAddress.getLocalHost();
				result.setPdfUrl(HTTP_PROTOCOL + inetAddress.getHostAddress() + PORT + pathRetrievePurchaseOrderPdf
						+ request.getId());
			} catch (UnknownHostException e) {
				log.error(":: Error getting IP Address ", e);
			}
			result.setRfc(provider.getRfc());
		}
		return result;
	}

	@Override
	public PurchaseOrderRequestDTO updatePurchaseOrder(PurchaseOrderRequestDTO request) {
		final Optional<Purchaseorder> model = purchaseOrderRepository.findById(request.getIdPurchaseOrder());
		if (model.isPresent()) {
			model.get().setQuantity(request.getQuantityItems());
			model.get().setStatusvalue(PurchaseOrderStatus.ON_PROCESS.getId());
			model.get().setDeliverydate(formatDate.format(Calendar.getInstance().getTime()));
			purchaseOrderRepository.save(model.get());
			try {
				asyncEmail.sendEmail(getPurchaseOrderOutputStreamPdf(request.getIdPurchaseOrder()),
						formatDate.format(Calendar.getInstance().getTime()), model.get().getId().toString(),
						model.get().getIdbranch().getManager().getName(),
						model.get().getIdproduct().getProvider().getContact());
			} catch (MessagingException e) {
				log.error(":: Error al enviar el correo de la orden de compra ", e);
			}
			request.setUpdated(Boolean.TRUE);
		} else {
			request.setUpdated(Boolean.FALSE);
		}
		return request;
	}

	@Override
	public byte[] getPurchaseOrderPdf(Integer idPurchaseOrder) {
		log.info(":: GetPurchaseOrderPdf service {} ", idPurchaseOrder);
		return getPurchaseOrderOutputStreamPdf(idPurchaseOrder).toByteArray();
	}

	private ByteArrayOutputStream getPurchaseOrderOutputStreamPdf(Integer idPurchaseOrder) {
		final Optional<Purchaseorder> purchaseOrder = purchaseOrderRepository.findById(idPurchaseOrder);
		if (BooleanUtils.negate(purchaseOrder.isPresent())) {
			return null;
		}
		Document document = new Document();

		document.addTitle(PURCHASE_ORDER_IMG);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			PdfWriter.getInstance(document, byteArrayOutputStream);
		} catch (DocumentException e) {
			log.error(":: Error pdf purchaseOrder ", e);
		}
		document.open();
		Font fontBold = FontFactory.getFont(FontFactory.TIMES_BOLD, 12, BaseColor.BLACK);
		Font font = FontFactory.getFont(FontFactory.TIMES, 12, BaseColor.BLACK);
		try {
			Paragraph paraghaph = new Paragraph();
			Chunk chunk = new Chunk(PURCHASE_ORDER_LABEL, fontBold);
			paraghaph.add(chunk);
			paraghaph.setAlignment(Paragraph.ALIGN_LEFT);
			document.add(paraghaph);

			paraghaph = new Paragraph();
			paraghaph.setAlignment(Paragraph.ALIGN_CENTER);
			Path path = Paths.get(ClassLoader.getSystemResource(LOGO_IMG).toURI());
			Image img = Image.getInstance(path.toAbsolutePath().toString());
			img.scalePercent(14, 10);
			img.setAbsolutePosition(430, 780);
			paraghaph.add(img);
			document.add(paraghaph);

			chunk = new Chunk(DATE_LABEL, fontBold);
			document.add(chunk);
			chunk = new Chunk(formatDate.format(Calendar.getInstance().getTime()) + "\n\n", font);
			document.add(chunk);

			chunk = new Chunk(NUMBER_PURCHASE_ORDER_LABEL, fontBold);
			document.add(chunk);
			chunk = new Chunk(purchaseOrder.get().getId() + "\n\n", font);
			document.add(chunk);

			chunk = new Chunk(PROVIDER_LABEL, fontBold);
			document.add(chunk);
			chunk = new Chunk(purchaseOrder.get().getIdproduct().getProvider().getName() + "\n\n\n", font);
			document.add(chunk);

			final BigDecimal price = productHistoryRepository
					.findPurchasePriceByIdproduct(purchaseOrder.get().getIdproduct(), ProductStatus.ACTIVE.getId());
			final PdfPTable table = new PdfPTable(4);
			table.setWidthPercentage(100);
			addTableHeader(table, HEADERS);
			addRows(table, purchaseOrder.get(), price);
			for (int index = 0; index < 4; index++) {
				addRowsEmpty(table);
			}
			document.add(table);

			paraghaph = new Paragraph();
			chunk = new Chunk(TOTAL_LABEL
					+ price.multiply(
							new BigDecimal(purchaseOrder.get().getQuantity()).setScale(2, RoundingMode.HALF_UP))
					+ "\n\n\n", fontBold);
			paraghaph.add(chunk);
			paraghaph.setAlignment(Paragraph.ALIGN_RIGHT);
			document.add(paraghaph);

			chunk = new Chunk(PREPARE_FOR_LABEL, fontBold);
			document.add(chunk);
			chunk = new Chunk(purchaseOrder.get().getIdbranch().getManager().getName(), font);
			document.add(chunk);

			chunk = new Chunk(SIGNATURE_LABEL, fontBold);
			document.add(chunk);

			paraghaph = new Paragraph();
			path = Paths.get(ClassLoader.getSystemResource("signature.jpg").toURI());
			img = Image.getInstance(path.toAbsolutePath().toString());
			img.scalePercent(10, 10);
			img.setAbsolutePosition(90, 400);
			paraghaph.add(img);
			document.add(paraghaph);

		} catch (Exception e) {
			log.error(":: Error pdf purchaseOrder ", e);
		}
		document.close();
		return byteArrayOutputStream;
	}

	private void addTableHeader(PdfPTable table, String[] headers) {
		Stream.of(headers).forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(columnTitle));
			table.addCell(header);
		});
	}

	private void addRows(PdfPTable table, Purchaseorder purchaseOrder, BigDecimal price) {
		table.addCell(purchaseOrder.getQuantity().toString());
		table.addCell(purchaseOrder.getIdproduct().getName());
		table.addCell(purchaseOrder.getQuantity().toString());
		table.addCell(price.toString());
	}

	private void addRowsEmpty(PdfPTable table) {
		table.addCell(" ");
		table.addCell(" ");
		table.addCell(" ");
		table.addCell(" ");
	}

}
