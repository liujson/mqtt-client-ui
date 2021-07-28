package cn.liujson.client.ui.util;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ubains.lib.mqtt.mod.provider.IConnectionProfileStore;
import com.ubains.lib.mqtt.mod.provider.bean.ConnectionProfile;
import com.ubains.lib.mqtt.mod.provider.bean.SimpleTopic;

import java.util.ArrayList;
import java.util.List;

import cn.liujson.client.ui.db.DatabaseHelper;
import cn.liujson.client.ui.db.dao.ConnectionProfileStarDao;
import cn.liujson.client.ui.db.entities.ConnectionProfileStar;
import cn.liujson.client.ui.widget.popup.MarkStarPopupView;
import io.reactivex.schedulers.Schedulers;

/**
 * @author liujson
 * @date 2021/7/22.
 */
public class MqttProfileStoreImpl implements IConnectionProfileStore {

    @Override
    public boolean store(ConnectionProfile connectionProfile) {
        // 不实现
        return false;
    }

    /**
     * 需要运行在线程中
     *
     * @return
     */
    @Override
    public ConnectionProfile load() {
        final cn.liujson.client.ui.db.entities.ConnectionProfile connectionProfile = DatabaseHelper.getInstance()
                .connectionProfileDao()
                .getMarkedStarProfile();
        if (connectionProfile != null) {
            JSONObject json = (JSONObject) JSON.toJSON(connectionProfile);
            final ConnectionProfile profile = json.toJavaObject(ConnectionProfile.class);
            profile.defineTopics = new ArrayList<>();
            final ConnectionProfileStar connectionProfileStar = DatabaseHelper
                    .getInstance().starDao().getMarkedStar().blockingGet();
            if (!TextUtils.isEmpty(connectionProfileStar.defineTopics)) {
                List<MarkStarPopupView.TopicWrapper> topicWrapper = JSON.parseObject(connectionProfileStar.defineTopics, new TypeReference<List<MarkStarPopupView.TopicWrapper>>() {
                });
                for (MarkStarPopupView.TopicWrapper wrapper : topicWrapper) {
                    SimpleTopic simpleTopic = new SimpleTopic();
                    simpleTopic.qos = wrapper.qos;
                    simpleTopic.topic = wrapper.topic;
                    profile.defineTopics.add(simpleTopic);
                }
            }
            return profile;
        } else {
            return null;
        }
    }
}
