package com.my.chen.fabric.app.service;

import com.my.chen.fabric.app.client.ChannelClient;
import com.my.chen.fabric.app.client.FabricClient;
import com.my.chen.fabric.app.config.Config;
import com.my.chen.fabric.app.user.SimpleOrg;
import com.my.chen.fabric.app.user.UserContext;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/7/18
 * @description
 */
public class MainClassTest {

    public static void main(String[] args) {

        // first init org
        SimpleOrgService simpleOrgService = new SimpleOrgService();
        ChannelService channelService = new ChannelService();

        ChainCodeService chainCodeService = new ChainCodeService(channelService);

        try {
            List<SimpleOrg> list = simpleOrgService.initOrg();

            // construct network
            channelService.createChannel(list, Config.CHANNEL_CONFIG_PATH);

            // get channel Client
            ChannelClient channelClient = channelService.getChannelClient(list.get(0));
            System.out.println(channelClient);

            // find peers on channel
            Collection peers = channelClient.getChannel().getPeers();


            // deploy chaincode

            // query chaincode
            List<Query.ChaincodeInfo> chaincodeInfos = chainCodeService.queryInstantiatedChainCode(channelClient);
            System.out.println(chaincodeInfos);


            String str = chainCodeService.queryChainCode(channelClient, "mycc1", "getWallet", new String[]{"a"});
            System.out.println(str);

            str = chainCodeService.invokeChainCode(channelClient, "mycc1", "createWallet", new String[]{"a", "100"});

            str = chainCodeService.invokeChainCode(channelClient, "mycc1", "getWallet", new String[]{"a"});

            channelClient.getChannel().shutdown(false);

            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
