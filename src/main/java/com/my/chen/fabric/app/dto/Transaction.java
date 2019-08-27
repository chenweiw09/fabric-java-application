package com.my.chen.fabric.app.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 作者：Aberic on 2018/6/23 10:25
 * 邮箱：abericyang@gmail.com
 */
@Setter
@Getter
public class Transaction {

    /** 序号，无实际意义 */
    private int index;

    /** 块高度 */
    private int blockHeight;

    private int txCount;

    private String channelName;

    private String dataHash;

    private String previousDataHash;

    private String date;

}
