package com.rest.webservices.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rest.webservices.UserRepository;
import com.rest.webservices.io.entity.UserEntity;
import com.rest.webservices.service.UserService;
import com.rest.webservices.shared.Utils;
import com.rest.webservices.shared.dto.UserDTO;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	Utils utils;
	
	@Override
	public UserDTO createUser(UserDTO user) {
				
		if(userRepository.findByEmail(user.getEmail()) != null) {
			throw new RuntimeException("Record already exists");
		}

		UserEntity userEntity = new UserEntity();
		BeanUtils.copyProperties(user, userEntity);
		
		String publicUserId = utils.generateUserId(30);
		userEntity.setEncryptedPassword("test");
		userEntity.setUserId(publicUserId);
		
		UserEntity storedUserDetails =  userRepository.save(userEntity);
		
		UserDTO returnValue = new UserDTO();
		BeanUtils.copyProperties(storedUserDetails, returnValue);
		
		return returnValue;
	}

}
