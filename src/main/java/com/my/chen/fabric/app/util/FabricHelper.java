package com.my.chen.fabric.app.util;

import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.domain.*;
import com.my.chen.fabric.sdk.FbNetworkManager;
import com.my.chen.fabric.sdk.OrgManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/2
 * @description  这里是在页面端看到的chaincode，之所以用chaincodeid做关联，是因为我们通常在chaincode上做管理
 *               修正：这里不能用chaincodeId做关联，因为同一个chaincode，在安装的时候用到的身份是peer，实例化的时候用到的身份是Orderer，
 *               所以需要根据身份来保存对应的网路信息
 */
@Slf4j
public class FabricHelper {

    private static FabricHelper instance;

    private Map<String, FbNetworkManager> fabricManagerMap;

    private Map<Integer, String> signMap;


    private FabricHelper(){
        fabricManagerMap = new ConcurrentHashMap<>();
        signMap = new ConcurrentHashMap<>();
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
            removeManager(channelMapper.findByPeerId(peer.getId()), chaincodeMapper);
        }
    }


    public void removeManager(List<Channel> channels, ChaincodeMapper chaincodeMapper){
        for (Channel channel : channels) {
            removeManager(chaincodeMapper.findByChannelId(channel.getId()));
        }
    }

    public void removeManager(List<Chaincode> chaincodes){
        for(Chaincode chaincode:chaincodes){
            removeManager(chaincode.getId());
        }
    }

    public void removeManager(int chaincodeId){
        String sign = signMap.get(chaincodeId);
        if(StringUtils.isNotBlank(sign)){
            fabricManagerMap.remove(sign);
        }
        signMap.remove(chaincodeId);
    }


    public FbNetworkManager get(LeagueMapper leagueMapper, OrgMapper orgMapper, ChannelMapper channelMapper, ChaincodeMapper chaincodeMapper,
                                OrdererMapper ordererMapper, PeerMapper peerMapper, CA ca, int chaincodeId) throws Exception {

        String cacheName = ca.getFlag()+chaincodeId;

        FbNetworkManager fabricManager = fabricManagerMap.get(cacheName);
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

            League league = leagueMapper.findById(org.getLeagueId()).get();
            log.info(String.format("league = %s", league.getName()));

            if (orderers.size() != 0 && peers.size() != 0) {
                fabricManager = createFabricManager(league, org, channel, chaincode, orderers, peers, ca, cacheName);
                fabricManagerMap.put(cacheName, fabricManager);
                signMap.put(chaincodeId, cacheName);
            }

        }
        return fabricManager;
    }



    private static FbNetworkManager createFabricManager(League league, Org org, Channel channel, Chaincode chainCode,
                                                        List<Orderer> orderers, List<Peer> peers, CA ca, String cacheName) throws Exception {
        OrgManager orgManager = new OrgManager();

        // init manager
        orgManager.init(cacheName, league.getName(), org.getName(), org.getMspId(), org.isTls())
                .setUser(ca.getName(), ca.getSk(), ca.getCertificate())
                .setChannel(channel.getName())
                .setChainCode(chainCode.getName(), chainCode.getPath(), chainCode.getSource(), chainCode.getPolicy(),
                        chainCode.getVersion(), chainCode.getProposalWaitTime());

        for (Orderer orderer : orderers) {
            orgManager.addOrderer(orderer.getName(), orderer.getLocation(),orderer.getServerCrtPath(), orderer.getClientCertPath(), orderer.getClientKeyPath());
        }
        for (Peer peer : peers) {
            orgManager.addPeer(peer.getName(), peer.getLocation(), peer.getEventHubLocation(), peer.getServerCrtPath(), peer.getClientCertPath(), peer.getClientKeyPath());
        }


        // init blockListener
        orgManager.setBlockListener(jsonObject -> {
            try {
                if (channel.isBlockListener() && StringUtils.isNotEmpty(channel.getCallbackLocation())) {
                    HttpUtil.post(channel.getCallbackLocation(), jsonObject.toJSONString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            BlockUtil.obtain().updataChannelData(channel.getId());
        });

        // init chaincodeListener
        if (chainCode.isChaincodeEventListener() && StringUtils.isNotEmpty(chainCode.getCallbackLocation())
                && StringUtils.isNotEmpty(chainCode.getEvents())) {
            orgManager.setChaincodeEventListener(chainCode.getEvents(), (handle, jsonObject, eventName, chaincodeId, txId) -> {
                log.debug(String.format("handle = %s", handle));
                log.debug(String.format("eventName = %s", eventName));
                log.debug(String.format("chaincodeId = %s", chaincodeId));
                log.debug(String.format("txId = %s", txId));
                log.debug(String.format("code = %s", String.valueOf(jsonObject.getInteger("code"))));
                log.debug(String.format("data = %s", jsonObject.getJSONObject("data").toJSONString()));
                try {
                    jsonObject.put("handle", handle);
                    jsonObject.put("eventName", eventName);
                    jsonObject.put("chaincodeId", chaincodeId);
                    jsonObject.put("txId", txId);
                    HttpUtil.post(chainCode.getCallbackLocation(), jsonObject.toJSONString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }

        orgManager.add();
        return orgManager.use(cacheName);
    }




//    public static void main(String[] args) {
//
//        Org org = new Org();
//        org.setDomainName("org1.test.com");
//        org.setCryptoConfigDir("C://home/xiaomi/Org1/crypto-config");
//        org.setUsername("admin");
//        org.setMspId("Org1MSP");
//        org.setOrdererDomainName("test.com");
//        org.setTls(false);
//        org.setLeagueName("xiaomi");
//        org.setName("org1");
//
//        Orderer orderer = new Orderer();
//        orderer.setName("orderer.test.com");
//        orderer.setLocation("grpc://192.168.235.128:7050");
//        List<Orderer> orderers = Arrays.asList(orderer);
//
//        Peer peer = new Peer();
//        peer.setName("peer0.org1.test.com");
//        peer.setLocation("grpc://192.168.235.128:7051");
//        peer.setEventHubName("eventhub0.org1.test.com");
//        peer.setEventHubLocation("grpc://192.168.235.128:7053");
//
//
//        Peer peer1 = new Peer();
//        peer1.setName("peer1.org1.test.com");
//        peer1.setLocation("grpc://192.168.235.128:8051");
//        peer1.setEventHubName("eventhub1.org1.test.com");
//        peer1.setEventHubLocation("grpc://192.168.235.128:8053");
//
//        List<Peer> peers = Arrays.asList(peer);
//
//
//        Channel channel = new Channel();
//        channel.setName("mychannel");
//        channel.setPeerName(peer.getName());
//        channel.setOrgName(org.getName());
//
//
//        Chaincode chaincode = new Chaincode();
//        chaincode.setName("mycc");
//        chaincode.setSource("/go/path");
//        chaincode.setPath("github.com/chaincode/chaincode_example02/go/");
//        chaincode.setVersion("1.0");
//        chaincode.setInvokeWaitTime(100000);
//        chaincode.setProposalWaitTime(120);
//
//
//        try {
//            FbNetworkManager manager = createFabricManager(org, channel, chaincode, orderers, peers);
//
//            List list = manager.getChannelPeers();
//            System.out.println(list);
//
//            Collection<org.hyperledger.fabric.sdk.Peer> peers1 = manager.getOrg().getChannel().getChannel().getPeers();
//            List chaincodes  = manager.getOrg().getChannel().getChannel().queryInstantiatedChaincodes(peers1.iterator().next());
//
//            System.out.println(chaincodes);
//
//            Map<String, String> map = manager.invoke("query",new String[]{"a"});
//
//            System.out.println(map);
////            QueryByChaincodeRequest request = manager.getOrg().getClient().getClient().newQueryProposalRequest();
////            ChaincodeID ccid = ChaincodeID.newBuilder().setName(chaincode.getName()).build();
////            request.setChaincodeID(ccid);
////            request.setFcn("query");
////            request.setArgs(new String[]{"a"});
////            Collection<ProposalResponse> response = manager.getOrg().getChannel().getChannel().queryByChaincode(request);
////
////            System.out.println(response);
//
//            System.out.println(manager);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
}
