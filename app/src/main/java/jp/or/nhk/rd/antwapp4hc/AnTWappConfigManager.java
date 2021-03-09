package jp.or.nhk.rd.antwapp4hc ;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Message;
import android.util.Log;

import org.json.JSONjava.JSONArray;
import org.json.JSONjava.JSONException;
import org.json.JSONjava.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import io.netty.buffer.ByteBuf;

/**
 * 設定情報管理
 */
class AnTWappConfigManager {
    private String config_path = null;

    //config
    private Map<String, Object> config = new HashMap<String, Object>();

    //media Data
    private static Map<String, String> mediaData = new HashMap<String, String>() {{
        put("TD", "Available");
        put("BS", "Available");
        put("CS", "Available");
        put("ABS", "Available");
        put("ACS", "Available");
        put("NCS", "Available");
    }};

    //channels Data
    private static Map<String, JSONObject> channelsObj_fromFile = new HashMap<String, JSONObject>() {{
        put("TD", new JSONObject());
        put("BS", new JSONObject());
        put("CS", new JSONObject());
        put("ABS", new JSONObject());
        put("ACS", new JSONObject());
        put("NCS", new JSONObject());
    }};

    //current channels Data
    private static Map<String, JSONObject> channelsObj = new HashMap<String, JSONObject>() {{
        put("TD", new JSONObject() );
        put("BS", new JSONObject());
        put("CS", new JSONObject());
    }};


    /**
     *
     * @param config_path Configパス（内臓ファイル）
     */
    AnTWappConfigManager( String config_path ) {
        this.config_path = config_path;

        //変更不可
        config.put( Const.Config.versionName.Name, null);
        config.put( Const.Config.userAgent.Name, Const.Config.userAgent.Value );


        //変更可能Configパラメターのデフォルト値設定

        config.put( Const.Config.tuneMode.Name, Const.Config.tuneMode.Value.Emulator );
        config.put( Const.Config.hcViewMode.Name, Const.Config.hcViewMode.Value.Full );
        config.put( Const.Config.aitLoad.Name, true);
        config.put( Const.Config.aitVerifierMode.Name, Const.Config.aitVerifierMode.Value.AllOK );
        config.put( Const.Config.aitVerifierUrl.Name, Const.Config.aitVerifierUrl.Url.getAllUrl() );
        config.put( Const.Config.aitVerificationMethod.Name, Const.Config.aitVerificationMethod.Value.POST );
        config.put( Const.Config.aitVerificationTimeout.Name, Const.Config.aitVerificationTimeout.DefaultValue );
        config.put( Const.Config.aitRequestTimeout.Name, Const.Config.aitRequestTimeout.DefaultValue );
        config.put( Const.Config.media.Name, "");
        config.put( Const.Config.channels.Name, "");
        config.put( Const.Config.channels4K8K.Name, "");
        config.put( Const.Config.channelsFrom.Name, "");
        config.put( Const.Config.tuneDelay.Name, Const.Config.tuneDelay.DefaultValue );
        config.put( Const.Config.SetUrl.Name, "");
        config.put( Const.Config.SetUrlAutoStart.Name, true);
        config.put( Const.Config.SetUrlAppTitle.Name, "");
        config.put( Const.Config.SetUrlAppDesc.Name, "");
        config.put( Const.Config.WSBroadcastMode.Name, true);
        config.put( Const.Config.mDNS.Name, true );
        config.put( Const.Config.support4K8K.Name, true);
        config.put( Const.Config.allowBIA.Name, true);
    }

    /**
     * WebViewへメッセージを送信
     * @param type 種別
     * @param obj 送信内容
     */
    private void activity_sendmsg(WebViewActivity.MessageType type, Object obj) {
        Message msg = new Message();
        msg.arg1 = type.getId();
        msg.obj = obj;
        WebViewActivity.sendmsg(msg);
    }

    /**
     * Debug情報送出
     * @param type
     * @param status
     * @param codepoint
     * @param message
     * @param color
     * @param remark
     */
    private void send_loginfo(String type, String status, String codepoint, String message, String color, String remark ) {
        Date dt = new Date();
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dtf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        String ctime = dtf.format(dt);

        JSONObject sendobj = new JSONObject()
                .put("type", type)
                .put("time", ctime)
                .put("status", status)
                .put("codepoint", codepoint)
                .put("message", message)
                .put("color", color)
                .put("remark", remark);
        String wsstr = sendobj.toString();
        HTTPServerInitializer.sendWSLog(wsstr);
    }

    /**
     * バージョン名の取得
     * @param context
     * @return
     */
    private String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            String versionInfo[] = packageInfo.versionName.split("_");

            //get Build DateTime
            long dtimeSec = Long.parseLong(versionInfo[1]);
            Date dt = new Date();
            dt.setTime(dtimeSec*1000);
            SimpleDateFormat dtf = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss+0900");
            dtf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            String dttimestr = dtf.format(dt);

            versionName = versionInfo[0] + "_" + dttimestr;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * Configの読み込み
     * @param context
     */
    private void readConfig(Context context) {
        //Device and Software Info
        config.put( Const.Config.versionName.Name, getVersionName(context) );

        try {
            byte [] content = WebViewActivity.readAssetTextFile( config_path );
            if(content == null) {
                //TODO:forOSS
            }
            else {
                String configJsonStr = new String(content, "UTF-8");
                Log.i("readConfig:", String.format("Config: %s", configJsonStr));
                try {
                    JSONObject configJson = new JSONObject(configJsonStr);
                    //設定の取り込み

                    if (configJson.has(Const.Config.aitLoad.Name)) {
                        config.put(Const.Config.aitLoad.Name, (Boolean) configJson.get(Const.Config.aitLoad.Name));
                    }
                    if (configJson.has(Const.Config.aitVerifierMode.Name)) {
                        config.put(Const.Config.aitVerifierMode.Name, configJson.get(Const.Config.aitVerifierMode.Name));
                    }
                    if (configJson.has(Const.Config.aitVerifierUrl.Name)) {
                        config.put(Const.Config.aitVerifierUrl.Name, configJson.get(Const.Config.aitVerifierUrl.Name));
                    }
                    if (configJson.has(Const.Config.aitVerificationMethod.Name)) {
                        config.put(Const.Config.aitVerificationMethod.Name, configJson.get(Const.Config.aitVerificationMethod.Name));
                    }
                    if (configJson.has(Const.Config.aitVerificationTimeout.Name)) {
                        config.put(Const.Config.aitVerificationTimeout.Name, configJson.get(Const.Config.aitVerificationTimeout.Name));
                    }
                    if (configJson.has(Const.Config.aitRequestTimeout.Name)) {
                        config.put(Const.Config.aitRequestTimeout.Name, configJson.get(Const.Config.aitRequestTimeout.Name));
                    }
                    if (configJson.has(Const.Config.media.Name)) {
                        config.put(Const.Config.media.Name, (String) configJson.get(Const.Config.media.Name));
                    }
                    if (configJson.has(Const.Config.channels.Name)) {
                        config.put(Const.Config.channels.Name, configJson.get(Const.Config.channels.Name));
                    }
                    if (configJson.has(Const.Config.channels4K8K.Name)) {
                        config.put(Const.Config.channels4K8K.Name, configJson.get(Const.Config.channels4K8K.Name));
                    }
                    if (configJson.has(Const.Config.channelsFrom.Name)) {
                        config.put(Const.Config.channelsFrom.Name, configJson.get(Const.Config.channelsFrom.Name));
                    }
                    if (configJson.has(Const.Config.tuneMode.Name)) {
                        config.put(Const.Config.tuneMode.Name, configJson.get(Const.Config.tuneMode.Name));
                    }
                    if (configJson.has(Const.Config.hcViewMode.Name)) {
                        config.put(Const.Config.hcViewMode.Name, configJson.get(Const.Config.hcViewMode.Name));
                    }
                    if (configJson.has(Const.Config.tuneDelay.Name)) {
                        config.put(Const.Config.tuneDelay.Name, configJson.getInt(Const.Config.tuneDelay.Name));
                    }
                    if (configJson.has(Const.Config.mDNS.Name)) {
                        config.put(Const.Config.mDNS.Name, configJson.get(Const.Config.mDNS.Name));
                    }
                    if (configJson.has(Const.Config.support4K8K.Name)) {
                        config.put(Const.Config.support4K8K.Name, (Boolean)configJson.get(Const.Config.support4K8K.Name));
                    }
                    if (configJson.has(Const.Config.allowBIA.Name)) {
                        config.put(Const.Config.allowBIA.Name, (Boolean)configJson.get(Const.Config.allowBIA.Name));
                    }
                } catch (org.json.JSONjava.JSONException e) {
                    //TODO:forOSS
                    Log.i("readConfig:", String.format("JSON Error"));
                }
            }
        }
        catch (IOException e) {
            //TODO:forOSS
            Log.i("readConfig:", String.format("Config Read Error %s", e.toString()));
        }
    }

    /**
     * Media configの読み込み
     */
    private void getMediaConfig() {
        //Media Info
        String mediaJsonStr = "";
        String mediaFname = (String) config.get(Const.Config.media.Name);
        if (!mediaFname.equals("")) {
            Log.i("getMediaConfig:", String.format("Media: %s", mediaFname));
            if (mediaFname.substring(0, 7).equals("http://") || mediaFname.substring(0, 8).equals("https://")) {
                Map<String,Object> ret = WebViewActivity.getHTTP(mediaFname,5000);
                mediaJsonStr = (String)ret.get(Const.HTTP.Response);
            }
            else {
                try {
                    mediaJsonStr = new String(WebViewActivity.readAssetTextFile(mediaFname), "UTF-8");
                }
                catch (IOException e) {
                    Log.i("getMediaConfig:", String.format("MediaFile Read Error %s", e.toString()));
                }
            }

            //Log.i("WebViewActivity:", String.format("Media: %s", mediaJsonStr));
            if (mediaJsonStr.equals("")) {
                Log.i("getMediaConfig:", String.format("MediaFile Read Error %s", mediaFname));
            }
            else {
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "getMediaConfig()", String.format("getMediaConfig:MediaJson from: %s", mediaFname), Const.LogGreen, "" );
                try {
                    JSONObject mediaJson = new JSONObject(mediaJsonStr);
                    if (mediaJson.has("TD")) { mediaData.put("TD", mediaJson.getString("TD")); }
                    if (mediaJson.has("BS")) { mediaData.put("BS", mediaJson.getString("BS")); }
                    if (mediaJson.has("CS")) { mediaData.put("CS", mediaJson.getString("CS")); }
                    if ((boolean)WebViewActivity.configMan().get(Const.Config.support4K8K.Name)) {
                        if (mediaJson.has("ABS")) { mediaData.put("ABS", mediaJson.getString("ABS")); }
                        if (mediaJson.has("ACS")) { mediaData.put("ACS", mediaJson.getString("ACS")); }
                        if (mediaJson.has("NCS")) { mediaData.put("NCS", mediaJson.getString("NCS")); }
                    }
                }
                catch (JSONException e) {
                    Log.i("getMediaConfig:", String.format("Media JSON Error %s", e.toString()));
                }
            }
        }
    }

    /**
     * Config File からのチャネル情報取得
     * @return
     */
    private void getAllChannelInfoFromFile() {
        String channelsJsonStr = "";
        String channelsFname = (String) config.get(Const.Config.channels.Name);
        channelsObj_fromFile.put("TD", new JSONObject());
        channelsObj_fromFile.put("BS", new JSONObject());
        channelsObj_fromFile.put("CS", new JSONObject());
        if ((boolean)WebViewActivity.configMan().get(Const.Config.support4K8K.Name)) {
            channelsFname = (String) config.get(Const.Config.channels4K8K.Name);
            channelsObj_fromFile.put("ABS", new JSONObject());
            channelsObj_fromFile.put("ACS", new JSONObject());
            channelsObj_fromFile.put("NCS", new JSONObject());
        }

        //read channels info
        if (channelsFname.substring(0, 7).equals("http://") || channelsFname.substring(0, 8).equals("https://")) {
            Map<String,Object> ret = WebViewActivity.getHTTP(channelsFname,5000);
            channelsJsonStr = (String)ret.get(Const.HTTP.Response);
        } else {
            try {
                channelsJsonStr = new String(WebViewActivity.readAssetTextFile(channelsFname), "UTF-8");
            } catch (IOException e) {
                Log.i("IndexPageHandler:", String.format("Channels File Read Error %s", e.toString()));
            }
        }
        //Log.i("WebViewActivity:", String.format("Channels: %s", channelsJsonStr));

        //Parse channels info
        if (channelsJsonStr.equals("")) {
            Log.i("IndexPageHandler:", String.format("Channels File Read Error %s", channelsFname));
        }
        else {
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "getAllChannelInfoFromFile()", String.format("getAllChannelInfoFromFile:ChannelsJson from: %s", channelsFname), Const.LogGreen, "" );
            try {
                JSONArray channelsJson = new JSONArray(channelsJsonStr);
                for (int i = 0; i < channelsJson.length(); i++) {
                    JSONObject obj = channelsJson.getJSONObject(i);
                    if (obj.get("type").equals("TD")) {
                        channelsObj_fromFile.put("TD", obj);
                    }
                    if (obj.get("type").equals("BS")) {
                        channelsObj_fromFile.put("BS", obj);
                    }
                    if (obj.get("type").equals("CS")) {
                        channelsObj_fromFile.put("CS", obj);
                    }
                    if ((boolean)WebViewActivity.configMan().get(Const.Config.support4K8K.Name)) {
                        if (obj.get("type").equals("ABS")) { channelsObj_fromFile.put("ABS", obj); }
                        if (obj.get("type").equals("ACS")) { channelsObj_fromFile.put("ACS", obj); }
                        if (obj.get("type").equals("NCS")) { channelsObj_fromFile.put("NCS", obj); }
                    }
                }
            }
            catch (JSONException e) {
                Log.i("IndexPageHandler:", String.format("Channels JSON Error %s", e.toString()));
            }
        }
    }

    /**
     * チャネル情報のセット
     */
    private void setChannelsInfo() {
        //Channels Info
        channelsObj.put("TD", new JSONObject());
        channelsObj.put("BS", new JSONObject());
        channelsObj.put("CS", new JSONObject());
        if ((boolean)WebViewActivity.configMan().get(Const.Config.support4K8K.Name)) {
            channelsObj.put("ABS", new JSONObject());
            channelsObj.put("ACS", new JSONObject());
            channelsObj.put("NCS", new JSONObject());
        }

        String channelsFrom = (String) config.get(Const.Config.channelsFrom.Name);
        if(channelsFrom.equals(Const.Config.channelsFrom.Value.File)) {
            getAllChannelInfoFromFile();
            channelsObj.put("TD", channelsObj_fromFile.get("TD"));
            channelsObj.put("BS", channelsObj_fromFile.get("BS"));
            channelsObj.put("CS", channelsObj_fromFile.get("CS"));
            if ((boolean)WebViewActivity.configMan().get(Const.Config.support4K8K.Name)) {
                channelsObj.put("ABS", channelsObj_fromFile.get("ABS"));
                channelsObj.put("ACS", channelsObj_fromFile.get("ACS"));
                channelsObj.put("NCS", channelsObj_fromFile.get("NCS"));
            }
        }
        else {
            Log.i("setChannelsInfo:", String.format("channelsFrom invalid: %s", channelsFrom));
        }
    }

    /**
     * Config関連情報の読み込み
     * @param context
     */
    public void loadConfig(Context context) {
        readConfig(context);

        getMediaConfig();

        setChannelsInfo();
    }

    /**
     * 設定情報の参照
     * @param key 参照する項目
     * @return 参照項目の内容
     */
    public Object get(String key) {
        return config.get(key);
    }

    /**
     *
     * @param media メディア
     * @return
     */
    public String getMediaStatus(String media) {
        return (String)mediaData.get(media);
    }

    /**
     * チャネルオブジェクト
     * @param media メディア
     * @return
     */
    public JSONObject getChannelsObj(String media) {
        return (JSONObject)channelsObj.get(media);
    }

    /**
     * チャネルオブジェクトの探索（現行情報）
     * @param nwid
     * @param trid
     * @param svid
     * @return
     */
    public JSONObject getChannelObj( int nwid, int trid,  int svid) {
        JSONObject channelObj = null ;
//        String[] channelsMedia = {"TD", "BS", "CS"};
        ArrayList<String> channelsMedia = new ArrayList<String>();
        if( getMediaStatus("TD").equals("Available") ) { channelsMedia.add("TD"); }
        if( getMediaStatus("BS").equals("Available") ) { channelsMedia.add("BS"); }
        if( getMediaStatus("CS").equals("Available") ) { channelsMedia.add("CS"); }
        if ((boolean)WebViewActivity.configMan().get(Const.Config.support4K8K.Name)) {
            if( getMediaStatus("ABS").equals("Available") ) { channelsMedia.add("ABS"); }
            if( getMediaStatus("ACS").equals("Available") ) { channelsMedia.add("ACS"); }
            if( getMediaStatus("NCS").equals("Available") ) { channelsMedia.add("NCS"); }
        }

        for( String media: channelsMedia ) {
            JSONObject chXXobj = channelsObj.get(media);
            try {
                JSONArray chobj = chXXobj.getJSONArray("channels");
                for (int i = 0; i < chobj.length(); i++) {
                    JSONObject channel = chobj.getJSONObject(i);
                    JSONObject resource = channel.getJSONObject("resource");
                    int ch_nwid = resource.getInt("original_network_id");
                    int ch_trid = 0;
                    if (media.equals("TD") || media.equals("BS") || media.equals("CS")) {
                        ch_trid = resource.getInt("transport_stream_id");
                    } else if (media.equals("ABS") || media.equals("ACS") || media.equals("NCS")) {
                        ch_trid = resource.getInt("tlv_stream_id");
                    }
                    int ch_svid = resource.getInt("service_id");

                    if ((ch_nwid == nwid) && (ch_trid == trid) && (ch_svid == svid)) {
                        channelObj = channel;
                        break;
                    }
                }
            } catch (JSONException e) {
                send_loginfo(Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "getChannelObj()", String.format("getChannelObj:JSONException: %s", e.getMessage()), Const.LogRed, "");
                throw new JSONException(e);
            }

            if (channelObj != null) {
                break;
            }
        }

        return channelObj;
    }
    /**
     * Configの更新
     * @param newurl 新しいHCsetURL
     * @return
     */
    public void updateSetURL(String newurl) {
        config.put(Const.Config.SetUrl.Name, newurl);
    }

    /**
     * Configの更新
     * @param paramsObj 新しいHCsetURL
     * @return
     */
    public void updateSetURLParams(JSONObject paramsObj) {
        /*
        expectedJsonObject
        {
          "url": URLString,
          "options":{
            "auto_start": bool,
            "app_title": String,
            "app_desc": String
        }
         */

        if( paramsObj.has("url")) {
            config.put(Const.Config.SetUrl.Name, paramsObj.getString("url"));
        }
        if( paramsObj.has("options")) {
            if (paramsObj.getJSONObject("options").has(Const.Config.SetUrlAutoStart.Name)) {
                config.put(Const.Config.SetUrlAutoStart.Name, paramsObj.getJSONObject("options").getBoolean(Const.Config.SetUrlAutoStart.Name));
            }
            if (paramsObj.getJSONObject("options").has(Const.Config.SetUrlAppTitle.Name)) {
                config.put(Const.Config.SetUrlAppTitle.Name, paramsObj.getJSONObject("options").getString(Const.Config.SetUrlAppTitle.Name));
            }
            if (paramsObj.getJSONObject("options").has(Const.Config.SetUrlAppDesc.Name)) {
                config.put(Const.Config.SetUrlAppDesc.Name, paramsObj.getJSONObject("options").getString(Const.Config.SetUrlAppDesc.Name));
            }
        }
    }


    /**
     * Configの更新
     * @param reqConfig 新しいConfig情報
     * @return
     */
    public void updateConfig(JSONObject reqConfig) {
        Boolean channlesObj_reset = false;
        Boolean channlesObjDevice_request = false;

        Log.i("updateConfig" , "updateConfig: " + reqConfig.toString());

        //Set New Value
        if( reqConfig.has(Const.Config.aitLoad.Name)) {
            Boolean aitload = (Boolean) reqConfig.get(Const.Config.aitLoad.Name);
            config.put(Const.Config.aitLoad.Name, aitload);
        }
        if( reqConfig.has(Const.Config.aitVerifierMode.Name)) {
            String aitVerifierMode = (String) reqConfig.get(Const.Config.aitVerifierMode.Name);
            config.put(Const.Config.aitVerifierMode.Name, aitVerifierMode);
        }
        if( reqConfig.has(Const.Config.aitVerifierUrl.Name)) {
            String aitVerifierUrl = (String) reqConfig.get(Const.Config.aitVerifierUrl.Name);
            config.put(Const.Config.aitVerifierUrl.Name, aitVerifierUrl);
        }
        if( reqConfig.has(Const.Config.aitVerificationMethod.Name)) {
            String aitVerificationMethod = (String) reqConfig.get(Const.Config.aitVerificationMethod.Name);
            config.put(Const.Config.aitVerificationMethod.Name, aitVerificationMethod);
        }
        if( reqConfig.has(Const.Config.aitVerificationTimeout.Name)) {
            int aitVerificationTimeout = (int) reqConfig.get(Const.Config.aitVerificationTimeout.Name);
            config.put(Const.Config.aitVerificationTimeout.Name, aitVerificationTimeout);
        }
        if( reqConfig.has(Const.Config.aitRequestTimeout.Name)) {
            int aitRequestTimeout = (int) reqConfig.get(Const.Config.aitRequestTimeout.Name);
            config.put(Const.Config.aitRequestTimeout.Name, aitRequestTimeout);
        }
        if( reqConfig.has(Const.Config.channelsFrom.Name)) {
            String channelsFrom = (String)reqConfig.get(Const.Config.channelsFrom.Name);
            if( !channelsFrom.equals( config.get(Const.Config.channelsFrom.Name)) ) {
                channlesObj_reset = true;
            }
            config.put(Const.Config.channelsFrom.Name, channelsFrom);
        }
        if( reqConfig.has(Const.Config.tuneMode.Name)) {
            String tuneMode = (String)reqConfig.get(Const.Config.tuneMode.Name);
            config.put(Const.Config.tuneMode.Name, tuneMode);
        }
        if( reqConfig.has(Const.Config.hcViewMode.Name)) {
            String hcViewMode = (String)reqConfig.get(Const.Config.hcViewMode.Name);
            config.put(Const.Config.hcViewMode.Name, hcViewMode);
        }
        if( reqConfig.has(Const.Config.tuneDelay.Name)) {
            int tuneDelay = (int)reqConfig.get(Const.Config.tuneDelay.Name);
            config.put(Const.Config.tuneDelay.Name, tuneDelay);
        }
        if( reqConfig.has(Const.Config.WSBroadcastMode.Name)) {
            Boolean wsBroadcastMode = (Boolean) reqConfig.get(Const.Config.WSBroadcastMode.Name);
            config.put(Const.Config.WSBroadcastMode.Name, wsBroadcastMode);
        }
        if( reqConfig.has(Const.Config.mDNS.Name)) {
            Boolean mDNS = (Boolean) reqConfig.get(Const.Config.mDNS.Name);
            config.put(Const.Config.mDNS.Name, mDNS);
        }
        if( reqConfig.has(Const.Config.support4K8K.Name)) {
            Boolean support4K8K = (Boolean) reqConfig.get(Const.Config.support4K8K.Name);
            config.put(Const.Config.support4K8K.Name, support4K8K);
        }
        if( reqConfig.has(Const.Config.allowBIA.Name)) {
            Boolean allowBIA = (Boolean) reqConfig.get(Const.Config.allowBIA.Name);
            config.put(Const.Config.allowBIA.Name, allowBIA);
        }

        if( channlesObj_reset ) {
            setChannelsInfo();
        }
    }
}
