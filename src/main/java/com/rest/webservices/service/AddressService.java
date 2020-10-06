package com.rest.webservices.service;

import java.util.List;

import com.rest.webservices.shared.dto.AddressDTO;

public interface AddressService {
	
	List<AddressDTO> getAddresses(String userId);
	AddressDTO getAddress(String addressId);
}
