package com.my.chen.fabric.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.my.chen.fabric.app.client.ChannelClient;
import com.my.chen.fabric.app.service.ChainCodeService;
import com.my.chen.fabric.app.service.ChannelService;
import com.my.chen.fabric.app.service.SimpleOrgService;
import com.my.chen.fabric.app.user.SimpleOrg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/7/18
 * @description
 */

@RestController
@Slf4j
@RequestMapping("mywallet")
public class MyWalletController {


    @GetMapping("/create/{walletId}")
    @ResponseBody
    public JSONObject createWallet(@PathVariable("walletId")String walletId){
        log.info("createWallet|walletId:{}",walletId);

        SimpleOrgService simpleOrgService = new SimpleOrgService();

        ChannelService channelService = new ChannelService();

        ChainCodeService chainCodeService = new ChainCodeService(channelService);


        try {
            List<SimpleOrg> list = simpleOrgService.initOrg();

            ChannelClient channelClient = channelService.getChannelClient(list.get(0));

            String str = chainCodeService.invokeChainCode(channelClient, "mycc1", "createWallet",new String[]{"a","100"});

            if(StringUtils.isEmpty(str)){
                JSONObject object = new JSONObject();
                object.put("code",200);
                object.put("desc","OK");
                return object;
            }
        } catch (Exception e) {
            log.error("error",e);
        }

        JSONObject object = new JSONObject();
        object.put("code",501);
        object.put("desc","internal error");
        return object;
    }


}
