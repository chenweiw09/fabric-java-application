package com.my.chen.fabric.app.dao;


import com.my.chen.fabric.app.domain.Chaincode;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 默认的方法有：
 * save(S s)
 * saveAll(Iterable<S> iterable)
 * saveAndFlush(S s)
 * <p>
 * delete(S s)
 * deleteById(long id)
 * deleteAll()
 * deleteAll(Iterable<S> iterable)
 * <p>
 * findAll()
 * findAll(Sort sort)
 * findAll(Pageable pageable)
 * findAllById(Iterable<Long> ids)
 * findById(Long id)
 * findOne(Example<S> s)
 *
 * @param
 * @return
 */

@Repository
public interface ChaincodeMapper extends PagingAndSortingRepository<Chaincode, Integer>, JpaSpecificationExecutor<Chaincode> {



    int countByChannelId(int channelId);


//    @Query("select id,name,path,version,proposal_wait_time,invoke_wait_time,channel_id,date,source,policy from chaincode " +
//            "where name=:#{c.name} and path=:#{c.path} and version=:#{c.version} and channel_id=:#{c.channelId}")
//    Chaincode check(@Param("c") Chaincode chaincode);

    Chaincode findByNameAndVersionAndChannelId(String name, String version, int channelId);


    List<Chaincode> findByChannelId(int channelId);

}
