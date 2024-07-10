package org.example.controller;


import org.example.MQTT.MqttServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "publish")
public class PublishController {
    @Resource
    private MqttServer mqttServer;

    @RequestMapping(value = "test-publish")
    public String testPublish(
            @RequestParam(value = "topic") String topic,
            @RequestParam(value = "msg") String msg,
            @RequestParam(value = "qos") int qos) {
        mqttServer.SendMQTTMessage(topic, msg, qos);
        return "topic: " + topic + ", msg: " + msg + ", qos: " + qos;
    }

    @RequestMapping(value = "test-subscribe")
    public String testSubscribe(
            @RequestParam(value = "topic") String topic,
            @RequestParam(value = "qos") int qos) {
        mqttServer.init(topic, qos);
        return "Topic: " + topic + ", Qos: " + qos + " is subscribed.";
    }

}
