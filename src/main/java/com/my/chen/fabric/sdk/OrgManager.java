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
 * @description 先对组织init，然后设置组织的管理员账户，最后初始化组织的网络环境
 */
public class OrgManager {

    private Map<Integer, FbOrg> orgMap;

    private int orgId;

    public OrgManager() {
        this.orgMap = new HashMap<>();
    }

    public OrgManager init(int orgId, String leagueName, String orgName, String orgMSPID, boolean openTLS){
        this.orgId = orgId;

        if (orgMap.get(orgId) != null) {
            throw new RuntimeException(String.format("OrgManager had the same id of %s", orgId));
        } else {
            orgMap.put(orgId, new FbOrg());
        }

        orgMap.get(orgId).openTLS(openTLS);
        orgMap.get(orgId).setOrgMSPID(orgMSPID);
        orgMap.get(orgId).setLeagueName(leagueName);
        orgMap.get(orgId).setOrgName(orgName);

        File storeFile = new File(String.format("%s/HFCStore%s.properties", System.getProperty("java.io.tmpdir"), orgId));
        FbStore fabricStore = new FbStore(storeFile);
        orgMap.get(orgId).setFabricStore(fabricStore);

        return this;
    }

    public OrgManager setUser(@Nonnull String username, @Nonnull String skPath, @Nonnull String certificatePath) {
        orgMap.get(orgId).setUsername(username);
        orgMap.get(orgId).addUser(username, skPath, certificatePath);
        return this;
    }

    public FbNetworkManager use(int orgId) throws Exception {
        FbOrg org = orgMap.get(orgId);
        org.setClient(new FbClient(org.getUser()));
        org.getChannel().init(org);
        return new FbNetworkManager(org);
    }

    public void addOrderer(String name, String location, String serverCrtPath, String clientCertPath, String clientKeyPath) {
        orgMap.get(orgId).addOrderer(name, String.format("%s%s", "grpc://", location), serverCrtPath, clientCertPath, clientKeyPath);
    }


    public void addPeer(String peerName, String peerLocation,  String peerEventHubLocation, String serverCrtPath, String clientCertPath, String clientKeyPath) {
        orgMap.get(orgId).addPeer(peerName,String.format("%s%s", "grpc://", peerLocation), String.format("%s%s", "grpc://", peerEventHubLocation),
                serverCrtPath, clientCertPath, clientKeyPath);
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
            orgMap.get(orgId).getPeers().get(i).
                    setPeerLocation(grpcTLSify(orgMap.get(orgId).isOpenTLS(), orgMap.get(orgId).getPeers().get(i).getPeerLocation()));

            orgMap.get(orgId).getPeers().get(i)
                    .setPeerEventHubLocation(grpcTLSify(orgMap.get(orgId).isOpenTLS(), orgMap.get(orgId).getPeers().get(i).getPeerEventHubLocation()));
        }

        // 根据TLS开启状态循环确认Orderer节点各服务的请求grpc协议
        for (int i = 0; i < orgMap.get(orgId).getOrderers().size(); i++) {
            orgMap.get(orgId).getOrderers().get(i)
                    .setOrdererLocation(grpcTLSify(orgMap.get(orgId).isOpenTLS(), orgMap.get(orgId).getOrderers().get(i).getOrdererLocation()));
        }
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
