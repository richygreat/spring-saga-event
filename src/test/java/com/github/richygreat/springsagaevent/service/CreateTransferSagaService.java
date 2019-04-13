package com.github.richygreat.springsagaevent.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.richygreat.springsagaevent.annotation.SagaCompensationBranchStart;
import com.github.richygreat.springsagaevent.annotation.SagaCompensationEnd;
import com.github.richygreat.springsagaevent.annotation.SagaEnd;
import com.github.richygreat.springsagaevent.annotation.SagaEventHandler;
import com.github.richygreat.springsagaevent.annotation.SagaStart;
import com.github.richygreat.springsagaevent.annotation.SagaTransition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@SagaEventHandler
public class CreateTransferSagaService {
	@SagaStart(name = "CreateTransfer", initEvent = "RecordCreated", triggerPoint = "ApiCall")
	public TransferDto initTransfer() {
		String id = UUID.randomUUID().toString();
		log.info("initTransfer: Entering id: {}", id);
		TransferDto dto = new TransferDto();
		dto.setId(id);
		dto.setAmount(1000.00);
		return dto;
	}

	@SagaTransition(name = "CreateTransfer", previousEvent = "RecordCreated", nextEvent = "DebitDone", failureEvent = "DebitFailed")
	public TransferDto doDebit(TransferDto dto) {
		log.info("doDebit: Entering id: {}", dto.getId());
		return dto;
	}

	@SagaCompensationEnd(name = "CreateTransfer", previousEvent = "DebitFailed", finalOutcome = "TransactionFailed")
	public void markAsDebitFailed(TransferDto dto) {
		log.info("markAsDebitFailed: Entering id: {}", dto.getId());
	}

	@SagaTransition(name = "CreateTransfer", previousEvent = "DebitDone", nextEvent = "CreditDone", failureEvent = "CreditFailed")
	public TransferDto doCredit(TransferDto dto) {
		log.info("doCredit: Entering id: {}", dto.getId());
		throw new RuntimeException("DebitFailed");
	}

	@SagaCompensationBranchStart(name = "CreateReversalTransfer", initEvent = "RecordCreated", branchoutSagaName = "CreateTransfer", branchoutEvent = "CreditFailed")
	public TransferDto markAsCreditFailed(TransferDto dto) {
		log.info("markAsCreditFailed: Entering id: {}", dto.getId());
		return dto;
	}

	@SagaTransition(name = "CreateReversalTransfer", previousEvent = "RecordCreated", nextEvent = "DebitReversalDone")
	public TransferDto doDebitReversal(TransferDto dto) {
		log.info("doDebitReversal: Entering id: {}", dto.getId());
		return dto;
	}

	@SagaEnd(name = "CreateReversalTransfer", previousEvent = "DebitReversalDone", finalOutcome = "TransactionFailed")
	public void markReversalAsComplete(TransferDto dto) {
		log.info("markReversalAsComplete: Entering id: {}", dto.getId());
	}

	@SagaEnd(name = "CreateTransfer", previousEvent = "CreditDone", finalOutcome = "TransferDone")
	public void markAsComplete(TransferDto dto) {
		log.info("markAsComplete: Entering id: {}", dto.getId());
	}
}
