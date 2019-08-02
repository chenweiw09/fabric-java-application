package com.my.chen.fabric.app.dao;

import com.my.chen.fabric.app.domain.Org;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作者：Aberic on 2018/6/9 13:53
 * 邮箱：abericyang@gmail.com
 */
@Repository
public interface OrgMapper extends PagingAndSortingRepository<Org, Integer>, JpaSpecificationExecutor<Org> {


    int countByLeagueId( int leagueId);

    List<Org> findByLeagueId(int leagueId);


}
