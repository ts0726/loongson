package org.example.MQTT;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.example.config.MQTTConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MqttConnect {

    @Autowired
    private MQTTConfig config;

    public MqttConnect(MQTTConfig config) {
        this.config = config;
    }

    public MqttConnectOptions getOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(config.isCleanSession());
        options.setUserName(config.getUsername());
        options.setPassword(config.getPassword().toCharArray());
        options.setConnectionTimeout(config.getConnectionTimeout());
        options.setKeepAliveInterval(config.getKeepAlive());
        return options;
    }

//    public MqttConnectOptions getOptions(MqttConnectOptions options) {
//        options.setCleanSession(config.isCleanSession());
//        options.setUserName(config.getUsername());
//        options.setPassword(config.getPassword().toCharArray());
//        options.setConnectionTimeout(config.getConnectionTimeout());
//        options.setKeepAliveInterval(config.getKeepAlive());
//        return options;
//    }

}
