package cn.liujson.client.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;




import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.liujson.client.R;


/**
 * @author liujson
 * @date 2021/3/23.
 */
public class LogsPreviewView extends RelativeLayout {

    //-------------------------------------------------------------------------------------------
    /**
     * 默认日志队列大小
     */
    public static final int DEF_LOG_QUEUE_SIZE = 1024;
    /**
     * 默认的日志格式化
     */
    private final DefaultLogFormatStrategy defaultLogFormatStrategy = new DefaultLogFormatStrategy();


    //-------------------------------------------------------------------------------------------

    private RecyclerView mLogRecyclerView;

    private LogsPreviewAdapter adapter;

    /**
     * 日志记录列表
     */
    private final LinkedList<LogRecord> logRecords = new LinkedList<>();

    /**
     * 自定义格式化策略
     */
    private IFormatStrategy logFormatStrategy;
    /**
     * 日志缓存队列大小
     */
    private int logQueueSize;

    /**
     * 是否滑动到底部
     */
    private boolean isAutoScroll = true;

    public LogsPreviewView(Context context) {
        super(context);
        init();
    }

    public LogsPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LogsPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LogsPreviewView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        /*
         * 类似于这样的布局：
         *
         *     <HorizontalScrollView
         *         android:layout_width="match_parent"
         *         android:layout_margin="5dp"
         *         android:background="#000"
         *         android:layout_height="match_parent">
         *         <RelativeLayout
         *             android:layout_width="wrap_content"
         *             android:layout_height="match_parent">
         *             <androidx.recyclerview.widget.RecyclerView
         *                 android:id="@+id/logs_rv_list"
         *                 android:layout_width="2000dp"
         *                 android:layout_height="match_parent" />
         *         </RelativeLayout>
         *     </HorizontalScrollView>
         */

        final HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getContext());
        horizontalScrollView.setId(R.id.logs_horizontal_scroll);
        LayoutParams horizontalParams =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final int dp5 = getResources().getDimensionPixelSize(R.dimen.logs_dp5);
        horizontalParams.setMargins(0, dp5, 0, dp5);
        horizontalScrollView.setBackgroundResource(R.drawable.selector_log_card_bg);

        final RelativeLayout relativeLayout = new RelativeLayout(getContext());
        relativeLayout.setId(R.id.logs_wrapper_relative);
        LayoutParams relativeParams =
                new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        horizontalScrollView.addView(relativeLayout, relativeParams);

        mLogRecyclerView = new RecyclerView(getContext());
        mLogRecyclerView.setId(R.id.logs_rv_list);
        mLogRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mLogRecyclerView.addItemDecoration(new DividerItemDecoration());
        mLogRecyclerView.setAdapter(adapter = new LogsPreviewAdapter(logRecords));
        final int rvWidth = getResources().getDimensionPixelSize(R.dimen.logs_dp_rv_width);
        LayoutParams rvParams =
                new LayoutParams(rvWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        relativeLayout.addView(mLogRecyclerView, rvParams);
        mLogRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < 0) {
                    isAutoScroll = false;
                } else {
                    isAutoScroll = isSlideToBottom(recyclerView);
                }
            }
        });

        addView(horizontalScrollView, horizontalParams);
    }

    public void logD(@NonNull String message) {
        log(Level.D, null, message);
    }

    public void logD(String tag, @NonNull String message) {
        log(Level.D, tag, message);
    }

    public void logE(@NonNull String message) {
        log(Level.E, null, message);
    }

    public void logE(String tag, @NonNull String message) {
        log(Level.E, tag, message);
    }

    public void addLogs(List<LogRecord> logRecords) {
        this.logRecords.clear();
        this.logRecords.addAll(logRecords);
        adapter.notifyDataSetChanged();
        if (!logRecords.isEmpty()) {
            mLogRecyclerView.smoothScrollToPosition(logRecords.size() - 1);
        }
    }

    public void log(@NonNull Level level, String tag, @NonNull String message) {
        Objects.requireNonNull(level);
        Objects.requireNonNull(message);
        if (logRecords.size() > (logQueueSize > 0 ? logQueueSize : DEF_LOG_QUEUE_SIZE)) {
            logRecords.poll();
        }

        CharSequence logFormat;
        if (logFormatStrategy != null) {
            logFormat = logFormatStrategy.format(level, tag, message, System.currentTimeMillis());
        } else {
            logFormat = defaultLogFormatStrategy.format(level, tag, message, System.currentTimeMillis());
        }
        final LogRecord logRecord = new LogRecord();
        logRecord.level = level;
        logRecord.message = logFormat == null ? "" : logFormat.toString();
        logRecords.add(logRecord);
        adapter.notifyDataSetChanged();
        if (!logRecords.isEmpty() && isAutoScroll) {
            mLogRecyclerView.smoothScrollToPosition(logRecords.size() - 1);
        }
    }

    /**
     * 设置格式化策略
     */
    public void setLogFormatStrategy(IFormatStrategy logFormatStrategy) {
        this.logFormatStrategy = logFormatStrategy;
    }

    public void setLogQueueSize(int logQueueSize) {
        if (logQueueSize < 0 || logQueueSize == Integer.MAX_VALUE) {
            return;
        }
        this.logQueueSize = logQueueSize;
    }

    public int getLogQueueSize() {
        return logQueueSize == 0 ? DEF_LOG_QUEUE_SIZE : logQueueSize;
    }


    //region 内部类----------------------------------------------------------------------------------

    /**
     * 日志预览 Adapter
     */
    private static class LogsPreviewAdapter extends RecyclerView.Adapter<LogsPreviewAdapter.ViewHolder> {
        private final List<LogRecord> data;

        public LogsPreviewAdapter(@Nullable List<LogRecord> data) {
            Objects.requireNonNull(data);
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_logs_preview, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LogRecord logItem = data.get(position);
            holder.tvLog.setText(logItem.message);
            holder.tvLog.setTextColor(logItem.level.color);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvLog = itemView.findViewById(R.id.tv_log_line);
            }

            TextView tvLog;
        }
    }

    /**
     * 日志记录
     */
    public static class LogRecord implements Serializable {
        Level level;
        String message;

        public LogRecord(@NonNull Level level, @NonNull String message) {
            Objects.requireNonNull(level);
            Objects.requireNonNull(message);
            this.level = level;
            this.message = message;
        }

        LogRecord() {

        }

    }

    /**
     * 日志等级
     */
    public enum Level implements Serializable {
        /**
         * 对应Android的日志级别
         */
        V(android.util.Log.VERBOSE), D(android.util.Log.DEBUG), I(android.util.Log.INFO),
        W(android.util.Log.WARN, 0xFFFFA500),
        E(android.util.Log.ERROR, Color.RED);

        int level;
        int color = Color.WHITE;

        Level(int level) {
            this.level = level;
        }

        Level(int level, int color) {
            this.level = level;
            this.color = color;
        }

        public int level() {
            return level;
        }

        public int color() {
            return color;
        }

        public String levelName() {
            switch (this) {
                case V:
                    return "VERBOSE";
                case D:
                    return "DEBUG";
                case I:
                    return "INFO";
                case W:
                    return "WARN";
                case E:
                    return "ERROR";
                default:
                    return "UNKNOWN";
            }
        }

    }


    /**
     * 默认日志格式化策略
     */
    private static class DefaultLogFormatStrategy implements IFormatStrategy {

        private final SimpleDateFormat mSimpleDateFormat
                = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

        private final Date date;

        public DefaultLogFormatStrategy() {
            date = new Date();
        }

        @Override
        public CharSequence format(@NonNull Level level, String tag, @NonNull String message, long timeMillis) {

            StringBuilder builder = new StringBuilder();

            if (timeMillis > 0) {
                date.setTime(System.currentTimeMillis());
                builder.append(mSimpleDateFormat.format(new Date()));
                builder.append("    ");

                builder.append(String.format("%-16s", level.levelName()));
                builder.append(" --- ");
                builder.append(String.format("%-32s", tag == null ? "NO_TAG" : tag));
            }

            builder.append(" ---  :");
            builder.append(message);
            return builder.toString();
        }

    }

    /**
     * 分隔线
     */
    private static class DividerItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, @NonNull RecyclerView parent) {
            outRect.set(0, 0, 0, 8);
        }
    }

    /**
     * 格式化策略
     */
    public interface IFormatStrategy {
        /**
         * 格式策略
         *
         * @return 格式化后的文字样式
         */
        CharSequence format(@NonNull Level level, String tag, @NonNull String message, long timeMillis);
    }


    //endregion 内部类------------------------------------------------------------------------------

    /**
     * 最后一个可见
     *
     * @param recyclerView
     * @return
     */
    public static boolean isVisBottom(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        //屏幕中最后一个可见子项的position
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        //当前屏幕所看到的子项个数
        int visibleItemCount = layoutManager.getChildCount();
//         当前RecyclerView的所有子项个数
        int totalItemCount = layoutManager.getItemCount();
//         RecyclerView的滑动状态
        int state = recyclerView.getScrollState();
        if (visibleItemCount > 0
                && lastVisibleItemPosition > totalItemCount - 1
                && state == RecyclerView.SCROLL_STATE_IDLE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否滚动到底部
     * computeVerticalScrollExtent()是当前屏幕显示的区域高度，computeVerticalScrollOffset() 是当前屏幕之前滑过的距离，而computeVerticalScrollRange()是整个View控件的高度。
     * RecyclerView.canScrollVertically(1)的值表示是否能向上滚动，false表示已经滚动到底部
     * RecyclerView.canScrollVertically(-1)的值表示是否能向下滚动，false表示已经滚动到顶部
     *
     * @param recyclerView
     * @return
     */
    public static boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return false;
        }
        return recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange();
    }

}
