package com.my.chen.fabric.app.service;

import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.domain.Org;
import com.my.chen.fabric.app.util.DateUtil;
import com.my.chen.fabric.app.util.FabricHelper;
import com.my.chen.fabric.app.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class OrgService {

    @Resource
    private OrgMapper orgMapper;

    @Resource
    private LeagueMapper leagueMapper;
    @Resource
    private PeerMapper peerMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ChaincodeMapper chaincodeMapper;

    @Resource
    private OrdererMapper ordererMapper;


    public int add(Org org) {
        if (StringUtils.isEmpty(org.getName()) || StringUtils.isEmpty(org.getMspId())) {
            return 0;
        }

        org.setCreateTime(DateUtil.getCurrent());
        org.setUpdateTime(DateUtil.getCurrent());
        orgMapper.save(org);
        return 1;
    }


    public int update(Org org) {

        // 同样的道理，org中更新的数据仅包括修改的字段信息
        Org entity = orgMapper.findById(org.getId()).get();
        FabricHelper.getInstance().removeManager(peerMapper.findByOrgId(org.getId()), channelMapper, chaincodeMapper);
        org.setUpdateTime(DateUtil.getCurrent());
        org.setCreateTime(entity.getCreateTime());
        orgMapper.save(org);
        return 1;
    }


    public List<Org> listAll() {
        List<Org> list = Lists.newArrayList(orgMapper.findAll());
        for (Org org : list) {
            org.setOrdererCount(ordererMapper.countByOrgId(org.getId()));
            org.setPeerCount(peerMapper.countByOrOrgId(org.getId()));
            org.setLeagueName(leagueMapper.findById(org.getLeagueId()).get().getName());
        }
        return list;
    }


    public List<Org> listByLeagueId(int leagueId) {
        return orgMapper.findByLeagueId(leagueId);
    }


    public List<Org> getAllPartOrg(){
        List<Org> list = Lists.newArrayList(orgMapper.findAll());
        for (Org org : list) {
            org.setLeagueName(leagueMapper.findById(org.getLeagueId()).get().getName());
        }
        return list;
    }


    public int delOrgByid(int orgId){
        Org org =  orgMapper.findById(orgId).get();
        int ordererCount = ordererMapper.countByOrgId(org.getId());
        int peerCount = peerMapper.countByOrOrgId(org.getId());
        if(ordererCount > 0 || peerCount >0){
            log.error("org has orderer or org has peer, can not be deleted");
            return 0;
        }

        orgMapper.deleteById(orgId);
        return 1;
    }

    public Org get(int id) {
        return orgMapper.findById(id).get();
    }


    public int countByLeagueId(int leagueId) {
        return orgMapper.countByLeagueId(leagueId);
    }


    public int count() {
        return (int) orgMapper.count();
    }

}
