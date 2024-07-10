package org.example.Test;

import org.example.dao.ValueMapper;
import org.example.pojo.Value;
import org.example.service.impl.ValueServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@MapperScan("org.example.dao")
public class IotTest {

    @Autowired
    ValueMapper valueMapper;

    @Autowired
    ValueServiceImpl valueServiceImple;

    @Test
    public void add() {
        Value value = new Value();
        value.setTime(new Date());
        value.setDeviceData(1);
        valueMapper.insert(value);
    }

    @Test
    public void select() {
        List<Value> values = valueServiceImple.getCurrentValue();
        for (Value value : values) {
            System.out.println(value.getDeviceData().toString());
        }
    }

}
