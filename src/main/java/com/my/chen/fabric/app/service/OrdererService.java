package com.my.chen.fabric.app.service;


import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.domain.League;
import com.my.chen.fabric.app.domain.Orderer;
import com.my.chen.fabric.app.domain.Org;
import com.my.chen.fabric.app.util.DateUtil;
import com.my.chen.fabric.app.util.FabricHelper;
import com.my.chen.fabric.app.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
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

    @Resource
    private OrgMapper orgMapper;

    @Resource
    private LeagueMapper leagueMapper;

    @Resource
    private FileManageService manageService;


    public int add(Orderer orderer, MultipartFile serverCrtFile, MultipartFile clientCertFile, MultipartFile clientKeyFile) {
        if (StringUtils.isEmpty(orderer.getName()) || StringUtils.isEmpty(orderer.getLocation())) {
            return 0;
        }

        if (StringUtils.isNotEmpty(serverCrtFile.getOriginalFilename()) && StringUtils.isNotEmpty(clientCertFile.getOriginalFilename())
                && StringUtils.isNotEmpty(clientKeyFile.getOriginalFilename())) {

            resetOrderer(orderer);
            boolean flag = saveOrderCertFile(orderer, serverCrtFile, clientCertFile, clientKeyFile);
            if (!flag) {
                return 0;
            }
            orderer.setCreateTime(DateUtil.getCurrent());
            orderer.setUpdateTime(DateUtil.getCurrent());
            ordererMapper.save(orderer);
            return 1;
        }

        return 0;
    }


    private boolean saveOrderCertFile(Orderer orderer, MultipartFile serverCrtFile, MultipartFile clientCertFile, MultipartFile clientKeyFile) {
        String ordererTlsPath = manageService.getOrdererPath(orderer.getLeagueName(), orderer.getOrgName(), orderer.getName());

        if (serverCrtFile != null && StringUtils.isNotEmpty(serverCrtFile.getOriginalFilename())) {
            String serverCrtPath = String.format("%s%s%s", ordererTlsPath, File.separator, serverCrtFile.getOriginalFilename());
            orderer.setServerCrtPath(serverCrtPath);
            try {
                FileUtil.saveFile(serverCrtFile, serverCrtPath);
            } catch (IOException e) {
                log.error("saveOrderCertFile error", e);
                return false;
            }
        }

        if (clientCertFile != null && StringUtils.isNotEmpty(clientCertFile.getOriginalFilename())) {
            String clientCertPath = String.format("%s%s%s", ordererTlsPath, File.separator, clientCertFile.getOriginalFilename());
            orderer.setClientCertPath(clientCertPath);
            try {
                FileUtil.saveFile(clientCertFile, clientCertPath);
            } catch (IOException e) {
                log.error("saveOrderCertFile error", e);
                return false;
            }
        }


        if (clientKeyFile != null && StringUtils.isNotEmpty(clientKeyFile.getOriginalFilename())) {
            String clientKeyPath = String.format("%s%s%s", ordererTlsPath,File.separator, clientKeyFile.getOriginalFilename());
            orderer.setClientKeyPath(clientKeyPath);
            try {
                FileUtil.saveFile(clientKeyFile, clientKeyPath);
            } catch (IOException e) {
                log.error("saveOrderCertFile error", e);
                return false;
            }
        }

        return true;
    }


    // 如果update修改完成后，需要做对应的证书文件调整
    public int update(Orderer orderer, MultipartFile serverCrtFile, MultipartFile clientCertFile, MultipartFile clientKeyFile) {
        FabricHelper.getInstance().removeManager(peerMapper.findByOrgId(orderer.getOrgId()), channelMapper, chaincodeMapper);
        Orderer entity = ordererMapper.findById(orderer.getId()).get();

        resetOrderer(orderer);

        saveOrderCertFile(orderer, serverCrtFile,clientCertFile,clientKeyFile);

        if(StringUtils.isBlank(orderer.getServerCrtPath())){
            orderer.setServerCrtPath(entity.getServerCrtPath());
        }

        if(StringUtils.isBlank(orderer.getClientCertPath())){
            orderer.setClientCertPath(entity.getClientCertPath());
        }
        if(StringUtils.isBlank(orderer.getClientKeyPath())){
            orderer.setClientKeyPath(entity.getClientKeyPath());
        }

        orderer.setCreateTime(entity.getCreateTime());
        orderer.setUpdateTime(DateUtil.getCurrent());
        ordererMapper.save(orderer);
        return 1;
    }

    public int del(int id) {
        ordererMapper.deleteById(id);
        return 1;
    }


    public List<Orderer> listAll() {
        return Lists.newArrayList(ordererMapper.findAll());
    }


    public List<Orderer> listByOrgId(int orgId) {
        return ordererMapper.findByOrgId(orgId);
    }


    public Orderer get(int id) {
        return ordererMapper.findById(id).get();
    }


    public int countByOrgId(int orgId) {
        return ordererMapper.countByOrgId(orgId);
    }


    public int count() {
        return (int) ordererMapper.count();
    }


    public void resetOrderer(Orderer orderer) {
        Org org = orgMapper.findById(orderer.getOrgId()).get();
        League league = leagueMapper.findById(org.getLeagueId()).get();
        orderer.setLeagueName(league.getName());
        orderer.setOrgName(org.getMspId());
    }
}
