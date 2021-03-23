package cn.liujson.client.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SwipeLayout;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import cn.liujson.client.R;
import cn.liujson.client.ui.db.entities.ConnectionProfile;

/**
 * @author liujson
 * @date 2021/3/3.
 */
public class ConnectionProfilesAdapter extends BaseQuickAdapter<ConnectionProfilesAdapter.ItemProfile, BaseViewHolder> {


    public ConnectionProfilesAdapter(@Nullable List<ItemProfile> data) {
        super(R.layout.item_connection_profile, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ItemProfile itemProfile) {
        final SwipeLayout swipeLayout = holder.findView(R.id.swipe);
        holder.setText(R.id.tv_profile_name, itemProfile.profileName);
        holder.setText(R.id.tv_broker, itemProfile.brokerAddress + ":" + itemProfile.brokerPort);

        assert swipeLayout != null;

        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
//                YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.rl_actions));
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });
    }

    public static class ItemProfile {

        public int id;
        public String profileName;
        public String brokerAddress;
        public int brokerPort;

        public static ItemProfile covert(@NonNull ConnectionProfile connectionProfile) {
            ItemProfile itemProfile = new ItemProfile();
            itemProfile.profileName = connectionProfile.profileName;
            itemProfile.id = connectionProfile.id;
            itemProfile.brokerAddress = connectionProfile.brokerAddress;
            itemProfile.brokerPort = connectionProfile.brokerPort;
            return itemProfile;
        }
    }
}
