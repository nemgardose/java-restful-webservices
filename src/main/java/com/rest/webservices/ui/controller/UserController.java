package com.rest.webservices.ui.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rest.webservices.service.UserService;
import com.rest.webservices.shared.dto.UserDTO;
import com.rest.webservices.ui.model.request.UserDetailsRequestModel;
import com.rest.webservices.ui.model.response.UserRest;

@RestController
@RequestMapping("users") //http://localhost:8080/users/
public class UserController {
	
	@Autowired
	UserService userService;
	
	@GetMapping(path="/{id}",
			produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest getUser(@PathVariable String id) {
		
		UserRest returnValue = new UserRest();
		
		UserDTO userDto = userService.getUserByUserId(id);
		BeanUtils.copyProperties(userDto, returnValue);
		
		return returnValue;
	}
	
	@PostMapping(
			consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE },
			produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
			)
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) {
		
		UserRest returnValue = new UserRest();
		
		UserDTO userDto = new UserDTO();
		BeanUtils.copyProperties(userDetails, userDto);
		
		UserDTO createdUser = userService.createUser(userDto);
		BeanUtils.copyProperties(createdUser, returnValue);
		
		return returnValue;
	}
	
	@PutMapping
	public String updateUser() {
		
		return "update user was called";
	}
	
	@DeleteMapping
	public String deleteUser() {
		
		return "delete user was called";
	}

}
