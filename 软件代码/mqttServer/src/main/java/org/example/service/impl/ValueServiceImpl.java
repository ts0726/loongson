package org.example.service.impl;

import org.example.dao.ValueMapper;
import org.example.pojo.Value;
import org.example.pojo.ValueExample;
import org.example.service.ValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ValueServiceImpl implements ValueService {

    @Autowired
    ValueMapper valueMapper;

    @Override
    public List<Value> getCurrentValue() {
        List<Value> values = valueMapper.selectCurrentValue();
        Collections.reverse(values);
        return values;
    }
}
