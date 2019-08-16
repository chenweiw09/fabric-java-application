package com.my.chen.fabric.app.service;

import com.my.chen.fabric.app.dao.BlockMapper;
import com.my.chen.fabric.app.dao.ChannelMapper;
import com.my.chen.fabric.app.dao.PeerMapper;
import com.my.chen.fabric.app.domain.Block;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/15
 * @description
 */

@Service
public class BlockService {

    @Resource
    private BlockMapper blockMapper;
    @Resource
    private PeerMapper peerMapper;
    @Resource
    private ChannelMapper channelMapper;


    public int add(Block block){
        blockMapper.save(block);
        return 1;
    }


    @Transactional(rollbackFor = Exception.class, timeout = 5)
    public int addBlockList(List<Block> list){
        list.forEach(t -> add(t));
        return 1;
    }

    public List<Block> getByChannelId(int channelId){
        return blockMapper.findByChannelId(channelId);
    }




}
