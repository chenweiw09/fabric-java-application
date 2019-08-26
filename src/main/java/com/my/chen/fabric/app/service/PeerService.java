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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
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


    public int add(Peer peer, MultipartFile serverCrtFile, MultipartFile clientCertFile, MultipartFile clientKeyFile) {
        if (StringUtils.isEmpty(peer.getName()) || StringUtils.isEmpty(peer.getLocation()) || StringUtils.isEmpty(peer.getEventHubLocation())) {
            return 0;
        }

        resetPeer(peer);

        if (StringUtils.isNotEmpty(serverCrtFile.getOriginalFilename()) && StringUtils.isNotEmpty(clientCertFile.getOriginalFilename())
                && StringUtils.isNotEmpty(clientKeyFile.getOriginalFilename())) {

            boolean flag = savePeerCertFile(peer, serverCrtFile, clientCertFile, clientKeyFile);
            if (!flag) {
                return 0;
            }

        }
        peer.setCreateTime(DateUtil.getCurrent());
        peer.setUpdateTime(DateUtil.getCurrent());
        peerMapper.save(peer);
        return 1;
    }


    // 因为peer 下挂载的是智能合约地址，所以如果peer的域名有变化，需要调整对应的智能合约地址地址
    public int update(Peer peer, MultipartFile serverCrtFile, MultipartFile clientCertFile, MultipartFile clientKeyFile) {
        FabricHelper.getInstance().removeManager(channelMapper.findByPeerId(peer.getId()), chaincodeMapper);

        Peer entity = peerMapper.findById(peer.getId()).get();
        resetPeer(peer);
        savePeerCertFile(peer, serverCrtFile, clientCertFile, clientKeyFile);

        if (StringUtils.isBlank(peer.getServerCrtPath())) {
            peer.setServerCrtPath(entity.getServerCrtPath());
        }

        if (StringUtils.isBlank(peer.getClientCertPath())) {
            peer.setClientCertPath(entity.getClientCertPath());
        }
        if (StringUtils.isBlank(peer.getClientKeyPath())) {
            peer.setClientKeyPath(entity.getClientKeyPath());
        }

        peer.setCreateTime(entity.getCreateTime());
        peer.setUpdateTime(DateUtil.getCurrent());
        peerMapper.save(peer);
        return 1;
    }


    public List<Peer> listAll() {
        List<Peer> list = Lists.newArrayList(peerMapper.findAll());
        for (Peer peer : list) {
            Org org = orgMapper.findById(peer.getOrgId()).get();
            League league = leagueMapper.findById(org.getLeagueId()).get();
            peer.setLeagueName(league.getName());
            peer.setOrgName(org.getMspId());
            peer.setChannelCount(channelMapper.countByPeerId(peer.getId()));
        }

        return list;
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

    private boolean savePeerCertFile(Peer peer, MultipartFile serverCrtFile, MultipartFile clientCertFile, MultipartFile clientKeyFile) {
        String ordererTlsPath = manageService.getPeerPath(peer.getLeagueName(), peer.getOrgName(), peer.getName());

        if (serverCrtFile != null && org.apache.commons.lang3.StringUtils.isNotEmpty(serverCrtFile.getOriginalFilename())) {
            String serverCrtPath = String.format("%s%s%s", ordererTlsPath, File.separator, serverCrtFile.getOriginalFilename());
            peer.setServerCrtPath(serverCrtPath);
            try {
                FileUtil.saveFile(serverCrtFile, serverCrtPath);
            } catch (IOException e) {
                log.error("saveOrderCertFile error", e);
                return false;
            }
        }

        if (clientCertFile != null && org.apache.commons.lang3.StringUtils.isNotEmpty(clientCertFile.getOriginalFilename())) {
            String clientCertPath = String.format("%s%s%s", ordererTlsPath, File.separator, clientCertFile.getOriginalFilename());
            peer.setClientCertPath(clientCertPath);
            try {
                FileUtil.saveFile(clientCertFile, clientCertPath);
            } catch (IOException e) {
                log.error("saveOrderCertFile error", e);
                return false;
            }
        }


        if (clientKeyFile != null && org.apache.commons.lang3.StringUtils.isNotEmpty(clientKeyFile.getOriginalFilename())) {
            String clientKeyPath = String.format("%s%s%s", ordererTlsPath, File.separator, clientKeyFile.getOriginalFilename());
            peer.setClientKeyPath(clientKeyPath);
            try {
                FileUtil.saveFile(clientKeyFile, clientKeyPath);
            } catch (IOException e) {
                log.error("saveOrderCertFile error", e);
                return false;
            }
        }

        return true;
    }

    public void resetPeer(Peer peer) {
        Org org = orgMapper.findById(peer.getOrgId()).get();
        League league = leagueMapper.findById(org.getLeagueId()).get();
        peer.setLeagueName(league.getName());
        peer.setOrgName(org.getMspId());
    }
}
