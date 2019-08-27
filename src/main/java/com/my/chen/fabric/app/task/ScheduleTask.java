package com.my.chen.fabric.app.task;

import com.my.chen.fabric.app.service.*;
import com.my.chen.fabric.app.util.BlockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/27
 * @description
 */
@Component
@EnableScheduling
@Slf4j
public class ScheduleTask {

    @Resource
    private LeagueService leagueService;
    @Resource
    private OrgService orgService;
    @Resource
    private OrdererService ordererService;
    @Resource
    private PeerService peerService;
    @Resource
    private CAService caService;
    @Resource
    private ChannelService channelService;
    @Resource
    private ChaincodeService chaincodeService;
    @Resource
    private TraceService traceService;
    @Resource
    private BlockService blockService;



    @Scheduled(cron = "0 0/1 * * * ?")
    public void runTask(){
        log.info("start update channel block height task");
        BlockUtil.obtain().checkChannel(channelService, caService, blockService, traceService, channelService.listAll());
    }

}
