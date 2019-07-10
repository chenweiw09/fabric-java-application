package com.my.chen.fabric.app.config;

import java.io.File;
import java.util.Properties;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/7/10
 * @description
 */
public class BasicConfig {

    // File peerCert = Paths.get(config.getCryptoConfigPath(), "/peerOrganizations", peers.getOrgDomainName(), "peers", peers.get().get(i).getPeerName(), "tls/server.crt")
    //                    .toFile();

    public Properties getPeerProperties(File peerCert, String hostnameOverride){
        Properties peerProperties = new Properties();
        peerProperties.setProperty("pemFile", peerCert.getAbsolutePath());
        // ret.setProperty("trustServerCertificate", "true"); //testing
        // environment only NOT FOR PRODUCTION!
        peerProperties.setProperty("hostnameOverride", hostnameOverride);
        peerProperties.setProperty("sslProvider", "openSSL");
        peerProperties.setProperty("negotiationType", "TLS");
        // 在grpc的NettyChannelBuilder上设置特定选项
        peerProperties.put("grpc.ManagedChannelBuilderOption.maxInboundMessageSize", 9000000);

        return peerProperties;
    }


    // File ordererCert = Paths.get(config.getCryptoConfigPath(), "/ordererOrganizations", orderers.getOrdererDomainName(), "orderers", orderers.get().get(i).getOrdererName(),
    //                    "tls/server.crt").toFile();

    public Properties getOrdererProperties(File ordererCert, String hostnameOverride){
        Properties ordererProperties = new Properties();
        ordererProperties.setProperty("pemFile", ordererCert.getAbsolutePath());
        ordererProperties.setProperty("hostnameOverride", hostnameOverride);
        ordererProperties.setProperty("sslProvider", "openSSL");
        ordererProperties.setProperty("negotiationType", "TLS");
        ordererProperties.put("grpc.ManagedChannelBuilderOption.maxInboundMessageSize", 9000000);
        ordererProperties.setProperty("ordererWaitTimeMilliSecs", "300000");
        return ordererProperties;
    }

}
