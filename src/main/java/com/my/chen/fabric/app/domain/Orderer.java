package com.my.chen.fabric.app.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name="orderer")
public class Orderer implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "id_Sequence")
    @SequenceGenerator(name = "id_Sequence", sequenceName = "ID_SEQ")
    @Column(name = "id")
    private int id; // required

    @Column(length = 60)
    private String name; // required

    @Column
    private String location; // required


    // 平台的公钥
    @Column(length = 512)
    private String serverCrtPath;

    // 客户端的公钥
    @Column(length = 512)
    private String clientCertPath;

    // 客户端的私钥
    @Column(length = 512)
    private String clientKeyPath;

    @Column
    private int orgId; // required

    @Column
    private long createTime;

    @Column
    private long updateTime;


    private String orgName;

    private String leagueName;
}
