package com.my.chen.fabric.app.dto;

import lombok.Data;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/15
 * @description
 */
@Data
public class BlockDto {

    /** 序号，无实际意义 */
    private int index;
    /** 块高度 */
    private int num;
    private String peerName;
    private String channelName;
    private String calculatedBlockHash;
    private String date;
    private double percent;
    private String percentStr;
}
