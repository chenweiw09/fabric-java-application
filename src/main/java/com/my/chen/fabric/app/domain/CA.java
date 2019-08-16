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
@Table(name = "ca")
public class CA {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "id_Sequence")
    @SequenceGenerator(name = "id_Sequence", sequenceName = "ID_SEQ")
    @Column(name = "id")
    private int id;

    @Column(length = 40)
    private String name;

    @Column(columnDefinition="TEXT")
    private String sk;

    @Column(columnDefinition="TEXT")
    private String certificate;

    @Column(length = 128)
    private String flag; // optional

    @Column(length = 9)
    private int peerId;

    @Column(length = 20)
    private long createTime;

    @Column(length = 20)
    private long updateTime;

    private String peerName; // optional
    private String orgName; // optional
    private String leagueName; // optional
}
