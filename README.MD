## MQTT.Cli

> **MQTT.Cli** 是一个模仿 **MQTT.fx** 的MQTT 客户端应用。**MQTT.Cli** 可以用来测试MQTT连接和通信。**libmqtt **是为了解决之前项目异常掉线和为了通信稳定发展来的。**libmqtt **只能在Android平台上使用。

### libmqtt 使用

#### 引入依赖

```groovy
dependencies {
    //直接引入aar方式 (也可以用依赖库的方式引入)
    implementation (name: 'libmqtt-{version}', ext: 'aar')
    
    //paho client mqttv3 (必须)
    implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5'
}
```

#### 使用同步客户端

 Paho提供了同步堵塞的 MqttClient ，一般不会直接使用这种方式，如果确有相关需求可直接使用。下面是官方的Demo示例。

```java
    public static void main(String[] args) {

        String topic        = "MQTT Examples";
        String content      = "Message from MqttPublishSample";
        int qos             = 2;
        String broker       = "tcp://iot.eclipse.org:1883";
        String clientId     = "JavaSample";
        //使用内存持久化
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            //Mqtt 连接参数配置
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
```

#### 使用异步客户端

如果你阅读源码可以发现 c是基于 MqttAsyncClient 封装的，也就是意味着学会了MqttAsyncClient 就学会了使用整个 Paho Java Client。下面详细介绍如何使用 MqttAsyncClient。

##### setp 1 构造 MqttAsyncClient 

MqttAsyncClient  提供了5个构造函数，我们一般只需要使用前两个就行了

```java

public MqttAsyncClient(String serverURI, String clientId) throws MqttException

public PahoMqttAsyncClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException
```

* 第一个参数：服务端URI接收String类型的参数但**只支持`ssl://`和`tcp://`**开头的地址，如果不填端口号默认tcp是**1883**，ssl是**8883**；
* 第二个参数：clientId 不填会报错， clientId是用来服务端确定唯一的Client标识；
* 第二个参数：MqttClientPersistence 文档上说是用来持久化出入站消息的。

##### setp 2 配置连接参数

> TODO // 

##### setp 3 监听消息接收

> TODO // 

##### setp 4 订阅主题

> TODO // 

##### setp 5 发送消息

> TODO // 

#### 使用Rx封装客户端

RxPahoClient 是基于 MqttAsyncClient + MqttAsyncClient 封装的RxJava客户端，带有异步和同步的调用方式。使用方式和MqttAsyncClient是很相似的。

以下是伪代码：

```java
RxPahoClient client = new RxPahoClient(createConnectionParams());
//创建之后就可以使用Rx方式调用了

//添加监听
client.setCallback(new MqttCallback() {
    @Override
    public void connectionLost(Throwable cause) {
		//连接丢失
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
		//消息抵达
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
		//发送完成
    }
})

//连接
client.connect().observeOn(AndroidSchedulers.mainThread()).subscribe();
client.connectWithResult().waitForCompletion();
//订阅
client.subscribe("topicName", QoS.AT_MOST_ONCE).observeOn(AndroidSchedulers.mainThread()).subscribe();
client.subscribeWithResponse(new String[]{"topicName"}, new QoS[]{QoS.AT_MOST_ONCE}).waitForCompletion(10000);
//取消订阅
client.unsubscribe("topicName").observeOn(AndroidSchedulers.mainThread()).subscribe();
//发布
client.publish("topicName","Hello".getBytes(),QoS.AT_MOST_ONCE,false).observeOn(AndroidSchedulers.mainThread()).subscribe();
//断开连接
client.disconnect().observeOn(AndroidSchedulers.mainThread()).subscribe();
//安全关闭，关闭成功后会释放资源(建议使用)
client.closeSafety().observeOn(AndroidSchedulers.mainThread()).subscribe();
//强制关闭，关闭成功后会释放资源,强制关闭会堵塞调用线程（建议优先使用disconnect，当不能关闭时再使用强制关闭）
client.disconnectForcibly(100,200);
//是否连接
client.isConnected();
//是否关闭（关闭后该RxPahoClient就不能使用了，需要重新创建）
client.isClosed();
```

**【注意】：RxPahoClient 如果想要自动重连请先打开参数`automaticReconnect`**

**【注意】：首次连接失败不会自动重连，就算设置了automaticReconnect也不会重连**

#### 在后台运行客户端（Service）

后台运行例子：后台运行意味着需要有后台组件，让RxRxPahoClient 依赖于后台组件生命周期运行。

**ConnectionService.java**

```java
public class ConnectionService extends Service {

    private static final String TAG = "ConnectionService";

    private final ConnectionBinder binder = new ConnectionBinder();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //直接通过绑定启动后台服务很快就会被系统杀死
        LogX.d(TAG, "==onBind==");
        return binder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        LogX.d(TAG, "==onCreate==");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogX.d(TAG, "==onStartCommand==");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogX.d(TAG, "==onDestroy==");
        if (binder.isInstalled()) {
            //尝试关闭并释放资源
            final Disposable rxCloseSafety = binder.getClient()
                    .closeForcibly(500, 1000)
                    .subscribe(() -> {
                        LogX.d(TAG, "==disconnectForcibly success==");
                    }, throwable -> {
                        LogX.d(TAG, "==disconnectForcibly failure:" + throwable.toString());
                    });
        }
    }
}

```

**ConnectionBinder.java**

```java
public class ConnectionBinder extends AbstractPahoConnServiceBinder {

    private static final String TAG = "ConnectionBinder";

    final List<OnRecMsgListener> recMsgListenerList = new ArrayList<>();
    final List<OnConnectedListener> connectedListenerList = new ArrayList<>();

    Handler mHandler = new Handler(Looper.myLooper());

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        LogX.d(TAG,"MQTT 消息抵达，topic:" + topic + ",message:" + new String(message.getPayload()));
        mHandler.post(() -> {
            //接受到消息会回调这里
            final Iterator<OnRecMsgListener> it = recMsgListenerList.iterator();
            while (it.hasNext()) {
                it.next().onReceiveMessage(topic, message.getPayload(), MqttUtils.int2QoS(message.getQos()));
            }
        });
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        LogX.d(TAG,"MQTT 连接完成，是否重连：" + reconnect + ",server uri:" + serverURI);
        if (reconnect) {
            // 如果cleanSession是true,重连后需要重新订阅topic
        }
        final Iterator<OnConnectedListener> it = connectedListenerList.iterator();
        while (it.hasNext()) {
            it.next().onConnectComplete(reconnect, serverURI);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        super.connectionLost(cause);
        LogX.e(TAG,"MQTT 失去连接:" + cause.toString());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        super.deliveryComplete(token);
        try {
            LogX.d(TAG,"MQTT 发布成功,topic:" + Arrays.toString(token.getTopics()) +
                    ",message:" + new String(token.getMessage().getPayload()));
        } catch (Exception e) {
            LogX.d(TAG,"MQTT 消息发布成功");
        }
    }


    public void addOnRecMsgListener(ConnectionBinder.OnRecMsgListener recMsgListener) {
        if (!recMsgListenerList.contains(recMsgListener)) {
            recMsgListenerList.add(recMsgListener);
        }
    }

    public void removeOnRecMsgListener(ConnectionBinder.OnRecMsgListener recMsgListener) {
        recMsgListenerList.remove(recMsgListener);
    }

    public void addOnConnectedListener(ConnectionBinder.OnConnectedListener connectedListener) {
        if (!connectedListenerList.contains(connectedListener)) {
            connectedListenerList.add(connectedListener);
        }
    }

    public void removeOnConnectedListener(ConnectionBinder.OnConnectedListener connectedListener) {
        connectedListenerList.remove(connectedListener);
    }

    //-------------------------------------------------------------------------------------------

    public interface OnRecMsgListener {
        /**
         * 接收到消息
         */
        @MainThread
        void onReceiveMessage(String topic, byte[] payload, QoS qoS);
    }

    public interface OnConnectedListener {
        /**
         * 连接成功
         */
        @MainThread
        void onConnectComplete(boolean reconnect, String serverURI);
    }

    //-------------------------------------------------------------------------------------------
}
```

**DemoActivity 中使用**

以下是伪代码：

```java
private ConnectionBinder mBinder;   

private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (ConnectionBinder) service;
            ToastUtils.showToast("MQTT后台服务绑定成功");
            
            install(createRxPahoClient());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
            ToastUtils.showToast("MQTT后台服务绑定失败");
        }
    };

public ConnectionBinder binder() {
    return mBinder;
}

//是否绑定服务
public boolean isBind() {
    return binder() != null;
}
//是否安装RxPahoClient 到服务
public boolean isInstalled() {
    return binder().isInstalled();
}
//比对已经安装的RxPahoClient的参数和object连接对象参数
public boolean isSame(Object object) {
    return binder().isSame(object);
}
//是否已经连接成功
public boolean isConnected() {
    return isInstalled() && binder().getClient().isConnected();
}
//是否已经关闭
public boolean isClosed() {
    return binder().getClient().isClosed();
}
//安装
public void install(RxPahoClient client) {
    binder().install(client);
}
//卸载
public void uninstall() {
    binder().uninstall();
}
//获取已经订阅的主题列表
public List<Pair<String, QoS>> getSubList() {
    return binder().getClient().getActiveSubs();
}
//添加消息接收监听
public void addOnRecMsgListener(ConnectionBinder.OnRecMsgListener recMsgListener) {
    binder().addOnRecMsgListener(recMsgListener);
}
//移除消息接收监听
public void removeOnRecMsgListener(ConnectionBinder.OnRecMsgListener recMsgListener) {
    binder().removeOnRecMsgListener(recMsgListener);
}
//添加连接成功监听
public void addOnConnectedListener(ConnectionBinder.OnConnectedListener connectedListener) {
    binder().addOnConnectedListener(connectedListener);
}
//移除连接成功监听
public void removeOnConnectedListener(ConnectionBinder.OnConnectedListener connectedListener) {
    binder().removeOnConnectedListener(connectedListener);
}


@Override
protected void onCreate(Bundle savedInstanceState) {
	......
   //绑定服务，绑定成功后说明服务起来了
   bindService(this, mServiceConnection,Context.BIND_AUTO_CREATE);
}
```





#### ConnectionParams 连接参数说明

```java
ConnectionParams.newBuilder(String serverURI)
    			//【必填】支持两种类型的连接｛tcp://localhost:1883,ssl://localhost:8883｝如果没有指定端口tcp:// 默认使用1883 ，ssl:// 默认 8883
               .serverURI()
    			//【必填】它会覆盖serverURI,客户端连接时会从第一个开始尝试，直到连接成功，或者所有都失败；如果其中地址指向不同的服务器，cleanSession应当设置为false
    		   .setServerURIs(String[] serverURIs)
    			//如果你不想服务端记住你对话请设置为true，默认是true
               .cleanSession(boolean cleanSession)
    			//用户名,默认无
               .username(String username)
    			//密码,默认无
     		   .password(String password)
    			//设置遗嘱消息,默认无
               .setWill(@NonNull String willTopic, @NonNull Message willMessage)
               //设置“保持存活”间隔（秒）。客户端与服务端ping消息的时间间隔，单位是秒，设置为0时表示禁用，默认15秒
               .keepAlive(int keepAlive)
               //【必填】 clientId是用来服务端确定唯一的Client标识 不允许为空或者字符长度超过65535
               .clientId(String clientId)
    		   //连接超时间（单位秒）,默认10秒
               .connectionTimeout(int connectionTimeout)
    			//重连的最大等待时间（单位ms）当设置了断线自动重连（automaticReconnect）后，会每个一段时间进行自动重连，如果连不上则进行延时后再尝试（延时算法：指数补偿），当达到最大时延（maxReconnectDelay）后这个过程会重新来，直到又达到最大时延，如此周期。默认 128000
               .maxReconnectDelay(int maxReconnectDelay)
    			//自动重连（默认false）
               .automaticReconnect(boolean automaticReconnect)
   				// 设置MQTT版本。默认操作是与版本 3.1.1 连接,如果失败，则返回到 3.1。版本 3.1.1 或 3.1 可以分别使用MQTT_VERSION_3_1_1或MQTT_VERSION_3_1选项来具体选择，无需回退。
               .mqttVersion(int mqttVersion)
               .build();
```



#### 可能出现的疑难杂症和解决方案

> TODO // 



### 其他控件使用

#### AVLoadingIndicatorView 使用

![img](.assets/20151103101420429)

**Indicator table**

| Col 1                       | Col 2                              | Col 3                         | Col 4                             |
| --------------------------- | ---------------------------------- | ----------------------------- | --------------------------------- |
| `BallPulseIndicator`        | `BallGridPulseIndicator`           | `BallClipRotateIndicator`     | `BallClipRotatePulseIndicator`    |
| `SquareSpinIndicator`       | `BallClipRotateMultipleIndicator`  | `BallPulseRiseIndicator`      | `BallRotateIndicator`             |
| `CubeTransitionIndicator`   | `BallZigZagIndicator`              | `BallZigZagDeflectIndicator`  | `BallTrianglePathIndicator`       |
| `BallScaleIndicator`        | `LineScaleIndicator`               | `LineScalePartyIndicator`     | `BallScaleMultipleIndicator`      |
| `BallPulseSyncIndicator`    | `BallBeatIndicator`                | `LineScalePulseOutIndicator`  | `LineScalePulseOutRapidIndicator` |
| `BallScaleRippleIndicator`  | `BallScaleRippleMultipleIndicator` | `BallSpinFadeLoaderIndicator` | `LineSpinFadeLoaderIndicator`     |
| `TriangleSkewSpinIndicator` | `PacmanIndicator`                  | `BallGridBeatIndicator`       | `SemiCircleSpinIndicator`         |

### 开源使用和参考文档

> - https://www.eclipse.org/paho
> - http://www.eclipse.org/paho/files/javadoc/index.html
>
> 
>
> - https://github.com/eclipse/paho.mqtt.java
> - https://github.com/fusesource/mqtt-client
> - https://github.com/hackware1993/MagicIndicator
> - https://github.com/CymChad/BaseRecyclerViewAdapterHelper
> - https://github.com/daimajia/AndroidSwipeLayout
> - https://developer.android.google.cn/training/data-storage/room
> - https://github.com/jaredrummler/MaterialSpinner
> - https://github.com/daimajia/AndroidViewAnimations
> - https://github.com/li-xiaojun/XPopup
> - https://github.com/JessYanCoding/AndroidAutoSize
> - https://github.com/EverythingMe/overscroll-decor
> - https://github.com/81813780/AVLoadingIndicatorView
> - https://github.com/arcadefire/nice-spinner
> - https://github.com/iqiyi/xCrash/blob/master/README.zh-CN.md

