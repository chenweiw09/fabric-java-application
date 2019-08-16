package com.my.chen.fabric.app.domain;

import lombok.Data;

import javax.persistence.*;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/15
 * @description
 */
@Data
@Entity
@Table(name = "block")
public class Block {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "id_Sequence")
    @SequenceGenerator(name = "id_Sequence", sequenceName = "ID_SEQ")
    @Column(name = "id")
    private int id;

    @Column(length = 9)
    private int channelId;

    @Column(length = 9)
    private int height;

    @Column(length = 256)
    private String dataHash;

    @Column(length = 256)
    private String calculatedHash;

    @Column(length = 256)
    private String previousHash;

    @Column(length = 4)
    private int envelopeCount;

    @Column(length = 5)
    private int txCount;

    @Column(length = 5)
    private int rwSetCount;

    @Column(length = 32)
    private String timestamp;

    @Column(length = 8)
    private int calculateDate;

    @Column(length = 20)
    private long createTime;

    @Column(length = 20)
    private long updateTime;

    private String peerChannelName;

}
