package com.rest.webservices.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.rest.webservices.shared.dto.AddressDTO;
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
		
		for(int index = 0; index < user.getAddresses().size(); index++) {
			AddressDTO address = user.getAddresses().get(index);
			address.setUserDetails(user);
			address.setAddressId(utils.generateAddressId(30));
			user.getAddresses().set(index, address);
		}

//		BeanUtils.copyProperties(user, userEntity);
		ModelMapper modelMapper = new ModelMapper();
		UserEntity userEntity = modelMapper.map(user, UserEntity.class);
		
		String publicUserId = utils.generateUserId(30);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		userEntity.setUserId(publicUserId);
		userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
		userEntity.setEmailVerificationStatus(false);
		
		
		UserEntity storedUserDetails =  userRepository.save(userEntity);
		
//		BeanUtils.copyProperties(storedUserDetails, returnValue);
		UserDTO returnValue = modelMapper.map(storedUserDetails, UserDTO.class);
		
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
			throw new UsernameNotFoundException("User with ID " + userId + " not found");
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

	@Override
	public void deleteUser(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		if(userEntity == null) {
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		} else {
			userRepository.delete(userEntity);
		}
	}

	@Override
	public List<UserDTO> getUsers(int page, int limit) {
		List<UserDTO> returnValue = new ArrayList<>();
		
		if(page > 0) page -= 1;
		
		Pageable pageableRequest = PageRequest.of(page, limit);
		Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
		List<UserEntity> users = usersPage.getContent();
		
		for(UserEntity userEntity: users) {
			UserDTO userDto = new UserDTO();
			BeanUtils.copyProperties(userEntity, userDto);
			returnValue.add(userDto);
		}
		
		return returnValue;
	}

	@Override
	public boolean verifyEmailToken(String token) {
		
		boolean returnValue = false;
		
		UserEntity userEntity = userRepository.findUserByEmailVerification(token);
		
		if(userEntity != null) {
			boolean hastokenExpired = Utils.hasTokenExpired(token);
			if(!hastokenExpired) {
				userEntity.setEmailVerificationToken(null);
				userEntity.setEmailVerificationStatus(Boolean.TRUE);
				userRepository.save(userEntity);
				returnValue = true;
			}
		}
		
		return returnValue;
	}	
}


