package com.my.chen.fabric.app.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import static java.lang.String.format;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/7/10
 * @description
 */
@Slf4j
public class BasicConfig {

    private static final String DEFAULT_CONFIG_PATH="src"+File.separator+"main"+File.separator+"resources"+File.separator+"sdk.properties";
    /**
     * for timeout setting
     */
    public static final String PROPOSAL_WAIT_TIME = "org.hyperledger.fabric.sdk.proposal.wait.time";
    public static final String CHANNEL_CONFIG_WAIT_TIME = "org.hyperledger.fabric.sdk.channelconfig.wait_time";
    public static final String TRANSACTION_CLEANUP_UP_TIMEOUT_WAIT_TIME = "org.hyperledger.fabric.sdk.client.transaction_cleanup_up_timeout_wait_time";
    public static final String ORDERER_RETRY_WAIT_TIME = "org.hyperledger.fabric.sdk.orderer_retry.wait_time";
    public static final String ORDERER_WAIT_TIME = "org.hyperledger.fabric.sdk.orderer.ordererWaitTimeMilliSecs";
    public static final String PEER_EVENT_REGISTRATION_WAIT_TIME = "org.hyperledger.fabric.sdk.peer.eventRegistration.wait_time";
    public static final String PEER_EVENT_RETRY_WAIT_TIME = "org.hyperledger.fabric.sdk.peer.retry_wait_time";
    public static final String EVENTHUB_CONNECTION_WAIT_TIME = "org.hyperledger.fabric.sdk.eventhub_connection.wait_time";
    public static final String EVENTHUB_RECONNECTION_WARNING_RATE = "org.hyperledger.fabric.sdk.eventhub.reconnection_warning_rate";
    public static final String PEER_EVENT_RECONNECTION_WARNING_RATE = "org.hyperledger.fabric.sdk.peer.reconnection_warning_rate";
    public static final String GENESISBLOCK_WAIT_TIME = "org.hyperledger.fabric.sdk.channel.genesisblock_wait_time";


    /**
     * Crypto configuration settings -- settings should not be changed.
     **/
    public static final String DEFAULT_CRYPTO_SUITE_FACTORY = "org.hyperledger.fabric.sdk.crypto.default_crypto_suite_factory";
    public static final String SECURITY_LEVEL = "org.hyperledger.fabric.sdk.security_level";
    public static final String SECURITY_PROVIDER_CLASS_NAME = "org.hyperledger.fabric.sdk.security_provider_class_name";
    public static final String SECURITY_CURVE_MAPPING = "org.hyperledger.fabric.sdk.security_curve_mapping";
    public static final String HASH_ALGORITHM = "org.hyperledger.fabric.sdk.hash_algorithm";
    public static final String ASYMMETRIC_KEY_TYPE = "org.hyperledger.fabric.sdk.crypto.asymmetric_key_type";
    public static final String CERTIFICATE_FORMAT = "org.hyperledger.fabric.sdk.crypto.certificate_format";
    public static final String SIGNATURE_ALGORITHM = "org.hyperledger.fabric.sdk.crypto.default_signature_algorithm";
    /**
     * Logging settings
     **/
    public static final String MAX_LOG_STRING_LENGTH = "org.hyperledger.fabric.sdk.log.stringlengthmax";
    public static final String EXTRALOGLEVEL = "org.hyperledger.fabric.sdk.log.extraloglevel";
    public static final String LOGGERLEVEL = "org.hyperledger.fabric.sdk.loglevel";
    public static final String DIAGNOTISTIC_FILE_DIRECTORY = "org.hyperledger.fabric.sdk.diagnosticFileDir";

    /**
     * Connections settings
     */
    public static final String CONN_SSL_PROVIDER = "org.hyperledger.fabric.sdk.connections.ssl.sslProvider";
    public static final String CONN_SSL_NEGTYPE = "org.hyperledger.fabric.sdk.connections.ssl.negotiationType";

    /**
     * Miscellaneous settings
     **/
    public static final String PROPOSAL_CONSISTENCY_VALIDATION = "org.hyperledger.fabric.sdk.proposal.consistency_validation";


    private static final Properties sdkProperties = new Properties();


    public Properties getDefaultProperties(){
        return sdkProperties;
    }

    private BasicConfig(){
        File loadFile;
        FileInputStream configProps;

        try {

            loadFile = new File(DEFAULT_CONFIG_PATH);
            if(loadFile.exists()){
                configProps = new FileInputStream(loadFile);
                sdkProperties.load(configProps);
            }
        } catch (IOException e) {
            log.error("default config file error",e);
        } finally {

            // Default values
            /**
             * Timeout settings
             **/
            defaultProperty(PROPOSAL_WAIT_TIME, "20000");
            defaultProperty(CHANNEL_CONFIG_WAIT_TIME, "15000");
            defaultProperty(ORDERER_RETRY_WAIT_TIME, "200");
            defaultProperty(ORDERER_WAIT_TIME, "10000");
            defaultProperty(PEER_EVENT_REGISTRATION_WAIT_TIME, "5000");
            defaultProperty(PEER_EVENT_RETRY_WAIT_TIME, "500");
            defaultProperty(EVENTHUB_CONNECTION_WAIT_TIME, "5000");
            defaultProperty(GENESISBLOCK_WAIT_TIME, "5000");
            /**
             * This will NOT complete any transaction futures time out and must be kept WELL above any expected future timeout
             * for transactions sent to the Orderer. For internal cleanup only.
             */

            defaultProperty(TRANSACTION_CLEANUP_UP_TIMEOUT_WAIT_TIME, "600000");

            /**
             * Crypto configuration settings
             **/
            defaultProperty(DEFAULT_CRYPTO_SUITE_FACTORY, "org.hyperledger.fabric.sdk.security.HLSDKJCryptoSuiteFactory");
            defaultProperty(SECURITY_LEVEL, "256");
            defaultProperty(SECURITY_PROVIDER_CLASS_NAME, BouncyCastleProvider.class.getName());
            defaultProperty(SECURITY_CURVE_MAPPING, "256=secp256r1:384=secp384r1");
            defaultProperty(HASH_ALGORITHM, "SHA2");
            defaultProperty(ASYMMETRIC_KEY_TYPE, "EC");

            defaultProperty(CERTIFICATE_FORMAT, "X.509");
            defaultProperty(SIGNATURE_ALGORITHM, "SHA256withECDSA");

            /**
             * Connection defaults
             */

            defaultProperty(CONN_SSL_PROVIDER, "openSSL");
            defaultProperty(CONN_SSL_NEGTYPE, "TLS");

            /**
             * Logging settings
             **/
            defaultProperty(MAX_LOG_STRING_LENGTH, "64");
            defaultProperty(EXTRALOGLEVEL, "0");
            defaultProperty(LOGGERLEVEL, null);
            defaultProperty(DIAGNOTISTIC_FILE_DIRECTORY, null);
            /**
             * Miscellaneous settings
             */
            defaultProperty(PROPOSAL_CONSISTENCY_VALIDATION, "true");
            defaultProperty(EVENTHUB_RECONNECTION_WARNING_RATE, "50");
            defaultProperty(PEER_EVENT_RECONNECTION_WARNING_RATE, "50");

            final String inLogLevel = sdkProperties.getProperty(LOGGERLEVEL);

            if (null != inLogLevel) {

                org.apache.log4j.Level setTo;

                switch (inLogLevel.toUpperCase()) {
                    case "TRACE":
                        setTo = org.apache.log4j.Level.TRACE;
                        break;

                    case "DEBUG":
                        setTo = org.apache.log4j.Level.DEBUG;
                        break;

                    case "INFO":
                        setTo = Level.INFO;
                        break;

                    case "WARN":
                        setTo = Level.WARN;
                        break;
                    case "ERROR":
                        setTo = Level.ERROR;
                        break;
                    default:
                        setTo = Level.INFO;
                        break;
                }

                if (null != setTo) {
                    org.apache.log4j.Logger.getLogger("org.hyperledger.fabric").setLevel(setTo);
                }

            }

        }
    }

    private static void defaultProperty(String key, String value){
        if(null == sdkProperties.getProperty(key) && value != null){
            sdkProperties.put(key, value);
        }
    }

    public Properties getEndPointProperties(final String type, final String name){
        Properties ret = new Properties();

        final String domainName = getDomainName(name);
        File cert = Paths.get(getChannelPath(), "crypto-config/ordererOrganizations".replace("orderer", type), domainName, type + "s",
                name, "tls/server.crt").toFile();
        if (!cert.exists()) {
            throw new RuntimeException(String.format("Missing cert file for: %s. Could not find at location: %s", name,
                    cert.getAbsolutePath()));
        }

        File clientCert;
        File clientKey;
        if ("orderer".equals(type)) {
            clientCert = Paths.get(getChannelPath(), "crypto-config/ordererOrganizations/example.com/users/Admin@example.com/tls/client.crt").toFile();
            clientKey = Paths.get(getChannelPath(), "crypto-config/ordererOrganizations/example.com/users/Admin@example.com/tls/client.key").toFile();
        } else {
            clientCert = Paths.get(getChannelPath(), "crypto-config/peerOrganizations/", domainName, "users/User1@" + domainName, "tls/client.crt").toFile();
            clientKey = Paths.get(getChannelPath(), "crypto-config/peerOrganizations/", domainName, "users/User1@" + domainName, "tls/client.key").toFile();
        }

        if (!clientCert.exists()) {
            throw new RuntimeException(String.format("Missing client cert file for: %s. Could not find at location: %s", name,
                    clientCert.getPath()));
        }

        if (!clientKey.exists()) {
            throw new RuntimeException(String.format("Missing  client key file for: %s. Could not find at location: %s", name,
                    clientKey.getPath()));
        }
        ret.setProperty("clientCertFile", clientCert.getAbsolutePath());
        ret.setProperty("clientKeyFile", clientKey.getAbsolutePath());

        ret.setProperty("pemFile", cert.getAbsolutePath());

        ret.setProperty("hostnameOverride", name);
        ret.setProperty("sslProvider", "openSSL");
        ret.setProperty("negotiationType", "TLS");

        return ret;
    }

    private static String getDomainName(final String name) {
        int dot = name.indexOf(".");
        if (-1 == dot) {
            return null;
        } else {
            return name.substring(dot + 1);
        }
    }

    public static String getChannelPath() {
        return Config.BASIC_CONFIG_PATH+"data"+File.separator;
    }



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
