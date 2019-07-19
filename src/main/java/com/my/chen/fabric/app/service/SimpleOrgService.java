package com.my.chen.fabric.app.service;

import com.my.chen.fabric.app.client.CAClient;
import com.my.chen.fabric.app.config.Config;
import com.my.chen.fabric.app.user.RegisterEnrollUser;
import com.my.chen.fabric.app.user.SimpleOrg;
import com.my.chen.fabric.app.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/7/17
 * @description
 */
public class SimpleOrgService {

    private static final String SUB_CONFIG_PATH = Config.BASIC_PATH+ "crypto-config"+ File.separator;

    private static final String PEER_ORG = "peerOrganizations";

    private static final String ORDERER_ORG="ordererOrganizations";


    public List<SimpleOrg> initOrg() throws Exception {
        Util.cleanUp();

        List<SimpleOrg> list = new ArrayList<>();
        list.add(initOrg1());
        list.add(initOrg2());

        return list;
    }


    private SimpleOrg initOrg2()throws Exception{
        SimpleOrg org2 = new SimpleOrg(Config.ORG2, Config.ORG2_MSP);

        Map<String, String> peerLocations = new HashMap<>();
        peerLocations.put(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
        peerLocations.put(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);

        Map<String, String> ordererLocations = new HashMap<>();
        ordererLocations.put(Config.ORDERER_NAME, Config.ORDERER_URL);

        org2.setDomainName("org2.example.com");
        org2.setPeerLocations(peerLocations);
        org2.setOrdererLocations(ordererLocations);
        org2.setCaLocation(Config.CA_ORG2_URL);
        CAClient caClient = new CAClient(org2.getCaLocation(), null);
        org2.setCaClient(caClient);

        // add event hub
        Map<String, String> eventHubs = new HashMap<>();
        eventHubs.put("eventhub02", Config.ORG2_EVENTHUB_URL);
        org2.setEventHubLocations(eventHubs);

        org2.setAdmin(RegisterEnrollUser.getOrg2Admin());
        org2.setChannelName(Config.CHANNEL_NAME);
        return org2;

    }


    private SimpleOrg initOrg1() throws Exception{
        SimpleOrg org1 = new SimpleOrg(Config.ORG1, Config.ORG1_MSP);

        Map<String, String> peerLocations = new HashMap<>();
        peerLocations.put(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
        peerLocations.put(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);

        Map<String, String> ordererLocations = new HashMap<>();
        ordererLocations.put(Config.ORDERER_NAME, Config.ORDERER_URL);

        org1.setDomainName("org1.example.com");
        org1.setPeerLocations(peerLocations);
        org1.setOrdererLocations(ordererLocations);
        org1.setCaLocation(Config.CA_ORG1_URL);
        CAClient caClient = new CAClient(org1.getCaLocation(), null);
        org1.setCaClient(caClient);

        // add event hub
        Map<String, String> eventHubs = new HashMap<>();
        eventHubs.put("eventhub01", Config.ORG1_EVENTHUB_URL);
        org1.setEventHubLocations(eventHubs);

        org1.setAdmin(RegisterEnrollUser.getOrg1Admin());
        org1.setChannelName(Config.CHANNEL_NAME);
        return org1;
    }

    public static void main(String[] args) {

        File file = new File(SUB_CONFIG_PATH+"peerOrganizations");
        String [] files = file.list();

        System.out.println(false);
    }

}
