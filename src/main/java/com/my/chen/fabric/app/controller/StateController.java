package com.my.chen.fabric.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.my.chen.fabric.app.dto.State;
import com.my.chen.fabric.app.service.StateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 描述：
 */
@CrossOrigin
@RestController
@RequestMapping("state")
public class StateController {

    @Resource
    private StateService stateService;

    @PostMapping(value = "invoke")
    public JSONObject invoke(@RequestBody State state) {
        return stateService.invoke(state);
    }

    @PostMapping(value = "query")
    public JSONObject query(@RequestBody State state) {
        return stateService.query(state);
    }

}
