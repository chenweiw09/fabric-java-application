package com.my.chen.fabric.app.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name="peer")
public class Peer implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "id_Sequence")
    @SequenceGenerator(name = "id_Sequence", sequenceName = "ID_SEQ")
    @Column(name = "id")
    private int id; // required

    @Column(length = 60)
    private String name; // required

    @Column
    private String eventHubName; // required

    @Column
    private String location; // required

    @Column
    private String eventHubLocation; // required

    @Column
    private boolean eventListener; // required

    @Column
    private int orgId; // required

    @Column
    private String orgName; // required

    @Column
    private int channelCount; // required

    @Column
    private long createTime;

    @Column
    private long updateTime;
}
