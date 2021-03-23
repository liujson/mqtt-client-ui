package cn.liujson.logger.memory;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.LogStrategy;

import java.util.LinkedList;
import java.util.Objects;

import cn.liujson.logger.LogRecord;


/**
 * @author liujson
 * @date 2021/3/15.
 */
public class MemoryLogStrategy implements LogStrategy {

    public static final byte MSG_CLEAR = 0;
    public static final byte MSG_POLL = 1;
    public static final byte MSG_OFFER = 2;

    @NonNull
    private final Handler handler;
    /**
     * 缓存列表
     */
    static final LinkedList<LogRecord> memoryLogQueue = new LinkedList<>();

    public MemoryLogStrategy(@NonNull Handler handler) {
        this.handler = Objects.requireNonNull(handler);

    }

    @Override
    public void log(int priority, @Nullable String tag, @NonNull String message) {
        this.handler.sendMessage(this.handler.obtainMessage(MSG_OFFER, priority, 0, message));
    }


    public final int logSize() {
        return memoryLogQueue.size();
    }

    public final LinkedList<LogRecord> cacheList() {
        return memoryLogQueue;
    }


    static class MemoryLogHandler extends Handler {

        /**
         * JAVA 1.8 使用char[]实现String 一个char占16个字节
         *      一个空 String 所占空间为：
         *      对象头（8 字节）+ 引用 (4 字节 )  + char 数组（16 字节）+ 1个 int（4字节）+ 1个long（8字节）= 40 字节。
         *      String占用内存计算公式：40 + 2*n，n为字符串长度。
         *      例：每一行是一个String对象，假设最大行数是3000，每行最大字符数是1000，则最大占用内存空间为
         *          3000x(40+2x1000) = 6120000B = 5.83649M
         * 更高版本内部使用的是byte[]计算方式大同小异
         */

        /**
         * 缓存最大行数
         */
        private int maxLines = 1024;
        /**
         * 最大字符数
         */
        private int maxLength = 1000;

        public MemoryLogHandler(@NonNull Looper looper, int maxLines, int maxLength) {
            super(looper);
            this.maxLines = maxLines;
            this.maxLength = maxLength;
        }

        public MemoryLogHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CLEAR:
                    memoryLogQueue.clear();
                    break;
                case MSG_POLL:
                    memoryLogQueue.poll();
                    break;
                case MSG_OFFER:
                    //超过最大行数，移除最早添加进来的日志
                    if (memoryLogQueue.size() >= maxLines) {
                        memoryLogQueue.poll();
                    }
                    String message = msg.obj.toString();
                    //如果大于最大字符数，则截取前面[0,maxLength)存入缓存
                    if (message.length() > maxLength) {
                        message = message.substring(0, maxLength);
                    }
                    memoryLogQueue.offer(new LogRecord(msg.arg1, message));
                    break;
                default:
                    break;
            }
        }
    }
}
