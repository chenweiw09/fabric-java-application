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
 *    这里同样不能用orgId作为map的key，因为org里面有用户的信息，还有channel的代理，如果用不同的身份登录，需要看到不同的org，所以map的key需要身份关联
 */
public class OrgManager {

    private Map<String, FbOrg> orgMap;

    private String chaincodeAndUserSign;

    public OrgManager() {
        this.orgMap = new HashMap<>();
    }

    public OrgManager init(String sign, String leagueName, String orgName, String orgMSPID, boolean openTLS){
        this.chaincodeAndUserSign = sign;

        if (orgMap.get(sign) != null) {
            throw new RuntimeException(String.format("OrgManager had the same id of %s", sign));
        } else {
            orgMap.put(sign, new FbOrg());
        }

        orgMap.get(sign).openTLS(openTLS);
        orgMap.get(sign).setOrgMSPID(orgMSPID);
        orgMap.get(sign).setLeagueName(leagueName);
        orgMap.get(sign).setOrgName(orgName);

        File storeFile = new File(String.format("%s/HFCStore%s.properties", System.getProperty("java.io.tmpdir"), sign));
        FbStore fabricStore = new FbStore(storeFile);
        orgMap.get(sign).setFabricStore(fabricStore);

        return this;
    }

    public OrgManager setUser(@Nonnull String username, @Nonnull String skPath, @Nonnull String certificatePath) {
        orgMap.get(chaincodeAndUserSign).setUsername(username);
        orgMap.get(chaincodeAndUserSign).addUser(username, skPath, certificatePath);
        return this;
    }

    public FbNetworkManager use(String chaincodeAndUserSign) throws Exception {
        FbOrg org = orgMap.get(chaincodeAndUserSign);
        org.setClient(new FbClient(org.getUser()));
        org.getChannel().init(org);
        return new FbNetworkManager(org);
    }

    public void addOrderer(String name, String location, String serverCrtPath, String clientCertPath, String clientKeyPath) {
        orgMap.get(chaincodeAndUserSign).addOrderer(name, String.format("%s%s", "grpc://", location), serverCrtPath, clientCertPath, clientKeyPath);
    }


    public void addPeer(String peerName, String peerLocation,  String peerEventHubLocation, String serverCrtPath, String clientCertPath, String clientKeyPath) {
        orgMap.get(
                chaincodeAndUserSign
        ).addPeer(peerName,String.format("%s%s", "grpc://", peerLocation), String.format("%s%s", "grpc://", peerEventHubLocation),
                serverCrtPath, clientCertPath, clientKeyPath);
    }


    public OrgManager setChainCode(String chaincodeName, String chaincodePath, String chaincodeSource, String chaincodePolicy, String chaincodeVersion, int proposalWaitTime) {
        FbChainCode chaincode = new FbChainCode();
        chaincode.setChaincodeName(chaincodeName);
        chaincode.setChaincodeSource(chaincodeSource);
        chaincode.setChaincodePath(chaincodePath);
        chaincode.setChaincodePolicy(chaincodePolicy);
        chaincode.setChaincodeVersion(chaincodeVersion);
        chaincode.setProposalWaitTime(proposalWaitTime);
//        chaincode.setTransactionWaitTime(invokeWaitTime);
        if(orgMap.get(chaincodeAndUserSign).getChainCodes() == null){
            List<FbChainCode> list = new LinkedList<>();
            list.add(chaincode);
            orgMap.get(chaincodeAndUserSign).setChainCodes(list);
        }else {
            orgMap.get(chaincodeAndUserSign).getChainCodes().add(chaincode);
        }
        return this;
    }

    public OrgManager setChannel(String channelName) {
        FbChannel channel = new FbChannel();
        channel.setChannelName(channelName);
        orgMap.get(chaincodeAndUserSign).setChannel(channel);
        return this;
    }

    public OrgManager setBlockListener(BlockListener blockListener) {
        orgMap.get(chaincodeAndUserSign).setBlockListener(blockListener);
        return this;
    }

    public void setChaincodeEventListener(String eventNames, ChaincodeEventListener chaincodeEventListener) {
        orgMap.get(chaincodeAndUserSign).setChaincodeEventListener(eventNames, chaincodeEventListener);
    }

    public void add() {
        if (orgMap.get(chaincodeAndUserSign).getPeers().size() == 0) {
            throw new RuntimeException("peers is null or peers size is 0");
        }
        if (orgMap.get(chaincodeAndUserSign).getOrderers().size() == 0) {
            throw new RuntimeException("orderers is null or orderers size is 0");
        }
        if (orgMap.get(chaincodeAndUserSign).getChainCodes() == null) {
            throw new RuntimeException("chaincode must be instantiated");
        }

        // 根据TLS开启状态循环确认Peer节点各服务的请求grpc协议
        for (int i = 0; i < orgMap.get(chaincodeAndUserSign).getPeers().size(); i++) {
            orgMap.get(chaincodeAndUserSign).getPeers().get(i).
                    setPeerLocation(grpcTLSify(orgMap.get(chaincodeAndUserSign).isOpenTLS(), orgMap.get(chaincodeAndUserSign).getPeers().get(i).getPeerLocation()));

            orgMap.get(chaincodeAndUserSign).getPeers().get(i)
                    .setPeerEventHubLocation(grpcTLSify(orgMap.get(chaincodeAndUserSign).isOpenTLS(), orgMap.get(chaincodeAndUserSign).getPeers().get(i).getPeerEventHubLocation()));
        }

        // 根据TLS开启状态循环确认Orderer节点各服务的请求grpc协议
        for (int i = 0; i < orgMap.get(chaincodeAndUserSign).getOrderers().size(); i++) {
            orgMap.get(chaincodeAndUserSign).getOrderers().get(i)
                    .setOrdererLocation(grpcTLSify(orgMap.get(chaincodeAndUserSign).isOpenTLS(), orgMap.get(chaincodeAndUserSign).getOrderers().get(i).getOrdererLocation()));
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
