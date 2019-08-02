package com.my.chen.fabric.app.controller;

import com.my.chen.fabric.app.domain.Org;
import com.my.chen.fabric.app.service.LeagueService;
import com.my.chen.fabric.app.service.OrgService;
import com.my.chen.fabric.app.service.PeerService;
import com.my.chen.fabric.app.util.DateUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 *
 * @author : Aberic 【2018/6/4 15:01】
 */
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

    @PostMapping(value = "submit")
    public ModelAndView submit(@ModelAttribute Org org,
                               @RequestParam("intent") String intent,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam("id") int id) {
        switch (intent) {
            case "add":
                orgService.add(org, file);
                break;
            case "edit":
                org.setId(id);
                orgService.update(org, file);
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
            org.setOrdererCount(orgService.countById(org.getId()));
            org.setPeerCount(peerService.countById(org.getId()));
            org.setLeagueName(leagueService.getById(org.getLeagueId()).getName());
            org.setDate(DateUtil.getTimeStr(org.getUpdateTime()));
        }
        modelAndView.addObject("orgs", orgs);
        return modelAndView;
    }

}
