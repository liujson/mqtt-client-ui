package cn.liujson.client.ui.widget;

import android.view.View;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;


/**
 * 通过监听 ChipGroup 实现单选不可取消功能
 *
 * @author liujson
 * @date 2021/3/10.
 */
public class OnSingleCheckedListener implements ChipGroup.OnCheckedChangeListener {

    /**
     * 最后一次选中的Id
     */
    private int lastCheckedChipId = View.NO_ID;

    public OnSingleCheckedListener(ChipGroup group) {
        this.lastCheckedChipId = group.getCheckedChipId();
    }

    @Override
    public void onCheckedChanged(ChipGroup group, int checkedId) {
        if (checkedId == View.NO_ID) {
            if (lastCheckedChipId != View.NO_ID) {
                final View view = group.findViewById(lastCheckedChipId);
                if (view instanceof Chip) {
                    ((Chip) view).setChecked(true);
                }
            }
        } else {
            lastCheckedChipId = checkedId;
        }
    }
}
