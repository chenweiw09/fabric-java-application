package com.my.chen.fabric.app.service;

import com.alibaba.fastjson.JSONObject;
import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.domain.CA;
import com.my.chen.fabric.app.dto.State;
import com.my.chen.fabric.app.util.FabricHelper;
import com.my.chen.fabric.sdk.FbNetworkManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.List;

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

    @Resource
    private CAMapper caMapper;

    @Resource
    private LeagueMapper leagueMapper;

    enum ChainCodeIntent {
        INVOKE, QUERY
    }

    public JSONObject invoke(State state) {
        return chainCode(state, ChainCodeIntent.INVOKE, caMapper);
    }


    public JSONObject query(State state) {
        return chainCode(state, ChainCodeIntent.QUERY, caMapper);
    }


    private JSONObject chainCode(State state, ChainCodeIntent intent, CAMapper caMapper) {
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

        CA ca = caMapper.findByFlag(state.getFlag());

        JSONObject result = new JSONObject();
        try {
            FbNetworkManager manager = FabricHelper.getInstance().get(leagueMapper, orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper,
                    ca, state.getId());
            switch (intent) {
                case INVOKE:
                    result = manager.invoke(fcn, argArray);
                    break;
                case QUERY:
                    result= manager.query(fcn, argArray);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = responseFail(String.format("Request failed： %s", e.getMessage()));
        }

        return result;
    }

}
