package com.rest.webservices.service.impl;

import java.util.ArrayList;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.rest.webservices.exception.UserServiceException;
import com.rest.webservices.io.entity.UserEntity;
import com.rest.webservices.io.repositories.UserRepository;
import com.rest.webservices.service.UserService;
import com.rest.webservices.shared.Utils;
import com.rest.webservices.shared.dto.UserDTO;
import com.rest.webservices.ui.model.response.ErrorMessages;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	Utils utils;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public UserDTO createUser(UserDTO user) {
				
		if(userRepository.findByEmail(user.getEmail()) != null) {
			throw new RuntimeException("Record already exists");
		}

		UserEntity userEntity = new UserEntity();
		BeanUtils.copyProperties(user, userEntity);
		
		String publicUserId = utils.generateUserId(30);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		userEntity.setUserId(publicUserId);
		
		UserEntity storedUserDetails =  userRepository.save(userEntity);
		
		UserDTO returnValue = new UserDTO();
		BeanUtils.copyProperties(storedUserDetails, returnValue);
		
		return returnValue;
	}
	
	@Override
	public UserDTO getUser(String email) {
		UserEntity userEntity = userRepository.findByEmail(email);
		
		if(userEntity == null) {
			throw new UsernameNotFoundException(email);
		} else {
			UserDTO returnValue = new UserDTO();
			BeanUtils.copyProperties(userEntity, returnValue);
			
			return returnValue;
		}
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);
		
		if(userEntity == null) {
			throw new UsernameNotFoundException(email);
		} else {
			return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
		}
	}

	@Override
	public UserDTO getUserByUserId(String userId) {
		UserDTO returnValue = new UserDTO();
		UserEntity userEntity = userRepository.findByUserId(userId);
		
		if(userEntity == null) {
			throw new UsernameNotFoundException(userId);
		} else {
			BeanUtils.copyProperties(userEntity, returnValue);
			return returnValue;
		}
	}

	@Override
	public UserDTO updateUser(String userId, UserDTO user) {
		UserDTO returnValue = new UserDTO();
		UserEntity userEntity = userRepository.findByUserId(userId);
		
		if(userEntity == null) {
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		} else {
			userEntity.setFirstName(user.getFirstName());
			userEntity.setLastName(user.getLastName());
			
			UserEntity updateUserDetails = userRepository.save(userEntity);
			BeanUtils.copyProperties(updateUserDetails, returnValue);
			return returnValue;
		}
	}
}


