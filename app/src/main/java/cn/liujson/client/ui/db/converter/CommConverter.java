package cn.liujson.client.ui.db.converter;

import androidx.room.TypeConverter;

import java.util.Date;

import cn.liujson.lib.mqtt.api.QoS;
import cn.liujson.lib.mqtt.util.MqttUtils;

public class CommConverter {
    @TypeConverter
    public static Date revertDate(Long value) {
        return value != null ? new Date(value) : null;
    }

    @TypeConverter
    public static Long converterDate(Date value) {
        return value == null ? null : value.getTime();
    }

    @TypeConverter
    public static QoS revertQoS(int qoS) {
        return MqttUtils.int2QoS(qoS);
    }

    @TypeConverter
    public static int converterQoS(QoS qoS) {
        return MqttUtils.qoS2Int(qoS);
    }
}