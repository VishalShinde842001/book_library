package com.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bo.Response;
import com.dao.ObjectDao;

import com.helper.AppConstants;
import com.helper.CommonMessages;
import com.helper.ErrorConstants;
import com.model.Address;
import com.model.User;
import com.service.UserService;

import com.utils.RandomCreator;

@Service
public class UserServiceImpl implements UserService {

	private final ObjectDao objectDao;

	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(ObjectDao objectDao, PasswordEncoder passwordEncoder) {
		this.objectDao = objectDao;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	// For Adding user firstName,lastName,birthDate,email,password,mobile is must
	public Response addUser(User user) throws Exception {
		Response response = new Response();
		try {
			if (userNullChecker("ADD", user)) {
				User existingUserByEmail = objectDao.getObjectByParam(User.class, "email", user.getEmail());
				if (null != existingUserByEmail) {
					response.setStatus(ErrorConstants.ALREADY_EXIST);
					response.setMessage("Email Already Registerd Try Again With Another");
					return response;
				}

				User existingUserByMobile = objectDao.getObjectByParam(User.class, "mobile", user.getMobile());
				if (null != existingUserByMobile) {
					response.setStatus(ErrorConstants.ALREADY_EXIST);
					response.setMessage("Mobile Number Already Registerd Try Again With Another");
					return response;
				}
				if (null != user.getAddress()) {
					Address address = new Address();
					address.setStreet(user.getAddress().getStreet());
					address.setCity(user.getAddress().getCity());
					address.setState(user.getAddress().getState());
					address.setCountry(user.getAddress().getCountry());
					address.setZipCode(user.getAddress().getZipCode());
					objectDao.saveObject(address);
					user.setAddress(address);
				}
				user.setUserType(AppConstants.NORMAL_USER);
				user.setIsActive(true);
				user.setUserUniqueUID(RandomCreator.generateUID(AppConstants.USER_UID_PREFIX, 8));
				user.setPassword(passwordEncoder.encode(user.getPassword()));

				objectDao.saveObject(user);
				response.setStatus(ErrorConstants.SUCESS);
				response.setMessage("User Added Sucessfully");
				response.setResult(user.getUserId());

			} else {
				response.setStatus(ErrorConstants.BAD_REQUEST);
				response.setMessage(CommonMessages.REQUIRED_FIELD_MISSING);
			}
		} catch (Exception e) {
			throw e;
		}
		return response;
	}

	public static Boolean userNullChecker(String operation, User user) {
		if (operation.equalsIgnoreCase("EDIT")) {
			return null != user && null != user.getUserId();
		}
		return null != user && null != user.getEmail() && null != user.getPassword() && null != user.getMobile()
				&& null != user.getFirstName() && null != user.getLastName() && null != user.getBirthDate();

	}

	@Override
	public Response editUser(User user) throws Exception {
		Response response = new Response();
		try {
			if (userNullChecker("EDIT", user)) {
				User existingUser = objectDao.getObjectById(User.class, user.getUserId());
				if (null != existingUser) {
					if (null != user.getEmail()) {
						User exstingUserByEmail = objectDao.getObjectByParam(User.class, "email", user.getEmail());
						if (null != exstingUserByEmail && existingUser.getUserId() != exstingUserByEmail.getUserId()) {
							response.setStatus(ErrorConstants.ALREADY_EXIST);
							response.setMessage("Email Already Registered Try With Another");
							return response;
						}else {
							existingUser.setEmail(user.getEmail());
						}
					}
					if (null != user.getMobile()) {
						User exstingUserByMobile = objectDao.getObjectByParam(User.class, "mobile", user.getMobile());
						if (null != exstingUserByMobile
								&& existingUser.getUserId() != exstingUserByMobile.getUserId()) {
							response.setStatus(ErrorConstants.ALREADY_EXIST);
							response.setMessage("Mobile Number Already Registered Try With Another");
							return response;
						}else {
							existingUser.setMobile(user.getMobile());
						}
					}

					if (user.getBirthDate() != null) {
						existingUser.setBirthDate(user.getBirthDate());
					}
					if (user.getFirstName() != null) {
						existingUser.setFirstName(user.getFirstName());
					}
					if (user.getLastName() != null) {
						existingUser.setLastName(user.getLastName());
					}
					
					if (null != user.getAddress()) {
						if (null != user.getAddress().getAddressId()) { Address existingAddress = objectDao.getObjectById(Address.class, user.getAddress().getAddressId());
                        if (existingAddress != null) {
                            if (user.getAddress().getStreet() != null) {
                                existingAddress.setStreet(user.getAddress().getStreet());
                            }
                            if (user.getAddress().getCity() != null) {
                                existingAddress.setCity(user.getAddress().getCity());
                            }
                            if (user.getAddress().getState() != null) {
                                existingAddress.setState(user.getAddress().getState());
                            }
                            if (user.getAddress().getCountry() != null) {
                                existingAddress.setCountry(user.getAddress().getCountry());
                            }
                            if (user.getAddress().getZipCode() != null) {
                                existingAddress.setZipCode(user.getAddress().getZipCode());
                            }
                            objectDao.updateObject(existingAddress);
                            existingUser.setAddress(existingAddress);
                        }
                    }  else {
							Address address = new Address();
							address.setStreet(user.getAddress().getStreet());
							address.setCity(user.getAddress().getCity());
							address.setState(user.getAddress().getState());
							address.setCountry(user.getAddress().getCountry());
							address.setZipCode(user.getAddress().getZipCode());
							objectDao.saveObject(address);
							existingUser.setAddress(address);
						}
					}
					objectDao.updateObject(existingUser);
					response.setStatus(ErrorConstants.SUCESS);
					response.setMessage("User Updated Sucessfully");

				} else {
					response.setStatus(ErrorConstants.NOT_FOUND);
					response.setMessage("User Not Found with id:" + user.getUserId());
				}

			}
		} catch (Exception e) {
			throw e;
		}
		return response;
	}

	@Override
	public Response getUserDetailsById(Long userId) throws Exception {
		Response response=new Response();
		try {
			if(null!=userId && userId>0) {
				User user=objectDao.getObjectById(User.class, userId);
				if(null!=user) {
					user.setPassword(null);
					response.setResult(user);
					response.setStatus(ErrorConstants.SUCESS);
					response.setMessage("User get sucessfully..");
				}else {
					response.setStatus(ErrorConstants.NOT_FOUND);
					response.setMessage("User Not Found");
				}
			}else {
				response.setStatus(ErrorConstants.BAD_REQUEST);
				response.setMessage(CommonMessages.REQUIRED_FIELD_MISSING);
			}
		}
		catch(Exception e) {
			throw e;
		}
		return response;
	}

}
