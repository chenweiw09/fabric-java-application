package com.my.chen.fabric.app.dto;

import lombok.Data;
import org.hyperledger.fabric.sdk.TransactionRequest;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/7/18
 * @description
 */
@Data
public class ChainCodeDeployDto {

    private String chainCodeName;

    private String chainCodePath;

    private String codeSourcePath;

    private TransactionRequest.Type language;

    private String version;

    private String[] arguments;

    private String initFunctionName;

}
