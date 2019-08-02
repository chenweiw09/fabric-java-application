package com.my.chen.fabric.app.service;


import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.domain.Chaincode;
import com.my.chen.fabric.app.util.DateUtil;
import com.my.chen.fabric.app.util.FileUtil;
import org.springframework.core.env.Environment;
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
    private Environment env;

    enum ChainCodeIntent {
        INSTALL, INSTANTIATE
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


    public String install(Chaincode chaincode, MultipartFile file) {
        if (!verify(chaincode) || null == file || null != check(chaincode)) {
            return responseFail("install error, param has none value and source mush be uploaded or had the same chaincode");
        }
        String chaincodeSource = String.format("%s%s%s%s%s%s%s%s%s%schaincode",
                env.getProperty("config.dir"),
                File.separator,
                chaincode.getLeagueName(),
                File.separator,
                chaincode.getOrgName(),
                File.separator,
                chaincode.getPeerName(),
                File.separator,
                chaincode.getChannelName(),
                File.separator);
        String chaincodePath = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[0];
        String childrenPath = String.format("%s%ssrc%s%s", chaincodeSource, File.separator, File.separator, Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[0]);
        chaincode.setSource(chaincodeSource);
        chaincode.setPath(chaincodePath);
        chaincode.setPolicy(String.format("%s%spolicy.yaml", childrenPath, File.separator));
        chaincode.setCreateTime(DateUtil.getCurrent());
        chaincode.setUpdateTime(DateUtil.getCurrent());
        try {
            FileUtil.unZipAndSave(file, String.format("%s%ssrc", chaincodeSource, File.separator), childrenPath);
        } catch (IOException e) {
            e.printStackTrace();
            return responseFail("source unzip fail");
        }
        chaincodeMapper.save(chaincode);
        chaincode.setId(check(chaincode).getId());
        return chainCode(chaincode.getId(), orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper, ChainCodeIntent.INSTALL, new String[]{});
    }


    public String instantiate(Chaincode chaincodeInfo, List<String> strArray) {
        int size = strArray.size();
        String[] args = new String[size];
        for (int i = 0; i < size; i++) {
            args[i] = strArray.get(i);
        }
        return chainCode(chaincodeInfo.getId(), orgMapper, channelMapper, chaincodeMapper, ordererMapper, peerMapper, ChainCodeIntent.INSTANTIATE, args);
    }


    public int update(Chaincode chaincodeInfo) {
//        FabricHelper.obtain().removeManager(chaincodeInfo.getId());
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



    private String chainCode(int chaincodeId, OrgMapper orgMapper, ChannelMapper channelMapper, ChaincodeMapper chainCodeMapper,
                             OrdererMapper ordererMapper, PeerMapper peerMapper, ChainCodeIntent intent, String[] args) {
        Map<String, String> resultMap = new HashMap<>();
        try {
//            FabricManager manager = FabricHelper.obtain().get(orgMapper, channelMapper, chainCodeMapper, ordererMapper, peerMapper,
//                    chaincodeId);
//            switch (intent) {
//                case INSTALL:
//                    resultMap = manager.install();
//                    break;
//                case INSTANTIATE:
//                    resultMap = manager.instantiate(args);
//                    break;
//            }
            if (resultMap.get("code").equals("error")) {
                return responseFail(resultMap.get("data"));
            } else {
                return responseSuccess(resultMap.get("data"), resultMap.get("txid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return responseFail(String.format("Request failed： %s", e.getMessage()));
        }
    }

    private boolean verify(Chaincode chaincode) {
        return StringUtils.isEmpty(chaincode.getName()) ||
                StringUtils.isEmpty(chaincode.getPath()) ||
                StringUtils.isEmpty(chaincode.getVersion()) ||
                chaincode.getProposalWaitTime() == 0 ||
                chaincode.getInvokeWaitTime() == 0;
    }

    private Chaincode check(Chaincode chaincode){
        Chaincode code = chaincodeMapper.findByNameAndVersionAndChannelId(chaincode.getName(),chaincode.getVersion(),chaincode.getChannelId());
        return code;
    }
}
