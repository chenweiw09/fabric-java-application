package com.my.chen.fabric.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/15
 * @description
 */
@Data
@AllArgsConstructor
public class Key {

    private String privateKey;
    private String publicKey;

}
