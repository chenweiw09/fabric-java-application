package com.my.chen.fabric.sdk;

import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.lang.reflect.InvocationTargetException;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/1
 * @description
 */
public class FbClient {

    private HFClient client;

    public HFClient getClient() {
        return client;
    }

    public FbClient(User context) throws CryptoException, InvalidArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // setup the client
        client = HFClient.createNewInstance();
        client.setCryptoSuite(cryptoSuite);
        client.setUserContext(context);
    }
}
