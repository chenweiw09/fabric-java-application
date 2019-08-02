package com.my.chen.fabric.app.dao;

import com.my.chen.fabric.app.domain.League;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * 作者：Aberic on 2018/6/9 13:53
 * 邮箱：abericyang@gmail.com
 */
@Repository
public interface LeagueMapper extends PagingAndSortingRepository<League, Integer>, JpaSpecificationExecutor<League> {

}
