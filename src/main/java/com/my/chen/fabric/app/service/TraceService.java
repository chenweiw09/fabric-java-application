package com.my.chen.fabric.app.service;

import com.alibaba.fastjson.JSONObject;
import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.dto.Trace;
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

    enum TraceIntent {
        TRANSACTION, HASH, NUMBER, INFO
    }

    public String queryBlockByTransactionID(Trace trace) {
        return trace(trace, orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper, TraceIntent.TRANSACTION);
    }


    public String queryBlockByHash(Trace trace) {
        return trace(trace, orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper, TraceIntent.HASH);
    }


    public String queryBlockByNumber(Trace trace) {
        return trace(trace, orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper, TraceIntent.NUMBER);
    }


    public String queryBlockChainInfo(int id) {
        Trace trace = new Trace();
        trace.setId(id);
        return trace(trace, orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper, TraceIntent.INFO);
    }



    private String trace(Trace trace, OrgMapper orgMapper, ChannelMapper channelMapper, ChaincodeMapper chaincodeMapper,
                         OrdererMapper ordererMapper, PeerMapper peerMapper, TraceIntent intent) {
        Map<String, String> resultMap = null;
        try {
//            FabricManager manager = FabricHelper.obtain().get(orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper,
//                    trace.getId());
//            switch (intent) {
//                case TRANSACTION:
//                    resultMap = manager.queryBlockByTransactionID(trace.getTrace());
//                    break;
//                case HASH:
//                    resultMap = manager.queryBlockByHash(Hex.decodeHex(trace.getTrace().toCharArray()));
//                    break;
//                case NUMBER:
//                    resultMap = manager.queryBlockByNumber(Long.valueOf(trace.getTrace()));
//                    break;
//                case INFO:
//                    resultMap = manager.getBlockchainInfo();
//                    break;
//            }
            return responseSuccess(JSONObject.parseObject(resultMap.get("data")));
        } catch (Exception e) {
            e.printStackTrace();
            return responseFail(String.format("Request failed： %s", e.getMessage()));
        }
    }
}
