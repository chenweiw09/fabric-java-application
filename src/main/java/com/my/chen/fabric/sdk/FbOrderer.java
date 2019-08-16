package com.my.chen.fabric.sdk;

import lombok.Data;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/1
 * @description
 */
@Data
public class FbOrderer {

    /** orderer的域名 orderer.example.com */
    private String ordererName;

    /** orderer的访问地址 orderer.test.com:7050*/
    private String ordererLocation;

    private String serverCrtPath;
    private String clientCertPath;
    private String clientKeyPath;

    public FbOrderer(String ordererName, String ordererLocation) {
        this.ordererName = ordererName;
        this.ordererLocation = ordererLocation;
    }

    public FbOrderer(String ordererName, String ordererLocation, String serverCrtPath, String clientCertPath, String clientKeyPath) {
        this.ordererName = ordererName;
        this.ordererLocation = ordererLocation;
        this.serverCrtPath = serverCrtPath;
        this.clientCertPath = clientCertPath;
        this.clientKeyPath = clientKeyPath;
    }
}
