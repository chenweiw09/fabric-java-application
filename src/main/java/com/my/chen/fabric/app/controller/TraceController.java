package com.my.chen.fabric.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.my.chen.fabric.app.dto.Trace;
import com.my.chen.fabric.app.service.TraceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 描述：
 *
 * @author : Aberic 【2018/6/4 15:01】
 */
@CrossOrigin
@RestController
@RequestMapping("trace")
public class TraceController {

    @Resource
    private TraceService traceService;

    @PostMapping(value = "txid")
    public JSONObject queryBlockByTransactionID(@RequestBody Trace trace) {
        return traceService.queryBlockByTransactionID(trace);
    }

    @PostMapping(value = "hash")
    public JSONObject queryBlockByHash(@RequestBody Trace trace) {
        return traceService.queryBlockByHash(trace);
    }

    @PostMapping(value = "number")
    public JSONObject queryBlockByNumber(@RequestBody Trace trace) {
        return traceService.queryBlockByNumber(trace);
    }

    @GetMapping(value = "info/{id}/{key}")
    public JSONObject queryBlockChainInfo(@PathVariable("id") int id, @PathVariable("key")String key) {
        return traceService.queryBlockChainInfo(id,key);
    }

}
