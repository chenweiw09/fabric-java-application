package com.my.chen.fabric.app.user;

import com.my.chen.fabric.app.client.CAClient;
import lombok.Data;
import org.hyperledger.fabric.sdk.User;

import java.util.*;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/7/17
 * @description
 */
@Data
public class SimpleOrg {

    // 组织名称
    private String orgName;

    // 组织mspId
    private String mspId;

    // 组织域名
    private String domainName;

    // 组织CA
    private CAClient caClient;

    // 组织中的用户
    Map<String, User> userMap = new HashMap<>();

    // peer
    Map<String, String> peerLocations = new HashMap<>();

    // orderer
    Map<String, String> ordererLocations = new HashMap<>();

    // event hubs
    Map<String, String> eventHubLocations = new HashMap<>();

    // 管理员用户
    private UserContext admin;

    // ca 地址
    private String caLocation;


    private Properties caProps;

    private UserContext peerAdmin;

    private String channelName;

    public SimpleOrg(String orgName, String mspId) {
        this.orgName = orgName;
        this.mspId = mspId;
    }

    public Set<String> getPeerNames(){
        return Collections.unmodifiableSet(peerLocations.keySet());
    }

    public Set<String> getOrdererNames(){
        return Collections.unmodifiableSet(ordererLocations.keySet());
    }

//    public Collection<String> getOrdererLocations() {
//        return Collections.unmodifiableCollection(ordererLocations.values());
//    }

    public Collection<String> getEventHubLocations() {
        return Collections.unmodifiableCollection(eventHubLocations.values());
    }

}
