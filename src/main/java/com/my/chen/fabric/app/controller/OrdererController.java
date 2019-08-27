package com.my.chen.fabric.app.controller;

import com.my.chen.fabric.app.domain.League;
import com.my.chen.fabric.app.domain.Orderer;
import com.my.chen.fabric.app.domain.Org;
import com.my.chen.fabric.app.service.LeagueService;
import com.my.chen.fabric.app.service.OrdererService;
import com.my.chen.fabric.app.service.OrgService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import java.util.List;

/**
 * 描述：
 */
@CrossOrigin
@RestController
@RequestMapping("orderer")
public class OrdererController {

    @Resource
    private OrdererService ordererService;
    @Resource
    private OrgService orgService;
    @Resource
    private LeagueService leagueService;

    @PostMapping(value = "submit")
    public ModelAndView submit(@ModelAttribute Orderer orderer,
                               @RequestParam("intent") String intent,
                               @RequestParam("serverCrtFile") MultipartFile serverCrtFile,
                               @RequestParam("clientCertFile") MultipartFile clientCertFile,
                               @RequestParam("clientKeyFile") MultipartFile clientKeyFile,
                               @RequestParam("id") int id) {
        switch (intent) {
            case "add":
                ordererService.add(orderer,serverCrtFile, clientCertFile, clientKeyFile);
                break;
            case "edit":
                orderer.setId(id);
                ordererService.update(orderer,serverCrtFile, clientCertFile, clientKeyFile);
                break;
        }
        return new ModelAndView(new RedirectView("list"));
    }

    @GetMapping(value = "add")
    public ModelAndView add() {
        ModelAndView modelAndView = new ModelAndView("ordererSubmit");
        modelAndView.addObject("intentLarge", "新建排序服务");
        modelAndView.addObject("intentLittle", "新建");
        modelAndView.addObject("submit", "新增");
        modelAndView.addObject("intent", "add");
        modelAndView.addObject("orderer", new Orderer());
        modelAndView.addObject("orgs", orgService.getAllPartOrg());
        return modelAndView;
    }

    @GetMapping(value = "edit")
    public ModelAndView edit(@RequestParam("id") int id) {
        ModelAndView modelAndView = new ModelAndView("ordererSubmit");
        modelAndView.addObject("intentLarge", "编辑排序服务");
        modelAndView.addObject("intentLittle", "编辑");
        modelAndView.addObject("submit", "修改");
        modelAndView.addObject("intent", "edit");
        Orderer orderer = ordererService.get(id);
        League league = leagueService.getById(orgService.get(orderer.getOrgId()).getLeagueId());
        List<Org> orgs = orgService.listByLeagueId(league.getId());
        for (Org org : orgs) {
            org.setLeagueName(league.getName());
        }
        modelAndView.addObject("orderer", orderer);
        modelAndView.addObject("orgs", orgs);
        return modelAndView;
    }

    @GetMapping(value = "list")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("orderers");
        List<Orderer> orderers = ordererService.listAll();
        for (Orderer orderer : orderers) {
            Org org = orgService.get(orderer.getOrgId());
            orderer.setOrgName(org.getName());
            orderer.setLeagueName(leagueService.getById(org.getLeagueId()).getName());
        }
        modelAndView.addObject("orderers", orderers);
        return modelAndView;
    }

    @GetMapping(value = "del")
    public ModelAndView delOrderer(@RequestParam("id") int id){
        ordererService.del(id);
        return new ModelAndView(new RedirectView("list"));
    }

}
