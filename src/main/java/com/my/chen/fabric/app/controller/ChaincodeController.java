package com.my.chen.fabric.app.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.my.chen.fabric.app.domain.*;
import com.my.chen.fabric.app.dto.Api;
import com.my.chen.fabric.app.dto.State;
import com.my.chen.fabric.app.dto.Trace;
import com.my.chen.fabric.app.service.*;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 描述：
 *
 */
@CrossOrigin
@RestController
@RequestMapping("chaincode")
public class ChaincodeController {

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
    @Resource
    private StateService stateService;
    @Resource
    private TraceService traceService;
    @Resource
    private Environment env;

    @Resource
    private CAService caService;

    @PostMapping(value = "submit")
    public ModelAndView submit(@ModelAttribute Chaincode chaincode,
                               @RequestParam("intent") String intent,
                               @RequestParam(value = "sourceFile", required = false) MultipartFile sourceFile,
                               @RequestParam("id") int id) {
        switch (intent) {
            case "add":
                chaincodeService.add(chaincode);
                break;
            case "edit":
                chaincodeService.update(chaincode);
                break;
            case "install":
                Channel channel = channelService.get(chaincode.getChannelId());
                Peer peer = peerService.get(channel.getPeerId());
                Org org = orgService.get(peer.getOrgId());
                League league = leagueService.getById(org.getLeagueId());
                chaincode.setLeagueName(league.getName());
                chaincode.setOrgName(org.getName());
                chaincode.setPeerName(peer.getName());
                chaincode.setChannelName(channel.getName());
                chaincodeService.install(chaincode, sourceFile);
                break;
        }
        return list();
    }

    @GetMapping(value = "add")
    public ModelAndView add() {
        ModelAndView modelAndView = new ModelAndView("chaincodeSubmit");
        modelAndView.addObject("intentLarge", "新建合约");
        modelAndView.addObject("intentLittle", "新建");
        modelAndView.addObject("submit", "新增");
        modelAndView.addObject("intent", "add");
        modelAndView.addObject("chaincode", new Chaincode());
        modelAndView.addObject("channels", getChannelFullList());
        return modelAndView;
    }

    @GetMapping(value = "install")
    public ModelAndView install() {
        ModelAndView modelAndView = new ModelAndView("chaincodeInstall");
        modelAndView.addObject("intentLarge", "安装合约");
        modelAndView.addObject("intentLittle", "安装");
        modelAndView.addObject("submit", "安装");
        modelAndView.addObject("intent", "install");
        modelAndView.addObject("chaincode", new Chaincode());
        modelAndView.addObject("channels", getChannelFullList());
        modelAndView.addObject("cas", caService.listFullCA());
        return modelAndView;
    }


    @GetMapping(value = "instantiate")
    public ModelAndView instantiate(@RequestParam("chaincodeId") int chaincodeId) {
        ModelAndView modelAndView = new ModelAndView("chaincodeInstantiate");
        modelAndView.addObject("intentLarge", "实例化合约");
        modelAndView.addObject("intentLittle", "实例化");
        modelAndView.addObject("submit", "实例化");
        modelAndView.addObject("chaincodeId", chaincodeId);
        modelAndView.addObject("cas", chaincodeService.getCAs(chaincodeId));
        Api apiInstantiate = new Api("实例化智能合约", Api.Intent.INSTANTIATE.getIndex());

        modelAndView.addObject("api", apiInstantiate);
        return modelAndView;
    }

    @PostMapping(value = "instantiate")
    public ModelAndView instantiate(@ModelAttribute Api api, @RequestParam("chaincodeId") int id) {
        Chaincode chaincode = chaincodeService.get(id);
        Channel channel = channelService.get(chaincode.getChannelId());
        Peer peer = peerService.get(channel.getPeerId());
        Org org = orgService.get(peer.getOrgId());
        League league = leagueService.getById(org.getLeagueId());
        chaincode.setLeagueName(league.getName());
        chaincode.setOrgName(org.getName());
        chaincode.setPeerName(peer.getName());
        chaincode.setChannelName(channel.getName());
        chaincode.setFlag(api.getFlag());
        chaincodeService.instantiate(chaincode, Arrays.asList(api.exec.split(",")));
        return new ModelAndView(new RedirectView("list"));
    }

    @GetMapping(value = "upgrade")
    public ModelAndView upgrade(@RequestParam("id") int chaincodeId) {
        ModelAndView modelAndView = new ModelAndView("chaincodeUpgrade");
        modelAndView.addObject("intentLarge", "合约升级");
        modelAndView.addObject("intentLittle", "合约升级");
        modelAndView.addObject("submit", "升级");
        modelAndView.addObject("intent", "upgrade");
        modelAndView.addObject("init", true);
        modelAndView.addObject("chaincode", chaincodeService.get(chaincodeId));
        modelAndView.addObject("cas", chaincodeService.getCAs(chaincodeId));
        modelAndView.addObject("api", new Api("升级智能合约", Api.Intent.UPGRADE.getIndex()));
        return modelAndView;
    }


    @PostMapping(value = "upgrade")
    public ModelAndView upgrade(@ModelAttribute Chaincode chaincode, @ModelAttribute Api api,
                                @RequestParam(value = "sourceFile", required = false) MultipartFile sourceFile,
                                @RequestParam("id") int id){

        Chaincode chaincode1 = chaincodeService.get(id);
        chaincode1 = chaincodeService.resetChaincode(chaincode1);
        chaincode1.setVersion(chaincode.getVersion());
        chaincode1.setFlag(api.getFlag());
        List<String> strArray = Arrays.asList(api.getExec().split(","));
        chaincodeService.upgrade(chaincode1, sourceFile, strArray);
        return new ModelAndView(new RedirectView("list"));
    }

    @GetMapping(value = "edit")
    public ModelAndView edit(@RequestParam("id") int id) {
        ModelAndView modelAndView = new ModelAndView("chaincodeSubmit");
        modelAndView.addObject("intentLarge", "编辑合约");
        modelAndView.addObject("intentLittle", "编辑");
        modelAndView.addObject("submit", "修改");
        modelAndView.addObject("intent", "edit");
        Chaincode chaincode = chaincodeService.get(id);
        Peer peer = peerService.get(channelService.get(chaincode.getChannelId()).getPeerId());
        Org org = orgService.get(peer.getOrgId());
        League league = leagueService.getById(org.getLeagueId());
        chaincode.setPeerName(peer.getName());
        chaincode.setOrgName(org.getName());
        chaincode.setLeagueName(league.getName());
        List<Channel> channels = channelService.listByPeerId(peer.getId());
        for (Channel channel : channels) {
            channel.setPeerName(peer.getName());
            channel.setOrgName(org.getName());
            channel.setLeagueName(league.getName());
        }
        modelAndView.addObject("chaincode", chaincode);
        modelAndView.addObject("channels", channels);
        return modelAndView;
    }


    @GetMapping(value = "verify")
    public ModelAndView verify(@RequestParam("chaincodeId") int chaincodeId) {
        ModelAndView modelAndView = new ModelAndView("chaincodeVerify");
        modelAndView.addObject("intentLarge", "验证合约");
        modelAndView.addObject("intentLittle", "验证");
        modelAndView.addObject("submit", "验证");
        modelAndView.addObject("chaincodeId", chaincodeId);

        List<Api> apis = new ArrayList<>();
        Api api = new Api("查询当前链信息", Api.Intent.INFO.getIndex());
        Api apiInvoke = new Api("执行智能合约", Api.Intent.INVOKE.getIndex());
        Api apiQuery = new Api("查询智能合约", Api.Intent.QUERY.getIndex());

        Api apiHash = new Api("根据交易hash查询区块", Api.Intent.HASH.getIndex());
        Api apiNumber = new Api("根据交易区块高度查询区块", Api.Intent.NUMBER.getIndex());
        Api apiTxid = new Api("根据交易ID查询区块", Api.Intent.TXID.getIndex());
        apis.add(api);
        apis.add(apiInvoke);
        apis.add(apiQuery);
        apis.add(apiHash);
        apis.add(apiNumber);
        apis.add(apiTxid);

        Api apiIntent = new Api();
        modelAndView.addObject("apis", apis);
        modelAndView.addObject("apiIntent", apiIntent);
        modelAndView.addObject("cas",caService.listFullCA());
        return modelAndView;
    }

    @PostMapping(value = "verify")
    public ModelAndView verify(@ModelAttribute Api api, @RequestParam("chaincodeId") int id) {
        ModelAndView modelAndView = new ModelAndView("chaincodeResult");
        Api.Intent intent = Api.Intent.get(api.index);
        JSONObject result = new JSONObject();
        String url = String.format("http://localhost:%s/%s", env.getProperty("server.port"), intent.getApiUrl());
        switch (intent) {
            case INVOKE:
                State state = getState(id, api);
                result = stateService.invoke(state);
                modelAndView.addObject("jsonStr", formatState(state));
                modelAndView.addObject("method", "POST");
                break;
            case QUERY:
                state = getState(id, api);
                result = stateService.query(state);
                modelAndView.addObject("jsonStr", formatState(state));
                modelAndView.addObject("method", "POST");
                break;
            case INFO:
                result = traceService.queryBlockChainInfo(id,api.getKey(), api.getFlag());
                modelAndView.addObject("jsonStr", "");
                modelAndView.addObject("method", "GET");
                break;
            case HASH:
                Trace trace = getTrace(id, api);
                result = traceService.queryBlockByHash(trace);
                modelAndView.addObject("jsonStr", formatTrace(trace));
                modelAndView.addObject("method", "POST");
                break;
            case NUMBER:
                trace = getTrace(id, api);
                result = traceService.queryBlockByNumber(trace);
                modelAndView.addObject("jsonStr", formatTrace(trace));
                modelAndView.addObject("method", "POST");
                break;
            case TXID:
                trace = getTrace(id, api);
                result = traceService.queryBlockByTransactionID(trace);
                modelAndView.addObject("jsonStr", formatTrace(trace));
                modelAndView.addObject("method", "POST");
                break;
        }
        modelAndView.addObject("result", result);
        modelAndView.addObject("api", api);
        modelAndView.addObject("url", url);
        return modelAndView;
    }

    @GetMapping(value = "list")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("chaincodes");
        List<Chaincode> chaincodes = chaincodeService.listAll();
        for (Chaincode chaincode : chaincodes) {
            chaincode.setChannelName(channelService.get(chaincode.getChannelId()).getName());
        }
        modelAndView.addObject("chaincodes", chaincodes);
        return modelAndView;
    }

    @GetMapping(value = "del")
    public ModelAndView delChainCode(@RequestParam("chaincodeId") int chaincodeId){
        chaincodeService.delete(chaincodeId);
        return new ModelAndView(new RedirectView("list"));
    }

    private List<Channel> getChannelFullList() {
        List<Channel> channels = channelService.listAll();
        for (Channel channel : channels) {
            Peer peer = peerService.get(channel.getPeerId());
            channel.setPeerName(peer.getName());
            Org org = orgService.get(peer.getOrgId());
            channel.setOrgName(org.getName());
            League league = leagueService.getById(org.getLeagueId());
            channel.setLeagueName(league.getName());
        }
        return channels;
    }

    private State getState(int id, Api api) {
        State state = new State();
        state.setId(id);

        String[] str = api.exec.trim().replaceAll("\\[","").replaceAll("\\]","").split(",");
        state.setStrArray(Arrays.asList(str));
        state.setFlag(api.getFlag());
        return state;
    }

    private String formatState(State state) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", state.getId());
        JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(state.getStrArray()));
        jsonObject.put("strArray", jsonArray);
        return jsonObject.toJSONString();
    }

    private Trace getTrace(int id, Api api) {
        Trace trace = new Trace();
        trace.setId(id);
        trace.setTrace(api.exec.trim());
        trace.setFlag(api.getFlag());
        return trace;
    }

    private String formatTrace(Trace trace) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", trace.getId());
        jsonObject.put("trace", trace.getTrace());
        jsonObject.put("caFlag",trace.getFlag());
        return jsonObject.toJSONString();
    }

}
