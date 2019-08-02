package com.my.chen.fabric.app.dao;

import com.my.chen.fabric.app.domain.Channel;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作者：Aberic on 2018/6/9 13:53
 * 邮箱：abericyang@gmail.com
 */
@Repository
public interface ChannelMapper extends PagingAndSortingRepository<Channel, Integer>, JpaSpecificationExecutor<Channel> {


//    @Modifying
//    @Query("update channel set name=:channelName where id=:id")
//    int updateChannelName(@Param("channelName")String channelName, @Param("id") int id);


    int countByPeerId(int peerId);


//    @Query("select id,name,peer_id,date from channel where name=:#{#c.name} and peer_id=:#{#c.peerId}")
    Channel findByNameAndPeerId(String name, int peerId);


    List<Channel> findByPeerId(int peerId);

}
