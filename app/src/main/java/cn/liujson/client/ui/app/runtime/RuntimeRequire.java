package cn.liujson.client.ui.app.runtime;

import java.util.ArrayList;
import java.util.List;

import cn.liujson.client.ui.widget.popup.MarkStarPopupView;

/**
 * 运行时需要的参数
 *
 * @author liujson
 * @date 2021/3/29.
 */
public class RuntimeRequire {

    private RuntimeRequire() {

    }

    public static RuntimeRequire getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final RuntimeRequire INSTANCE = new RuntimeRequire();
    }

    /**
     * 初始化需要订阅的Topic列表
     */
    private List<MarkStarPopupView.TopicWrapper> defineTopics = new ArrayList<>(3);


    public void updateDefineTopics(List<MarkStarPopupView.TopicWrapper> topics) {
        defineTopics.clear();
        defineTopics.addAll(topics);
    }
}
