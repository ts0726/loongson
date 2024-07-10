package org.example.MQTT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.example.dao.ValueMapper;
import org.example.pojo.Value;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;


public class PushCallback implements MqttCallback {

    private final ValueMapper valueMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(PushCallback.class);

    private final MqttServer mqttServer;


    public PushCallback(MqttServer mqttServer, ValueMapper valueMapper) {
        this.valueMapper = valueMapper;
        this.mqttServer = mqttServer;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        LOGGER.info("CONNECTION LOST");
        mqttServer.subscribeConnect();

        while (true) {
            try {
                Thread.sleep(1000);
                break;
            } catch (Exception e) {
                LOGGER.error("CONNECT FAILED: " + e);
                LOGGER.info("CONNECT FAILED, RETRYING...");
            }
        }
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        try {
            String result = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
            LOGGER.info("TOPIC: " + s);
            LOGGER.info("QOS: " + mqttMessage.getQos());
            LOGGER.info("MESSAGE: " + result);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readValue(result, JsonNode.class);
            int deviceValue = jsonNode.get("value").asInt();
            Value value = new Value();
            value.setDeviceData(deviceValue);
            value.setTime(new Date());
            if (valueMapper == null) {
                LOGGER.error("VALUE MAPPER IS NULL");
                return;
            }
            valueMapper.insert(value);
            LOGGER.info("DEVICE VALUE: " + deviceValue);
        } catch (Exception e) {
            LOGGER.error("Error processing message", e);
            throw e;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        LOGGER.info("DELIVERY Status: " + iMqttDeliveryToken.isComplete());
    }
}
