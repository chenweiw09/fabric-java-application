package com.my.chen.fabric.app.controller;

import com.my.chen.fabric.app.domain.League;
import com.my.chen.fabric.app.domain.Org;
import com.my.chen.fabric.app.domain.Peer;
import com.my.chen.fabric.app.service.ChannelService;
import com.my.chen.fabric.app.service.LeagueService;
import com.my.chen.fabric.app.service.OrgService;
import com.my.chen.fabric.app.service.PeerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("peer")
public class PeerController {

    @Resource
    private PeerService peerService;
    @Resource
    private OrgService orgService;
    @Resource
    private LeagueService leagueService;
    @Resource
    private ChannelService channelService;

    @PostMapping(value = "submit")
    public ModelAndView submit(@ModelAttribute Peer peer,
                               @RequestParam("intent") String intent,
                               @RequestParam("serverCrtFile") MultipartFile serverCrtFile,
                               @RequestParam("clientCertFile") MultipartFile clientCertFile,
                               @RequestParam("clientKeyFile") MultipartFile clientKeyFile,
                               @RequestParam("id") int id) {
        switch (intent.toLowerCase()) {
            case "add":
                peerService.add(peer,serverCrtFile, clientCertFile, clientKeyFile);
                break;
            case "edit":
                peer.setId(id);
                peerService.update(peer,serverCrtFile, clientCertFile, clientKeyFile);
                break;
        }
        return new ModelAndView(new RedirectView("list"));
    }

    @GetMapping(value = "add")
    public ModelAndView add() {
        ModelAndView modelAndView = new ModelAndView("peerSubmit");
        modelAndView.addObject("intentLarge", "新建节点");
        modelAndView.addObject("intentLittle", "新建");
        modelAndView.addObject("submit", "新增");
        modelAndView.addObject("intent", "add");
        modelAndView.addObject("peer", new Peer());
        modelAndView.addObject("orgs", getForPeerAndOrderer());
        return modelAndView;
    }

    @GetMapping(value = "edit")
    public ModelAndView edit(@RequestParam("id") int id) {
        ModelAndView modelAndView = new ModelAndView("peerSubmit");
        modelAndView.addObject("intentLarge", "编辑节点");
        modelAndView.addObject("intentLittle", "编辑");
        modelAndView.addObject("submit", "修改");
        modelAndView.addObject("intent", "edit");
        Peer peer = peerService.get(id);
        League league = leagueService.getById(orgService.get(peer.getOrgId()).getLeagueId());
        List<Org> orgs = orgService.listByLeagueId(league.getId());
        for (Org org : orgs) {
            org.setLeagueName(league.getName());
        }
        modelAndView.addObject("peer", peer);
        modelAndView.addObject("orgs", orgs);
        return modelAndView;
    }

    @GetMapping(value = "list")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("peers");
        List<Peer> peers = peerService.listAll();
        for (Peer peer : peers) {
            peer.setOrgName(orgService.get(peer.getOrgId()).getName());
            peer.setChannelCount(channelService.countByPeerId(peer.getId()));
        }
        modelAndView.addObject("peers", peers);
        return modelAndView;
    }

    @GetMapping("delete")
    public ModelAndView deletePeer(@RequestParam("id") int peerId){
        int channelCount = channelService.countByPeerId(peerId);
        if(channelCount > 0){
            log.error("peer has more than one channel and can not delete");
            return new ModelAndView(new RedirectView("list"));
        }
        peerService.delPeer(peerId);
        return new ModelAndView(new RedirectView("list"));
    }

    private List<Org> getForPeerAndOrderer() {
        List<Org> orgs = orgService.listAll();
        for (Org org : orgs) {
            org.setLeagueName(leagueService.getById(org.getLeagueId()).getName());
        }
        return orgs;
    }

}
