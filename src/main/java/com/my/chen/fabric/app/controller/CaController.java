package com.my.chen.fabric.app.controller;

import com.my.chen.fabric.app.domain.CA;
import com.my.chen.fabric.app.service.CAService;
import com.my.chen.fabric.app.service.PeerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/15
 * @description
 */
@CrossOrigin
@RestController
@RequestMapping("ca")
@Slf4j
public class CaController {

    @Resource
    private CAService caService;

    @Resource
    private PeerService peerService;

    @PostMapping(value = "submit")
    public ModelAndView submit(@ModelAttribute CA ca,
                               @RequestParam("intent") String intent,
                               @RequestParam("skFile") MultipartFile skFile,
                               @RequestParam("certificateFile") MultipartFile certificateFile) {
        switch (intent) {
            case "add":
                caService.addCa(ca, skFile, certificateFile);
                break;
            case "edit":
                caService.update(ca, skFile, certificateFile);
                break;
        }
        return new ModelAndView(new RedirectView("list"));
    }

    @GetMapping(value = "add")
    public ModelAndView add() {
        ModelAndView modelAndView = new ModelAndView("caSubmit");
        modelAndView.addObject("intentLarge", "新增CA");
        modelAndView.addObject("intentLittle", "新增");
        modelAndView.addObject("submit", "提交");
        modelAndView.addObject("intent", "add");
        modelAndView.addObject("ca", new CA());
        modelAndView.addObject("peers", peerService.listAll());
        return modelAndView;
    }

    @GetMapping(value = "edit")
    public ModelAndView edit(@RequestParam("id") int id) {
        ModelAndView modelAndView = new ModelAndView("caSubmit");
        modelAndView.addObject("intentLittle", "编辑");
        modelAndView.addObject("submit", "修改");
        modelAndView.addObject("intent", "edit");

        CA ca = caService.findById(id);
        modelAndView.addObject("ca", ca);
        modelAndView.addObject("peers", caService.getPeersByCA(ca));
        return modelAndView;
    }

    @GetMapping(value = "list")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("cas");
        modelAndView.addObject("cas", caService.listFullCA());
        return modelAndView;
    }

    @GetMapping(value = "delete")
    public ModelAndView delete(@RequestParam("id") int id) {
        caService.delete(id);
        return new ModelAndView(new RedirectView("list"));
    }

}
