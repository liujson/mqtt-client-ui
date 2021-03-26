package cn.liujson.client.ui.util;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.NetworkRequest.Builder;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 网络工具类
 *
 * @author liujson
 */
public final class NetworkUtils {
    private static NetworkCallback mNetworkCallback;

    private NetworkUtils() {
        throw new UnsupportedOperationException("无法初始化！");
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static boolean isConnected(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected();
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static boolean useMobileNetwork(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == 0;
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static boolean use4G(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected() && networkInfo.getSubtype() == 13;
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        } else {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.getType() == 1;
        }
    }

    private static boolean isSpace(String str) {
        if (str != null) {
            int i = 0;

            for (int len = str.length(); i < len; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }

        }
        return true;
    }

    @RequiresPermission(Manifest.permission.INTERNET)
    public static String getIPAddress(boolean useIpv4) {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            LinkedList adds = new LinkedList();

            label64:
            while (true) {
                NetworkInterface ni;
                do {
                    do {
                        if (!nis.hasMoreElements()) {
                            Iterator var9 = adds.iterator();

                            while (var9.hasNext()) {
                                InetAddress add = (InetAddress) var9.next();
                                if (!add.isLoopbackAddress()) {
                                    String hostAddress = add.getHostAddress();
                                    boolean isIpv4 = hostAddress.indexOf(58) < 0;
                                    if (useIpv4) {
                                        if (isIpv4) {
                                            return hostAddress;
                                        }
                                    } else if (!isIpv4) {
                                        int index = hostAddress.indexOf(37);
                                        return index < 0 ? hostAddress.toUpperCase() : hostAddress.substring(0, index).toUpperCase();
                                    }
                                }
                            }
                            break label64;
                        }

                        ni = (NetworkInterface) nis.nextElement();
                    } while (!ni.isUp());
                } while (ni.isLoopback());

                Enumeration addresses = ni.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    adds.addFirst(addresses.nextElement());
                }
            }
        } catch (SocketException var8) {
            var8.printStackTrace();
        }

        return "";
    }

    @RequiresPermission("android.permission.CHANGE_WIFI_STATE")
    public static void setWifiEnabled(Context context, boolean enabled) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            if (enabled != manager.isWifiEnabled()) {
                manager.setWifiEnabled(enabled);
            }
        }
    }

    @RequiresPermission("android.permission.CHANGE_NETWORK_STATE")
    public static void registerNetwork(Context context, @NonNull final NetworkUtils.ArcNetworkCallback callback) {
        if (VERSION.SDK_INT >= 24) {
            if (mNetworkCallback == null) {
                mNetworkCallback = new NetworkCallback() {
                    public void onAvailable(Network network) {
                        if (callback != null) {
                            callback.isConnected(true);
                        }

                    }

                    public void onLost(Network network) {
                        if (callback != null) {
                            callback.isConnected(false);
                        }

                    }
                };
            }

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.requestNetwork((new Builder()).build(), mNetworkCallback);
            }
        }

    }

    public static void unRegisterNetwork(Context context) {
        if (VERSION.SDK_INT >= 24 && mNetworkCallback != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(mNetworkCallback);
            mNetworkCallback = null;
        }

    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static NetworkUtils.NetworkType getNetworkType(Context context) {
        if (isEthernet(context)) {
            return NetworkUtils.NetworkType.NETWORK_ETHERNET;
        } else {
            NetworkInfo info = getActiveNetworkInfo(context);
            if (info != null && info.isAvailable()) {
                if (info.getType() == 1) {
                    return NetworkUtils.NetworkType.NETWORK_WIFI;
                } else if (info.getType() == 0) {
                    switch (info.getSubtype()) {
                        case 1:
                        case 2:
                        case 4:
                        case 7:
                        case 11:
                        case 16:
                            return NetworkUtils.NetworkType.NETWORK_2G;
                        case 3:
                        case 5:
                        case 6:
                        case 8:
                        case 9:
                        case 10:
                        case 12:
                        case 14:
                        case 15:
                        case 17:
                            return NetworkUtils.NetworkType.NETWORK_3G;
                        case 13:
                        case 18:
                            return NetworkUtils.NetworkType.NETWORK_4G;
                        default:
                            String subtypeName = info.getSubtypeName();
                            return !"TD-SCDMA".equalsIgnoreCase(subtypeName) && !"WCDMA".equalsIgnoreCase(subtypeName) && !"CDMA2000".equalsIgnoreCase(subtypeName) ? NetworkUtils.NetworkType.NETWORK_UNKNOWN : NetworkUtils.NetworkType.NETWORK_3G;
                    }
                } else {
                    return NetworkUtils.NetworkType.NETWORK_UNKNOWN;
                }
            } else {
                return NetworkUtils.NetworkType.NETWORK_NO;
            }
        }
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    private static boolean isEthernet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        } else {
            NetworkInfo info = cm.getNetworkInfo(9);
            if (info == null) {
                return false;
            } else {
                State state = info.getState();
                if (null == state) {
                    return false;
                } else {
                    return state == State.CONNECTED || state == State.CONNECTING;
                }
            }
        }
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm == null ? null : cm.getActiveNetworkInfo();
    }

    public static enum NetworkType {
        NETWORK_ETHERNET,
        NETWORK_WIFI,
        NETWORK_4G,
        NETWORK_3G,
        NETWORK_2G,
        NETWORK_UNKNOWN,
        NETWORK_NO;

        private NetworkType() {
        }
    }

    public interface ArcNetworkCallback {
        void isConnected(boolean var1);
    }
}