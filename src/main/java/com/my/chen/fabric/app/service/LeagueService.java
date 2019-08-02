package com.my.chen.fabric.app.service;


import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.LeagueMapper;
import com.my.chen.fabric.app.domain.League;
import com.my.chen.fabric.app.util.DateUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LeagueService  {

    @Resource
    private LeagueMapper leagueMapper;


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
        leagueMapper.save(leagueInfo);
        return 1;
    }


    public List<League> listAll() {
        return Lists.newArrayList(leagueMapper.findAll());
    }

    public League getById(int id) {
        return leagueMapper.findById(id).get();
    }

}
