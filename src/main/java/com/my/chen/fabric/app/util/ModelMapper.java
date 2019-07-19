package com.my.chen.fabric.app.util;

import org.modelmapper.convention.MatchingStrategies;

/**
 * @author chenwei
 * @version 1.0
 * @date 2018/10/24
 * @description
 */
public class ModelMapper {

    private static org.modelmapper.ModelMapper modelMapper;

    static {
        modelMapper = new org.modelmapper.ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }


    public static <S, T> T map(S source, Class<T> targetclass){
        return modelMapper.map(source, targetclass);
    }

    public static <S, T> T map(S source, T target){
        modelMapper.map(source,target);
        return target;
    }
}
