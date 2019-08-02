package com.my.chen.fabric.app.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 链码的展示信息
 */

@Data
@Entity
@Table(name = "chaincode")
public class Chaincode implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "id_Sequence")
    @SequenceGenerator(name = "id_Sequence", sequenceName = "ID_SEQ")
    @Column(name = "id")
    private int id;

    @Column(length = 32)
    private String name;

    @Column
    private String source;

    @Column
    private String path;

    @Column
    private String policy;

    @Column(length = 12)
    private String version;

    @Column
    private int proposalWaitTime;

    @Column
    private int invokeWaitTime;

    @Column
    private int channelId;

    @Column(length = 40)
    private String channelName;

    @Column(length = 40)
    private String peerName;

    @Column(length = 40)
    private String orgName;

    // 联盟名字
    @Column(length = 40)
    private String leagueName;

    @Column
    private long createTime;

    @Column
    private long updateTime;
}