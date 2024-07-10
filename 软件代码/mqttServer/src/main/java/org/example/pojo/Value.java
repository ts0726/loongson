package org.example.pojo;

import java.util.Date;

public class Value {
    private Integer id;

    private Date time;

    private Integer deviceData;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Integer getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(Integer deviceData) {
        this.deviceData = deviceData;
    }
}