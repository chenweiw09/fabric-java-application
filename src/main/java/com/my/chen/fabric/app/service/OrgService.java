package com.my.chen.fabric.app.service;

import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.domain.Org;
import com.my.chen.fabric.app.util.DateUtil;
import com.my.chen.fabric.app.util.FileUtil;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class OrgService  {

    @Resource
    private OrgMapper orgMapper;
    @Resource
    private Environment env;
    @Resource
    private LeagueMapper leagueMapper;
    @Resource
    private PeerMapper peerMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ChaincodeMapper chaincodeMapper;



    public int add(Org org, MultipartFile file) {
        if (StringUtils.isEmpty(org.getName()) || StringUtils.isEmpty(org.getMspId()) ||
                StringUtils.isEmpty(org.getDomainName()) || StringUtils.isEmpty(org.getOrdererDomainName()) ||
                StringUtils.isEmpty(org.getUsername()) || null == file) {
            return 0;
        }

        org.setCreateTime(DateUtil.getCurrent());
        org.setUpdateTime(DateUtil.getCurrent());
        String parentPath = String.format("%s%s%s%s%s",
                env.getProperty("config.dir"),
                File.separator,
                leagueMapper.findById(org.getLeagueId()).get().getName(),
                File.separator,
                org.getName());
        String childrenPath = parentPath + File.separator + Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[0];
        org.setCryptoConfigDir(childrenPath);
        try {
            FileUtil.unZipAndSave(file, parentPath, childrenPath);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        orgMapper.save(org);
        return 1;
    }


    public int update(Org org, MultipartFile file) {
        if (null != file) {
            String parentPath = String.format("%s%s%s%s%s",
                    env.getProperty("config.dir"),
                    File.separator,
                    leagueMapper.findById(org.getLeagueId()).get().getName(),
                    File.separator,
                    org.getName());
            String childrenPath = parentPath + File.separator + Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[0];
            org.setCryptoConfigDir(childrenPath);
            try {
                FileUtil.unZipAndSave(file, parentPath, childrenPath);
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }
//        FabricHelper.obtain().removeManager(peerMapper.list(org.getId()), channelMapper, chaincodeMapper);

        // 同样的道理，org中更新的数据仅包括修改的字段信息
        Org entity = orgMapper.findById(org.getId()).get();
        org.setPeerCount(entity.getPeerCount());
        org.setOrdererCount(entity.getOrdererCount());
        org.setUpdateTime(DateUtil.getCurrent());
        orgMapper.save(org);
        return 1;
    }


    public List<Org> listAll() {
        return Lists.newArrayList(orgMapper.findAll());
    }


    public List<Org> listById(int leagueId) {
        return orgMapper.findByLeagueId(leagueId);
    }


    public Org get(int id) {
        return orgMapper.findById(id).get();
    }


    public int countById(int leagueId) {
        return orgMapper.countByLeagueId(leagueId);
    }


    public int count() {
        return (int) orgMapper.count();
    }

}
