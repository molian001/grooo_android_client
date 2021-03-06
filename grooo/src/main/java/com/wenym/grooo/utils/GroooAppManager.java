package com.wenym.grooo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.testin.agent.TestinAgent;
import com.wenym.grooo.R;
import com.wenym.grooo.http.model.GetBaiduPictureData;
import com.wenym.grooo.http.model.GetBaiduPictureSuccessData;
import com.wenym.grooo.http.model.GetOrderData;
import com.wenym.grooo.http.model.GetOrderSuccessData;
import com.wenym.grooo.http.model.InitBuildingData;
import com.wenym.grooo.http.model.InitBuildingsSuccessData;
import com.wenym.grooo.http.util.HttpCallBack;
import com.wenym.grooo.http.util.HttpUtils;
import com.wenym.grooo.model.app.AppUser;
import com.wenym.grooo.model.app.Building;
import com.wenym.grooo.model.ecnomy.DeliveryOrder;
import com.wenym.grooo.model.ecnomy.FoodOrder;
import com.wenym.grooo.model.ecnomy.Restaurant;
import com.wenym.grooo.widgets.Toasts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by runzii on 15-10-30.
 */
public class GroooAppManager {

    private static DisplayImageOptions options;
    private static ArrayList<Building> buildings;
    private static List<FoodOrder> takeouts;
    private static List<DeliveryOrder> deliveries;
    private static Restaurant favorite;
    private static String home_back;

    private static AppUser appUser = null;

    private static Context appContext = null;

    public static void init(Context ctx) {
        appContext = ctx;
        options = new DisplayImageOptions.Builder().cacheOnDisk(true)
                .cacheInMemory(true).showImageOnFail(R.drawable.responsible)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        home_back = SmallTools.resourceIdToUri(R.drawable.grooo_spalash);
    }

    public static void initUserData(InitCallback callback) {
        GroooAppManager.callback = callback;
        options = new DisplayImageOptions.Builder().cacheOnDisk(true)
                .cacheInMemory(true).showImageOnFail(R.drawable.responsible)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        appUser = PreferencesUtil.getInstance().getAppUser();
        initBaidu(appContext);
        if (appUser != null) {
            TestinAgent.setUserInfo(appUser.getUsername());
        }
    }


    private static InitCallback callback;

    public static interface InitCallback {

        void onSuccess();
    }

    public synchronized static void initOrders(Context ctx) {

        HttpUtils.MakeAPICall(new GetOrderData(), ctx, new HttpCallBack() {
            @Override
            public void onSuccess(Object object) {
                GetOrderSuccessData data = (GetOrderSuccessData) object;
                if (data.getRestaurant().size() != 0) {
                    takeouts = data.getRestaurant();
                }
                if (data.getDelivery().size() != 0) {
                    deliveries = data.getDelivery();
                }
                initFavorite();
            }

            @Override
            public void onFailed(String reason) {
                Toasts.show(reason);
            }


            @Override
            public void onError(int statusCode) {
                Toasts.show("orders " + statusCode);
            }
        });

    }

    public static synchronized void initFavorite() {

        String shopid = "";

        if (PreferencesUtil.getInstance().getFavoriteShops().size() != 0) {
            shopid = PreferencesUtil.getInstance().getFavoriteShops().iterator().next();
        }
        HttpUtils.GetOneRestaurant(shopid, new HttpCallBack() {
            @Override
            public void onSuccess(Object object) {
                favorite = (Restaurant) object;
                initSuccess();
            }

            @Override
            public void onFailed(String reason) {
                Toasts.show(reason);
            }

            @Override
            public void onError(int statusCode) {
//                Toasts.show("initFavorite " + statusCode);
                initSuccess();
            }
        });
    }

    public static String getHome_back() {
        return home_back;
    }

    private static void initBaidu(final Context ctx) {
        HttpUtils.MakeAPICall(new GetBaiduPictureData(), ctx, new HttpCallBack() {
            @Override
            public void onSuccess(Object object) {

                GetBaiduPictureSuccessData data = (GetBaiduPictureSuccessData) object;

                home_back = data.getData().get(new Random().nextInt(10)).getImage_url();

                initBuildings(ctx);

            }

            @Override
            public void onFailed(String reason) {
                Toasts.show(reason);
            }

            @Override
            public void onError(int statusCode) {
                initBuildings(ctx);
            }
        });

    }

    private static void initBuildings(final Context ctx) {
        HttpUtils.MakeAPICall(new InitBuildingData(), ctx, new HttpCallBack() {
            @Override
            public void onSuccess(Object object) {
                InitBuildingsSuccessData buildingsSuccessData = (InitBuildingsSuccessData) object;

                if (buildingsSuccessData.getBuildings().size() != 0) {
                    PreferencesUtil.getInstance().setBuildings(buildingsSuccessData);
                    buildings = buildingsSuccessData.getBuildings();
                } else {
                    buildings = PreferencesUtil.getInstance().getBuildings();
                }

                if (appUser != null) {
                    initOrders(ctx);
                } else {
                    initSuccess();
                }
            }

            @Override
            public void onFailed(String reason) {
                Toasts.show(reason);
            }

            @Override
            public void onError(int statusCode) {
                if (appUser != null) {
                    initOrders(ctx);
                } else {
                    initSuccess();
                }
            }
        });
    }

    private static void initSuccess() {
        if (callback != null) {
            callback.onSuccess();
        }
        callback = null;
    }


    public static AppUser getAppUser() {
        if (appUser == null) {
            appUser = PreferencesUtil.getInstance().getAppUser();
        }
        return appUser;
    }

    public static void setAppUser(AppUser user) {
        appUser = user;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static void setAppContext(Context appContext) {
        GroooAppManager.appContext = appContext;
    }

    public static DisplayImageOptions getOptions() {
        return options;
    }

    public static ArrayList<Building> getBuildings() {
        if (buildings == null || buildings.size() == 0) {
            initBuildings(appContext);
            return PreferencesUtil.getInstance().getBuildings();
        } else {
            return buildings;
        }
    }

    public static Restaurant getFavorite() {
        if (favorite == null) {
            initFavorite();
        }
        return favorite;
    }

    public static List<FoodOrder> getTakeouts() {
        if (takeouts == null) {
            initOrders(appContext);
            return null;
        }
        return takeouts;
    }

    public static List<DeliveryOrder> getDeliveries() {
        if (deliveries == null) {
            initOrders(appContext);
            return null;
        }
        return deliveries;
    }

    //包名
    private static String pkgName = "com.wenym.grooo";

    public static String getPackageName() {
        return pkgName;
    }

    /**
     * 获取应用程序版本名称
     *
     * @return
     */
    public static String getVersion() {
        String version = "0.0.0";
        if (appContext == null) {
            return version;
        }
        try {
            PackageInfo packageInfo = appContext.getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    public static int getVersionCode() {
        int versionCode = 0;
        try {
            versionCode = appContext.getPackageManager().getPackageInfo(
                    getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }
}
