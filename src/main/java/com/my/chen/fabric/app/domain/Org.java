package com.my.chen.fabric.app.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "org")
public class Org implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "id_Sequence")
    @SequenceGenerator(name = "id_Sequence", sequenceName = "ID_SEQ")
    @Column(name = "id")
    private int id; // required

    @Column(length = 60)
    private String name; // required

    @Column
    private boolean tls; // required

    @Column
    private String username; // required

    @Column
    private String cryptoConfigDir; // required

    @Column
    private String mspId; // required

    @Column
    private String domainName; // required

    @Column
    private String ordererDomainName; // required

    @Column
    private int leagueId; // required

    @Column
    private String leagueName; // required

    @Column
    private int peerCount; // required

    @Column
    private int ordererCount; // required


    @Column
    private long createTime;

    @Column
    private long updateTime;
}
