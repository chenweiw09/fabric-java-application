package com.my.chen.fabric.app.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.my.chen.fabric.app.domain.Chaincode;
import com.my.chen.fabric.app.domain.Channel;
import com.my.chen.fabric.app.dto.Trace;
import com.my.chen.fabric.app.dto.Transaction;
import com.my.chen.fabric.app.service.*;
import com.my.chen.fabric.app.util.DateUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 *
 * @author : Aberic 【2018/6/4 15:01】
 */
@RestController
@RequestMapping("")
public class CommonController {

    @Resource
    private LeagueService leagueService;
    @Resource
    private OrgService orgService;
    @Resource
    private OrdererService ordererService;
    @Resource
    private PeerService peerService;
    @Resource
    private ChannelService channelService;
    @Resource
    private ChaincodeService chaincodeService;
    @Resource
    private TraceService traceService;

    @Resource
    private CAService caService;

    private static final String SUCCESS_CODE = "200";

    @GetMapping(value = "index")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("index");
        getCount(modelAndView);


        List<Transaction> tmpTransactions = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();

        List<Channel> channels = channelService.listAll();
        for (Channel channel : channels) {
            List<Chaincode> chaincodes = chaincodeService.listById(channel.getId());
            for (Chaincode chaincode: chaincodes) {
                try {
                    JSONObject blockInfo = traceService.queryBlockChainInfo(chaincode.getId(),chaincode.getFlag());
                    int height = blockInfo.getJSONObject("data").getInteger("height");
                    for (int num = height - 1; num >= 0; num--) {
                        Trace trace = new Trace();
                        trace.setId(chaincode.getId());
                        trace.setTrace(String.valueOf(num));
                        JSONObject blockMessage = traceService.queryBlockByNumber(trace);
                        getTmpTransactions(blockMessage, tmpTransactions);

                        if ((height - num) > 6) {
                            break;
                        }
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        tmpTransactions.sort((t1, t2) -> {
            try {
                long td1 = DateUtil.str2Date(t1.getDate(), "yyyy/MM/dd HH:mm:ss").getTime();
                long td2 = DateUtil.str2Date(t2.getDate(), "yyyy/MM/dd HH:mm:ss").getTime();
                return Math.toIntExact(td2 - td1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        });
        int size = tmpTransactions.size() > 7 ? 7 : tmpTransactions.size();
        for (int i = 0; i < size; i++) {
            Transaction transaction = tmpTransactions.get(i);
            transaction.setIndex(i + 1);
            transactions.add(transaction);
        }

        modelAndView.addObject("transactions", transactions);

        return modelAndView;
    }



    private void getCount(ModelAndView modelAndView){
        int leagueCount = leagueService.listAll().size();
        int orgCount =orgService.count();
        int ordererCount = ordererService.count();
        int peerCount = peerService.count();
        int channelCount = channelService.count();
        int chaincodeCount = chaincodeService.count();
        modelAndView.addObject("leagueCount", leagueCount);
        modelAndView.addObject("orgCount", orgCount);
        modelAndView.addObject("ordererCount", ordererCount);
        modelAndView.addObject("peerCount", peerCount);
        modelAndView.addObject("channelCount", channelCount);
        modelAndView.addObject("chaincodeCount", chaincodeCount);
        modelAndView.addObject("caCount", caService.count());
    }


    private void getTmpTransactions(JSONObject blockMessage, List<Transaction> tmpTransactions){

        if(SUCCESS_CODE.equals(blockMessage.getString("code"))){
            JSONObject data = blockMessage.getJSONObject("data");
            int blockNum = Integer.valueOf(data.getString("blockNumber"));
            JSONArray envelopes = data.getJSONArray("envelopes");

            int size = envelopes.size();

            for (int i = 0; i < size; i++) {
                Transaction transaction = new Transaction();
                transaction.setNum(blockNum);
                JSONObject envelope = envelopes.getJSONObject(i);
                transaction.setTxCount(envelope.getJSONObject("transactionEnvelopeInfo").getInteger("txCount"));
                transaction.setChannelName(envelope.getString("channelId"));
                transaction.setCreateMSPID(envelope.getString("createMSPID"));
                transaction.setDate(envelope.getString("timestamp"));
                tmpTransactions.add(transaction);
            }
        }
    }
}
