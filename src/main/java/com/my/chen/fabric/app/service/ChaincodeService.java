package com.my.chen.fabric.app.service;


import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.domain.CA;
import com.my.chen.fabric.app.domain.Chaincode;
import com.my.chen.fabric.app.util.DateUtil;
import com.my.chen.fabric.app.util.FabricHelper;
import com.my.chen.fabric.app.util.FileUtil;
import com.my.chen.fabric.sdk.FbNetworkManager;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ChaincodeService implements BaseService {

    @Resource
    private OrgMapper orgMapper;
    @Resource
    private OrdererMapper ordererMapper;
    @Resource
    private PeerMapper peerMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ChaincodeMapper chaincodeMapper;

    @Resource
    private FileManageService manageService;

    @Resource
    private CAMapper caMapper;

    enum ChainCodeIntent {
        INSTALL, INSTANTIATE, UPGRADE
    }

    public int add(Chaincode chaincode) {
        if (verify(chaincode) || null != check(chaincode)) {
            return 0;
        }
        chaincode.setCreateTime(DateUtil.getCurrent());
        chaincode.setUpdateTime(DateUtil.getCurrent());
        chaincodeMapper.save(chaincode);
        return 1;
    }


    public JSONObject install(Chaincode chaincode, MultipartFile file) {
        if (!verify(chaincode) || null == file || null != check(chaincode)) {
            return responseFail("install error, param has none value and source mush be uploaded or had the same chaincode");
        }

        if (!uploadSource(chaincode, file)) {
            responseFail("source unzip fail");
        }

        chaincodeMapper.save(chaincode);
        chaincode.setId(check(chaincode).getId());

        JSONObject obj = chainCode(chaincode.getId(), caMapper.findByFlag(chaincode.getFlag()), ChainCodeIntent.INSTALL, new String[]{});

        if (SUCCESS != obj.getIntValue("code")) {
            chaincodeMapper.deleteById(chaincode.getId());
        } else {
            chaincode.setInstalled(1);
            chaincodeMapper.save(chaincode);
        }
        return obj;
    }

    public JSONObject instantiate(Chaincode chaincodeInfo, List<String> strArray) {
        int size = strArray.size();
        String[] args = new String[size];
        for (int i = 0; i < size; i++) {
            args[i] = strArray.get(i).trim();
        }
        JSONObject obj = chainCode(chaincodeInfo.getId(), caMapper.findByFlag(chaincodeInfo.getFlag()), ChainCodeIntent.INSTANTIATE, args);

        if (SUCCESS == obj.getIntValue("code")) {
            chaincodeInfo.setInstantiated(1);
            chaincodeMapper.save(chaincodeInfo);
        }
        return obj;
    }

    public JSONObject upgrade(Chaincode chaincode, MultipartFile file, List<String> strArray) {
        if (!verify(chaincode) || null == file || null != check(chaincode)) {
            return responseFail("install error, param has none value and source mush be uploaded or had the same chaincode");
        }

        if (!uploadSource(chaincode, file)) {
            responseFail("source unzip fail");
        }

        FabricHelper.getInstance().removeManager(chaincode.getId());
        CA ca = caMapper.findByFlag(chaincode.getFlag());
        JSONObject obj = chainCode(chaincode.getId(), ca, ChainCodeIntent.INSTALL, new String[]{});

        // 如果升级失败，保持原来的应用
        if (SUCCESS != obj.getIntValue("code")) {
            return obj;
        }

        int size = strArray.size();
        String[] args = new String[size];
        for (int i = 0; i < size; i++) {
            args[i] = strArray.get(i).trim();
        }

        obj = chainCode(chaincode.getId(), ca, ChainCodeIntent.UPGRADE, args);
        // 安装成功了，但是升级失败了，保留原来的版本信息可用
        if (SUCCESS != obj.getIntValue("code")) {
            return obj;
        }

        // 开始做更新操作
        FabricHelper.getInstance().removeManager(chaincode.getId());
        Chaincode entity = chaincodeMapper.findById(chaincode.getId()).get();
        chaincode.setInstalled(1);
        chaincode.setInstantiated(1);
        chaincode.setCreateTime(DateUtil.getCurrent());
        chaincode.setUpdateTime(DateUtil.getCurrent());

        String str = entity.getHistoryVersion();
        if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
            chaincode.setHistoryVersion(entity.getVersion());
        } else {
            chaincode.setHistoryVersion(entity.getHistoryVersion() + "," + entity.getVersion());
        }

        chaincodeMapper.save(chaincode);
        return obj;
    }


    private boolean uploadSource(Chaincode chaincode, MultipartFile file) {

        String chaincodeSource = manageService.getChainCodePath(chaincode.getLeagueName(), chaincode.getOrgName(), chaincode.getPeerName(), chaincode.getChannelName());

        String chaincodePath = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[0];
        String childrenPath = String.format("%s%ssrc%s%s", chaincodeSource, File.separator, File.separator, Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[0]);
        chaincode.setSource(chaincodeSource);
        chaincode.setPath(chaincodePath);
        chaincode.setPolicy(String.format("%s%spolicy.yaml", childrenPath, File.separator));
        chaincode.setCreateTime(DateUtil.getCurrent());
        chaincode.setUpdateTime(DateUtil.getCurrent());
        try {
            FileUtil.chaincodeUnzipAndSave(file, String.format("%s%ssrc", chaincodeSource, File.separator), childrenPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public int update(Chaincode chaincodeInfo) {
        FabricHelper.getInstance().removeManager(chaincodeInfo.getId());
        Chaincode entity = chaincodeMapper.findById(chaincodeInfo.getId()).get();

        chaincodeInfo.setCreateTime(entity.getCreateTime());
        chaincodeInfo.setUpdateTime(DateUtil.getCurrent());
        chaincodeInfo.setPolicy(entity.getPolicy());
        chaincodeInfo.setSource(entity.getSource());
        chaincodeMapper.save(chaincodeInfo);
        return 1;
    }


    public List<Chaincode> listAll() {
        return Lists.newArrayList(chaincodeMapper.findAll());
    }


    public List<Chaincode> listById(int id) {
        return chaincodeMapper.findByChannelId(id);
    }


    public Chaincode get(int id) {
        Optional<Chaincode> optional = chaincodeMapper.findById(id);
        return optional.orElse(null);
    }


    public int countById(int channelId) {
        return chaincodeMapper.countByChannelId(channelId);
    }


    public int count() {
        return (int) chaincodeMapper.count();
    }


    public int delete(int chaincodeId) {
        chaincodeMapper.deleteById(chaincodeId);
        return 1;
    }

    public int deleteByChannelId(int channelId) {
        List<Chaincode> chaincodes = chaincodeMapper.findByChannelId(channelId);
        for (Chaincode chaincode : chaincodes) {
            FabricHelper.getInstance().removeManager(chaincode.getId());
            chaincodeMapper.deleteById(chaincode.getId());
        }
        return 1;
    }

    private JSONObject chainCode(int chaincodeId, CA ca, ChainCodeIntent intent, String[] args) {
        JSONObject resultMap = new JSONObject();
        try {
            FbNetworkManager manager = FabricHelper.getInstance().get(orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper, ca,
                    chaincodeId);
            switch (intent) {
                case INSTALL:
                    resultMap = manager.install();
                    break;
                case INSTANTIATE:
                    resultMap = manager.instantiate(args);
                    break;
                case UPGRADE:
                    resultMap = manager.upgrade(args);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return responseFail(String.format("Request failed： %s", e.getMessage()));
        }
        return resultMap;
    }

    private boolean verify(Chaincode chaincode) {
        if (chaincode.getProposalWaitTime() == 0) {
            chaincode.setProposalWaitTime(90000);
        }
        if (chaincode.getInvokeWaitTime() == 0) {
            chaincode.setInvokeWaitTime(120);
        }
        return StringUtils.isEmpty(chaincode.getName()) ||
                StringUtils.isEmpty(chaincode.getPath()) ||
                StringUtils.isEmpty(chaincode.getVersion());
    }

    private Chaincode check(Chaincode chaincode) {
        Chaincode code = chaincodeMapper.findByNameAndVersionAndChannelId(chaincode.getName(), chaincode.getVersion(), chaincode.getChannelId());
        return code;
    }
}
