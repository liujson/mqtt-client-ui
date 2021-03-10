package cn.liujson.client.ui.bean.event;

/**
 * 连接改变事件
 *
 * @author liujson
 * @date 2021/3/9.
 */
public class ConnectChangeEvent {

    public final boolean isConnected ;

    public ConnectChangeEvent(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
