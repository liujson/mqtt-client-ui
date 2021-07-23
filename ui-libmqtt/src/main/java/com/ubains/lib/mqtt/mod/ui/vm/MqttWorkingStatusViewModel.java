package com.ubains.lib.mqtt.mod.ui.vm;

import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.ubains.android.ubutil.comm.AppUtils;
import com.ubains.android.ubutil.comm.NetworkUtils;
import com.ubains.lib.mqtt.mod.R;
import com.ubains.lib.mqtt.mod.provider.MqttConnection;
import com.ubains.lib.mqtt.mod.provider.MqttConnectionImpl;


import java.util.Objects;



/**
 * @author liujson
 */
public class MqttWorkingStatusViewModel extends ViewModel {

    private MutableLiveData<WorkingStatus> fieldNetworkConnectedStatus;
    private MutableLiveData<WorkingStatus> fieldServiceBindStatus;
    private MutableLiveData<WorkingStatus> fieldClientInstalledStatus;
    private MutableLiveData<WorkingStatus> fieldClientConnectedStatus;
    private MutableLiveData<WorkingStatus> fieldClientClosedStatus;


    MqttConnection mqttConnection;

    public MqttWorkingStatusViewModel() {
        mqttConnection = new MqttConnectionImpl();
    }

    public MutableLiveData<WorkingStatus> getFieldNetworkConnectedStatus() {
        if (fieldNetworkConnectedStatus == null) {
            fieldNetworkConnectedStatus = new MutableLiveData<>();
        }
        return fieldNetworkConnectedStatus;
    }

    public MutableLiveData<WorkingStatus> getFieldServiceBindStatus() {
        if (fieldServiceBindStatus == null) {
            fieldServiceBindStatus = new MutableLiveData<>();
        }
        return fieldServiceBindStatus;
    }

    public MutableLiveData<WorkingStatus> getFieldClientInstalledStatus() {
        if (fieldClientInstalledStatus == null) {
            fieldClientInstalledStatus = new MutableLiveData<>();
        }
        return fieldClientInstalledStatus;
    }

    public MutableLiveData<WorkingStatus> getFieldClientConnectedStatus() {
        if (fieldClientConnectedStatus == null) {
            fieldClientConnectedStatus = new MutableLiveData<>();
        }
        return fieldClientConnectedStatus;
    }

    public MutableLiveData<WorkingStatus> getFieldClientClosedStatus() {
        if (fieldClientClosedStatus == null) {
            fieldClientClosedStatus = new MutableLiveData<>();
        }
        return fieldClientClosedStatus;
    }

    /**
     * 刷新状态
     */
    public void refreshStatus() {
        if (NetworkUtils.isConnected(AppUtils.getApp())) {
            getFieldNetworkConnectedStatus().setValue(WorkingStatus.OK);
        } else {
            getFieldNetworkConnectedStatus().setValue(WorkingStatus.ERR);
        }
        if (!mqttConnection.isBind()) {
            getFieldServiceBindStatus().setValue(WorkingStatus.ERR);
            getFieldClientInstalledStatus().setValue(WorkingStatus.DISABLE);
            getFieldClientConnectedStatus().setValue(WorkingStatus.DISABLE);
            getFieldClientClosedStatus().setValue(WorkingStatus.DISABLE);
            return;
        }
        getFieldServiceBindStatus().setValue(WorkingStatus.OK);
        if (!mqttConnection.isInstalled()) {
            getFieldClientInstalledStatus().setValue(WorkingStatus.ERR);
            getFieldClientConnectedStatus().setValue(WorkingStatus.DISABLE);
            getFieldClientClosedStatus().setValue(WorkingStatus.DISABLE);
            return;
        }
        getFieldClientInstalledStatus().setValue(WorkingStatus.OK);
        final boolean connected = mqttConnection.isConnected();
        final boolean closed = mqttConnection.isClosed();
        if (connected || closed) {
            if (connected) {
                getFieldClientConnectedStatus().setValue(WorkingStatus.OK);
                getFieldClientClosedStatus().setValue(WorkingStatus.DISABLE);
            } else {
                getFieldClientConnectedStatus().setValue(WorkingStatus.DISABLE);
                getFieldClientClosedStatus().setValue(WorkingStatus.ERR);
            }
        } else {
            getFieldClientConnectedStatus().setValue(WorkingStatus.DISABLE);
            getFieldClientClosedStatus().setValue(WorkingStatus.DISABLE);
        }
    }


    public static void bindWorkingStatus(ImageView imageView, WorkingStatus workingStatus) {
        if (imageView != null && workingStatus != null) {
            switch (Objects.requireNonNull(workingStatus)) {
                case OK:
                    imageView.setImageResource(R.mipmap.setting_ic_status_successful);
                    break;
                case ERR:
                    imageView.setImageResource(R.mipmap.setting_ic_status_failure);
                    break;
                case DISABLE:
                    imageView.setImageResource(R.mipmap.setting_ic_status_successful_gray);
                    break;
                default:

                    break;
            }
        }
    }


    public enum WorkingStatus {
        OK(R.mipmap.setting_ic_status_successful),
        ERR(R.mipmap.setting_ic_status_failure),
        DISABLE(R.mipmap.setting_ic_status_successful_gray);

        @DrawableRes
        public int resId;

        WorkingStatus(int resId) {
            this.resId = resId;
        }

        public int getResId() {
            return resId;
        }
    }
}