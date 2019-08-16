package com.my.chen.fabric.app.dao;

import com.my.chen.fabric.app.domain.CA;
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
public interface CAMapper extends PagingAndSortingRepository<CA, Integer>, JpaSpecificationExecutor<CA> {

    CA findByNameAndPeerId(String name, int peerId);

    List<CA> findByPeerId(int peerId);

    CA findByFlag(String flag);

    int countByPeerId(int peerId);
}
