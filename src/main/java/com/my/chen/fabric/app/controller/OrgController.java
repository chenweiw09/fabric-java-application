package com.my.chen.fabric.app.controller;

import com.my.chen.fabric.app.domain.Org;
import com.my.chen.fabric.app.service.LeagueService;
import com.my.chen.fabric.app.service.OrdererService;
import com.my.chen.fabric.app.service.OrgService;
import com.my.chen.fabric.app.service.PeerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("org")
public class OrgController {

    @Resource
    private OrgService orgService;
    @Resource
    private LeagueService leagueService;
    @Resource
    private PeerService peerService;

    @Resource
    private OrdererService ordererService;

    @PostMapping(value = "submit")
    public ModelAndView submit(@ModelAttribute Org org,
                               @RequestParam("intent") String intent,
                               @RequestParam("id") int id) {
        switch (intent.toLowerCase()) {
            case "add":
                orgService.add(org);
                break;
            case "edit":
                org.setId(id);
                orgService.update(org);
                break;
        }
        return new ModelAndView(new RedirectView("list"));
    }

    @GetMapping(value = "add")
    public ModelAndView add() {
        ModelAndView modelAndView = new ModelAndView("orgSubmit");
        modelAndView.addObject("intentLarge", "新建组织");
        modelAndView.addObject("intentLittle", "新建");
        modelAndView.addObject("submit", "新增");
        modelAndView.addObject("intent", "add");
        modelAndView.addObject("org", new Org());
        modelAndView.addObject("leagues", leagueService.listAll());
        return modelAndView;
    }

    @GetMapping(value = "edit")
    public ModelAndView edit(@RequestParam("id") int id) {
        ModelAndView modelAndView = new ModelAndView("orgSubmit");
        modelAndView.addObject("intentLarge", "编辑组织");
        modelAndView.addObject("intentLittle", "编辑");
        modelAndView.addObject("submit", "修改");
        modelAndView.addObject("intent", "edit");
        modelAndView.addObject("org", orgService.get(id));
        modelAndView.addObject("leagues", leagueService.listAll());
        return modelAndView;
    }

    @GetMapping(value = "list")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("orgs");
        List<Org> orgs = new ArrayList<>(orgService.listAll());
        for (Org org : orgs) {
            org.setOrdererCount(ordererService.countByOrgId(org.getId()));
            org.setPeerCount(peerService.countByOrgId(org.getId()));
            org.setLeagueName(leagueService.getById(org.getLeagueId()).getName());
        }
        modelAndView.addObject("orgs", orgs);
        return modelAndView;
    }

    @GetMapping(value = "delete")
    public ModelAndView delete(@RequestParam("id") int id) {
        int ordererCount = ordererService.countByOrgId(id);
        if(ordererCount > 0){
            log.error("org has more then 1 orderer and can not delete");
            return new ModelAndView(new RedirectView("list"));
        }
        int peerCount = peerService.countByOrgId(id);
        if(peerCount > 0){
            log.error("org has more then 1 peer and can not delete");
            return new ModelAndView(new RedirectView("list"));
        }
        orgService.delOrgByid(id);
        return new ModelAndView(new RedirectView("list"));
    }

}
