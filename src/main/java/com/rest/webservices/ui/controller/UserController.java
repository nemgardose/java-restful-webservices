package com.rest.webservices.ui.controller;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rest.webservices.exception.UserServiceException;
import com.rest.webservices.service.AddressService;
import com.rest.webservices.service.UserService;
import com.rest.webservices.shared.dto.AddressDTO;
import com.rest.webservices.shared.dto.UserDTO;
import com.rest.webservices.ui.model.request.UserDetailsRequestModel;
import com.rest.webservices.ui.model.response.AddressesRest;
import com.rest.webservices.ui.model.response.ErrorMessages;
import com.rest.webservices.ui.model.response.OperationStatusModel;
import com.rest.webservices.ui.model.response.RequestOperationStatus;
import com.rest.webservices.ui.model.response.UserRest;

@RestController
@RequestMapping("users") // http://localhost:8080/users/
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	AddressService addressService;

	@Autowired
	AddressService addressesService;

	@GetMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest getUser(@PathVariable String id) {

		UserRest returnValue = new UserRest();

		UserDTO userDto = userService.getUserByUserId(id);
		BeanUtils.copyProperties(userDto, returnValue);

		return returnValue;
	}

	@PostMapping(consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {

		UserRest returnValue = new UserRest();

		if (userDetails.getFirstName().isEmpty()) {
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		}

//		UserDTO userDto = new UserDTO();
//		BeanUtils.copyProperties(userDetails, userDto);
		ModelMapper modelMapper = new ModelMapper();
		UserDTO userDto = modelMapper.map(userDetails, UserDTO.class);

		UserDTO createdUser = userService.createUser(userDto);
		returnValue = modelMapper.map(createdUser, UserRest.class);

		return returnValue;
	}

	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {

		UserRest returnValue = new UserRest();

		if (userDetails.getFirstName().isEmpty()) {
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		}

		UserDTO userDto = new UserDTO();
		BeanUtils.copyProperties(userDetails, userDto);

		UserDTO updateUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(updateUser, returnValue);

		return returnValue;
	}

	@DeleteMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public OperationStatusModel deleteUser(@PathVariable String id) {

		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.DELETE.name());

		userService.deleteUser(id);

		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

		return returnValue;
	}

	@GetMapping(produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "25") int limit) {

		List<UserRest> returnValue = new ArrayList<>();
		List<UserDTO> users = userService.getUsers(page, limit);

		for (UserDTO userDTO : users) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDTO, userModel);
			returnValue.add(userModel);
		}

		return returnValue;
	}

	// http://localhost:8080/mobile-app-ws/users/etLzapkWW94rl8KbUNjA9cOiwPSt3O/addresses
	@GetMapping(path = "/{id}/addresses", produces = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE, "application/hal+json" })
	public CollectionModel<AddressesRest> getUserAddresses(@PathVariable String id) {

		List<AddressesRest> addressesListRestModel = new ArrayList<>();
		List<AddressDTO> addressesDTO = addressesService.getAddresses(id);

		if (addressesDTO != null && !addressesDTO.isEmpty()) {
			Type listType = new TypeToken<List<AddressesRest>>() {
			}.getType();
			addressesListRestModel = new ModelMapper().map(addressesDTO, listType);

			for (AddressesRest addressRest : addressesListRestModel) {
				Link addressLink = WebMvcLinkBuilder.linkTo(
						WebMvcLinkBuilder.methodOn(UserController.class).getUserAddress(id, addressRest.getAddressId()))
						.withSelfRel();
				addressRest.add(addressLink);

				Link userLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUser(id))
						.withRel("user");
				addressRest.add(userLink);
			}
		}

		return new CollectionModel<>(addressesListRestModel);
	}

	@GetMapping(path = "/{userId}/addresses/{addressId}", produces = { MediaType.APPLICATION_XML_VALUE, 
			MediaType.APPLICATION_JSON_VALUE, "application/hal+json" })
	public EntityModel<AddressesRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId) {
		AddressDTO addressesDTO = addressService.getAddress(addressId);
		ModelMapper modelMapper = new ModelMapper();
		
//		Link addressLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).slash("addresses").slash(addressId).withSelfRel();
//		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");
//		Link addressesLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).slash("addresses").withRel("addresses");
		
		
		Link addressLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
				.getUserAddress(userId, addressId)).withSelfRel();
		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");
		Link addressesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
				.getUserAddresses(userId)).withRel("addresses");
		
		AddressesRest addressesRestModel = modelMapper.map(addressesDTO, AddressesRest.class);
		
		addressesRestModel.add(addressLink);
		addressesRestModel.add(userLink);
		addressesRestModel.add(addressesLink);
		
		return new EntityModel<>(addressesRestModel);
	}

	/**
	 * http://localhost:8080/users/email-verification?token=sdsfsdf
	 */
	
	@GetMapping(path = "/email-verification", produces = {MediaType.APPLICATION_PROBLEM_JSON_VALUE, 
			MediaType.APPLICATION_ATOM_XML_VALUE })
	public OperationStatusModel verifyEmailToken(@RequestParam(value="token") String token) {
		
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());
		
		boolean isVerified = userService.verifyEmailToken(token);
		
		if(isVerified) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		} else {
			returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		}
		
		return returnValue;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
