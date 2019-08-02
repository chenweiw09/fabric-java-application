package com.my.chen.fabric.sdk;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;

import java.io.*;
import java.security.PrivateKey;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/1
 * @description 文件存储
 */

@Slf4j
public class FbStore {

    private String file;

    private final Map<String, FbUser> userMap = new HashMap<>();


    static {
        try {
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FbStore(File file) {
        this.file = file.getAbsolutePath();
    }

    public void setValue(String name, String value){
        Properties properties = loadProperties();
        try (OutputStream output = new FileOutputStream(file)) {
            properties.setProperty(name, value);
            properties.store(output, "");
        } catch (IOException e) {
            System.out.println(String.format("Could not save the keyvalue store, reason:%s", e.getMessage()));
        }
    }

    public String getValue(String name) {
        Properties properties = loadProperties();
        return properties.getProperty(name);
    }


    public FbUser getUser(String name, String orgName, String mspId, File privateKeyFile, File certificateFile) throws IOException {
        FbUser user = userMap.get(FbUser.getKeyForFabricStoreName(name, orgName));
        if(user != null){
            log.info("read user from userMap,{}",user);
            return user;
        }

        user = new FbUser(name, orgName, this);
        user.setMspId(mspId);
        CAEnrollment enrollment = getEnrollment(privateKeyFile, certificateFile);
        user.setEnrollment(enrollment);
        user.saveState();
        userMap.put(FbUser.getKeyForFabricStoreName(name, orgName), user);
        return user;
    }



    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            properties.load(input);
        } catch (FileNotFoundException e) {
            System.out.println(String.format("Could not find the file \"%s\"", file));
        } catch (IOException e) {
            System.out.println(String.format("Could not load keyvalue store from file \"%s\", reason:%s", file, e.getMessage()));
        }
        return properties;
    }


    static CAEnrollment getEnrollment(File privateKeyFile, File certificateFile) throws IOException {
        String certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), "UTF-8");
        PrivateKey privateKey = getPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream(privateKeyFile)));
        return new CAEnrollment(privateKey, certificate);

    }

    static PrivateKey getPrivateKeyFromBytes(byte[] data) throws IOException {
        final Reader pemReader = new StringReader(new String(data));
        final PrivateKeyInfo pemPair;
        try (PEMParser pemParser = new PEMParser(pemReader)) {
            pemPair = (PrivateKeyInfo) pemParser.readObject();
        }
        PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(pemPair);
        return privateKey;
    }


    static final class CAEnrollment implements Enrollment, Serializable {

        private static final long serialVersionUID = 6965341351799577442L;

        /** 私钥 */
        private final PrivateKey privateKey;
        /** 授权证书 */
        private final String certificate;

        CAEnrollment(PrivateKey privateKey, String certificate) {
            this.certificate = certificate;
            this.privateKey = privateKey;
        }

        @Override
        public PrivateKey getKey() {
            return privateKey;
        }

        @Override
        public String getCert() {
            return certificate;
        }
    }
}
