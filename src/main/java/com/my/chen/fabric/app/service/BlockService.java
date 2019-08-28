package com.my.chen.fabric.app.service;

import com.my.chen.fabric.app.dao.BlockMapper;
import com.my.chen.fabric.app.dao.ChannelMapper;
import com.my.chen.fabric.app.dao.PeerMapper;
import com.my.chen.fabric.app.domain.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenwei
 * @version 1.0
 * @date 2019/8/15
 * @description
 */

@Service
public class BlockService {

    @Resource
    private BlockMapper blockMapper;
    @Resource
    private PeerMapper peerMapper;
    @Resource
    private ChannelMapper channelMapper;


    public int add(Block block) {
        blockMapper.save(block);
        return 1;
    }


    @Transactional(rollbackFor = Exception.class, timeout = 5)
    public int addBlockList(List<Block> list) {
        list.forEach(t -> add(t));
        return 1;
    }

    public List<Block> getByChannelId(int channelId) {
        List<Block> list = blockMapper.findByChannelId(channelId);
        return list;
    }


    public Page<Block> getAllBlocks(int channelId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");

        Specification specification = (Specification<Block>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (channelId > 0) {
                predicates.add(criteriaBuilder.equal(root.get("channelId").as(Integer.class), channelId));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Block> pageList = blockMapper.findAll(specification, pageable);
        return pageList;

    }


}
