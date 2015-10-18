package com.wenym.grooo.http.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.wenym.grooo.GroooApplication;
import com.wenym.grooo.http.model.CancelOrderData;
import com.wenym.grooo.http.model.CancelOrderSuccessData;
import com.wenym.grooo.http.model.CheckUpdateData;
import com.wenym.grooo.http.model.CheckUpdateSuccessData;
import com.wenym.grooo.http.model.FetchKuaidiData;
import com.wenym.grooo.http.model.FetchKuaidiSuccessData;
import com.wenym.grooo.http.model.GetBaiduPictureData;
import com.wenym.grooo.http.model.GetBaiduPictureSuccessData;
import com.wenym.grooo.http.model.GetMenuData;
import com.wenym.grooo.http.model.GetMenuSuccessData;
import com.wenym.grooo.http.model.GetNewsData;
import com.wenym.grooo.http.model.GetNewsSuccessData;
import com.wenym.grooo.http.model.GetOrderData;
import com.wenym.grooo.http.model.GetOrderSuccessData;
import com.wenym.grooo.http.model.GetRestaurantData;
import com.wenym.grooo.http.model.GetRestaurantSuccessData;
import com.wenym.grooo.http.model.InitBuildingData;
import com.wenym.grooo.http.model.InitBuildingsSuccessData;
import com.wenym.grooo.http.model.InitRestaurantData;
import com.wenym.grooo.http.model.InitRestaurantSuccessData;
import com.wenym.grooo.http.model.LoginData;
import com.wenym.grooo.http.model.LoginSuccessData;
import com.wenym.grooo.http.model.OrderFoodData;
import com.wenym.grooo.http.model.OrderFoodSuccessData;
import com.wenym.grooo.http.model.PushInfoData;
import com.wenym.grooo.http.model.PushInfoSuccessData;
import com.wenym.grooo.http.model.RegistData;
import com.wenym.grooo.http.model.RegistSuccessData;
import com.wenym.grooo.http.model.SuggestData;
import com.wenym.grooo.http.model.SuggestSuccessData;
import com.wenym.grooo.utils.SharedPreferencesUtil;
import com.wenym.grooo.utils.SmallTools;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class HttpUtils {

    private static AsyncHttpClient httpClient;
    private static SyncHttpClient syncHttpClient;

    public static void init(Context context) {
        httpClient = new AsyncHttpClient();
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        httpClient.setCookieStore(cookieStore);
        httpClient.setEnableRedirects(true, true, true);
        syncHttpClient = new SyncHttpClient();
        syncHttpClient.setEnableRedirects(true, true, true);
    }

    public static void CancelHttpTask() {
        httpClient.cancelAllRequests(true);
    }

    public static void InitBuildings(InitBuildingData data, final HttpCallBack callBack) {
        httpClient.post(HttpConstants.GETBUILDING, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                String buildinglist = GroooApplication.getAppSharedPreferences().getString("buildinglist", "");
                InitBuildingsSuccessData buildingsSuccessData = new InitBuildingsSuccessData(buildinglist);
                if (buildingsSuccessData.getBuildings().size() == 0) {
                    callBack.onError(statusCode);
                } else {
                    callBack.onSuccess(buildingsSuccessData);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                GroooApplication.getAppSharedPreferences().edit().putString("buildinglist", responseString).commit();
                InitBuildingsSuccessData buildingsSuccessData = new InitBuildingsSuccessData(responseString);
                callBack.onSuccess(buildingsSuccessData);
            }
        });
    }

    public static void InitRestaurantList(InitRestaurantData data, final HttpCallBack callBack) {
        httpClient.post(HttpConstants.GETRESTAURANLISTTURL, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  String responseString, Throwable throwable) {
                callBack.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  String response) {
                GroooApplication.getAppSharedPreferences().edit().putString("restaurantlist", response).commit();
                if (response.equals("")) {
                    callBack.onFailed();
                    return;
                }
                callBack.onSuccess(new InitRestaurantSuccessData(response));
            }

        });
    }

    public static void CheckUpdate(CheckUpdateData data, Context context,
                                   final HttpCallBack callBack) {
        httpClient.post(HttpConstants.CHECKUPDATEURL,
                new TextHttpResponseHandler() {

                    @Override
                    public void onSuccess(int arg0, Header[] arg1, String arg2) {
                        CheckUpdateSuccessData updateSuccessData = new Gson().fromJson(arg2, CheckUpdateSuccessData.class);
                        callBack.onSuccess(updateSuccessData);
                    }

                    @Override
                    public void onFailure(int arg0, Header[] arg1, String arg2,
                                          Throwable arg3) {
                        callBack.onError(arg0);
                    }
                });
    }

    public static void Login(final LoginData data, Context context,
                             final HttpCallBack callBack) {
//        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
//        List<Cookie> cookies = myCookieStore.getCookies();
//        for (Cookie cookie : cookies) {
//            Toasts.show(cookie.getName() + " = " + cookie.getValue());
//        }
        httpClient.post(context, HttpConstants.LOGINURL, data.toStringEntity(),
                HttpConstants.contentType, new JsonHttpResponseHandler() {


                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseString, Throwable throwable) {
                        callBack.onError(statusCode);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONObject response) {
                        SharedPreferences.Editor editor = GroooApplication.getAppSharedPreferences().edit();
                        if ("401".equals(response.toString())) {
                            callBack.onFailed();
                            SharedPreferencesUtil.clearAll();
                        } else {
                            LoginSuccessData loginSuccessData = new Gson().fromJson(response.toString(), LoginSuccessData.class);
                            SharedPreferencesUtil.setUsername(data.getUsername());
                            SharedPreferencesUtil.setPassword(data.getPassword());
                            SharedPreferencesUtil.setUserBuilding(SmallTools.buildingIdToText(GroooApplication.buildings, loginSuccessData.getBuildingNum()));
                            SharedPreferencesUtil.setUserRoom(loginSuccessData.getRoomNum());
                            SharedPreferencesUtil.setUserId(loginSuccessData.getUserid());
                            callBack.onSuccess(loginSuccessData);
                        }
                    }

                });
    }

    public static void Suggest(SuggestData data, Context context,
                               final HttpCallBack callBack) throws UnsupportedEncodingException {
        data.setUserid(SharedPreferencesUtil.getUserId());
        httpClient
                .post(context, HttpConstants.SUGGESTUSURL,
                        new StringEntity(new Gson().toJson(data)),
                        HttpConstants.contentType,
                        new TextHttpResponseHandler() {

                            @Override
                            public void onSuccess(int arg0, Header[] arg1, String arg2) {
                                callBack.onSuccess(new SuggestSuccessData());
                            }

                            @Override
                            public void onFailure(int arg0, Header[] arg1, String arg2, Throwable arg3) {
                                callBack.onError(arg0);
                            }
                        });
    }

    public static void GetNews(final HttpCallBack callBack) {
        httpClient.post(HttpConstants.NEWSURL, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                GetNewsSuccessData newsSuccessData = new GetNewsSuccessData(responseString);
                callBack.onSuccess(newsSuccessData);
            }
        });
    }

    public static void Regist(RegistData data, Context context,
                              final HttpCallBack callBack) throws UnsupportedEncodingException {

        httpClient.post(context, HttpConstants.REGISTERURL, new StringEntity(data.getRegistdata()), HttpConstants.contentType, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callBack.onSuccess(new RegistSuccessData(responseString));
            }

        });

    }

    public static void GetOrder(GetOrderData data, Context context,
                                final HttpCallBack callBack) throws UnsupportedEncodingException {
        data.setUserid(SharedPreferencesUtil.getUserId());
        httpClient.post(context, HttpConstants.GETORDERURL, new StringEntity(new Gson().toJson(data)), HttpConstants.contentType, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  String responseString, Throwable throwable) {
                callBack.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  JSONObject response) {
                callBack.onSuccess(new Gson().fromJson(response.toString(), GetOrderSuccessData.class));
            }
        });
    }

    public static void CancelOrder(CancelOrderData data, Context context, final HttpCallBack callBack) throws UnsupportedEncodingException {
        data.setUserid(SharedPreferencesUtil.getUserId());
        httpClient.post(context, HttpConstants.CANCELORDERURL, new StringEntity(new Gson().toJson(data)), HttpConstants.contentType, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if ("退单成功".equals(responseString)) {
                    callBack.onSuccess(new CancelOrderSuccessData(responseString));
                } else {
                    callBack.onFailed();
                }
            }
        });
    }

    public static void GetRestaurants(Context context, final HttpCallBack callBack) {
        httpClient.post(HttpConstants.GETRESTAURANTURL, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callBack.onSuccess(new GetRestaurantSuccessData(responseString));
            }
        });
    }

    public static void GetMenu(GetMenuData data, Context context, final HttpCallBack callBack) {
        httpClient.post(HttpConstants.GETRESTAURANTMENUURL + data.getId() + "/getmenu/", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callBack.onSuccess(new GetMenuSuccessData(responseString));
            }
        });
    }

    public static void OrderFood(OrderFoodData data, Context context, final HttpCallBack callBack) throws UnsupportedEncodingException {
        data.setUserid(SharedPreferencesUtil.getUserId());
        httpClient.post(context, HttpConstants.ORDERFOODURL, new StringEntity(new Gson().toJson(data)),
                HttpConstants.contentType, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        callBack.onError(statusCode);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        callBack.onSuccess(new OrderFoodSuccessData(responseString));
                    }
                });
    }

    public static void FetchKuaidi(FetchKuaidiData data, Context context, final HttpCallBack callback) throws UnsupportedEncodingException {
        data.setUserid(SharedPreferencesUtil.getUserId());
        httpClient.post(context, HttpConstants.ORDERKUAIDIURL, new StringEntity(new Gson().toJson(data)), HttpConstants.contentType, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callback.onSuccess(new FetchKuaidiSuccessData(responseString));
            }
        });
    }

    public static void SetPushInfo(PushInfoData data, Context context, final HttpCallBack callback) throws UnsupportedEncodingException {
        data.setUid(SharedPreferencesUtil.getUserId());
        httpClient.post(context, HttpConstants.SETPUSHINFO, new StringEntity(new Gson().toJson(data)), HttpConstants.contentType, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callback.onSuccess(new PushInfoSuccessData(responseString));
            }
        });
    }

    public static void getBaiduPicture(GetBaiduPictureData data, Context context, final HttpCallBack callback) throws UnsupportedEncodingException {
        httpClient.post(HttpConstants.GETBAIDUPICTURE, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callback.onSuccess(new Gson().fromJson(responseString, GetBaiduPictureSuccessData.class));
            }
        });
    }

    public static void MakeAPICall(Object data, Context context, final HttpCallBack callback) {
        try {
            if (data instanceof CancelOrderData) {
                CancelOrder((CancelOrderData) data, context, callback);
            } else if (data instanceof FetchKuaidiData) {
                FetchKuaidi((FetchKuaidiData) data, context, callback);
            } else if (data instanceof GetMenuData) {
                GetMenu((GetMenuData) data, context, callback);
            } else if (data instanceof CheckUpdateData) {
                CheckUpdate((CheckUpdateData) data, context, callback);
            } else if (data instanceof GetNewsData) {
                GetNews(callback);
            } else if (data instanceof GetOrderData) {
                GetOrder((GetOrderData) data, context, callback);
            } else if (data instanceof GetRestaurantData) {
                GetRestaurants(context, callback);
            } else if (data instanceof InitBuildingData) {
                InitBuildings((InitBuildingData) data, callback);
            } else if (data instanceof InitRestaurantData) {
                InitRestaurantList((InitRestaurantData) data, callback);
            } else if (data instanceof LoginData) {
                Login((LoginData) data, context, callback);
            } else if (data instanceof OrderFoodData) {
                OrderFood((OrderFoodData) data, context, callback);
            } else if (data instanceof PushInfoData) {
                SetPushInfo((PushInfoData) data, context, callback);
            } else if (data instanceof RegistData) {
                Regist((RegistData) data, context, callback);
            } else if (data instanceof SuggestData) {
                Suggest((SuggestData) data, context, callback);
            } else if (data instanceof GetBaiduPictureData) {
                getBaiduPicture((GetBaiduPictureData) data, context, callback);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}