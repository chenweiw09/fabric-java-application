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
package com.my.chen.fabric.app.user;

import com.my.chen.fabric.app.client.CAClient;
import com.my.chen.fabric.app.config.Config;
import com.my.chen.fabric.app.util.Util;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * 
 * @author Balaji Kadambi
 *
 */

public class RegisterEnrollUser {


	public static UserContext getOrg1Admin() throws InvalidKeySpecException, NoSuchAlgorithmException, CryptoException, IOException {
		UserContext org1Admin = new UserContext();
		File pkFolder1 = new File(Config.ORG1_USR_ADMIN_PK);
		File[] pkFiles1 = pkFolder1.listFiles();
		File certFolder1 = new File(Config.ORG1_USR_ADMIN_CERT);
		File[] certFiles1 = certFolder1.listFiles();


		Enrollment enrollOrg1Admin = Util.getEnrollment(Config.ORG1_USR_ADMIN_PK, pkFiles1[0].getName(),
				Config.ORG1_USR_ADMIN_CERT, certFiles1[0].getName());
		org1Admin.setEnrollment(enrollOrg1Admin);
		org1Admin.setMspId(Config.ORG1_MSP);
		org1Admin.setName(Config.ADMIN);

		return org1Admin;
	}


	public static UserContext getOrg2Admin() throws InvalidKeySpecException, NoSuchAlgorithmException, CryptoException, IOException {
		UserContext org2Admin = new UserContext();
		File pkFolder2 = new File(Config.ORG2_USR_ADMIN_PK);
		File[] pkFiles2 = pkFolder2.listFiles();
		File certFolder2 = new File(Config.ORG2_USR_ADMIN_CERT);
		File[] certFiles2 = certFolder2.listFiles();
		Enrollment enrollOrg2Admin = Util.getEnrollment(Config.ORG2_USR_ADMIN_PK, pkFiles2[0].getName(),
				Config.ORG2_USR_ADMIN_CERT, certFiles2[0].getName());
		org2Admin.setEnrollment(enrollOrg2Admin);
		org2Admin.setMspId(Config.ORG2_MSP);
		org2Admin.setName(Config.ADMIN);

		return org2Admin;
	}

	public static UserContext enrollUser(UserContext adminUserContext, String userName) throws Exception {
		Util.cleanUp();

		String caUrl = Config.CA_ORG1_URL;
		CAClient caClient = new CAClient(caUrl, null);
		caClient.setAdminUserContext(adminUserContext);
		caClient.enrollAdminUser(adminUserContext.getAffiliation(), Config.ADMIN_PASSWORD);

		UserContext userContext = new UserContext();
		userContext.setName(userName);
		userContext.setAffiliation(adminUserContext.getAffiliation());
		userContext.setMspId(adminUserContext.getMspId());

		String eSecret = caClient.registerUser(userName, adminUserContext.getAffiliation());
		userContext = caClient.enrollUser(userContext, eSecret);

		return userContext;

	}




	public static void main(String args[]) {
		try {
			Util.cleanUp();
			String caUrl = Config.CA_ORG1_URL;
			CAClient caClient = new CAClient(caUrl, null);
			// Enroll Admin to Org1MSP
			UserContext adminUserContext = new UserContext();
			adminUserContext.setName(Config.ADMIN);
			adminUserContext.setAffiliation(Config.ORG1);
			adminUserContext.setMspId(Config.ORG1_MSP);
			caClient.setAdminUserContext(adminUserContext);
			adminUserContext = caClient.enrollAdminUser(Config.ADMIN, Config.ADMIN_PASSWORD);

			// Register and Enroll user to Org1MSP
			UserContext userContext = new UserContext();
			String name = "user"+System.currentTimeMillis();
			userContext.setName(name);
			userContext.setAffiliation(Config.ORG1);
			userContext.setMspId(Config.ORG1_MSP);

			String eSecret = caClient.registerUser(name, Config.ORG1);

			userContext = caClient.enrollUser(userContext, eSecret);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
