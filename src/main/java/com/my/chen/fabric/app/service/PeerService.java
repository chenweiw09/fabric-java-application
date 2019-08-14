package com.my.chen.fabric.app.service;

import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.domain.League;
import com.my.chen.fabric.app.domain.Org;
import com.my.chen.fabric.app.domain.Peer;
import com.my.chen.fabric.app.util.DateUtil;
import com.my.chen.fabric.app.util.FabricHelper;
import com.my.chen.fabric.app.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class PeerService {

    @Resource
    private PeerMapper peerMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ChaincodeMapper chaincodeMapper;

    @Resource
    private OrgMapper orgMapper;

    @Resource
    private LeagueMapper leagueMapper;


    @Resource
    private FileManageService manageService;


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


    // 因为peer 下挂载的是智能合约地址，所以如果peer的域名有变化，需要调整对应的智能合约地址地址
    public int update(Peer peer) {
        Peer entity = peerMapper.findById(peer.getId()).get();
        if(!entity.getName().equals(peer.getName().trim())){
            Org org = orgMapper.findById(peer.getOrgId()).get();
            League league = leagueMapper.findById(org.getLeagueId()).get();
            String oldPath = manageService.getPeerPath(league.getName(), org.getName(), entity.getName());
            String newPath = manageService.getPeerPath(league.getName(), org.getName(), peer.getName());
            try {
                FileUtil.copyDirectory(oldPath, newPath);
            } catch (IOException e) {
                log.error(String.format("peer path has changed from %s to %s", oldPath, newPath),e);
                return 0;
            }
        }

        FabricHelper.getInstance().removeManager(peerMapper.findByOrgId(peer.getOrgId()), channelMapper, chaincodeMapper);

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
