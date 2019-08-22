package com.my.chen.fabric.app.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.my.chen.fabric.app.domain.Block;
import com.my.chen.fabric.app.domain.CA;
import com.my.chen.fabric.app.domain.Channel;
import com.my.chen.fabric.app.dto.Trace;
import com.my.chen.fabric.app.service.*;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;

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

    private final List<Channel> channels = new LinkedList<>();
    private final List<Block> blocks = new LinkedList<>();
    private final Map<Integer, Boolean> channelRun = new HashMap<>();
    private final Map<Integer, Boolean> channelUpdata = new HashMap<>();

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

    public void checkChannel(ChannelService channelService, CAService caService, BlockService blockService, TraceService traceService, List<Channel> channels) {
        for (Channel channel : channels) {
            boolean hadOne = false;
            for (Channel channelTmp : this.channels) {
                if (channelTmp.getId() == channel.getId()) {
                    hadOne = true;
                }
            }
            if (!hadOne) {
                this.channels.add(channel);
                this.channelRun.put(channel.getId(), true);
                this.channelUpdata.put(channel.getId(), true);
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
        poolExecutor.submit(() -> {
            Block block = blockService.getByChannelId(channelId).get(0);
            int height = -1;
            if (null != block) {
                height = block.getHeight();
            }
            height = height == -1 ? 0 : height + 1;
            CA ca = caService.listByPeerId(channelService.get(channelId).getPeerId()).get(0);
            while (channelRun.get(channelId)) {
                if (!channelUpdata.get(channelId)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if (execBlock(blockService, traceService, channelId, height, ca)) {
                    height++;
                } else {
                    synchronized (blocks) {
                        insert(blockService);
                    }
                    channelUpdata.put(channelId, false);
                }
            }
        });
    }

    private boolean execBlock(BlockService blockService, TraceService traceService, int channelId, int height, CA ca) {
        Trace trace = new Trace();
        trace.setChannelId(channelId);
        trace.setTrace(String.valueOf(height));
        JSONObject blockMessage = traceService.queryBlockByNumberWithCa(trace, ca);
        return execBlock(blockMessage, blockService, channelId, height);
    }

    private boolean execBlock(JSONObject blockJson, BlockService blockService, int channelId, int height) {
        try {
            int code = blockJson.containsKey("code") ? blockJson.getInteger("code") : BaseService.FAIL;
            if (code != BaseService.SUCCESS) {
                return false;
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

            synchronized (blocks) {
                blocks.add(block);
                if (blocks.size() >= 100) {
                    insert(blockService);
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void insert(BlockService blockService) {
        blockService.addBlockList(blocks);
        blocks.clear();
    }

    void removeChannel(int channelId) {
        channelRun.put(channelId, false);
        for (Channel channel : channels) {
            if (channel.getId() == channelId) {
                channels.remove(channel);
            }
        }
    }

    void updataChannelData(int channelId) {
        channelUpdata.put(channelId, true);
    }
}

