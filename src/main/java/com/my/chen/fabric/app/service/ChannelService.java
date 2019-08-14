package com.my.chen.fabric.app.service;

import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.ChaincodeMapper;
import com.my.chen.fabric.app.dao.ChannelMapper;
import com.my.chen.fabric.app.domain.Chaincode;
import com.my.chen.fabric.app.domain.Channel;
import com.my.chen.fabric.app.util.DateUtil;
import com.my.chen.fabric.app.util.FabricHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class ChannelService {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ChaincodeMapper chaincodeMapper;


    public int add(Channel channel) {
        if (StringUtils.isEmpty(channel.getName())) {
            log.debug("channel name is empty");
            return 0;
        }

        if (null != channelMapper.findByNameAndPeerId(channel.getName(), channel.getPeerId())) {
            log.debug("had the same channel in this peer");
            return 0;
        }

        channel.setCreateTime(DateUtil.getCurrent());
        channel.setUpdateTime(DateUtil.getCurrent());
        channelMapper.save(channel);
        return 1;
    }


    // 需要限定没有对应的合约，然后
    public int update(Channel channel) {

        Channel entity = channelMapper.findById(channel.getId()).get();
        List<Chaincode> chaincodes = chaincodeMapper.findByChannelId(entity.getId());
        if(!CollectionUtils.isEmpty(chaincodes)){
            log.info("channel "+channel.getName()+" has chain code and name can not be changed");
            channel.setName(entity.getName());
        }
        FabricHelper.getInstance().removeManager(channelMapper.findByPeerId(channel.getPeerId()), chaincodeMapper);
        channel.setChaincodeCount(entity.getChaincodeCount());
        channel.setUpdateTime(DateUtil.getCurrent());
        channel.setCreateTime(entity.getCreateTime());
        channelMapper.save(channel);
        return 1;
    }


    public List<Channel> listAll() {
        return Lists.newArrayList(channelMapper.findAll());
    }


    public List<Channel> listByPeerId(int peerId) {
        return channelMapper.findByPeerId(peerId);
    }


    public Channel get(int id) {
        return channelMapper.findById(id).get();
    }


    public int countByPeerId(int peerId) {
        return channelMapper.countByPeerId(peerId);
    }


    public int count() {
        return (int) channelMapper.count();
    }
}
