package cn.liujson.lib.mqtt.util.random;

import java.util.UUID;

/**
 * description
 *
 * @author Liujs
 * @date 2019/9/23
 */
public class UUIDUtils {

    public static String uuid(){
        return UUID.randomUUID().toString().replace("-","").toLowerCase();
    }

    public static String uuidUpperCase(){
        return uuid().toUpperCase();
    }
}
