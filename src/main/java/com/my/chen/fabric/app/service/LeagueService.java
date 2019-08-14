package com.my.chen.fabric.app.service;


import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.LeagueMapper;
import com.my.chen.fabric.app.domain.League;
import com.my.chen.fabric.app.util.DateUtil;
import com.my.chen.fabric.app.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class LeagueService  {

    @Resource
    private LeagueMapper leagueMapper;

    @Resource
    private FileManageService manageService;

    public int add(League leagueInfo) {
        if (StringUtils.isEmpty(leagueInfo.getName())) {
            return 0;
        }
        leagueInfo.setCreateTime(DateUtil.getCurrent());
        leagueInfo.setUpdateTime(DateUtil.getCurrent());
        leagueMapper.save(leagueInfo);
        return 1;
    }


    public int update(League leagueInfo) {
        League entity = leagueMapper.findById(leagueInfo.getId()).get();

        // 如果联盟变化名字，需要做对应的底层数据迁移
        if(!entity.getName().equals(leagueInfo.getName().trim())){
            log.info(String.format("league name has been changed from %s to %s", entity.getName(), leagueInfo.getName()));
            String newPath = manageService.getLeaguePath(leagueInfo.getName());
            String oldPath = manageService.getLeaguePath(entity.getName());
            try {
                FileUtil.copyDirectory(oldPath,newPath);
            } catch (IOException e) {
                log.error("file copy error", e);
                return 0;
            }
        }
        entity.setUpdateTime(DateUtil.getCurrent());
        entity.setName(leagueInfo.getName());
        leagueMapper.save(entity);
        return 1;
    }


    public List<League> listAll() {
        return Lists.newArrayList(leagueMapper.findAll());
    }

    public League getById(int id) {
        return leagueMapper.findById(id).get();
    }

}
