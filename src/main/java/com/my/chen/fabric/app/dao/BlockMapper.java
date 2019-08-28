package com.my.chen.fabric.app.dao;

import com.my.chen.fabric.app.domain.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/15
 * @description
 */
@Repository
public interface BlockMapper extends PagingAndSortingRepository<Block, Integer>, JpaSpecificationExecutor<Block> {

    List<Block> findByChannelId(int channelId);

}
