package com.github.richygreat.springsagaevent.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.richygreat.springsagaevent.annotation.SagaBranchStart;
import com.github.richygreat.springsagaevent.annotation.SagaEnd;
import com.github.richygreat.springsagaevent.annotation.SagaEventHandler;
import com.github.richygreat.springsagaevent.annotation.SagaSideStep;
import com.github.richygreat.springsagaevent.annotation.SagaStart;
import com.github.richygreat.springsagaevent.annotation.SagaTransition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SagaEventHandler
public class TestFailureService {
	@SagaStart(name = "ShippingCreated", initEvent = "RecordCreated", triggerPoint = "/ship api called")
	public String createShipping() {
		String uuid = UUID.randomUUID().toString();
		log.info("createShipping: message: {}", uuid);
		return uuid;
	}

	@SagaTransition(name = "ShippingCreated", previousEvent = "RecordCreated", nextEvent = "DHLPickupDone")
	public String handleRecordCreatedAndDHLPickup(String message) {
		log.info("handleRecordCreatedAndDHLPickup: message: {}", message);
		throw new RuntimeException("DHLPickupFailed");
	}

	@SagaTransition(name = "ShippingCreated", previousEvent = "DHLPickupDone", nextEvent = "DHLDelivered")
	public String handleDHLPickupDoneAndDHLDeliver(String message) {
		log.info("handleDHLPickupDoneAndDHLDeliver: message: {}", message);
		return message;
	}

	@SagaSideStep(name = "ShippingCreated", previousEvent = "DHLDelivered", finalOutcome = "Delivered Mail")
	public String handleSendMailDelivered(String message) {
		log.info("handleSendMailDelivered: message: {}", message);
		return message;
	}

	@SagaEnd(name = "ShippingCreated", previousEvent = "DHLDelivered", finalOutcome = "Done")
	public void handleShippingDone(String message) {
		log.info("handleShippingDone: message: {}", message);
	}

	@SagaBranchStart(name = "ReversalCreated", initEvent = "RecordCreated", branchoutSagaName = "ShippingCreated", branchoutEvent = "DHLPickupFailed")
	public String handlePaymentFailed(String message) {
		log.info("handlePaymentFailed: message: {}", message);
		return message;
	}

	@SagaTransition(name = "ReversalCreated", previousEvent = "RecordCreated", nextEvent = "ItemReturned")
	public String handleReversalCreatedAndReturnItem(String message) {
		log.info("handleReversalCreatedAndReturnItem: message: {}", message);
		return message;
	}

	@SagaEnd(name = "ReversalCreated", previousEvent = "ItemReturned", finalOutcome = "Done")
	public void handleItemReturned(String message) {
		log.info("handleItemReturned: message: {}", message);
	}
}
