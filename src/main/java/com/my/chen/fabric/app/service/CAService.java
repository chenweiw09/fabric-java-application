package com.my.chen.fabric.app.service;

import com.google.common.collect.Lists;
import com.my.chen.fabric.app.dao.*;
import com.my.chen.fabric.app.domain.CA;
import com.my.chen.fabric.app.domain.League;
import com.my.chen.fabric.app.domain.Org;
import com.my.chen.fabric.app.domain.Peer;
import com.my.chen.fabric.app.util.DateUtil;
import com.my.chen.fabric.app.util.FabricHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/15
 * @description
 */
@Service
@Slf4j
public class CAService {

    @Resource
    private LeagueMapper leagueMapper;
    @Resource
    private OrgMapper orgMapper;
    @Resource
    private PeerMapper peerMapper;
    @Resource
    private CAMapper caMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ChaincodeMapper chaincodeMapper;


    public int addCa(CA ca, MultipartFile skFile, MultipartFile certFile){
        if(skFile == null || StringUtils.isBlank(skFile.getOriginalFilename()) || certFile == null || StringUtils.isBlank(certFile.getOriginalFilename())){
            log.error("ca cert is null");
            return 0;
        }

        CA entity = caMapper.findByNameAndPeerId(ca.getName(), ca.getPeerId());
        if(entity != null){
            log.info("ca has been existed");
            return 0;
        }

        ca = resetCa(ca);

        try {
            ca.setSk(new String(IOUtils.toByteArray(skFile.getInputStream())));
            ca.setCertificate(new String(IOUtils.toByteArray(certFile.getInputStream()), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        ca.setCreateTime(DateUtil.getCurrent());
        ca.setUpdateTime(DateUtil.getCurrent());

        caMapper.save(ca);
        return 1;
    }

    public int update(CA ca, MultipartFile skFile, MultipartFile certFile) {
        FabricHelper.getInstance().removeManager(channelMapper.findByPeerId(ca.getPeerId()), chaincodeMapper);
        ca = resetCa(ca);

        CA entity = caMapper.findById(ca.getId()).get();
        ca.setCreateTime(entity.getCreateTime());
        ca.setUpdateTime(DateUtil.getCurrent());

        if(skFile != null && StringUtils.isNotBlank(skFile.getOriginalFilename()) && certFile != null && StringUtils.isNotBlank(certFile.getOriginalFilename())){
            try {
                ca.setSk(new String(IOUtils.toByteArray(skFile.getInputStream())));
                ca.setCertificate(new String(IOUtils.toByteArray(certFile.getInputStream()), "UTF-8"));
            } catch (IOException e) {
                return 0;
            }
        }else {
            ca.setSk(entity.getSk());
            ca.setCertificate(entity.getCertificate());
        }

        caMapper.save(ca);
        return 1;
    }

    public List<CA> listAll() {
        return Lists.newArrayList(caMapper.findAll());
    }

    public List<CA> listByPeerId(int peerId) {
        return caMapper.findByPeerId(peerId);
    }


    public CA findById(int id) {
        return caMapper.findById(id).get();
    }


    public CA getByFlag(String flag) {
        return caMapper.findByFlag(flag);
    }


    public int countByPeerId(int peerId) {
        return caMapper.countByPeerId(peerId);
    }

    public int count() {
        return (int)caMapper.count();
    }

    public int delete(int id) {
        FabricHelper.getInstance().removeManager(channelMapper.findByPeerId(findById(id).getPeerId()), chaincodeMapper);
         caMapper.deleteById(id);

         return 1;
    }

    public List<Peer> getPeersByCA(CA ca) {
        Org org = orgMapper.findById(peerMapper.findById(ca.getPeerId()).get().getOrgId()).get();
        List<Peer> peers = peerMapper.findByOrgId(org.getId());

        for (Peer peer : peers) {
            peer.setOrgName(org.getName());
        }
        return peers;
    }


    public List<CA> listFullCA() {
        List<CA> cas = listAll();
        for (CA ca: cas) {
            Peer peer = peerMapper.findById(ca.getPeerId()).get();
            Org org = orgMapper.findById(peer.getOrgId()).get();
            ca.setPeerName(peer.getName());
            ca.setOrgName(org.getMspId());
            ca.setLeagueName(leagueMapper.findById(org.getLeagueId()).get().getName());
        }
        return cas;
    }



    private CA resetCa(CA ca) {
        Peer peer = peerMapper.findById(ca.getPeerId()).get();
        Org org = orgMapper.findById(peer.getOrgId()).get();
        League league = leagueMapper.findById(org.getLeagueId()).get();

        ca.setLeagueName(league.getName());
        ca.setOrgName(org.getMspId());
        ca.setPeerName(peer.getName());
        ca.setFlag(peer.getName()+"-"+ca.getName());
        // ca.setName(String.format("%s-%s", ca.getName(), ca.getPeerId()));
        return ca;
    }


}
