package com.my.chen.fabric.app.service;

import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.dto.State;
import com.my.chen.fabric.app.util.FabricHelper;
import com.my.chen.fabric.sdk.FbNetworkManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述：
 *
 * @author : Aberic 【2018/6/4 15:03】
 */
@Service("stateService")
public class StateService implements BaseService {

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

    enum ChainCodeIntent {
        INVOKE, QUERY
    }

    public String invoke(State state) {
        return chainCode(state, orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper, ChainCodeIntent.INVOKE);
    }


    public String query(State state) {
        return chainCode(state, orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper, ChainCodeIntent.QUERY);
    }


    private String chainCode(State state, OrgMapper orgMapper, ChannelMapper channelMapper, ChaincodeMapper chainCodeMapper,
                             OrdererMapper ordererMapper, PeerMapper peerMapper, ChainCodeIntent intent) {
        List<String> array = state.getStrArray();
        int length = array.size();
        String fcn = null;
        String[] argArray = new String[length - 1];
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                fcn = array.get(i).trim();
            } else {
                argArray[i - 1] = array.get(i).trim();
            }
        }
        Map<String, String> resultMap = new HashMap<>();
        try {
            FbNetworkManager manager = FabricHelper.getInstance().get(orgMapper, channelMapper, chainCodeMapper, ordererMapper, peerMapper,
                    state.getId());
            switch (intent) {
                case INVOKE:
                    resultMap = manager.invoke(fcn, argArray);
                    break;
                case QUERY:
                    resultMap = manager.query(fcn, argArray);
                    break;
            }
            if (resultMap.get("code").equals("error")) {
                return responseFail(resultMap.get("data"));
            } else {
                return responseSuccess(resultMap.get("data"), resultMap.get("txid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseFail(String.format("Request failed： %s", e.getMessage()));
        }
    }

}
