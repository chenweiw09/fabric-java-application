package com.my.chen.fabric.app.service;

import com.my.chen.fabric.app.client.ChannelClient;
import com.my.chen.fabric.app.client.FabricClient;
import com.my.chen.fabric.app.dto.ChainCodeDeployDto;
import com.my.chen.fabric.app.user.SimpleOrg;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/7/18
 * @description
 */
@Slf4j
public class ChainCodeService {

    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";

    private ChannelService channelService;


    public ChainCodeService(ChannelService channelService){
        this.channelService = channelService;
    }


    public boolean deployAndInstantiateChainCode(List<SimpleOrg> orgs, ChainCodeDeployDto deployDto){
        try {
            ChannelClient channelClient = channelService.getChannelClient(orgs.get(0));

            FabricClient fabricClient = channelClient.getFabClient();
            List<Peer> peers = channelService.getPeers(fabricClient, orgs.get(0).getPeerLocations());

            // org deploy chainCode
            Collection<ProposalResponse> response = fabricClient.deployChainCode(deployDto.getChainCodeName(),
                    deployDto.getChainCodePath(), deployDto.getCodeSourcePath(), deployDto.getLanguage().toString(),
                    deployDto.getVersion(), peers);

            for (ProposalResponse res : response) {
                log.info(deployDto.getChainCodeName() + "- Chain code deployment " + res.getStatus());
                if(res.getStatus() != ProposalResponse.Status.SUCCESS){
                    throw new RuntimeException("chain code deploy failed");
                }
            }


            if(orgs.size() >1){
                for(int i=1; i< orgs.size(); i++){
                    peers = channelService.getPeers(fabricClient, orgs.get(i).getPeerLocations());
                    fabricClient.getInstance().setUserContext(orgs.get(i).getAdmin());

                    response = fabricClient.deployChainCode(deployDto.getChainCodeName(),
                            deployDto.getChainCodePath(), deployDto.getCodeSourcePath(), deployDto.getLanguage().toString(),
                            deployDto.getVersion(), peers);

                    for (ProposalResponse res : response) {
                        log.info(deployDto.getChainCodeName() + "- Chain code deployment on peer "+res.getPeer().getName()+"|return status:" + res.getStatus());
                        if(res.getStatus() != ProposalResponse.Status.SUCCESS){
                            throw new RuntimeException("chain code deploy failed");
                        }
                    }
                }
            }

            // instantiate chaincode
            response = channelClient.instantiateChainCode(deployDto.getChainCodeName(),
                    deployDto.getChainCodePath(), deployDto.getCodeSourcePath(), deployDto.getLanguage().toString(),
                    deployDto.getInitFunctionName(), deployDto.getArguments(), null);
            for (ProposalResponse res : response) {
                log.info(res.getTransactionID() + "- Chain code instantiation and return status" + res.getStatus());
                if(res.getStatus() != ProposalResponse.Status.SUCCESS){
                    throw new RuntimeException("chain code instantiate failed");
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public List<Query.ChaincodeInfo> queryInstantiatedChainCode(ChannelClient channelClient) throws ProposalException, InvalidArgumentException {
        Collection<Peer> peers = channelClient.getChannel().getPeers();
        Peer peer = peers.iterator().next();
        List<Query.ChaincodeInfo> ccdl = channelClient.getChannel().queryInstantiatedChaincodes(peer);
        return ccdl;
    }

    public String queryChainCode(ChannelClient channelClient, String chainCodeName, String functionName, String[] args) throws ProposalException, InvalidArgumentException {
        Collection<ProposalResponse>  responsesQuery = channelClient.queryByChainCode(chainCodeName, functionName, args);
        for (ProposalResponse pres : responsesQuery) {
//            String stringResponse = new String(pres.getChaincodeActionResponsePayload());
            log.info("queryChainCode with client: "+channelClient.getName()+"|chainCode:"+chainCodeName+"|functionName:{}|return Status:{} ", functionName , pres.getStatus());
//            return stringResponse;
        }

        return null;
    }

    public String invokeChainCode(ChannelClient channelClient, String chainCodeName, String functionName, String[] arguments) throws InvalidArgumentException, ProposalException {
        FabricClient fabricClient = channelClient.getFabClient();

        TransactionProposalRequest request = fabricClient.getInstance().newTransactionProposalRequest();

        ChaincodeID ccid = ChaincodeID.newBuilder().setName(chainCodeName).build();
        request.setChaincodeID(ccid);
        request.setFcn(functionName);
        request.setArgs(arguments);
        request.setProposalWaitTime(1000);

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        tm2.put("result", ":)".getBytes(UTF_8));
        tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);
        request.setTransientMap(tm2);
        Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(request);
        for (ProposalResponse res: responses) {
            ChaincodeResponse.Status status = res.getStatus();
            log.info("invokeChainCode with client: "+channelClient.getName()+"|chainCode:"+chainCodeName+"|functionName:{}|return Status:{} ", functionName , status);
            String stringResponse = new String(res.getChaincodeActionResponsePayload());

            return stringResponse;
        }

        return null;
    }


}
