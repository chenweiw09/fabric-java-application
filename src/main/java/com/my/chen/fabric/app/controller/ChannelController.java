package com.my.chen.fabric.app.controller;

import com.my.chen.fabric.app.domain.Channel;
import com.my.chen.fabric.app.domain.League;
import com.my.chen.fabric.app.domain.Org;
import com.my.chen.fabric.app.domain.Peer;
import com.my.chen.fabric.app.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import java.util.List;


@CrossOrigin
@RestController
@RequestMapping("channel")
public class ChannelController {

    @Resource
    private ChannelService channelService;
    @Resource
    private PeerService peerService;
    @Resource
    private OrgService orgService;
    @Resource
    private LeagueService leagueService;
    @Resource
    private ChaincodeService chaincodeService;

    @PostMapping(value = "submit")
    public ModelAndView submit(@ModelAttribute Channel channel,
                               @RequestParam("intent") String intent,
                               @RequestParam("id") int id) {
        switch (intent) {
            case "add":
                channelService.add(channel);
                break;
            case "edit":
                channel.setId(id);
                channelService.update(channel);
                break;
        }
        return new ModelAndView(new RedirectView("list"));
    }

    @GetMapping(value = "add")
    public ModelAndView add() {
        ModelAndView modelAndView = new ModelAndView("channelSubmit");
        modelAndView.addObject("intentLarge", "新建通道");
        modelAndView.addObject("intentLittle", "新建");
        modelAndView.addObject("submit", "新增");
        modelAndView.addObject("intent", "add");
        Channel channel = new Channel();
        List<Peer> peers = peerService.listAll();
        for (Peer peer : peers) {
            channel.setPeerName(peer.getName());
            Org org = orgService.get(peer.getOrgId());
            channel.setOrgName(org.getName());
            League league = leagueService.getById(org.getLeagueId());
            channel.setLeagueName(league.getName());
        }
        modelAndView.addObject("channel", channel);
        modelAndView.addObject("peers", peers);
        return modelAndView;
    }

    @GetMapping(value = "edit")
    public ModelAndView edit(@RequestParam("id") int id) {
        ModelAndView modelAndView = new ModelAndView("channelSubmit");
        modelAndView.addObject("intentLarge", "编辑通道");
        modelAndView.addObject("intentLittle", "编辑");
        modelAndView.addObject("submit", "修改");
        modelAndView.addObject("intent", "edit");
        Channel channel = channelService.get(id);
        Org org = orgService.get(peerService.get(channel.getPeerId()).getOrgId());
        channel.setOrgName(org.getName());
        List<Peer> peers = peerService.listById(org.getId());
        League league = leagueService.getById(orgService.get(org.getId()).getLeagueId());
        channel.setLeagueName(league.getName());
        org.setLeagueName(league.getName());
        for (Peer peer : peers) {
            peer.setOrgName(org.getName());
        }
        modelAndView.addObject("channel", channel);
        modelAndView.addObject("peers", peers);
        return modelAndView;
    }

    @GetMapping(value = "list")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("channels");
        List<Channel> channels = channelService.listAll();
        for (Channel channel : channels) {
            channel.setPeerName(peerService.get(channel.getPeerId()).getName());
            channel.setChaincodeCount(chaincodeService.countById(channel.getId()));
        }
        modelAndView.addObject("channels", channels);
        return modelAndView;
    }

}
