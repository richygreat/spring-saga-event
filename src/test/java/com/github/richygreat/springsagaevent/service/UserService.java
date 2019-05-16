package com.github.richygreat.springsagaevent.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.richygreat.springsagaevent.annotation.SagaEnd;
import com.github.richygreat.springsagaevent.annotation.SagaEventHandler;
import com.github.richygreat.springsagaevent.annotation.SagaSideStep;
import com.github.richygreat.springsagaevent.annotation.SagaStart;
import com.github.richygreat.springsagaevent.annotation.SagaTransition;
import com.github.richygreat.springsagaevent.model.UserDTO;

@Service
@SagaEventHandler
public class UserService {
	@SagaStart(name = "UserCreated", initEvent = "CreationRequested", triggerPoint = "API")
	public UserDTO requestUserCreation() {
		UserDTO userDTO = new UserDTO();
		userDTO.setId(UUID.randomUUID().toString());
		userDTO.setName("richygreat");
		return userDTO;
	}

	@SagaTransition(name = "UserCreated", previousEvent = "CreationRequested", nextEvent = "Created")
	public UserDTO createUser(UserDTO userDTO) {
		System.out.println("createUser: Entering: " + userDTO);
		return userDTO;
	}

	@SagaSideStep(name = "UserCreated", previousEvent = "Created", finalOutcome = "MailSent")
	public void sendVerificationMail(UserDTO userDTO) {
		System.out.println("sendVerificationMail: Entering: " + userDTO);
	}

	@SagaEnd(name = "UserCreated", previousEvent = "Created", finalOutcome = "Complete")
	public void completeUserCreation(UserDTO userDTO) {
		System.out.println("completeUserCreation: Entering: " + userDTO);
	}
}
