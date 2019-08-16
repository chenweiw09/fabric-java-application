package com.my.chen.fabric.sdk;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/1
 * @description
 */

@Slf4j
@Getter
public class FbChainCode {

    /**
     * 只能合约名字mycc
     */
    private String chaincodeName;

    /**
     * 只能合约文件路径
     */
    private String chaincodePath;

    /** 可能是包含智能合约的go环境路径 /opt/gopath*/
    private String chaincodeSource;

    /** 语言 */
    private TransactionRequest.Type chaincodeLanguage = TransactionRequest.Type.GO_LANG;

    private String chaincodeVersion;

    /** 指定ID的智能合约 */
    private ChaincodeID chaincodeID;

    /** 智能合约背书策略文件存放路径 policy.yaml*/
    private String chaincodePolicy;

    /** 单个提案请求的超时时间以毫秒为单位 */
    private int proposalWaitTime = 200000;
    /** 事务等待时间以秒为单位 */
    private int transactionWaitTime = 120;


    void setChaincodeName(String chaincodeName) {
        this.chaincodeName = chaincodeName;
        setChaincodeID();
    }

    void setChaincodeVersion(String chaincodeVersion) {
        this.chaincodeVersion = chaincodeVersion;
        setChaincodeID();
    }

    void setChaincodePath(String chaincodePath) {
        this.chaincodePath = chaincodePath;
        setChaincodeID();
    }

    public void setChaincodeSource(String chaincodeSource) {
        this.chaincodeSource = chaincodeSource;
    }

    void setChaincodePolicy(String chaincodePolicy) {
        this.chaincodePolicy = chaincodePolicy;
    }


    private void setChaincodeID(){
        if (null != chaincodeName && null != chaincodePath && null != chaincodeVersion) {
            this.chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(chaincodeVersion).setPath(chaincodePath).build();
        }
    }

    public void setChaincodeLanguage(TransactionRequest.Type chaincodeLanguage) {
        this.chaincodeLanguage = chaincodeLanguage;
    }

    public void setProposalWaitTime(int proposalWaitTime) {
        this.proposalWaitTime = proposalWaitTime;
    }

    public void setTransactionWaitTime(int transactionWaitTime) {
        this.transactionWaitTime = transactionWaitTime;
    }

    /**
     * 安装链码
     * @param org
     * @return
     * @throws InvalidArgumentException
     * @throws ProposalException
     */
    JSONObject install(FbOrg org) throws InvalidArgumentException, ProposalException {
        InstallProposalRequest installProposalRequest = org.getClient().getClient().newInstallProposalRequest();
        installProposalRequest.setChaincodeName(chaincodeName);
        installProposalRequest.setChaincodeVersion(chaincodeVersion);
        installProposalRequest.setChaincodeSourceLocation(new File(chaincodeSource));
        installProposalRequest.setChaincodePath(chaincodePath);
        installProposalRequest.setChaincodeLanguage(chaincodeLanguage);
        installProposalRequest.setProposalWaitTime(proposalWaitTime);

        long currentStart = System.currentTimeMillis();
        Collection<ProposalResponse> installProposalResponses = org.getClient().getClient().sendInstallProposal(installProposalRequest, org.getChannel().getChannel().getPeers());
        log.info("chaincode install transaction proposal time = " + (System.currentTimeMillis() - currentStart));
        return toPeerResponse(installProposalResponses, false);
    }


    /**
     * 实例化链码
     * @param org
     * @param args
     */
    JSONObject instantiate(FbOrg org, String[] args) throws ProposalException, InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException, InterruptedException, ExecutionException, TimeoutException {
        InstantiateProposalRequest instantiateProposalRequest = org.getClient().getClient().newInstantiationProposalRequest();
        instantiateProposalRequest.setChaincodeID(chaincodeID);
        instantiateProposalRequest.setProposalWaitTime(proposalWaitTime);
        instantiateProposalRequest.setArgs(args);

        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        chaincodeEndorsementPolicy.fromYamlFile(new File(chaincodePolicy));
        instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        tm2.put("result", ":)".getBytes(UTF_8));
        instantiateProposalRequest.setTransientMap(tm2);

        long currentStart = System.currentTimeMillis();
        Collection<ProposalResponse> instantiateProposalResponses = org.getChannel().getChannel().sendInstantiationProposal(instantiateProposalRequest, org.getChannel().getChannel().getPeers());
        log.info("chaincode instantiate transaction proposal time = " + (System.currentTimeMillis() - currentStart));
        return toOrdererResponse(instantiateProposalResponses, org);
    }


    /**
     * 升级智能合约
     *
     * @param org  中继组织对象
     * @param args 初始化参数数组
     */
    JSONObject upgrade(FbOrg org, String[] args) throws ProposalException, InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException, InterruptedException, ExecutionException, TimeoutException {
        UpgradeProposalRequest upgradeProposalRequest = org.getClient().getClient().newUpgradeProposalRequest();
        upgradeProposalRequest.setChaincodeID(chaincodeID);
        upgradeProposalRequest.setProposalWaitTime(proposalWaitTime);
        upgradeProposalRequest.setArgs(args);

        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        chaincodeEndorsementPolicy.fromYamlFile(new File(chaincodePolicy));
        upgradeProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "UpgradeProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "UpgradeProposalRequest".getBytes(UTF_8));
        tm2.put("result", ":)".getBytes(UTF_8));
        upgradeProposalRequest.setTransientMap(tm2);

        long currentStart = System.currentTimeMillis();
        Collection<ProposalResponse> upgradeProposalResponses = org.getChannel().getChannel().sendUpgradeProposal(upgradeProposalRequest, org.getChannel().getChannel().getPeers());
        log.info("chaincode instantiate transaction proposal time = " + (System.currentTimeMillis() - currentStart));
        return toOrdererResponse(upgradeProposalResponses, org);
    }

    /**
     * 执行智能合约
     *
     * @param org  中继组织对象
     * @param fcn  方法名
     * @param args 参数数组
     */
    JSONObject invoke(FbOrg org, String fcn, String[] args) throws InvalidArgumentException, ProposalException, IOException, InterruptedException, ExecutionException, TimeoutException {
        /// Send transaction proposal to all peers
        TransactionProposalRequest transactionProposalRequest = org.getClient().getClient().newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setFcn(fcn);
        transactionProposalRequest.setArgs(args);
        transactionProposalRequest.setProposalWaitTime(proposalWaitTime);

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        tm2.put("result", ":)".getBytes(UTF_8));
        transactionProposalRequest.setTransientMap(tm2);

        long currentStart = System.currentTimeMillis();
        Collection<ProposalResponse> transactionProposalResponses = org.getChannel().getChannel().sendTransactionProposal(transactionProposalRequest, org.getChannel().getChannel().getPeers());
        log.info("chaincode invoke transaction proposal time = " + (System.currentTimeMillis() - currentStart));
        return toOrdererResponse(transactionProposalResponses, org);
    }

    /**
     * 查询智能合约
     *
     * @param org  中继组织对象
     * @param fcn  方法名
     * @param args 参数数组
     */
    JSONObject query(FbOrg org, String fcn, String[] args) throws InvalidArgumentException, ProposalException {
        QueryByChaincodeRequest queryByChaincodeRequest = org.getClient().getClient().newQueryProposalRequest();
        queryByChaincodeRequest.setArgs(args);
        queryByChaincodeRequest.setFcn(fcn);
        queryByChaincodeRequest.setChaincodeID(chaincodeID);
        queryByChaincodeRequest.setProposalWaitTime(proposalWaitTime);

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        queryByChaincodeRequest.setTransientMap(tm2);

        long currentStart = System.currentTimeMillis();
        Collection<ProposalResponse> queryProposalResponses = org.getChannel().getChannel().queryByChaincode(queryByChaincodeRequest, org.getChannel().getChannel().getPeers());
        log.info("chaincode query transaction proposal time = " + (System.currentTimeMillis() - currentStart));
        return toPeerResponse(queryProposalResponses, true);
    }


    private JSONObject toOrdererResponse(Collection<ProposalResponse> proposalResponses, FbOrg org) throws InvalidArgumentException, UnsupportedEncodingException, InterruptedException, ExecutionException, TimeoutException {

        JSONObject result = new JSONObject();

        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        for (ProposalResponse response : proposalResponses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(proposalResponses);
        if (proposalConsistencySets.size() != 1) {
            log.error("Expected only one set of consistent proposal responses but got " + proposalConsistencySets.size());
        }

        if (failed.size() > 0) {
            ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
            log.error("Not enough endorsers for inspect:" + failed.size() + " endorser error: " + firstTransactionProposalResponse.getMessage() + ". Was verified: "
                    + firstTransactionProposalResponse.isVerified());
            result.put("code", "error");
            result.put("data", firstTransactionProposalResponse.getMessage());
            return result;
        } else {
            ProposalResponse resp = proposalResponses.iterator().next();
            log.debug("TransactionID: " + resp.getTransactionID());
            byte[] x = resp.getChaincodeActionResponsePayload();
            String resultAsString = null;
            if (x != null) {
                resultAsString = new String(x, "UTF-8");
            }
            log.info("resultAsString = " + resultAsString);

            // 这里修改为通过一部监听事件而不是等待
//            org.getChannel().getChannel().sendTransaction(successful).get(transactionWaitTime, TimeUnit.SECONDS);
            org.getChannel().getChannel().sendTransaction(successful);

            result = parseResult(resultAsString);
            result.put("code", BlockListener.SUCCESS);
            result.put("txid", resp.getTransactionID());

            return result;
        }
    }

    /**
     * 获取安装合约以及query合约的返回结果集合
     */
    private JSONObject toPeerResponse(Collection<ProposalResponse> proposalResponses, boolean checkVerified) {
        JSONObject resultMap = new JSONObject();
        for (ProposalResponse proposalResponse : proposalResponses) {
            if ((checkVerified && !proposalResponse.isVerified()) || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
                String data = String.format("Failed install/query proposal from peer %s status: %s. Messages: %s. Was verified : %s",
                        proposalResponse.getPeer().getName(), proposalResponse.getStatus(), proposalResponse.getMessage(), proposalResponse.isVerified());
                log.debug(data);
                resultMap.put("code", BlockListener.ERROR);
                resultMap.put("data", data);
            } else {
                String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                log.debug("Install/Query payload from peer: " + proposalResponse.getPeer().getName());
                log.debug("TransactionID: " + proposalResponse.getTransactionID());
                log.debug("" + payload);

                resultMap = parseResult(payload);

                resultMap.put("code", BlockListener.SUCCESS);
                resultMap.put("txid", proposalResponse.getTransactionID());
            }
        }
        return resultMap;
    }

    private JSONObject parseResult(String result) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("data", JSONObject.parseObject(result));
        }catch (JSONException ex){
            try {
                jsonObject.put("data", JSONObject.parseArray(result));
            }catch (JSONException ex1){
                jsonObject.put("data", result);
            }
        }
        return jsonObject;
    }

}
