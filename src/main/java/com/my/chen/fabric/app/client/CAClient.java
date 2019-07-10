/****************************************************** 
 *  Copyright 2018 IBM Corporation 
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */       
package com.my.chen.fabric.app.client;

import com.my.chen.fabric.app.user.UserContext;
import com.my.chen.fabric.app.util.Util;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper class for HFCAClient.
 */

public class CAClient {

	private String caUrl;
	private Properties caProperties;
	private HFCAClient caClient;
	private UserContext adminContext;


	public HFCAClient getCaClient() {
		return caClient;
	}

	/**
	 * Set the admin user context for registering and enrolling users.
	 * @param userContext
	 */
	public void setAdminUserContext(UserContext userContext) {
		this.adminContext = userContext;
	}

	/**
	 * Constructor
	 * @param caUrl 
	 * @param caProperties
	 * @throws MalformedURLException
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws InvalidArgumentException 
	 * @throws CryptoException 
	 * @throws ClassNotFoundException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 */
	public CAClient(String caUrl, Properties caProperties) throws MalformedURLException, IllegalAccessException, InstantiationException, ClassNotFoundException, CryptoException, InvalidArgumentException, NoSuchMethodException, InvocationTargetException {
		this.caUrl = caUrl;
		this.caProperties = caProperties;
		init();
	}

	private void init() throws MalformedURLException, IllegalAccessException, InstantiationException, ClassNotFoundException, CryptoException, InvalidArgumentException, NoSuchMethodException, InvocationTargetException {
		CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
		caClient = HFCAClient.createNewInstance(caUrl, caProperties);
		caClient.setCryptoSuite(cryptoSuite);
	}

	/**
	 * Enroll admin user.
	 *  basic admin user can be admin and adminpw
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public UserContext enrollAdminUser(String username, String password) throws Exception {
		UserContext userContext = Util.readUserContext(adminContext.getAffiliation(), username);
		if (userContext != null) {
			Logger.getLogger(CAClient.class.getName()).log(Level.WARNING, "CA -" + caUrl + " admin is already enrolled.");
			return userContext;
		}
		Enrollment adminEnrollment = caClient.enroll(username, password);
		adminContext.setEnrollment(adminEnrollment);
		Logger.getLogger(CAClient.class.getName()).log(Level.INFO, "CA -" + caUrl + " Enrolled Admin.");
		Util.writeUserContext(adminContext);
		return adminContext;
	}

	/**
	 * Register user.
	 *  because admin use has all the privileges, a new user should be enrolled and registered
	 * @param username
	 * @param organization
	 * @return
	 * @throws Exception
	 */
	public String registerUser(String username, String organization) throws Exception {
		UserContext userContext = Util.readUserContext(adminContext.getAffiliation(), username);
		if (userContext != null) {
			throw new RuntimeException("CA -" + caUrl +" User " + username+ " is already registered.");
		}
		RegistrationRequest rr = new RegistrationRequest(username, organization);
		String enrollmentSecret = caClient.register(rr, adminContext);
		Logger.getLogger(CAClient.class.getName()).log(Level.INFO, "CA -" + caUrl + " Registered User - " + username);
		return enrollmentSecret;
	}

	/**
	 * Enroll user.
	 * 
	 * @param user
	 * @param secret
	 * @return
	 * @throws Exception
	 */
	public UserContext enrollUser(UserContext user, String secret) throws Exception {
		UserContext userContext = Util.readUserContext(adminContext.getAffiliation(), user.getName());
		if (userContext != null) {
			Logger.getLogger(CAClient.class.getName()).log(Level.WARNING, "CA -" + caUrl + " User " + user.getName()+" is already enrolled");
			return userContext;
		}
		Enrollment enrollment = caClient.enroll(user.getName(), secret);
		user.setEnrollment(enrollment);
		Util.writeUserContext(user);
		Logger.getLogger(CAClient.class.getName()).log(Level.INFO, "CA -" + caUrl +" Enrolled User - " + user.getName());
		return user;
	}

	public UserContext registerAndEnrollUser(String username, String organization) throws Exception{
		UserContext userContext = Util.readUserContext(adminContext.getAffiliation(), username);
		if (userContext != null) {
			throw new RuntimeException("CA -" + caUrl +" User " + username+ " is already registered.");
		}
		RegistrationRequest rr = new RegistrationRequest(username, organization);
		String enrollmentSecret = caClient.register(rr, adminContext);

		UserContext user = new UserContext();
		user.setName(username);
		Enrollment enrollment = caClient.enroll(user.getName(), enrollmentSecret);
		user.setEnrollment(enrollment);
		user.setAffiliation(adminContext.getAffiliation());
		user.setMspId(adminContext.getMspId());

		Util.writeUserContext(user);
		return user;
	}

}
