package cn.liujson.lib.mqtt.service.paho;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.ScheduledExecutorPingSender;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.util.Debug;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;


/**
 * MqttClient 重写
 * 如果MqttClient有更新，直接再拷贝一遍代码过来即可
 *
 * @author liujson
 * @date 2021/3/18.
 * @see IMqttClient
 * @see MqttClient
 */
public class PahoMqttClient implements IMqttClient {

    protected PahoMqttAsyncClient aClient = null;  // Delegate implementation to MqttAsyncClient
    protected long timeToWait = -1;                // How long each method should wait for action to complete


    public PahoMqttClient(String serverURI, String clientId) throws MqttException {
        this(serverURI, clientId, new MqttDefaultFilePersistence());
    }


    public PahoMqttClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
        aClient = new PahoMqttAsyncClient(serverURI, clientId, persistence);
    }


    public PahoMqttClient(String serverURI, String clientId, MqttClientPersistence persistence, ScheduledExecutorService executorService) throws MqttException {
        aClient = new PahoMqttAsyncClient(serverURI, clientId, persistence, new ScheduledExecutorPingSender(executorService), executorService);
    }

    /*
     * @see IMqttClient#connect()
     */
    public void connect() throws MqttSecurityException, MqttException {
        this.connect(new MqttConnectOptions());
    }

    /*
     * @see IMqttClient#connect(MqttConnectOptions)
     */
    public void connect(MqttConnectOptions options) throws MqttSecurityException, MqttException {
        aClient.connect(options, null, null).waitForCompletion(getTimeToWait());
    }

    /*
     * @see IMqttClient#connect(MqttConnectOptions)
     */
    public IMqttToken connectWithResult(MqttConnectOptions options) throws MqttSecurityException, MqttException {
        IMqttToken tok = aClient.connect(options, null, null);
        tok.waitForCompletion(getTimeToWait());
        return tok;
    }

    /*
     * @see IMqttClient#disconnect()
     */
    public void disconnect() throws MqttException {
        aClient.disconnect().waitForCompletion();
    }

    /*
     * @see IMqttClient#disconnect(long)
     */
    public void disconnect(long quiesceTimeout) throws MqttException {
        aClient.disconnect(quiesceTimeout, null, null).waitForCompletion();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.paho.client.mqttv3.IMqttAsyncClient#disconnectForcibly()
     */
    public void disconnectForcibly() throws MqttException {
        aClient.disconnectForcibly();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.paho.client.mqttv3.IMqttAsyncClient#disconnectForcibly(long)
     */
    public void disconnectForcibly(long disconnectTimeout) throws MqttException {
        aClient.disconnectForcibly(disconnectTimeout);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.paho.client.mqttv3.IMqttAsyncClient#disconnectForcibly(long, long)
     */
    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout) throws MqttException {
        aClient.disconnectForcibly(quiesceTimeout, disconnectTimeout);
    }

    /**
     * Disconnects from the server forcibly to reset all the states. Could be useful when disconnect attempt failed.
     * <p>
     * Because the client is able to establish the TCP/IP connection to a none MQTT server and it will certainly fail to
     * send the disconnect packet.
     *
     * @param quiesceTimeout       the amount of time in milliseconds to allow for existing work to finish before
     *                             disconnecting. A value of zero or less means the client will not quiesce.
     * @param disconnectTimeout    the amount of time in milliseconds to allow send disconnect packet to server.
     * @param sendDisconnectPacket if true, will send the disconnect packet to the server
     * @throws MqttException if any unexpected error
     */
    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout, boolean sendDisconnectPacket) throws MqttException {
        aClient.disconnectForcibly(quiesceTimeout, disconnectTimeout, sendDisconnectPacket);
    }

    /*
     * @see IMqttClient#subscribe(String)
     */
    public void subscribe(String topicFilter) throws MqttException {
        this.subscribe(new String[]{topicFilter}, new int[]{1});
    }

    /*
     * @see IMqttClient#subscribe(String[])
     */
    public void subscribe(String[] topicFilters) throws MqttException {
        int[] qos = new int[topicFilters.length];
        Arrays.fill(qos, 1);
        this.subscribe(topicFilters, qos);
    }

    /*
     * @see IMqttClient#subscribe(String, int)
     */
    public void subscribe(String topicFilter, int qos) throws MqttException {
        this.subscribe(new String[]{topicFilter}, new int[]{qos});
    }

    /*
     * @see IMqttClient#subscribe(String[], int[])
     */
    public void subscribe(String[] topicFilters, int[] qos) throws MqttException {
        this.subscribe(topicFilters, qos, null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#subscribe(java.lang.String, int, java.lang.Object, org.eclipse.paho.client.mqttv3.IMqttActionListener)
     */
    public void subscribe(String topicFilter, IMqttMessageListener messageListener) throws MqttException {
        this.subscribe(new String[]{topicFilter}, new int[]{1}, new IMqttMessageListener[]{messageListener});
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#subscribe(java.lang.String, int, java.lang.Object, org.eclipse.paho.client.mqttv3.IMqttActionListener)
     */
    public void subscribe(String[] topicFilters, IMqttMessageListener[] messageListeners) throws MqttException {
        int[] qos = new int[topicFilters.length];
        Arrays.fill(qos, 1);
        this.subscribe(topicFilters, qos, messageListeners);
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#subscribe(java.lang.String, int)
     */
    public void subscribe(String topicFilter, int qos, IMqttMessageListener messageListener) throws MqttException {
        this.subscribe(new String[]{topicFilter}, new int[]{qos}, new IMqttMessageListener[]{messageListener});
    }


    public void subscribe(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners) throws MqttException {
        IMqttToken tok = aClient.subscribe(topicFilters, qos, null, null, messageListeners);
        tok.waitForCompletion(getTimeToWait());
        int[] grantedQos = tok.getGrantedQos();
        System.arraycopy(grantedQos, 0, qos, 0, grantedQos.length);
        if (grantedQos.length == 1 && qos[0] == 0x80) {
            throw new MqttException(MqttException.REASON_CODE_SUBSCRIBE_FAILED);
        }
    }

    /*
     * @see IMqttClient#subscribeWithResponse(String)
     */
    public IMqttToken subscribeWithResponse(String topicFilter) throws MqttException {
        return this.subscribeWithResponse(new String[]{topicFilter}, new int[]{1});
    }

    /*
     * @see IMqttClient#subscribeWithResponse(String, IMqttMessageListener)
     */
    public IMqttToken subscribeWithResponse(String topicFilter, IMqttMessageListener messageListener) throws MqttException {
        return this.subscribeWithResponse(new String[]{topicFilter}, new int[]{1}, new IMqttMessageListener[]{messageListener});
    }

    /*
     * @see IMqttClient#subscribeWithResponse(String, int)
     */
    public IMqttToken subscribeWithResponse(String topicFilter, int qos) throws MqttException {
        return this.subscribeWithResponse(new String[]{topicFilter}, new int[]{qos});
    }

    /*
     * @see IMqttClient#subscribeWithResponse(String, int, IMqttMessageListener)
     */
    public IMqttToken subscribeWithResponse(String topicFilter, int qos, IMqttMessageListener messageListener)
            throws MqttException {
        return this.subscribeWithResponse(new String[]{topicFilter}, new int[]{qos}, new IMqttMessageListener[]{messageListener});
    }

    /*
     * @see IMqttClient#subscribeWithResponse(String[])
     */
    public IMqttToken subscribeWithResponse(String[] topicFilters) throws MqttException {
        int[] qos = new int[topicFilters.length];
        Arrays.fill(qos, 1);
        return this.subscribeWithResponse(topicFilters, qos);
    }

    /*
     * @see IMqttClient#subscribeWithResponse(String[], IMqttMessageListener[])
     */
    public IMqttToken subscribeWithResponse(String[] topicFilters, IMqttMessageListener[] messageListeners)
            throws MqttException {
        int[] qos = new int[topicFilters.length];
        Arrays.fill(qos, 1);
        return this.subscribeWithResponse(topicFilters, qos, messageListeners);
    }

    /*
     * @see IMqttClient#subscribeWithResponse(String[], int[])
     */
    public IMqttToken subscribeWithResponse(String[] topicFilters, int[] qos) throws MqttException {
        return this.subscribeWithResponse(topicFilters, qos, null);
    }

    /*
     * @see IMqttClient#subscribeWithResponse(String[], int[], IMqttMessageListener[])
     */
    public IMqttToken subscribeWithResponse(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners)
            throws MqttException {
        IMqttToken tok = aClient.subscribe(topicFilters, qos, null, null, messageListeners);
        tok.waitForCompletion(getTimeToWait());
        return tok;
    }

    /*
     * @see IMqttClient#unsubscribe(String)
     */
    public void unsubscribe(String topicFilter) throws MqttException {
        unsubscribe(new String[]{topicFilter});
    }

    /*
     * @see IMqttClient#unsubscribe(String[])
     */
    public void unsubscribe(String[] topicFilters) throws MqttException {
        // message handlers removed in the async client unsubscribe below
        aClient.unsubscribe(topicFilters, null, null).waitForCompletion(getTimeToWait());
    }

    /*
     * @see IMqttClient#publishBlock(String, byte[], int, boolean)
     */
    public void publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException,
            MqttPersistenceException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        this.publish(topic, message);
    }

    /*
     * @see IMqttClient#publishBlock(String, MqttMessage)
     */
    public void publish(String topic, MqttMessage message) throws MqttException,
            MqttPersistenceException {
        aClient.publish(topic, message, null, null).waitForCompletion(getTimeToWait());
    }

    /**
     * Set the maximum time to wait for an action to complete.
     * <p>Set the maximum time to wait for an action to complete before
     * returning control to the invoking application. Control is returned
     * when:</p>
     * <ul>
     * <li>the action completes</li>
     * <li>or when the timeout if exceeded</li>
     * <li>or when the client is disconnect/shutdown</li>
     * </ul>
     * <p>
     * The default value is -1 which means the action will not timeout.
     * In the event of a timeout the action carries on running in the
     * background until it completes. The timeout is used on methods that
     * block while the action is in progress.
     * </p>
     *
     * @param timeToWaitInMillis before the action times out. A value or 0 or -1 will wait until
     *                           the action finishes and not timeout.
     * @throws IllegalArgumentException if timeToWaitInMillis is invalid
     */
    public void setTimeToWait(long timeToWaitInMillis) throws IllegalArgumentException {
        if (timeToWaitInMillis < -1) {
            throw new IllegalArgumentException();
        }
        this.timeToWait = timeToWaitInMillis;
    }

    /**
     * Return the maximum time to wait for an action to complete.
     *
     * @return the time to wait
     * @see #setTimeToWait(long)
     */
    public long getTimeToWait() {
        return this.timeToWait;
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#close()
     */
    public void close() throws MqttException {
        aClient.close(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#close()
     */
    public void close(boolean force) throws MqttException {
        aClient.close(force);
    }


    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#getClientId()
     */
    public String getClientId() {
        return aClient.getClientId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#getPendingDeliveryTokens()
     */
    public IMqttDeliveryToken[] getPendingDeliveryTokens() {
        return aClient.getPendingDeliveryTokens();
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#getServerURI()
     */
    public String getServerURI() {
        return aClient.getServerURI();
    }

    /**
     * Returns the currently connected Server URI
     * Implemented due to: https://bugs.eclipse.org/bugs/show_bug.cgi?id=481097
     * <p>
     * Where getServerURI only returns the URI that was provided in
     * MqttAsyncClient's constructor, getCurrentServerURI returns the URI of the
     * Server that the client is currently connected to. This would be different in scenarios
     * where multiple server URIs have been provided to the MqttConnectOptions.
     *
     * @return the currently connected server URI
     */
    public String getCurrentServerURI() {
        return aClient.getCurrentServerURI();
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#getTopic(java.lang.String)
     */
    public MqttTopic getTopic(String topic) {
        return aClient.getTopic(topic);
    }


    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#isConnected()
     */
    public boolean isConnected() {
        return aClient.isConnected();
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#setCallback(org.eclipse.paho.client.mqttv3.MqttCallback)
     */
    public void setCallback(MqttCallback callback) {
        aClient.setCallback(callback);
    }

    /* (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.IMqttClient#setCallback(org.eclipse.paho.client.mqttv3.MqttCallback)
     */
    public void setManualAcks(boolean manualAcks) {
        aClient.setManualAcks(manualAcks);
    }

    public void messageArrivedComplete(int messageId, int qos) throws MqttException {
        aClient.messageArrivedComplete(messageId, qos);
    }

    /**
     * Returns a randomly generated client identifier based on the current user's login
     * name and the system time.
     * <p>When cleanSession is set to false, an application must ensure it uses the
     * same client identifier when it reconnects to the server to resume state and maintain
     * assured message delivery.</p>
     *
     * @return a generated client identifier
     * @see MqttConnectOptions#setCleanSession(boolean)
     */
    public static String generateClientId() {
        return MqttAsyncClient.generateClientId();
    }

    /**
     * Will attempt to reconnect to the server after the client has lost connection.
     *
     * @throws MqttException if an error occurs attempting to reconnect
     */
    public void reconnect() throws MqttException {
        aClient.reconnect();
    }

    /**
     * Return a debug object that can be used to help solve problems.
     *
     * @return the {@link Debug} Object.
     */
    public Debug getDebug() {
        return (aClient.getDebug());
    }


    //-------------------------------------展出来的方法----------------------------------------------

    /**
     * 是否连接已经关闭
     */
    public boolean isClosed() {
        return aClient.isClosed();
    }

    /**
     * 是否连接已经关闭
     */
    public boolean isConnecting() {
        return aClient.isConnecting();
    }

    /**
     * 是否进入自动重连休息状态
     */
    public boolean isResting() {
        return aClient.isResting();
    }
}
