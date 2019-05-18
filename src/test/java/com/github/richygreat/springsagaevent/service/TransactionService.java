package com.github.richygreat.springsagaevent.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.richygreat.springsagaevent.annotation.SagaCompensationEnd;
import com.github.richygreat.springsagaevent.annotation.SagaEventHandler;
import com.github.richygreat.springsagaevent.annotation.SagaStart;
import com.github.richygreat.springsagaevent.annotation.SagaTransition;
import com.github.richygreat.springsagaevent.model.TransactionDTO;

@Service
@SagaEventHandler
public class TransactionService {
	@SagaStart(name = "CreateTransaction", initEvent = "Requested", triggerPoint = "API")
	public TransactionDTO request() {
		TransactionDTO transactionDTO = new TransactionDTO();
		transactionDTO.setAmount(10.00);
		transactionDTO.setId(UUID.randomUUID().toString());
		return transactionDTO;
	}

	@SagaTransition(name = "CreateTransaction", previousEvent = "Requested", nextEvent = "Created", failureEvent = "Failed")
	public TransactionDTO create(TransactionDTO transactionDTO) {
		System.out.println("create: Entering: " + transactionDTO);
		throw new RuntimeException();
	}

	@SagaCompensationEnd(name = "CreateTransaction", previousEvent = "Failed", finalOutcome = "FailureMailSent")
	public void failed(TransactionDTO transactionDTO) {
		System.out.println("failed: Entering: " + transactionDTO);
	}
}
