package org.example.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pojo.Value;
import org.example.pojo.ValueExample;
import org.springframework.stereotype.Component;


public interface ValueMapper {
    long countByExample(ValueExample example);

    int deleteByExample(ValueExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Value record);

    int insertSelective(Value record);

    List<Value> selectByExample(ValueExample example);

    List<Value> selectCurrentValue();

    Value selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Value record, @Param("example") ValueExample example);

    int updateByExample(@Param("record") Value record, @Param("example") ValueExample example);

    int updateByPrimaryKeySelective(Value record);

    int updateByPrimaryKey(Value record);
}