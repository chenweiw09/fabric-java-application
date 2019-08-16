package com.my.chen.fabric.app.controller;

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

    @PostMapping(value = "submit")
    public ModelAndView submit(@ModelAttribute CA ca,
                               @RequestParam("intent") String intent,
                               @RequestParam("skFile") MultipartFile skFile,
                               @RequestParam("certificateFile") MultipartFile certificateFile) {
        switch (intent) {
            case "add":
                caService.add(ca, skFile, certificateFile);
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
        modelAndView.addObject("intentLittle", SpringUtil.get("enter"));
        modelAndView.addObject("submit", SpringUtil.get("submit"));
        modelAndView.addObject("intent", "add");
        modelAndView.addObject("ca", new CA());
        modelAndView.addObject("peers", caService.getFullPeers());
        return modelAndView;
    }

    @GetMapping(value = "edit")
    public ModelAndView edit(@RequestParam("id") int id) {
        ModelAndView modelAndView = new ModelAndView("caSubmit");
        modelAndView.addObject("intentLittle", SpringUtil.get("edit"));
        modelAndView.addObject("submit", SpringUtil.get("modify"));
        modelAndView.addObject("intent", "edit");

        CA ca = caService.get(id);
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
}
