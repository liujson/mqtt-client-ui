package cn.liujson.lib.mqtt.service.refactor;


import cn.liujson.lib.mqtt.api.IMQTTMessageReceiver;

/**
 * @author liujson
 * @date 2021/3/8.
 */
public interface IMQTTWrapper<T> {

    /**
     * 获取 IMQTTService 的实现
     *
     * @return IMQTTService 的实现
     */
    T getClient();

    /**
     * 设置接收消息监听
     */
    void setMessageReceiver(IMQTTMessageReceiver messageReceiver);

    /**
     * destroy 销毁释放资源
     * 销毁之后客户端就不可以再使用了
     */
    void destroy();
}
