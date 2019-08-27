package com.my.chen.fabric.app.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.my.chen.fabric.app.domain.Block;
import com.my.chen.fabric.app.domain.CA;
import com.my.chen.fabric.app.domain.Channel;
import com.my.chen.fabric.app.dto.Trace;
import com.my.chen.fabric.app.service.*;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/15
 * @description
 */
public class BlockUtil {

    private static BlockUtil instance;

    private final List<Channel> cacheChannels = new LinkedList<>();
    private final Map<Integer, Boolean> channelRun = new HashMap<>();

    // 使用默认的拒绝策略
    private static final RejectedExecutionHandler defaultHandler = new java.util.concurrent.ThreadPoolExecutor.AbortPolicy();
    private ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 20, 600, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(), defaultHandler);

    public static BlockUtil obtain() {
        if (null == instance) {
            synchronized (BlockUtil.class) {
                if (null == instance) {
                    instance = new BlockUtil();
                }
            }
        }
        return instance;
    }

    // 提供给定时任务定时刷新channel中的block, 这里需要注意的是如果channel有变化，需要在这里去除缓存的channel
    public void checkChannel(ChannelService channelService, CAService caService, BlockService blockService, TraceService traceService, List<Channel> channels) {
        refreshCacheChannel(channels);
        for (Channel channel : channels) {
            boolean exist = false;
            for (Channel channelTmp : this.cacheChannels) {
                if (channelTmp.getId() == channel.getId()) {
                    exist = true;
                }
            }
            if (!exist) {
                this.cacheChannels.add(channel);
                this.channelRun.put(channel.getId(), true);
                execChannel(channelService, caService, blockService, traceService, channel.getId());
            }
            CA ca = caService.listByPeerId(channel.getPeerId()).get(0);
            Trace trace = new Trace();
            trace.setChannelId(channel.getId());
            JSONObject blockMessage = traceService.queryBlockInfoWithCa(trace, ca);
            int code = blockMessage.containsKey("code") ? blockMessage.getInteger("code") : BaseService.FAIL;
            if (code == BaseService.SUCCESS) {
                channelService.updateChannelHeight(channel.getId(), blockMessage.getJSONObject("data").getInteger("height"));
            }
        }
    }

    private void execChannel(ChannelService channelService, CAService caService, BlockService blockService, TraceService traceService, int channelId) {
        insertBlock(channelService, caService, blockService, traceService, channelId);
    }


    private void insertBlock(ChannelService channelService, CAService caService, BlockService blockService, TraceService traceService, int channelId) {
        List<Block> list = blockService.getByChannelId(channelId);

        int height = 0;
        if (!CollectionUtils.isEmpty(list)) {
            list.sort((o1, o2) -> o2.getHeight() - o1.getHeight());
            height = list.get(0).getHeight() + 1;
        }

        CA ca = caService.listByPeerId(channelService.get(channelId).getPeerId()).get(0);
        while (channelRun.get(channelId)) {
            if (execBlock(blockService, traceService, channelId, height, ca)) {
                height++;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean execBlock(BlockService blockService, TraceService traceService, int channelId, int height, CA ca) {
        Trace trace = new Trace();
        trace.setChannelId(channelId);
        trace.setTrace(String.valueOf(height));
        JSONObject blockMessage = traceService.queryBlockByNumberWithCa(trace, ca);
        try {
            Block block = execBlock(blockMessage, channelId, height);
            if(block != null){
                blockService.add(block);
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }



    private Block execBlock(JSONObject blockJson, int channelId, int height) throws ParseException {
        int code = blockJson.containsKey("code") ? blockJson.getInteger("code") : BaseService.FAIL;
        if (code != BaseService.SUCCESS) {
            return null;
        }

        JSONArray envelopes = blockJson.containsKey("data") ? blockJson.getJSONObject("data").getJSONArray("envelopes") : new JSONArray();
        int txCount = 0;
        int rwSetCount = 0;
        int size = envelopes.size();
        for (int i = 0; i < size; i++) {
            JSONObject envelope = envelopes.getJSONObject(i);
            if (envelope.containsKey("transactionEnvelopeInfo")) {
                txCount += envelope.getJSONObject("transactionEnvelopeInfo").getInteger("txCount");
                JSONArray transactionActionInfoArray = envelope.getJSONObject("transactionEnvelopeInfo").getJSONArray("transactionActionInfoArray");
                int transactionActionInfoArraySize = transactionActionInfoArray.size();
                for (int j = 0; j < transactionActionInfoArraySize; j++) {
                    JSONObject transactionActionInfo = transactionActionInfoArray.getJSONObject(j);
                    rwSetCount += transactionActionInfo.getJSONObject("rwsetInfo").getInteger("nsRWsetCount");
                }
            }
        }

        String timestamp = envelopes.getJSONObject(0).getString("timestamp");
        Date date = DateUtil.str2Date(timestamp, "yyyy/MM/dd HH:mm:ss");

        Block block = new Block();
        block.setChannelId(channelId);
        block.setHeight(height);
        block.setDataHash(blockJson.getJSONObject("data").getString("dataHash"));
        block.setCalculatedHash(blockJson.getJSONObject("data").getString("calculatedBlockHash"));
        block.setPreviousHash(blockJson.getJSONObject("data").getString("previousHashID"));
        block.setEnvelopeCount(size);
        block.setTxCount(txCount);
        block.setRwSetCount(rwSetCount);
        block.setTimestamp(timestamp);
        block.setCalculateDate(Integer.valueOf(DateUtil.date2Str(date, "yyyyMMdd")));
        block.setCreateTime(DateUtil.getCurrent());
        block.setUpdateTime(DateUtil.getCurrent());
        return block;
    }

    // 缓存的channel只能是查询出来和原来在缓存中有效的
    private void refreshCacheChannel(List<Channel> channels){
        synchronized (cacheChannels){
            List<Channel> list = new LinkedList<>();
            for(Channel channel : channels){
                if(cacheChannels.contains(channel)){
                    list.add(channel);
                }
            }
            cacheChannels.clear();
            cacheChannels.addAll(list);
        }
    }

    void updateChannelData(int channelId) {
        channelRun.put(channelId, true);
    }
}

