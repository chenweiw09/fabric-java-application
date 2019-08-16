package com.my.chen.fabric.app.service;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/14
 * @description
 */
@Service
public class FileManageService {

    @Resource
    private Environment env;


    public String getLeaguePath(String leagueName) {
        String newPath = String.format("%s%s%s",
                env.getProperty("config.dir"),
                File.separator,
                leagueName);

        return newPath;
    }

    public String getOrgPath(String leagueName, String orgName) {
        String parentPath = String.format("%s%s%s%s%s",
                env.getProperty("config.dir"),
                File.separator, leagueName,
                File.separator, orgName);
        return parentPath;
    }


    public String getOrdererPath(String leagueName, String orgName, String ordererName){
        String path = String.format("%s%s%s%s%s%s%s",
                env.getProperty("config.dir"),
                File.separator, leagueName,
                File.separator, orgName,
                File.separator, ordererName
        );
        return path;
    }


    public String getPeerPath(String leagueName, String orgName, String peerName) {
        String path = String.format("%s%s%s%s%s%s%s",
                env.getProperty("config.dir"),
                File.separator, leagueName,
                File.separator, orgName,
                File.separator, peerName
        );
        return path;
    }

    public String getChainCodePath(String leagueName, String orgName, String peerName, String channelName) {
        String chaincodeSource = String.format("%s%s%s%s%s%s%s%s%s%schaincode",
                env.getProperty("config.dir"),
                File.separator,
                leagueName,
                File.separator,
                orgName,
                File.separator,
                peerName,
                File.separator,
                channelName,
                File.separator);
        return chaincodeSource;
    }
}
