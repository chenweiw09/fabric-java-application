package com.my.chen.fabric.app.service;

import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.ChaincodeMapper;
import com.my.chen.fabric.app.dao.ChannelMapper;
import com.my.chen.fabric.app.dao.PeerMapper;
import com.my.chen.fabric.app.domain.Peer;
import com.my.chen.fabric.app.util.DateUtil;
import com.my.chen.fabric.app.util.FabricHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PeerService {

    @Resource
    private PeerMapper peerMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ChaincodeMapper chaincodeMapper;


    public int add(Peer peer) {
        if (StringUtils.isEmpty(peer.getName()) || StringUtils.isEmpty(peer.getLocation()) ||
                StringUtils.isEmpty(peer.getEventHubName()) || StringUtils.isEmpty(peer.getEventHubLocation())) {
            return 0;
        }
        peer.setCreateTime(DateUtil.getCurrent());
        peer.setUpdateTime(DateUtil.getCurrent());
        peerMapper.save(peer);
        return 1;
    }


    public int update(Peer peer) {
        FabricHelper.getInstance().removeManager(peerMapper.findByOrgId(peer.getOrgId()), channelMapper, chaincodeMapper);
        Peer entity = peerMapper.findById(peer.getId()).get();
        peer.setChannelCount(entity.getChannelCount());
        peer.setUpdateTime(DateUtil.getCurrent());
        peer.setCreateTime(entity.getCreateTime());
        peerMapper.save(peer);
        return 1;
    }


    public List<Peer> listAll() {
        return Lists.newArrayList(peerMapper.findAll());
    }


    public List<Peer> listById(int orgId) {
        return peerMapper.findByOrgId(orgId);
    }


    public Peer get(int id) {
        return peerMapper.findById(id).orElse(null);
    }


    public int countById(int orgId) {
        return peerMapper.countByOrOrgId(orgId);
    }


    public int count() {
        return (int) peerMapper.count();
    }
}
