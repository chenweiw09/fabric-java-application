package com.my.chen.fabric.app.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "channel")
public class Channel implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "id_Sequence")
    @SequenceGenerator(name = "id_Sequence", sequenceName = "ID_SEQ")
    @Column(name = "id")
    private int id; // required

    @Column(length = 60)
    private String name; // required

    @Column
    private int peerId; // required

    @Column(length = 30)
    private String date; // optional

    @Column
    private String peerName; // optional

    @Column
    private String orgName; // optional

    @Column
    private String leagueName; // optional

    @Column
    private int chaincodeCount; // optional

    @Column
    private long createTime;

    @Column
    private long updateTime;

}
