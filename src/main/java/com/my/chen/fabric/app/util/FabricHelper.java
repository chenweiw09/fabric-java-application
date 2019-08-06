package com.my.chen.fabric.app.util;

import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.domain.*;
import com.my.chen.fabric.sdk.FbNetworkManager;
import com.my.chen.fabric.sdk.OrgManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/2
 * @description
 */
@Slf4j
public class FabricHelper {

    // 这里是在页面端看到的chaincode，之所以用chaincodeid做关联，是因为我们通常在chaincode上做管理
    private int chaincodeId;

    private static FabricHelper instance;

    private Map<Integer, FbNetworkManager> fabricManagerMap;


    private FabricHelper(){
        fabricManagerMap = new ConcurrentHashMap<>();
    }

    // 单例模式获取对象
    public static FabricHelper getInstance(){
        if (null == instance) {
            synchronized (FabricHelper.class) {
                if (null == instance) {
                    instance = new FabricHelper();
                }
            }
        }
        return instance;
    }


    public void removeManager(List<Peer> peers, ChannelMapper channelMapper, ChaincodeMapper chaincodeMapper){
        for(Peer peer : peers){
            List<Channel> channels = channelMapper.findByPeerId(peer.getId());
            for (Channel channel : channels) {
                List<Chaincode> chaincodes = chaincodeMapper.findByChannelId(channel.getId());
                for (Chaincode chaincode : chaincodes) {
                    fabricManagerMap.remove(chaincode.getId());
                }
            }
        }
    }


    public void removeManager(List<Channel> channels, ChaincodeMapper chaincodeMapper){
        for (Channel channel : channels) {
            List<Chaincode> chaincodes = chaincodeMapper.findByChannelId(channel.getId());
            for (Chaincode chaincode : chaincodes) {
                fabricManagerMap.remove(chaincode.getId());
            }
        }
    }

    public void removeManager(int chaincodeId){
        fabricManagerMap.remove(chaincodeId);
    }


    public FbNetworkManager get(OrgMapper orgMapper, ChannelMapper channelMapper, ChaincodeMapper chaincodeMapper,
                             OrdererMapper ordererMapper, PeerMapper peerMapper) throws Exception {
        return get(orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper, -1);
    }


    public FbNetworkManager get(OrgMapper orgMapper, ChannelMapper channelMapper, ChaincodeMapper chaincodeMapper,
                                OrdererMapper ordererMapper, PeerMapper peerMapper, int chaincodeId) throws Exception {

        this.chaincodeId = chaincodeId;

        FbNetworkManager fabricManager = fabricManagerMap.get(chaincodeId);
        // 如果没有被缓存过，就需要创建网络
        if(null ==  fabricManager){
            Chaincode chaincode = chaincodeMapper.findById(chaincodeId).get();
            log.info(String.format("chaincode = %s", chaincode.toString()));

            Channel channel =  channelMapper.findById(chaincode.getChannelId()).get();
            log.info(String.format("channel = %s", channel.toString()));

            Peer peer = peerMapper.findById(channel.getPeerId()).get();
            log.info(String.format("peer = %s", peer.toString()));

            int orgId = peer.getOrgId();
            List<Peer> peers = peerMapper.findByOrgId(orgId);
            List<Orderer> orderers = ordererMapper.findByOrgId(orgId);
            Org org = orgMapper.findById(orgId).get();

            log.info(String.format("org = %s", org.toString()));
            if (orderers.size() != 0 && peers.size() != 0) {
                fabricManager = createFabricManager(org, channel, chaincode, orderers, peers);
                fabricManagerMap.put(chaincodeId, fabricManager);
            }

        }
        return fabricManager;
    }

    private FbNetworkManager createFabricManager(Org org, Channel channel, Chaincode chainCode, List<Orderer> orderers, List<Peer> peers) throws Exception {
        OrgManager orgManager = new OrgManager();
        orgManager
                .init(org.getId(), org.isTls())
                .setUser(org.getUsername(), org.getCryptoConfigDir())
                .setPeers(org.getName(), org.getMspId(), org.getDomainName())
                .setOrderers(org.getOrdererDomainName())
                .setChannel(channel.getName())
                .setChainCode(chainCode.getName(), chainCode.getPath(), chainCode.getSource(), chainCode.getPolicy(), chainCode.getVersion(), chainCode.getProposalWaitTime(), chainCode.getInvokeWaitTime())
                .setBlockListener(map -> {
                    log.info(map.get("code"));
                    log.info(map.get("data"));
                });
        for (Orderer orderer : orderers) {
            orgManager.addOrderer(orderer.getName(), orderer.getLocation());
        }
        for (Peer peer : peers) {
            orgManager.addPeer(peer.getName(), peer.getEventHubName(), peer.getLocation(), peer.getEventHubLocation(), peer.isEventListener());
        }
        orgManager.add();
        return orgManager.use(org.getId());
    }

}
