package com.my.chen.fabric.app.controller;

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
    public String queryBlockByTransactionID(@RequestBody Trace trace) {
        return traceService.queryBlockByTransactionID(trace);
    }

    @PostMapping(value = "hash")
    public String queryBlockByHash(@RequestBody Trace trace) {
        return traceService.queryBlockByHash(trace);
    }

    @PostMapping(value = "number")
    public String queryBlockByNumber(@RequestBody Trace trace) {
        return traceService.queryBlockByNumber(trace);
    }

    @GetMapping(value = "info/{id}")
    public String queryBlockChainInfo(@PathVariable("id") int id) {
        return traceService.queryBlockChainInfo(id);
    }

}
