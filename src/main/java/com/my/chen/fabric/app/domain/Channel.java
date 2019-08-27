package com.my.chen.fabric.app.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

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

    @Column(columnDefinition = "INT", length = 1)
    private boolean blockListener; // required

    @Column(length = 128)
    private String callbackLocation; // required

    @Column(length = 9)
    private int height;

    @Column
    private long createTime;

    @Column
    private long updateTime;

    private String peerName; // optional

    private String orgName; // optional

    private String leagueName; // optional

    private int chaincodeCount; // optional

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Channel channel = (Channel) o;
        return id == channel.id && peerId == channel.peerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name, peerId, blockListener, callbackLocation, height, createTime, updateTime, peerName, orgName, leagueName, chaincodeCount);
    }
}
