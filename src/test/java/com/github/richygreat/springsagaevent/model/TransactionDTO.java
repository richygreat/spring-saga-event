package com.github.richygreat.springsagaevent.model;

import com.github.richygreat.springsagaevent.annotation.EventPayload;

@EventPayload(topic = "transaction", owned = true, keyField = "id")
public class TransactionDTO {
	private String id;
	private Double amount;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}
}
