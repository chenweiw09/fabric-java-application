package com.my.chen.fabric.sdk;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.my.chen.fabric.app.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/1
 * @description channel通道实例
 */

public class FbChannel {

    private static final Logger log = LoggerFactory.getLogger(FbChannel.class);

    private String channelName;

    private Channel channel;

    private FbOrg org;


    public void init(FbOrg org) throws TransactionException, InvalidArgumentException {
        this.org = org;
        setChannel(org.getClient());
    }


    public Channel getChannel() {
        return channel;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    private void setChannel(FbClient fbClient) throws InvalidArgumentException, TransactionException {

        channel = fbClient.getClient().newChannel(channelName);
        log.info("get channel from client:" + channelName);

        // get order from org
        for (FbOrderer orderer : org.getOrderers()) {
            File ordererCert = Paths.get(org.getCryptoConfigPath(), "/ordererOrganizations", org.getOrdererDomainName(), "orderers", orderer.getOrdererName(),
                    "tls/server.crt").toFile();
            if (!ordererCert.exists()) {
                throw new RuntimeException(
                        String.format("Missing cert file for: %s. Could not find at location: %s", orderer.getOrdererName(), ordererCert.getAbsolutePath()));
            }
            Properties ordererProperties = new Properties();
            ordererProperties.setProperty("pemFile", ordererCert.getAbsolutePath());
            ordererProperties.setProperty("hostnameOverride", orderer.getOrdererName());
            ordererProperties.setProperty("sslProvider", "openSSL");
            ordererProperties.setProperty("negotiationType", "TLS");
            ordererProperties.put("grpc.ManagedChannelBuilderOption.maxInboundMessageSize", 9000000);
            // 设置keepAlive以避免在不活跃的http2连接上超时的例子。在5分钟内，需要对服务器端进行更改，以接受更快的ping速率。
            ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
            ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit.SECONDS});
            ordererProperties.setProperty("ordererWaitTimeMilliSecs", "300000");
            channel.addOrderer(fbClient.getClient().newOrderer(orderer.getOrdererName(), orderer.getOrdererLocation(), ordererProperties));
        }

        // get peer from org
        for (FbPeer peer : org.getPeers()) {
            File peerCert = Paths.get(org.getCryptoConfigPath(), "/peerOrganizations", org.getOrgDomainName(), "peers", peer.getPeerName(), "tls/server.crt")
                    .toFile();
            if (!peerCert.exists()) {
                throw new RuntimeException(String.format("Missing cert file for: %s. Could not find at location: %s", peer.getPeerName(), peerCert.getAbsolutePath()));
            }
            Properties peerProperties = new Properties();
            peerProperties.setProperty("pemFile", peerCert.getAbsolutePath());
            // ret.setProperty("trustServerCertificate", "true"); //testing
            peerProperties.setProperty("hostnameOverride", peer.getPeerName());
            peerProperties.setProperty("sslProvider", "openSSL");
            peerProperties.setProperty("negotiationType", "TLS");
            // 在grpc的NettyChannelBuilder上设置特定选项
            peerProperties.put("grpc.ManagedChannelBuilderOption.maxInboundMessageSize", 9000000);

            // 如果未加入频道，该方法执行加入。如果已加入频道，则执行下一行方面新增Peer
            // channel.joinPeer(client.newPeer(peers.get().get(i).getPeerName(), fabricOrg.getPeerLocation(peers.get().get(i).getPeerName()), peerProperties));
            channel.addPeer(fbClient.getClient().newPeer(peer.getPeerName(), peer.getPeerLocation(), peerProperties));
            if (peer.isAddEventHub()) {
                channel.addEventHub(fbClient.getClient().newEventHub(peer.getPeerEventHubName(), peer.getPeerEventHubLocation(), peerProperties));
            }

            if (!channel.isInitialized()) {
                channel.initialize();
            }

            if (null != org.getBlockListener()) {
                channel.registerBlockListener(blockEvent -> {
                    try {
                        org.getBlockListener().received(execBlockInfo(blockEvent));
                    } catch (Exception e) {
                        e.printStackTrace();
                        org.getBlockListener().received(getFailFromString(e.getMessage()));
                    }
                });
            }
        }
    }

    Map<String, String> joinPeer(FbPeer peer) throws InvalidArgumentException, ProposalException {
        File peerCert = Paths.get(org.getCryptoConfigPath(), "/peerOrganizations", org.getOrgDomainName(), "peers", peer.getPeerName(), "tls/server.crt")
                .toFile();
        if (!peerCert.exists()) {
            throw new RuntimeException(String.format("Missing cert file for: %s. Could not find at location: %s", peer.getPeerName(), peerCert.getAbsolutePath()));
        }
        Properties peerProperties = new Properties();
        peerProperties.setProperty("pemFile", peerCert.getAbsolutePath());
        // ret.setProperty("trustServerCertificate", "true"); //testing
        // environment only NOT FOR PRODUCTION!
        peerProperties.setProperty("hostnameOverride", peer.getPeerName());
        peerProperties.setProperty("sslProvider", "openSSL");
        peerProperties.setProperty("negotiationType", "TLS");
        // 在grpc的NettyChannelBuilder上设置特定选项
        peerProperties.put("grpc.ManagedChannelBuilderOption.maxInboundMessageSize", 9000000);

        Peer fabricPeer = org.getClient().getClient().newPeer(peer.getPeerName(), peer.getPeerLocation(), peerProperties);
        for (Peer peerNow : channel.getPeers()) {
            if (peerNow.getUrl().equals(fabricPeer.getUrl())) {
                return getFailFromString("peer has already in channel");
            }
        }
        channel.joinPeer(fabricPeer);
        if (peer.isAddEventHub()) {
            channel.addEventHub(org.getClient().getClient().newEventHub(peer.getPeerEventHubName(), peer.getPeerEventHubLocation(), peerProperties));
        }
        return getSuccessFromString("peer join channel success");
    }


    /** 查询当前频道的链信息，包括链长度、当前最新区块hash以及当前最新区块的上一区块hash */
    Map<String, String> getBlockchainInfo() throws InvalidArgumentException, ProposalException {
        org.json.JSONObject blockchainInfo = new org.json.JSONObject();
        blockchainInfo.put("height", channel.queryBlockchainInfo().getHeight());
        blockchainInfo.put("currentBlockHash", Hex.encodeHexString(channel.queryBlockchainInfo().getCurrentBlockHash()));
        blockchainInfo.put("previousBlockHash", Hex.encodeHexString(channel.queryBlockchainInfo().getPreviousBlockHash()));
        return getSuccessFromString(blockchainInfo.toString());
    }

    Map<String, String> queryBlockByTransactionID(String txID) throws InvalidArgumentException, ProposalException, IOException {
        return execBlockInfo(channel.queryBlockByTransactionID(txID));
    }


    /**
     * 在指定频道内根据hash查询区块
     *
     * @param blockHash hash
     */
    Map<String, String> queryBlockByHash(byte[] blockHash) throws InvalidArgumentException, ProposalException, IOException {
        return execBlockInfo(channel.queryBlockByHash(blockHash));
    }

    /**
     * 在指定频道内根据区块高度查询区块
     *
     * @param blockNumber 区块高度
     */
    Map<String, String> queryBlockByNumber(long blockNumber) throws InvalidArgumentException, ProposalException, IOException {
        Map<String, String> map = execBlockInfo(channel.queryBlockByNumber(blockNumber));
        return map;
    }

    /**
     * 解析区块信息对象
     *
     * @param blockInfo 区块信息对象
     */
    private Map<String, String> execBlockInfo(BlockInfo blockInfo) throws IOException, InvalidArgumentException {
        final long blockNumber = blockInfo.getBlockNumber();
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockNumber", blockNumber);
        blockJson.put("dataHash", Hex.encodeHexString(blockInfo.getDataHash()));
        blockJson.put("previousHashID", Hex.encodeHexString(blockInfo.getPreviousHash()));
        blockJson.put("calculatedBlockHash", Hex.encodeHexString(SDKUtils.calculateBlockHash(org.getClient().getClient(), blockNumber, blockInfo.getPreviousHash(), blockInfo.getDataHash())));
        blockJson.put("envelopeCount", blockInfo.getEnvelopeCount());

        log.debug("blockNumber = " + blockNumber);
        log.debug("data hash: " + Hex.encodeHexString(blockInfo.getDataHash()));
        log.debug("previous hash id: " + Hex.encodeHexString(blockInfo.getPreviousHash()));
        log.debug("calculated block hash is " + Hex.encodeHexString(SDKUtils.calculateBlockHash(org.getClient().getClient(), blockNumber, blockInfo.getPreviousHash(), blockInfo.getDataHash())));
        log.debug("block number " + blockNumber + " has " + blockInfo.getEnvelopeCount() + " envelope count:");

        JSONArray envelopeJsonArray = new JSONArray();
        for (BlockInfo.EnvelopeInfo info : blockInfo.getEnvelopeInfos()) {
            JSONObject envelopeJson = new JSONObject();
            envelopeJson.put("channelId", info.getChannelId());
            envelopeJson.put("transactionID", info.getTransactionID());
            envelopeJson.put("validationCode", info.getValidationCode());
            envelopeJson.put("timestamp", DateUtil.getTimeStr(info.getTimestamp().getTime()));
            envelopeJson.put("type", info.getType());
            envelopeJson.put("createId", info.getCreator().getId());
            envelopeJson.put("createMSPID", info.getCreator().getMspid());
            envelopeJson.put("isValid", info.isValid());
            envelopeJson.put("nonce", Hex.encodeHexString(info.getNonce()));

            log.debug("channelId = " + info.getChannelId());
            log.debug("nonce = " + Hex.encodeHexString(info.getNonce()));
            log.debug("createId = " + info.getCreator().getId());
            log.debug("createMSPID = " + info.getCreator().getMspid());
            log.debug("isValid = " + info.isValid());
            log.debug("transactionID = " + info.getTransactionID());
            log.debug("validationCode = " + info.getValidationCode());
            log.debug("timestamp = " + DateUtil.getTimeStr(info.getTimestamp().getTime()));
            log.debug("type = " + info.getType());

            if (info.getType() == BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE) {
                BlockInfo.TransactionEnvelopeInfo txeInfo = (BlockInfo.TransactionEnvelopeInfo) info;
                JSONObject transactionEnvelopeInfoJson = new JSONObject();
                int txCount = txeInfo.getTransactionActionInfoCount();
                transactionEnvelopeInfoJson.put("txCount", txCount);
                transactionEnvelopeInfoJson.put("isValid", txeInfo.isValid());
                transactionEnvelopeInfoJson.put("validationCode", txeInfo.getValidationCode());

                log.debug("Transaction number " + blockNumber + " has actions count = " + txCount);
                log.debug("Transaction number " + blockNumber + " isValid = " + txeInfo.isValid());
                log.debug("Transaction number " + blockNumber + " validation code = " + txeInfo.getValidationCode());

                JSONArray transactionActionInfoJsonArray = new JSONArray();
                for (int i = 0; i < txCount; i++) {
                    BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo txInfo = txeInfo.getTransactionActionInfo(i);
                    int endorsementsCount = txInfo.getEndorsementsCount();
                    int chaincodeInputArgsCount = txInfo.getChaincodeInputArgsCount();
                    JSONObject transactionActionInfoJson = new JSONObject();
                    transactionActionInfoJson.put("responseStatus", txInfo.getResponseStatus());
                    transactionActionInfoJson.put("responseMessageString", printableString(new String(txInfo.getResponseMessageBytes(), "UTF-8")));
                    transactionActionInfoJson.put("endorsementsCount", endorsementsCount);
                    transactionActionInfoJson.put("chaincodeInputArgsCount", chaincodeInputArgsCount);
                    transactionActionInfoJson.put("status", txInfo.getProposalResponseStatus());
                    transactionActionInfoJson.put("payload", printableString(new String(txInfo.getProposalResponsePayload(), "UTF-8")));

                    log.debug("Transaction action " + i + " has response status " + txInfo.getResponseStatus());
                    log.debug("Transaction action " + i + " has response message bytes as string: " + printableString(new String(txInfo.getResponseMessageBytes(), "UTF-8")));
                    log.debug("Transaction action " + i + " has endorsements " + endorsementsCount);

                    JSONArray endorserInfoJsonArray = new JSONArray();
                    for (int n = 0; n < endorsementsCount; ++n) {
                        BlockInfo.EndorserInfo endorserInfo = txInfo.getEndorsementInfo(n);
                        String signature = Hex.encodeHexString(endorserInfo.getSignature());
                        String id = endorserInfo.getId();
                        String mspId = endorserInfo.getMspid();
                        JSONObject endorserInfoJson = new JSONObject();
                        endorserInfoJson.put("signature", signature);
                        endorserInfoJson.put("id", id);
                        endorserInfoJson.put("mspId", mspId);

                        log.debug("Endorser " + n + " signature: " + signature);
                        log.debug("Endorser " + n + " id: " + id);
                        log.debug("Endorser " + n + " mspId: " + mspId);
                        endorserInfoJsonArray.add(endorserInfoJson);
                    }
                    transactionActionInfoJson.put("endorserInfoArray", endorserInfoJsonArray);

                    log.debug("Transaction action " + i + " has " + chaincodeInputArgsCount + " chaincode input arguments");
                    JSONArray argJsonArray = new JSONArray();
                    for (int z = 0; z < chaincodeInputArgsCount; ++z) {
                        argJsonArray.add(printableString(new String(txInfo.getChaincodeInputArgs(z), "UTF-8")));
                        log.debug("Transaction action " + i + " has chaincode input argument " + z + "is: " + printableString(new String(txInfo.getChaincodeInputArgs(z), "UTF-8")));
                    }
                    transactionActionInfoJson.put("argArray", argJsonArray);

                    log.debug("Transaction action " + i + " proposal response status: " + txInfo.getProposalResponseStatus());
                    log.debug("Transaction action " + i + " proposal response payload: " + printableString(new String(txInfo.getProposalResponsePayload())));

                    TxReadWriteSetInfo rwsetInfo = txInfo.getTxReadWriteSet();
                    JSONObject rwsetInfoJson = new JSONObject();
                    if (null != rwsetInfo) {
                        int nsRWsetCount = rwsetInfo.getNsRwsetCount();
                        rwsetInfoJson.put("nsRWsetCount", nsRWsetCount);
                        log.debug("Transaction action " + i + " has " + nsRWsetCount + " name space read write sets");

                        JSONArray nsRwsetInfoJsonArray = new JSONArray();
                        for (TxReadWriteSetInfo.NsRwsetInfo nsRwsetInfo : rwsetInfo.getNsRwsetInfos()) {
                            final String namespace = nsRwsetInfo.getNamespace();
                            KvRwset.KVRWSet rws = nsRwsetInfo.getRwset();
                            JSONObject nsRwsetInfoJson = new JSONObject();

                            JSONArray readJsonArray = new JSONArray();
                            int rs = -1;
                            for (KvRwset.KVRead readList : rws.getReadsList()) {
                                rs++;
                                String key = readList.getKey();
                                long readVersionBlockNum = readList.getVersion().getBlockNum();
                                long readVersionTxNum = readList.getVersion().getTxNum();
                                JSONObject readInfoJson = new JSONObject();
                                readInfoJson.put("namespace", namespace);
                                readInfoJson.put("readSetIndex", rs);
                                readInfoJson.put("key", key);
                                readInfoJson.put("readVersionBlockNum", readVersionBlockNum);
                                readInfoJson.put("readVersionTxNum", readVersionTxNum);
                                readInfoJson.put("version", String.format("[%s : %s]", readVersionBlockNum, readVersionTxNum));
                                readJsonArray.add(readInfoJson);
                                log.debug("Namespace " + namespace + " read set " + rs + " key " + key + " version [" + readVersionBlockNum + " : " + readVersionTxNum + "]");
                            }
                            nsRwsetInfoJson.put("readSet", readJsonArray);

                            JSONArray writeJsonArray = new JSONArray();
                            rs = -1;
                            for (KvRwset.KVWrite writeList : rws.getWritesList()) {
                                rs++;
                                String key = writeList.getKey();
                                String valAsString = printableString(new String(writeList.getValue().toByteArray(), "UTF-8"));
                                JSONObject writeInfoJson = new JSONObject();
                                writeInfoJson.put("namespace", namespace);
                                writeInfoJson.put("writeSetIndex", rs);
                                writeInfoJson.put("key", key);
                                writeInfoJson.put("value", valAsString);
                                writeJsonArray.add(writeInfoJson);
                                log.debug("Namespace " + namespace + " write set " + rs + " key " + key + " has value " + valAsString);
                            }
                            nsRwsetInfoJson.put("writeSet", writeJsonArray);
                            nsRwsetInfoJsonArray.add(nsRwsetInfoJson);
                        }
                        rwsetInfoJson.put("nsRwsetInfoArray", nsRwsetInfoJsonArray);
                    }
                    transactionActionInfoJson.put("rwsetInfo", rwsetInfoJson);
                    transactionActionInfoJsonArray.add(transactionActionInfoJson);
                }
                transactionEnvelopeInfoJson.put("transactionActionInfoArray", transactionActionInfoJsonArray);
                envelopeJson.put("transactionEnvelopeInfo", transactionEnvelopeInfoJson);
            }
            envelopeJsonArray.add(envelopeJson);
        }
        blockJson.put("envelopes", envelopeJsonArray);
        return getSuccessFromString(blockJson.toJSONString());
    }


    private Map<String, String> getSuccessFromString(String data) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("code", "success");
        resultMap.put("data", data);
        return resultMap;
    }

    private Map<String, String> getFailFromString(String data) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("code", "error");
        resultMap.put("data", data);
        return resultMap;
    }
    private String printableString(final String string) {
        int maxLogStringLength = 64;
        if (string == null || string.length() == 0) {
            return string;
        }
        String ret = string.replaceAll("[^\\p{Print}]", "?");
        ret = ret.substring(0, Math.min(ret.length(), maxLogStringLength)) + (ret.length() > maxLogStringLength ? "..." : "");
        return ret;
    }

}
