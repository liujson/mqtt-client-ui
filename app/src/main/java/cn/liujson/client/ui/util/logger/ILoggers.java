package cn.liujson.client.ui.util.logger;

/**
 * android 日志抽象
 *
 * @author liujson
 * @date 2021/3/14.
 */
public interface ILoggers {

    void d(String tag, String message);

    void e(String tag, String message);

    void w(String tag, String message);

    void v(String tag, String message);

    void i(String tag, String message);

    void d(String message);

    void e(String message);

    void w(String message);

    void v(String message);

    void i(String message);
}
