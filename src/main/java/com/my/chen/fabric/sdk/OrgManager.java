package com.my.chen.fabric.sdk;

import org.hyperledger.fabric.sdk.helper.Utils;

import javax.annotation.Nonnull;
import java.io.File;
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
public class OrgManager {

    private Map<Integer, FbOrg> orgMap;

    private int orgId;

    public OrgManager() {
        this.orgMap = new HashMap<>();
    }

    public OrgManager init(int orgId, boolean openTLS){
        this.orgId = orgId;

        if (orgMap.get(orgId) != null) {
            throw new RuntimeException(String.format("OrgManager had the same id of %s", orgId));
        } else {
            orgMap.put(orgId, new FbOrg());
        }
        orgMap.get(orgId).openTLS(openTLS);
        return this;
    }

    public OrgManager setUser(@Nonnull String username, @Nonnull String cryptoConfigPath) {
        orgMap.get(orgId).setUsername(username);
        orgMap.get(orgId).setCryptoConfigPath(cryptoConfigPath);
        return this;
    }

    public OrgManager setOrderers(String ordererDomainName) {
        orgMap.get(orgId).setOrdererDomainName(ordererDomainName);
        return this;
    }

    public OrgManager addOrderer(String name, String location) {
        orgMap.get(orgId).addOrderer(name, location);
        return this;
    }

    public OrgManager setPeers(String orgName, String orgMSPID, String orgDomainName) {
        orgMap.get(orgId).setOrgName(orgName);
        orgMap.get(orgId).setOrgMSPID(orgMSPID);
        orgMap.get(orgId).setOrgDomainName(orgDomainName);
        return this;
    }

    public OrgManager addPeer(String peerName, String peerEventHubName, String peerLocation, String peerEventHubLocation, boolean isEventListener) {
        orgMap.get(orgId).addPeer(peerName, peerEventHubName, peerLocation, peerEventHubLocation, isEventListener);
        return this;
    }


    public OrgManager setChainCode(String chaincodeName, String chaincodePath, String chaincodeSource, String chaincodePolicy, String chaincodeVersion, int proposalWaitTime, int invokeWaitTime) {
        FbChainCode chaincode = new FbChainCode();
        chaincode.setChaincodeName(chaincodeName);
        chaincode.setChaincodeSource(chaincodeSource);
        chaincode.setChaincodePath(chaincodePath);
        chaincode.setChaincodePolicy(chaincodePolicy);
        chaincode.setChaincodeVersion(chaincodeVersion);
        chaincode.setProposalWaitTime(proposalWaitTime);
        chaincode.setTransactionWaitTime(invokeWaitTime);
        if(orgMap.get(orgId).getChainCodes() == null){
            List<FbChainCode> list = new LinkedList<>();
            list.add(chaincode);
            orgMap.get(orgId).setChainCodes(list);
        }else {
            orgMap.get(orgId).getChainCodes().add(chaincode);
        }
        return this;
    }

    public OrgManager setChannel(String channelName) {
        FbChannel channel = new FbChannel();
        channel.setChannelName(channelName);
        orgMap.get(orgId).setChannel(channel);
        return this;
    }

    public OrgManager setBlockListener(BlockListener blockListener) {
        orgMap.get(orgId).setBlockListener(blockListener);
        return this;
    }

    public void add() {
        if (orgMap.get(orgId).getPeers().size() == 0) {
            throw new RuntimeException("peers is null or peers size is 0");
        }
        if (orgMap.get(orgId).getOrderers().size() == 0) {
            throw new RuntimeException("orderers is null or orderers size is 0");
        }
        if (orgMap.get(orgId).getChainCodes() == null) {
            throw new RuntimeException("chaincode must be instantiated");
        }

        // 根据TLS开启状态循环确认Peer节点各服务的请求grpc协议
        for (int i = 0; i < orgMap.get(orgId).getPeers().size(); i++) {
            orgMap.get(orgId).getPeers().get(i).setPeerLocation(grpcTLSify(orgMap.get(orgId).openTLS(), orgMap.get(orgId).getPeers().get(i).getPeerLocation()));
            orgMap.get(orgId).getPeers().get(i).setPeerEventHubLocation(grpcTLSify(orgMap.get(orgId).openTLS(), orgMap.get(orgId).getPeers().get(i).getPeerEventHubLocation()));
        }
        // 根据TLS开启状态循环确认Orderer节点各服务的请求grpc协议
        for (int i = 0; i < orgMap.get(orgId).getOrderers().size(); i++) {
            orgMap.get(orgId).getOrderers().get(i).setOrdererLocation(grpcTLSify(orgMap.get(orgId).openTLS(), orgMap.get(orgId).getOrderers().get(i).getOrdererLocation()));
        }
    }

    public FbNetworkManager use(int orgId) throws Exception {
        FbOrg org = orgMap.get(orgId);

        // java.io.tmpdir : C:\Users\aberic\AppData\Local\Temp\

        File storeFile = new File(String.format("%s/HFCStore%s.properties", System.getProperty("java.io.tmpdir"), orgId));
        FbStore fabricStore = new FbStore(storeFile);
        org.init(fabricStore);
        org.setClient(new FbClient(org.getUser()));
        org.getChannel().init(org);
        return new FbNetworkManager(org);
    }

    private String grpcTLSify(boolean openTLS, String location) {
        location = location.trim();
        Exception e = Utils.checkGrpcUrl(location);
        if (e != null) {
            throw new RuntimeException(String.format("Bad TEST parameters for grpc url %s", location), e);
        }
        return openTLS ? location.replaceFirst("^grpc://", "grpcs://") : location;

    }

    private String httpTLSify(boolean openCATLS, String location) {
        location = location.trim();
        return openCATLS ? location.replaceFirst("^http://", "https://") : location;
    }
}
