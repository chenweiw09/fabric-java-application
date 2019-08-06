package com.my.chen.fabric.app.service;


import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.ChaincodeMapper;
import com.my.chen.fabric.app.dao.ChannelMapper;
import com.my.chen.fabric.app.dao.OrdererMapper;
import com.my.chen.fabric.app.dao.PeerMapper;
import com.my.chen.fabric.app.domain.Orderer;
import com.my.chen.fabric.app.util.DateUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class OrdererService {

    @Resource
    private OrdererMapper ordererMapper;
    @Resource
    private PeerMapper peerMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ChaincodeMapper chaincodeMapper;


    public int add(Orderer orderer) {
        if (StringUtils.isEmpty(orderer.getName()) || StringUtils.isEmpty(orderer.getLocation())) {
            return 0;
        }
        orderer.setCreateTime(DateUtil.getCurrent());
        orderer.setUpdateTime(DateUtil.getCurrent());
        ordererMapper.save(orderer);
        return 1;
    }


    public int update(Orderer orderer) {
//        FabricHelper.obtain().removeManager(peerMapper.list(orderer.getOrgId()), channelMapper, chaincodeMapper);
        Orderer entity = ordererMapper.findById(orderer.getId()).get();
        orderer.setCreateTime(entity.getCreateTime());
        orderer.setUpdateTime(DateUtil.getCurrent());

        ordererMapper.save(orderer);
        return 1;
    }


    public List<Orderer> listAll() {
        return Lists.newArrayList(ordererMapper.findAll());
    }


    public List<Orderer> listById(int orgId) {
        return ordererMapper.findByOrgId(orgId);
    }


    public Orderer get(int id) {
        return ordererMapper.findById(id).get();
    }


    public int countById(int orgId) {
        return ordererMapper.countByOrgId(orgId);
    }


    public int count() {
        return (int) ordererMapper.count();
    }
}
