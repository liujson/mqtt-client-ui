package cn.liujson.client.ui.widget.popup.interfaces;

import android.view.View;

import com.lxj.xpopup.core.BasePopupView;

/**
 * @author liujson
 * @date 2021/2/24.
 */
public interface OnPopupClickListener {

    /**
     * 弹框中的点击事件
     * @param popupView
     * @param v
     */
    void onClick(BasePopupView popupView, View v);
}
