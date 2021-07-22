package cn.liujson.client.ui.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ubains.lib.mqtt.mod.provider.IConnectionProfileStore;
import com.ubains.lib.mqtt.mod.provider.bean.ConnectionProfile;

import cn.liujson.client.ui.db.DatabaseHelper;
import io.reactivex.schedulers.Schedulers;

/**
 * @author liujson
 * @date 2021/7/22.
 */
public class MqttProfileStoreImpl implements IConnectionProfileStore {

    @Override
    public boolean store(ConnectionProfile connectionProfile) {

        return false;
    }

    @Override
    public ConnectionProfile load() {
        final cn.liujson.client.ui.db.entities.ConnectionProfile connectionProfile = DatabaseHelper.getInstance()
                .connectionProfileDao()
                .getMarkedStarProfile();
        if (connectionProfile != null) {
            JSONObject json = (JSONObject) JSON.toJSON(connectionProfile);
            return json.toJavaObject(ConnectionProfile.class);
        } else {
            return null;
        }
    }
}
