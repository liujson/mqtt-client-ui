package cn.liujson.client.ui.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

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
        holder.setText(R.id.tv_profile_name, itemProfile.profileName);
        holder.setText(R.id.tv_broker, itemProfile.brokerAddress + ":" + itemProfile.brokerPort);
    }

    public static class ItemProfile {

        public String profileName;
        public String brokerAddress;
        public int brokerPort;

        public static ItemProfile covert(@NonNull ConnectionProfile connectionProfile) {
            ItemProfile itemProfile = new ItemProfile();
            itemProfile.profileName = connectionProfile.profileName;
            itemProfile.brokerAddress = connectionProfile.brokerAddress;
            itemProfile.brokerPort = connectionProfile.brokerPort;
            return itemProfile;
        }
    }
}
