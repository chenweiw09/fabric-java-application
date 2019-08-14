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
    private FileManageService manageService;
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
        String parentPath = manageService.getOrgPath(leagueMapper.findById(org.getLeagueId()).get().getName(), org.getName());

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
        if (null != file && !StringUtils.isBlank(file.getOriginalFilename())) {
            String parentPath = manageService.getOrgPath(leagueMapper.findById(org.getLeagueId()).get().getName(), org.getName());
            String childrenPath = parentPath + File.separator + Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[0];
            org.setCryptoConfigDir(childrenPath);
            try {
                FileUtil.unZipAndSave(file, parentPath, childrenPath);
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }

        // 同样的道理，org中更新的数据仅包括修改的字段信息
        Org entity = orgMapper.findById(org.getId()).get();

        // 如果修改组织名字，需要修改原来的配置名字
        if (file == null || StringUtils.isBlank(file.getOriginalFilename())) {
            String newPath = manageService.getOrgPath(leagueMapper.findById(org.getLeagueId()).get().getName(), org.getName());
            String oldPath = manageService.getOrgPath(leagueMapper.findById(org.getLeagueId()).get().getName(), entity.getName());
            try {
                FileUtil.copyDirectory(oldPath, newPath);
            } catch (IOException e) {
                log.error("file copy error", e);
                return 0;
            }
        }

        FabricHelper.getInstance().removeManager(peerMapper.findByOrgId(org.getId()), channelMapper, chaincodeMapper);
        org.setPeerCount(entity.getPeerCount());
        org.setOrdererCount(entity.getOrdererCount());
        org.setUpdateTime(DateUtil.getCurrent());
        org.setCreateTime(entity.getCreateTime());

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
