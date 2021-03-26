package cn.liujson.client.ui.viewmodel;

import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import java.util.Objects;

import cn.liujson.client.R;
import cn.liujson.client.ui.app.CustomApplication;
import cn.liujson.client.ui.base.BaseViewModel;
import cn.liujson.client.ui.service.ConnectionBinder;
import cn.liujson.client.ui.service.MqttMgr;
import cn.liujson.client.ui.util.NetworkUtils;
import cn.liujson.lib.mqtt.service.rx.RxPahoClient;

public class WorkingStatusViewModel extends ViewModel {

    private MutableLiveData<WorkingStatus> fieldNetworkConnectedStatus;
    private MutableLiveData<WorkingStatus> fieldServiceBindStatus;
    private MutableLiveData<WorkingStatus> fieldClientInstalledStatus;
    private MutableLiveData<WorkingStatus> fieldClientConnectedStatus;
    private MutableLiveData<WorkingStatus> fieldClientClosedStatus;
    private MutableLiveData<WorkingStatus> fieldCheckPingStatus;

    private MutableLiveData<String> fieldCheckPingTime = new MutableLiveData<>("check time ");

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

    public MutableLiveData<WorkingStatus> getFieldCheckPingStatus() {
        if (fieldCheckPingStatus == null) {
            fieldCheckPingStatus = new MutableLiveData<>();
        }
        return fieldCheckPingStatus;
    }

    public MutableLiveData<String> getFieldCheckPingTime() {
        return fieldCheckPingTime;
    }

    /**
     * 刷新状态
     */
    public void refreshStatus() {
        if (NetworkUtils.isConnected(CustomApplication.getApp())) {
            getFieldNetworkConnectedStatus().setValue(WorkingStatus.OK);
        } else {
            getFieldNetworkConnectedStatus().setValue(WorkingStatus.ERR);
        }
        final ConnectionBinder binder = MqttMgr.instance().binder();
        getFieldCheckPingStatus().setValue(WorkingStatus.DISABLE);
        if (binder == null) {
            getFieldServiceBindStatus().setValue(WorkingStatus.ERR);
            getFieldClientInstalledStatus().setValue(WorkingStatus.DISABLE);
            getFieldClientConnectedStatus().setValue(WorkingStatus.DISABLE);
            getFieldClientClosedStatus().setValue(WorkingStatus.DISABLE);
            return;
        }
        getFieldServiceBindStatus().setValue(WorkingStatus.OK);
        if (!binder.isInstalled()) {
            getFieldClientInstalledStatus().setValue(WorkingStatus.ERR);
            getFieldClientConnectedStatus().setValue(WorkingStatus.DISABLE);
            getFieldClientClosedStatus().setValue(WorkingStatus.DISABLE);
            return;
        }
        getFieldClientInstalledStatus().setValue(WorkingStatus.OK);
        final RxPahoClient rxPahoClient = binder.getClient();
        final boolean connected = rxPahoClient.isConnected();
        final boolean closed = rxPahoClient.isClosed();
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
                    imageView.setImageResource(R.mipmap.ic_status_successful);
                    break;
                case ERR:
                    imageView.setImageResource(R.mipmap.ic_status_failure);
                    break;
                case DISABLE:
                    imageView.setImageResource(R.mipmap.ic_status_successful_gray);
                    break;
                default:

                    break;
            }
        }
    }


    public enum WorkingStatus {
        OK(R.mipmap.ic_status_successful),
        ERR(R.mipmap.ic_status_failure),
        DISABLE(R.mipmap.ic_status_successful_gray);

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