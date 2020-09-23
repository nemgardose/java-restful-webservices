package com.rest.webservices.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.rest.webservices.shared.dto.UserDTO;

public interface UserService extends UserDetailsService {
	UserDTO createUser(UserDTO user);
	UserDTO getUser(String email);
	UserDTO getUserByUserId(String userId);
}
