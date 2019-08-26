package com.my.chen.fabric.sdk;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/1
 * @description network manager  因为现在是一个web端的chaincodeId，有一个对应的Manager，所以list里面只有一个chaincode
 */
@Getter
public class FbNetworkManager {

    private FbOrg org;

    public FbNetworkManager(FbOrg org) {
        this.org = org;
    }

    private FbChainCode getChainCode(){
        List<FbChainCode> list = org.getChainCodes();

        if(CollectionUtils.isEmpty(list)){
            throw new RuntimeException("org chain code not init");
        }

        return list.get(0);

//        for(FbChainCode code :list){
//            if(chaincodeName.equals(code.getChaincodeName())){
//                return code;
//            }
//        }
//
//        throw new RuntimeException("error chain code name");
    }

    /** 安装智能合约 */
    public JSONObject install() throws ProposalException, InvalidArgumentException {
        return getChainCode().install(org);
    }

    /**
     * 实例化智能合约
     *
     * @param args 初始化参数数组
     */
    public JSONObject instantiate(String[] args) throws ProposalException, InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException, InterruptedException, ExecutionException, TimeoutException {
        return getChainCode().instantiate(org, args);
    }

    /**
     * 升级智能合约
     *
     * @param args 初始化参数数组
     */
    public JSONObject upgrade(String[] args) throws ProposalException, InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException, InterruptedException, ExecutionException, TimeoutException {
        return getChainCode().upgrade(org, args);
    }

    /**
     * 执行智能合约
     *
     * @param fcn  方法名
     * @param args 参数数组
     */
    public JSONObject invoke(String fcn, String[] args) throws InvalidArgumentException, ProposalException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return getChainCode().invoke(org, fcn, args);
    }

    /**
     * 查询智能合约
     *
     * @param fcn  方法名
     * @param args 参数数组
     */
    public JSONObject query(String fcn, String[] args) throws InvalidArgumentException, ProposalException {
        return getChainCode().query(org, fcn, args);
    }

    /**
     * 在指定频道内根据transactionID查询区块
     *
     * @param txID transactionID
     */
    public JSONObject queryBlockByTransactionID(String txID) throws ProposalException, IOException, InvalidArgumentException {
        return org.getChannel().queryBlockByTransactionID(txID);
    }

    /**
     * 在指定频道内根据hash查询区块
     *
     * @param blockHash hash
     */
    public JSONObject queryBlockByHash(byte[] blockHash) throws ProposalException, IOException, InvalidArgumentException {
        return org.getChannel().queryBlockByHash(blockHash);
    }

    /**
     * 在指定频道内根据区块高度查询区块
     *
     * @param blockNumber 区块高度
     */
    public JSONObject queryBlockByNumber(long blockNumber) throws ProposalException, IOException, InvalidArgumentException {
        return org.getChannel().queryBlockByNumber(blockNumber);
    }

    public JSONObject joinPeer(String peerName, String peerLocation, String peerEventHubLocation, String serverCrtPath, String clientCertPath, String clientKeyPath) throws ProposalException, InvalidArgumentException {
        return org.getChannel().joinPeer(new FbPeer(peerName, peerLocation, peerEventHubLocation, serverCrtPath, clientCertPath,clientKeyPath));
    }

    /** 查询当前频道的链信息，包括链长度、当前最新区块hash以及当前最新区块的上一区块hash */
    public JSONObject getBlockchainInfo() throws ProposalException, InvalidArgumentException {
        return org.getChannel().getBlockchainInfo();
    }

    /**
     * 获取channel上的peer节点信息
     */
    public List<FbPeer> getChannelPeers(){
        Collection<Peer> peers = org.getChannel().getChannel().getPeers();
        return peers.stream().map(t -> new FbPeer(t.getName(), t.getUrl(),  null)).collect(Collectors.toList());
    }


}
