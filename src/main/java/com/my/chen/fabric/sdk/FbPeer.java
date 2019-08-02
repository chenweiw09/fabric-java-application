package com.my.chen.fabric.sdk;

import lombok.Data;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/1
 * @description
 */

@Data
public class FbPeer {

    /** 当前指定的组织节点域名 peer0.org1.example.com*/
    private String peerName;

    /** 当前指定的组织节点事件域名 peer0.org1.example.com*/
    private String peerEventHubName;

    /** 当前指定的组织节点访问地址 grpc://110.131.116.21:7051*/
    private String peerLocation;

    /** 当前指定的组织节点事件监听访问地址 grpc://110.131.116.21:7053*/
    private String peerEventHubLocation;

    /** 当前peer是否增加Event事件处理 */
    private boolean addEventHub;

    public FbPeer(String peerName, String peerEventHubName, String peerLocation, String peerEventHubLocation, boolean addEventHub) {
        this.peerName = peerName;
        this.peerEventHubName = peerEventHubName;
        this.peerLocation = peerLocation;
        this.peerEventHubLocation = peerEventHubLocation;
        this.addEventHub = addEventHub;
    }

}
