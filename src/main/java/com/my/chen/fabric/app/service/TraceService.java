package com.my.chen.fabric.app.service;

import com.alibaba.fastjson.JSONObject;
import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.domain.CA;
import com.my.chen.fabric.app.dto.Trace;
import com.my.chen.fabric.app.util.FabricHelper;
import com.my.chen.fabric.sdk.FbNetworkManager;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service("traceService")
public class TraceService implements BaseService {

    @Resource
    private OrgMapper orgMapper;
    @Resource
    private OrdererMapper ordererMapper;
    @Resource
    private PeerMapper peerMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ChaincodeMapper chaincodeMapper;

    @Resource
    private CAMapper caMapper;

    enum TraceIntent {
        TRANSACTION, HASH, NUMBER, INFO
    }

    public JSONObject queryBlockByTransactionID(Trace trace) {
        return trace(trace, TraceIntent.TRANSACTION, caMapper);
    }


    public JSONObject queryBlockByHash(Trace trace) {
        return trace(trace, TraceIntent.HASH, caMapper);
    }


    public JSONObject queryBlockByNumber(Trace trace) {
        return trace(trace, TraceIntent.NUMBER, caMapper);
    }


    public JSONObject queryBlockChainInfo(int id, String key) {
        Trace trace = new Trace();
        trace.setId(id);
        trace.setKey(key);
        return trace(trace, TraceIntent.INFO, caMapper);
    }


    private JSONObject trace(Trace trace, TraceIntent intent, CAMapper caMapper) {

        JSONObject result = new JSONObject();

        CA ca = caMapper.findByFlag(trace.getFlag());

        try {
            FbNetworkManager manager = FabricHelper.getInstance().get(orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper, ca,
                    trace.getId());
            switch (intent) {
                case TRANSACTION:
                    result = manager.queryBlockByTransactionID(trace.getTrace());
                    break;
                case HASH:
                    result = manager.queryBlockByHash(Hex.decodeHex(trace.getTrace().toCharArray()));
                    break;
                case NUMBER:
                    result = manager.queryBlockByNumber(Long.valueOf(trace.getTrace()));
                    break;
                case INFO:
                    result = manager.getBlockchainInfo();
                    break;
            }

        } catch (Exception e) {
            result = responseFail(String.format("Request failed： %s", e.getMessage()));
        }
        return result;
    }
}
