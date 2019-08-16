package com.my.chen.fabric.app.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "league")
public class League implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "id_Sequence")
    @SequenceGenerator(name = "id_Sequence", sequenceName = "ID_SEQ")
    @Column(name = "id")
    private int id; // required

    @Column(length = 60)
    private String name; // required


    @Column
    private long createTime;

    @Column
    private long updateTime;

    private int orgCount; // required

}
