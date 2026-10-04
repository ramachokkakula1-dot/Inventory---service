package com.example.demo.service;

public class OrderStatusEvent {
	private String orderId;
	private String status;
	private String message;

	public OrderStatusEvent() {
	}

	public OrderStatusEvent(String orderId, String status, String message) {
		this.orderId = orderId;
		this.status = status;
		this.message = message;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
}
