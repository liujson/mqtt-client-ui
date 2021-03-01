package cn.liujson.lib.mqtt.api;

/**
 * 接收消息接口
 *
 * @author liujson
 * @date 2021/2/23.
 */
public interface IMQTTMessageReceiver {

    /**
     * 接收到消息
     *
     * @param topic   主题
     * @param message 消息内容
     */
    void onReceive(String topic, byte[] body);

}
