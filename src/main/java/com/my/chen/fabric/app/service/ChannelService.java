package com.my.chen.fabric.app.service;

import com.my.chen.fabric.app.client.ChannelClient;
import com.my.chen.fabric.app.client.FabricClient;
import com.my.chen.fabric.app.user.SimpleOrg;
import com.my.chen.fabric.app.user.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import java.io.File;
import java.util.*;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/7/18
 * @description
 */
@Slf4j
public class ChannelService {



    public ChannelClient getChannelClient(SimpleOrg org){
        try {
            UserContext adminUserContext = org.getAdmin();

            FabricClient fabClient = new FabricClient(adminUserContext);

            // find one oerderer
            Map<String, String> ordererLocations = org.getOrdererLocations();
            String orderName = ordererLocations.keySet().iterator().next();
            Orderer orderer = fabClient.getInstance().newOrderer(orderName, ordererLocations.get(orderName));

            // find one peer
            Map<String, String> peerLocations = org.getPeerLocations();
            String peerName = peerLocations.keySet().iterator().next();
            Peer peer = fabClient.getInstance().newPeer(peerName, peerLocations.get(peerName));

            // find one eventHub
            Collection<String> hubLocations = org.getEventHubLocations();
            String hubLocation = hubLocations.iterator().next();
            EventHub eventHub = fabClient.getInstance().newEventHub("eventhub01", hubLocation);

            // init channel
            ChannelClient channelClient = fabClient.createChannelClient(org.getChannelName());
            Channel channel = channelClient.getChannel();
            channel.addPeer(peer);
            channel.addOrderer(orderer);
            channel.addEventHub(eventHub);
            if(!channel.isInitialized()){
                channel.initialize();
            }

            return channelClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public boolean createChannel(List<SimpleOrg> simpleOrgs, String channelConfigPath) {
        try {

            // use simpleOrg create channel;
            SimpleOrg org1 = simpleOrgs.get(0);
            UserContext org1Admin = org1.getAdmin();

            FabricClient fabClient = new FabricClient(org1Admin);

            Map<String, String> peerLocations = org1.getPeerLocations();
            List<Peer> peers = new ArrayList<>();
            for(String peerName:peerLocations.keySet()){
                peers.add(fabClient.getInstance().newPeer(peerName, peerLocations.get(peerName)));
            }

            // first query channel been created
            Set<String> channels = fabClient.getInstance().queryChannels(peers.get(0));
            if(channels != null && channels.contains(org1.getChannelName())){
                return true;
            }

            // create channel
            Map<String, String> ordererLocations = org1.getOrdererLocations();
            String orderName = ordererLocations.keySet().iterator().next();
            Orderer orderer = fabClient.getInstance().newOrderer(orderName, ordererLocations.get(orderName));


            ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(channelConfigPath));
            byte[] channelConfigurationSignatures = fabClient.getInstance()
                    .getChannelConfigurationSignature(channelConfiguration, org1Admin);

            Channel mychannel = fabClient.getInstance().newChannel(org1.getChannelName(), orderer, channelConfiguration,
                    channelConfigurationSignatures);

            mychannel.addOrderer(orderer);
            for(Peer peer : peers){
                mychannel.joinPeer(peer);
            }

            if(!mychannel.isInitialized()){
                mychannel.initialize();
            }

            // add org2
            SimpleOrg org2 = simpleOrgs.get(1);
            UserContext org2Admin = org2.getAdmin();
            fabClient.getInstance().setUserContext(org2Admin);
            mychannel = fabClient.getInstance().getChannel(org2.getChannelName());
            Map<String, String> org2PeerLocations = org2.getPeerLocations();
            for(Map.Entry<String, String> entry: org2PeerLocations.entrySet()){
                Peer peer = fabClient.getInstance().newPeer(entry.getKey(), entry.getValue());
                mychannel.joinPeer(peer);
            }

            printChannelPeer(mychannel);
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Peer> getPeers(FabricClient fabricClient, Map<String, String> peerLocations) throws InvalidArgumentException {
        List<Peer> peers = new ArrayList<>();
        for(String peerName:peerLocations.keySet()){
            peers.add(fabricClient.getInstance().newPeer(peerName, peerLocations.get(peerName)));
        }

        return peers;

    }


    private void printChannelPeer(Channel mychannel){
        if(mychannel != null){
            Collection peers = mychannel.getPeers();
            Iterator peerIter = peers.iterator();
            while (peerIter.hasNext()) {
                Peer pr = (Peer) peerIter.next();
                log.info("--------------"+pr.getName()+ " at " + pr.getUrl());
            }
        }else {
            log.error("no peer on channel "+mychannel);
        }
    }
}
