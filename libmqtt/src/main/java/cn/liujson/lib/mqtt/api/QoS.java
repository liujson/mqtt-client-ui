package cn.liujson.lib.mqtt.api;

import android.system.ErrnoException;

/**
 * MQTT QoS 封装
 * <p>
 * QoS 是消息的发送方（Sender）和接受方（Receiver）之间达成的一个协议：
 * <p>
 * QoS0 代表，Sender 发送的一条消息，Receiver 最多能收到一次，也就是说 Sender 尽力向 Receiver 发送消息，如果发送失败，也就算了；
 * QoS1 代表，Sender 发送的一条消息，Receiver 至少能收到一次，也就是说 Sender 向 Receiver 发送消息，如果发送失败，会继续重试，直到 Receiver 收到消息为止，但是因为重传的原因，Receiver 有可能会收到重复的消息；
 * QoS2 代表，Sender 发送的一条消息，Receiver 确保能收到而且只收到一次，也就是说 Sender 尽力向 Receiver 发送消息，如果发送失败，会继续重试，直到 Receiver 收到消息为止，同时保证 Receiver 不会因为消息重传而收到重复的消息。
 * <p>
 * 注意：
 * QoS是Sender和Receiver之间的协议，而不是Publisher和Subscriber之间的协议。换句话说，Publisher发布了一条QoS1的消息，只能保证Broker能至少收到一次这个消息；而对于Subscriber能否至少收到一次这个消息，还要取决于Subscriber在Subscibe的时候和Broker协商的QoS等级。
 * <p>
 * QoS降级
 * 在 MQTT 协议中，从 Broker 到 Subscriber 这段消息传递的实际 QoS 等于：Publisher 发布消息时指定的 QoS 等级和 Subscriber 在订阅时与 Broker 协商的 QoS 等级，这两个 QoS 等级中的最小那一个。
 * <p>
 * 参考文档
 * https://zhuanlan.zhihu.com/p/80203905
 *
 * @author liujson
 */
public enum QoS {
    /**
     * QoS0，At most once，至多一次
     */
    AT_MOST_ONCE,
    /**
     * QoS1，At least once，至少一次
     */
    AT_LEAST_ONCE,
    /**
     * QoS2，Exactly once，确保只有一次
     */
    EXACTLY_ONCE;

    public String qoSName() {
        switch (this) {
            case AT_MOST_ONCE:
                return "QoS 0";
            case AT_LEAST_ONCE:
                return "QoS 1";
            case EXACTLY_ONCE:
                return "QoS 2";
            default:
                throw new IllegalArgumentException("QoS name not exist");
        }
    }


}