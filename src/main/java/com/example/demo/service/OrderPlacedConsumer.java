package com.example.demo.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.example.demo.entity.Product;
import com.example.demo.repo.ProductRepository;


@Component
public class OrderPlacedConsumer {
	private static final Logger log = LoggerFactory.getLogger(OrderPlacedConsumer.class);

	private final ProductRepository productRepository;
	private final KafkaTemplate<String, OrderStatusEvent> kafkaTemplate;

	public OrderPlacedConsumer(ProductRepository productRepository,
			KafkaTemplate<String, OrderStatusEvent> kafkaTemplate) {
		this.productRepository = productRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	@KafkaListener(topics = "order-placed", groupId = "inventory-service-group")
	public void listen(OrderPlacedEvent event) {
		log.info("Received order-placed for orderId={} product={} quantity={}", event.getOrderId(), event.getProduct(),
				event.getQuantity());

		Optional<Product> found = productRepository.findById(event.getProduct());

		if (found.isEmpty()) {
			log.warn("Product {} not found -- rejecting orderId={}", event.getProduct(), event.getOrderId());
			publishStatus(event.getOrderId(), "REJECTED", "Product not found: " + event.getProduct());
			return;
		}

		Product product = found.get();
		if (product.getStock() < event.getQuantity()) {
			log.warn("Insufficient stock for {} (have {}, need {}) -- rejecting orderId={}", event.getProduct(),
					product.getStock(), event.getQuantity(), event.getOrderId());
			publishStatus(event.getOrderId(), "REJECTED", "Insufficient stock");
			return;
		}

		product.setStock(product.getStock() - event.getQuantity());
		productRepository.save(product);
		log.info("Deducted stock for {}: {} remaining", product.getProductId(), product.getStock());

		publishStatus(event.getOrderId(), "CONFIRMED", "Order confirmed");
	}

	private void publishStatus(String orderId, String status, String message) {
		OrderStatusEvent statusEvent = new OrderStatusEvent(orderId, status, message);
		kafkaTemplate.send("order-status-updated", orderId, statusEvent);
		log.info("Published order-status-updated: orderId={} status={}", orderId, status);
	}
}
