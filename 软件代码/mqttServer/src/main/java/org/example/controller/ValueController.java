package org.example.controller;


import org.example.Utils.ResultData;
import org.example.pojo.Value;
import org.example.service.impl.ValueServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "value")
public class ValueController {

    @Autowired
    ValueServiceImpl valueServiceImpl;

    @RequestMapping(value = "getCurrentValues")
    public ResultData<List<Value>> getCurrentValues() {
        ResultData<List<Value>> resultData = new ResultData<>();
        List<Value> values = valueServiceImpl.getCurrentValue();
        resultData.setData(values);
        resultData.setCode(200);
        resultData.setMsg("current data");
        resultData.setSuccess(true);
        return resultData;
    }

}
