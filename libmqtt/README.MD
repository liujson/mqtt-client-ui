## MQTT 封装库说明

### paho client

**异常码**诠释表

| CODE  | Field Name                               | Description                                                  |
| ----- | ---------------------------------------- | ------------------------------------------------------------ |
| 32000 | REASON_CODE_CLIENT_TIMEOUT               | 客户端发送心跳消息给服务端，等待服务端不响应keep-alive消息超时。 |
| 32001 | REASON_CODE_NO_MESSAGE_IDS_AVAILABLE     | 内部错误，由没有可用的新消息 ID 引起的。                     |
| 32002 | REASON_CODE_WRITE_TIMEOUT                | 客户端向服务器写入消息时超时。                               |
| 32100 | REASON_CODE_CLIENT_CONNECTED             | 客户端已经连接。                                             |
| 32101 | REASON_CODE_CLIENT_ALREADY_DISCONNECTED  | 客户端已经断开连接。                                         |
| 32102 | REASON_CODE_CLIENT_DISCONNECTING         | 客户端当前断开连接，无法接受任何新工作。当等待令牌，然后断开客户端时，可能会发生这种情况。如果消息传递没有在静默超时时间内完成，则将用异常通知等待令牌。 |
| 32103 | REASON_CODE_SERVER_CONNECT_ERROR         | 无法连接到服务器                                             |
| 32104 | REASON_CODE_CLIENT_NOT_CONNECTED         | 客户端没有连接到服务器。必须首先调用MqttClient.connect()或MqttClient.connect(MqttConnectOptions)方法。也有可能连接丢失了—请参阅MqttClient.setCallback(MqttCallback)以获得跟踪丢失连接的方法。 |
| 32105 | REASON_CODE_SOCKET_FACTORY_MISMATCH      | 服务器URI和提供的SocketFactory不匹配。以tcp://开头的uri必须使用javax.net.SocketFactory，以ssl://开头的uri必须使用javax.net.ssl.SSLSocketFactory。 |
| 32106 | REASON_CODE_SSL_CONFIG_ERROR             | SSL配置错误。                                                |
| 32107 | REASON_CODE_CLIENT_DISCONNECT_PROHIBITED | 当尝试从MqttCallback上的方法中调用MqttClient.disconnect()时抛出。这些方法由客户端线程调用，不能用于控制断开连接。 |
| 32108 | REASON_CODE_INVALID_MESSAGE              | 协议错误:消息不能被识别为有效的MQTT包。可能的原因包括连接到非mqtt服务器，或者在客户端不使用SSL时连接到SSL服务器端口。 |
| 32109 | REASON_CODE_CONNECTION_LOST              | 客户端异常断开与服务器的连接。getCause() 将提供更多细节。    |
| 32110 | REASON_CODE_CONNECT_IN_PROGRESS          | 连接操作正在进行中，这个时段不允许再进行连接操作了           |
| 32111 | REASON_CODE_CLIENT_CLOSED                | 客户端已关闭 - 在此状态下不允许客户端执行任何操作。 创建一个新客户端继续吧。 |
| 32201 | REASON_CODE_TOKEN_INUSE                  | A request has been made to use a token that is already associated with another action.  If the action is complete the reset() can ve called on the token to allow it to be reused. |
| 32202 | REASON_CODE_MAX_INFLIGHT                 | 已发出发送消息的请求，但已达到飞行消息（MAX_INFLIGHT）的最大数量。一旦移除了一条或多条消息，就可以发送新的消息了。 |
| 32203 | REASON_CODE_DISCONNECTED_BUFFER_FULL     | The Client has attempted to publish a message whilst in the 'resting' / offline state with Disconnected Publishing enabled, however the buffer is full and deleteOldestMessages is disabled, therefore no more messages can be published until the client reconnects, or the application deletes buffered message manually.（未必准确的翻译：发布消息时，客户端已经断开连接了，此时缓存区满了又禁用了删除旧消息，在不重新连接或不删除缓存区的情况下将无法发布消息） |

## Links

- Project Website: https://www.eclipse.org/paho
- Eclipse Project Information: https://projects.eclipse.org/projects/iot.paho
- Paho Java Client Page: [https://eclipse.org/paho/clients/java/](https://eclipse.org/paho/clients/java)
- GitHub: https://github.com/eclipse/paho.mqtt.java
- Twitter: [@eclipsepaho](https://twitter.com/eclipsepaho)
- Issues: https://github.com/eclipse/paho.mqtt.java/issues
- Mailing-list: https://dev.eclipse.org/mailman/listinfo/paho-dev