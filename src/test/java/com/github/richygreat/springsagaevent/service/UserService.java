package com.github.richygreat.springsagaevent.service;

import org.springframework.stereotype.Service;

import com.github.richygreat.springsagaevent.annotation.SagaEventHandler;
import com.github.richygreat.springsagaevent.annotation.SagaTransition;
import com.github.richygreat.springsagaevent.model.UserDTO;

@Service
@SagaEventHandler
public class UserService {
	@SagaTransition(name = "UserCreated", previousEvent = "CreationRequested", nextEvent = "Created")
	public UserDTO createUser(UserDTO userDTO) {
		System.out.println("createUser: Entering: " + userDTO);
		return userDTO;
	}

	@SagaTransition(name = "UserCreated", previousEvent = "Created", nextEvent = "Activated")
	public UserDTO activateUser(UserDTO userDTO) {
		System.out.println("activateUser: Entering: " + userDTO);
		return userDTO;
	}
}
