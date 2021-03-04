package cn.liujson.lib.mqtt.exception;

/**
 * MQTT 异常
 * @author liujson
 * @date 2021/3/4.
 */
public class WrapMQTTException extends Exception{

    public WrapMQTTException() {
    }

    public WrapMQTTException(String message) {
        super(message);
    }

    public WrapMQTTException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrapMQTTException(Throwable cause) {
        super(cause);
    }

}
