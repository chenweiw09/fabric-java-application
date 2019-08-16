package com.my.chen.fabric.sdk;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/1
 * @description
 */
@Slf4j
@Data
public class FbOrg {

    private FbClient client;

    /** org的执行用户，通常是从admin user授权注册回来的 */
    private String username;

    /** 当前指定的组织名称，如：Org1MSP */
    private String orgMSPID;


    /** orderer 集合 */
    private List<FbOrderer> orderers = new LinkedList<>();

    private List<FbPeer> peers = new LinkedList<>();

    /** 是否开启TLS访问 */
    private boolean openTLS;

    /** 属于组织的通道 */
    private FbChannel channel;

    /** 事件监听 */
    private BlockListener blockListener;


    private Map<String, User> userMap = new HashMap<>();

    /** 一个组织可能维护了多个智能合约对象 */
    private List<FbChainCode> chainCodes = new LinkedList<>();


    private String eventNames;

    private ChaincodeEventListener chaincodeEventListener;



    /** 新增节点服务器 */
    void addPeer(String peerName, String peerLocation, String peerEventHubLocation,String serverCrtPath, String clientCertPath, String clientKeyPath) {
        peers.add(new FbPeer(peerName, peerLocation, peerEventHubLocation, serverCrtPath,clientCertPath,clientKeyPath));
    }

    /** 新增排序服务器 */
    void addOrderer(String name, String location, String serverCrtPath, String clientCertPath, String clientKeyPath) {
        orderers.add(new FbOrderer(name, location, serverCrtPath,clientCertPath,clientKeyPath));
    }


    public User getUser(){
        return userMap.get(username);
    }

    private void addUser(FbUser user) {
        userMap.put(user.getName(), user);
    }


    void openTLS(boolean openTLS) {
        this.openTLS = openTLS;
    }




    /**
     * 获取 peer 节点的管理员
     * @param fbStore
     */
//    public void init(FbStore fbStore) throws IOException {
//        setPeerAdmin(fbStore);
//    }
//
//    private void setPeerAdmin(FbStore fabricStore) throws IOException {
//        File skFile = Paths.get(cryptoConfigPath, "/peerOrganizations/", orgDomainName, String.format("/users/%s@%s/msp/keystore", "Admin", orgDomainName)).toFile();
//        File certificateFile = Paths.get(cryptoConfigPath, "/peerOrganizations/", getOrgDomainName(),
//                String.format("/users/%s@%s/msp/signcerts/%s@%s-cert.pem", "Admin", orgDomainName, "Admin", orgDomainName)).toFile();
//        log.debug("skFile = " + skFile.getAbsolutePath());
//        log.debug("certificateFile = " + certificateFile.getAbsolutePath());
//        // 一个特殊的用户，可以创建通道，连接对等点，并安装链码
//        addUser(fabricStore.getUser(username, orgName, orgMSPID, findFileSk(skFile), certificateFile));
//    }


    private File findFileSk(File directory) {
        File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));
        if (null == matches) {
            throw new RuntimeException(String.format("Matches returned null does %s directory exist?", directory.getAbsoluteFile().getName()));
        }
        if (matches.length != 1) {
            throw new RuntimeException(String.format("Expected in %s only 1 sk file but found %d", directory.getAbsoluteFile().getName(), matches.length));
        }
        return matches[0];
    }

    void setChaincodeEventListener(String eventNames, ChaincodeEventListener chaincodeEventListener) {
        this.eventNames = eventNames;
        this.chaincodeEventListener = chaincodeEventListener;
    }

    ChaincodeEventListener getChaincodeEventListener() {
        return chaincodeEventListener;
    }

}
