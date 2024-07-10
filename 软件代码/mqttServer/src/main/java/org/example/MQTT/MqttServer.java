package org.example.MQTT;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.example.config.MQTTConfig;
import org.example.dao.ValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class MqttServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttServer.class);

    //订阅客户端
    private MqttClient subscribeClient;

    //发布客户端
    private MqttClient publishClient;

    //主题
    private MqttTopic topic;

    //消息
    public MqttMessage message;

    @Autowired
    private MqttConnect mqttConnect;

    @Autowired
    private ValueMapper valueMapper;

    @Autowired
    private MQTTConfig mqttConfig;

    public MqttServer() {
        LOGGER.info("7777 ONLINE\nCONNECT READY");
    }

    /**
     * 发布连接
     */
    public MqttClient publishConnect() {
        try {
            if (publishClient == null) {
                publishClient = new MqttClient(mqttConfig.getHost(), mqttConfig.getClientId(), new MemoryPersistence());
            }

            MqttConnectOptions options = mqttConnect.getOptions();

            if (!publishClient.isConnected()) {
                publishClient.connect(options);
            } else {
                //重新连接更新配置
                publishClient.disconnect();
                publishClient.connect(options);
            }
            LOGGER.info("PUBLISH CONNECT SUCCESS");
        } catch (Exception e) {
            LOGGER.error("PUBLISH CONNECT ERROR: " + e);
        }
        return publishClient;
    }

    /**
     * 订阅连接
     * 断线重连方法，如果是持久订阅，重连时不需要再次订阅
     * 如果是非持久订阅，重连是需要重新订阅主题 取决于options.setCleanSession(true);
     * true为非持久订阅
     */
    public void subscribeConnect() {
        try {
            if (subscribeClient == null) {
                subscribeClient = new MqttClient(mqttConfig.getHost(), mqttConfig.getClientId(), new MemoryPersistence());
                subscribeClient.setCallback(new PushCallback(MqttServer.this, valueMapper));
            }

            MqttConnectOptions options = mqttConnect.getOptions();

            if (!subscribeClient.isConnected()) {
                subscribeClient.connect(options);
            } else {
                //重新连接更新配置
                subscribeClient.disconnect();
                subscribeClient.connect(options);
            }
            LOGGER.info("SUBSCRIBE CONNECT SUCCESS");
        } catch (Exception e) {
            LOGGER.error("SUBSCRIBE CONNECT ERROR: " + e);
        }
    }

    /**
     * 发送组装好的消息
     * @param topic 主题
     * @param message 消息
     * @return boolean 发送成功返回true
     */
    public boolean publish(MqttTopic topic, MqttMessage message) {

        MqttDeliveryToken token = null;

        try {
            token = topic.publish(message);
            token.waitForCompletion();
            boolean flag = token.isComplete();

            StringBuilder sbf = new StringBuilder(200);
            sbf.append("Topic: ").append(topic.getName());
            sbf.append("Message: ");
            if (flag) {
                sbf.append("SUCCESS!Message is: ").append(new String(message.getPayload()));
            } else {
                sbf.append("FAILED with an ERROR!");
            }
            LOGGER.info(sbf.toString());
        } catch (MqttException e) {
            LOGGER.error("PUBLISH ERROR: " + e);
        }

        assert token != null;
        return token.isComplete();
    }

    /**
     * 发送消息
     * @param topic 主题
     * @param data 消息内容
     * @param qos 消息级别
     */
    public void SendMQTTMessage(String topic, String data, int qos) {
        try {
            this.publishClient = publishConnect();
            this.topic = this.publishClient.getTopic(topic);
            message = new MqttMessage();
            message.setQos(qos);
            //Retained为true时，服务器会将最后一次发布的消息保留在服务器上，当有新的订阅者订阅该主题时，服务器会将最后一次保留的消息发送给订阅者
            //Retained为false时，服务器不会保留最后一次发布的消息
            message.setRetained(false);
            message.setPayload(data.getBytes());
            if (publish(this.topic, message)) {
                LOGGER.info("PUBLISH SUCCESS");
            } else {
                LOGGER.error("PUBLISH FAILED");
            }
        } catch (Exception e) {
            LOGGER.error("PUBLISH ERROR: " + e);
        }
    }

    /**
     * 订阅主题
     * @param topic 主题
     * @param qos 消息级别
     */
    public void init(String topic, int qos) {
        subscribeConnect();
        try {
            subscribeClient.subscribe(topic, qos);
        } catch (MqttException e) {
            LOGGER.error("SUBSCRIBE ERROR: " + e);
        }
    }

    /**
     * 取消订阅
     * @param topic 主题
     */
    public void unionInit(String topic) {
        subscribeConnect();
        try {
            subscribeClient.unsubscribe(topic);
        } catch (MqttException e) {
            LOGGER.error("SUBSCRIBE ERROR: " + e);
        }
    }

}
