package com.github.richygreat.springsagaevent.model;

import com.github.richygreat.springsagaevent.annotation.EventPayload;

@EventPayload(topic = "test", owned = true)
public class TestDTO {
	private String id;
	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
