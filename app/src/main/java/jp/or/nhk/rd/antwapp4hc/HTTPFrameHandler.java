package jp.or.nhk.rd.antwapp4hc ;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;

import android.content.res.AssetManager;
import android.os.Build;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

import java.text.SimpleDateFormat;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONjava.JSONArray;
import org.json.JSONjava.JSONException;
import org.json.JSONjava.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * AntWapp HTTP Frame Handler
 */
public class HTTPFrameHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements HyconetHandlerInterface {
    private final String protocolVersion = HyconetHandlerInterface.protocolVersion;
    private final String apiPrefix = HyconetHandlerInterface.restApiEndpoint;
    private String websocketPath = HyconetHandlerInterface.websocketApiEndpoint;
    private AnTWappConfigManager configMan = WebViewActivity.configMan();

    //Device Status, HC Status
    private static int companion_apps = 0;
    private static Map<String, Integer> resourceStatus = new HashMap<String, Integer>() {{
        put("original_network_id", 0);
        put("transport_stream_id", 0);
        put("service_id", 0);
    }};
    private static JSONObject lastReqObject = new JSONObject();

    private static SessionManager sessMan = null;


    /**
     * Constructor
     *
     */
    public HTTPFrameHandler() {
        //Session Manager
        if( sessMan == null ) {
            sessMan = new SessionManager();
        }
    }

    /**
     * Constructor
     * @param websocketPath
     */
    public HTTPFrameHandler(String websocketPath) {
        this.websocketPath = websocketPath;

        //Session Manager
        if( sessMan == null ) {
            sessMan = new SessionManager();
        }
    }

    /***********************************************************************************************/
    /**
     * StartAITTask Management
     */
    class StartAITTask {
        public String taskid;
        public FullHttpRequest req;
        public String[] query_params = null;
        public String mode;
        public JSONObject bodyObj = null;

        public String hybridcastBrowserStatus = null ;
        public String startAITTaskStatus = null ;
        public HttpResponseStatus startAITTaskResult = null;

        public StartAITTask(String _taskid, FullHttpRequest _req, String[] _query_params) {
            taskid = _taskid;
            req = _req;
            query_params = _query_params;
        };
        public void setMode(String _mode) {
            mode= _mode;
        };
        public String getMode() {
            return mode;
        };
        public void setBodyObj(JSONObject _bodyObj) {
            bodyObj= _bodyObj;
        };
        public JSONObject getBodyObj() {
            return bodyObj;
        };
    }

    //private static int taskid = 0;   //TaskId Default = 0
    private static List<StartAITTask> startAITTaskQueue = new ArrayList<>();

    //Latest Task Status
    private static String lasttaskid = "";  //Last TaskId
    private static String hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted ;
    private static String startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Done ;
    private static HttpResponseStatus startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.OK;

    private String setLastTaskId(String taskid) {
        return (lasttaskid = taskid);
    }
    private String getLastTaskId() {
        return lasttaskid;
    }
    private boolean isLastTaskIdValid() {
        return ( lasttaskid != null );
    }

    private String resetLastTaskId() {
        return lasttaskid = ""; //TODO: initial taskid from HyconetOperationalInterface
    }

    private boolean addStartAITTask(StartAITTask _taskobj) {
        //Log.i("task",String.format("addTaskRequest: %d", _taskobj.taskid));
        boolean status = startAITTaskQueue.add( _taskobj );
        Log.i("task",String.format("addStartAITTask taskid: %s, num: %d", _taskobj.taskid, startAITTaskQueue.size()));
        return status;
    };
    private boolean removeStartAITTask(StartAITTask _taskobj) {
        boolean status = startAITTaskQueue.remove( _taskobj );
        Log.i("task",String.format("removeStartAITTask: taskid: %s, num: %d", _taskobj.taskid, startAITTaskQueue.size()));
        return status;
    };
    private int getStartAITTaskPosition(StartAITTask _taskobj) {
        return startAITTaskQueue.indexOf( _taskobj );
    };
    private int getStartAITTaskSize() {
        return startAITTaskQueue.size();
    };
    /***********************************************************************************************/


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * getSessionManager()
     * @return
     */
    public static SessionManager getSessionManager() {
        return sessMan;
    }


    /**
     * 日付(UTC)
     *
     * @return
     */
    private String getISODateTime() {
        Date dt = new Date();

        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dtf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dtstr = dtf.format(dt);

        return dtstr;
    }

    /**
     * WebViewへメッセージを送信
     * @param type
     * @param obj
     */
/*****
 private void activity_sendmsg(WebViewActivity.MessageType type, Object obj) {
 Message msg = new Message();
 msg.arg1 = type.getId();
 msg.obj = obj;
 WebViewActivity.sendmsg(msg);
 }
 *****/

    /**
     * WebViewにobjで指定されるへージ表示させる
     * @param type
     * @param obj
     */
    private void activity_sendurl(WebViewActivity.MessageType type, Object obj) {
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
/*
        "type": "HCXPLog",
                "time": Date,
                "status": "Notice",
                "codepoint": "startAIT()...",
                "message": "Notice説明"

        "color"と"remark"を
*/
        //String ctime = (new Date()).toString();

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
     * HTTP Response の送信
     * @param ctx
     * @param req
     * @param res
     */
//    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res, Boolean needEncrypt ) {
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res, Boolean needDecEnc ) {
        // Generate an error page if response getStatus code is not OK (200).
        if( (res.status().code() == OK.code()) || (res.status().code() == CREATED.code()) ) {

        }
        else {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * sendHttpResponseWithJSON
     * @param ctx ChannelHandlerContext
     * @param req FullHttpRequest
     * @param resStr Response String
     * @param sessInfo SessionInfo
     */
    private void sendHttpResponseWithJSON(ChannelHandlerContext ctx, FullHttpRequest req, String resStr, HttpResponseStatus resStatus, SessionManager.SessionInfo sessInfo ) {
        ByteBuf content = Unpooled.copiedBuffer(resStr.getBytes());
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, resStatus, content);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(res, content.readableBytes());
        sendHttpResponse(ctx, req, res, false);
    }

    /**
     * TVControlURLの取得
     * @param req
     * @return
     */
    @NonNull
    private String getTVControlURL(HttpRequest req) {
        String protocol = "http";
        return protocol + "://" + req.headers().get(HttpHeaderNames.HOST) + apiPrefix;
    }

    /**
     * WebSocketApiEndpointの取得
     * @param cp
     * @param req
     * @param path
     * @return
     */
    @NonNull
    private String getWebSocketLocation(ChannelPipeline cp, HttpRequest req, String path) {
        String protocol = "ws";
        if (cp.get(SslHandler.class) != null) {
            // SSL in use so use Secure WebSockets
            protocol = "wss";
        }
        return protocol + "://" + req.headers().get(HttpHeaderNames.HOST) + path;
    }

    private void calldialog() {
        WebViewActivity.getActivity().showInfoDialogOK( true );
    }

    /**
     * 設定情報を返す
     * @param ctx
     * @param req
     */
    private void sendAppConfig(ChannelHandlerContext ctx, FullHttpRequest req) {
        //Get IP Address
        String my_ipaddr = "";
        try{
            for (Enumeration<NetworkInterface> networkInterfaceEnum = NetworkInterface.getNetworkInterfaces();
                 networkInterfaceEnum.hasMoreElements();) {
                NetworkInterface networkInterface = networkInterfaceEnum.nextElement();
                for (Enumeration<InetAddress> ipAddressEnum = networkInterface.getInetAddresses(); ipAddressEnum.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) ipAddressEnum.nextElement();
                    //---check that it is not a loopback address and it is ipv4---
                    System.out.println("IP Address : " + inetAddress);
                    if(!inetAddress.isLoopbackAddress()){
                        if( inetAddress instanceof Inet4Address) {
                            my_ipaddr = inetAddress.getHostAddress();
                            //System.out.println("IP Address : " + my_ipaddr);
                            //break;
                        }
                    }
                }

                if( !my_ipaddr.equals("") && !my_ipaddr.equals("192.168.49.1") ) {
                    break;
                }
            }
        }
        catch (SocketException ex){
            System.out.println("Error:" + ex.toString());
        }
        Log.i("MyIPAddress", my_ipaddr );


        JSONObject resobj = new JSONObject()
                .put("head", new JSONObject()
                        .put("code", 200)
                        .put("message", "OK")
                )
                .put("body", new JSONObject()
                    .put( "Env", new JSONObject()
                        .put("MANUFACTURER", Build.MANUFACTURER)
                        .put("BRAND", Build.BRAND)
                        .put("PRODUCT", Build.PRODUCT)
                        .put("FINGERPRINT", Build.FINGERPRINT)
                        .put("IP Address", my_ipaddr)
                        .put("Emulator Version", (String)configMan.get(Const.Config.versionName.Name))
                    )
                    .put( "config", new JSONObject()
                        .put( Const.Config.media.Name, configMan.get(Const.Config.media.Name) )
                        .put( Const.Config.channels.Name, configMan.get(Const.Config.channels.Name) )
                        .put( Const.Config.channelsFrom.Name, configMan.get(Const.Config.channelsFrom.Name) )
                        .put( Const.Config.tuneMode.Name, configMan.get(Const.Config.tuneMode.Name) )
                        .put( Const.Config.tuneDelay.Name, configMan.get(Const.Config.tuneDelay.Name) )
                        .put( Const.Config.hcViewMode.Name, configMan.get(Const.Config.hcViewMode.Name) )
                        .put( Const.Config.aitLoad.Name, configMan.get(Const.Config.aitLoad.Name) )
                        .put( Const.Config.aitVerifierMode.Name, configMan.get(Const.Config.aitVerifierMode.Name) )
                        .put( Const.Config.aitVerifierUrl.Name, configMan.get(Const.Config.aitVerifierUrl.Name) )
                        .put( Const.Config.WSBroadcastMode.Name, configMan.get(Const.Config.WSBroadcastMode.Name) )
                    )

                );
        String resstr = resobj.toString();

        //send response
        ByteBuf content = Unpooled.copiedBuffer( resstr.getBytes() );
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(res, content.readableBytes());
        sendHttpResponse(ctx, req, res, false);
    }

    /**
     * 直近のHCアプリ起動情報を返す
     * @param ctx
     * @param req
     */
    private void sendLastAppInfo(ChannelHandlerContext ctx, FullHttpRequest req) {
        JSONObject resobj = new JSONObject()
                .put("head", new JSONObject()
                        .put("code", 200)
                        .put("message", "OK")
                )
                .put("body", lastReqObject
                );
        String resstr = resobj.toString();

        //send response
        ByteBuf content = Unpooled.copiedBuffer( resstr.getBytes() );
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(res, content.readableBytes());
        sendHttpResponse(ctx, req, res, false);
    }

    /**
     * 設定情報を更新する
     * @param ctx
     * @param req
     */
    private void setAppConfig(ChannelHandlerContext ctx, FullHttpRequest req) {
        ByteBuf buf = req.content();
        String body = "";
        JSONObject reqConfig = null;

        if (buf == null) {
        }
        else {
            body = buf.toString(CharsetUtil.UTF_8);
            try {
                reqConfig = new JSONObject(body);
                configMan.updateConfig(reqConfig);
            }
            catch (JSONException e){
            }
        }

        JSONObject resobj = new JSONObject()
                .put(
                        "head", new JSONObject()
                                .put("code", 200)
                                .put("message", "OK")
                )
                .put(
                        "body", new JSONObject()
                );
        String resstr = resobj.toString();

        //send response
        ByteBuf content = Unpooled.copiedBuffer( resstr.getBytes() );
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(res, content.readableBytes());
        sendHttpResponse(ctx, req, res, false);
    }

    /**
     * AIT可否判定 Internal
     * @param str
     * @return
     */
    public Boolean isAiturlValidOnAitVerification(String str) {
        Boolean isValid = true;
        // write interanal AITURI Verification Logic
        return isValid;
    }


    /**
     * AIT可否判定
     * @param ctx
     * @param req
     */
    private void verifyAIT(ChannelHandlerContext ctx, FullHttpRequest req) {
        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "verifyAIT()", "start verification process at internal: AITURI Verification API", Const.LogBlack, "" );
        JSONObject jsonStrForVerification = new JSONObject();
        ByteBuf content = null;
        FullHttpResponse res = null;
        if( isAiturlValidOnAitVerification(jsonStrForVerification.toString()) ) {
            content = Unpooled.copiedBuffer(("OK").getBytes());
            res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
        }
        else {
            content = Unpooled.copiedBuffer(("Unacceptable AIT specified").getBytes());
            res = new DefaultFullHttpResponse(HTTP_1_1, HyconetHandlerInterface.StartAITResponse.UnacceptableAITSpecified, content);
        }

        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=utf-8");
        HttpUtil.setContentLength(res, content.readableBytes());
        sendHttpResponse(ctx, req, res, false);
    }

    /**
     * 指定されたAITファイルから起動URLを取得する
     * @param aiturl
     * @return
     */
    public String getHCURLfromAIT(String aiturl) {
        String baseURL = "";
        String locationURL = "";
        String hcurl = "";

        String aitstr = WebViewActivity.getHTTP(aiturl, 5000);
        if(aitstr.equals("")) {
            Log.i("getHCURLfromAIT:", String.format("Error: NULL ait"));
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "getHCURLfromAIT()", "Error: NULL ait", Const.LogRed, "" );
        }
        else {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                try {
                    Document document = documentBuilder.parse(new ByteArrayInputStream(aitstr.getBytes("UTF-8")));
                    Element root = document.getDocumentElement();
                    NodeList rootChildren = root.getChildNodes();
//                            				System.out.println("子要素の数：" + rootChildren.getLength());
                    for (int i = 0; i < rootChildren.getLength(); i++) {
                        Node node = rootChildren.item(i);

                        if (node.getNodeName().equals("isdb:ApplicationDiscovery")) {
                            NodeList adChildren = node.getChildNodes();
                            for (int j = 0; j < adChildren.getLength(); j++) {
                                Node ad_node = adChildren.item(j);

                                if (ad_node.getNodeName().equals("isdb:ApplicationList")) {
                                    NodeList alChildren = ad_node.getChildNodes();
                                    for (int k = 0; k < alChildren.getLength(); k++) {
                                        Node al_node = alChildren.item(k);
                                        //if( al_node.getNodeType() == Node.TEXT_NODE ) {
                                        //    System.out.println("Text：" + al_node.getTextContent());
                                        //}

                                        if (al_node.getNodeName().equals("isdb:Application")) {
                                            NodeList appChildren = al_node.getChildNodes();
                                            for (int l = 0; l < appChildren.getLength(); l++) {
                                                Node app_node = appChildren.item(l);
                                                //if( app_node.getNodeType() == Node.TEXT_NODE ) {
                                                //    System.out.println("Text：" + app_node.getTextContent());
                                                //}
                                                if (app_node.getNodeName().equals("isdb:applicationTransport")) {
                                                    NodeList locChildren = app_node.getChildNodes();
                                                    System.out.println("--- isdb:applicationTransport");
                                                    for (int l2 = 0; l2 < locChildren.getLength(); l2++) {
                                                        Node loc_node = locChildren.item(l2);
                                                        //if( loc_node.getNodeType() == Node.TEXT_NODE ) {
                                                        //    System.out.println("Text：" + loc_node.getTextContent());
                                                        //}
                                                        if (loc_node.getNodeName().equals("isdb:URLBase")) {
                                                            NodeList urlChildren = loc_node.getChildNodes();
                                                            for (int l3 = 0; l3 < urlChildren.getLength(); l3++) {
                                                                Node url_node = urlChildren.item(l3);
                                                                if (url_node.getNodeType() == Node.TEXT_NODE) {
                                                                    System.out.println("Text：" + url_node.getTextContent());
                                                                    baseURL = url_node.getTextContent();
                                                                }
                                                            }
                                                        }
                                                    }
                                                    System.out.println("--- isdb:applicationTransport");
                                                }
                                                if (app_node.getNodeName().equals("isdb:applicationLocation")) {
                                                    NodeList locChildren = app_node.getChildNodes();
                                                    System.out.println("--- isdb:applicationLocation");
                                                    for (int l2 = 0; l2 < locChildren.getLength(); l2++) {
                                                        Node loc_node = locChildren.item(l2);
                                                        if (loc_node.getNodeType() == Node.TEXT_NODE) {
                                                            System.out.println("Text：" + loc_node.getTextContent());
                                                            locationURL = loc_node.getTextContent();
                                                        }
                                                    }
                                                    System.out.println("--- isdb:applicationLocation");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    hcurl = baseURL + locationURL;
                } catch (SAXException e) {
                    Log.i("getHCURLfromAIT:", String.format("SAX Error %s", e.toString()));
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "getHCURLfromAIT()", "Error: SAX Error", Const.LogRed, "" );
                } catch (IOException e) {
                    Log.i("getHCURLfromAIT:", String.format("IO Error %s", e.toString()));
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "getHCURLfromAIT()", "Error: IO Error", Const.LogRed, "" );
                }
            } catch (ParserConfigurationException e) {
                Log.i("getHCURLfromAIT:", String.format("Parse Error %s", e.toString()));
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "getHCURLfromAIT()", "Error: ParseError", Const.LogRed, "" );
            }
        }

        return hcurl;
    }


    /**
     * OrgIDチェック
     * @param orgid organization_id for Hybridcast
     * @return
     */
    @Deprecated
    private boolean isOrgIdOk(Integer orgid){
        boolean ret = false;
        if((orgid != null) && (orgid < 65536) && (orgid > -1)){ // 16bitの正の整数
            ret = true;
        }
        return ret;
    }

    /**
     * AppIDチェック
     * @param appid organization_id for Hybridcast
     * @return
     */
    @Deprecated
    private boolean isAppIdOk(Long appid){
        boolean ret = false;
        if((appid != null) && (appid < 4294967295L ) && (appid > -1)){ // 16bitの正の整数
            ret = true;
        }
        return ret;
    }

    /**
     * チャネル情報整形
     * @param selmedia メディア
     * @return
     */
    private JSONArray getChannelInfo( String selmedia ) {
        //setup request media
        ArrayList<String> reqMedia = new ArrayList<String>();
        if( selmedia.equals("TD") || selmedia.equals("ALL") ) {
            reqMedia.add("TD");
        }
        if( selmedia.equals("BS") || selmedia.equals("ALL") ) {
            reqMedia.add("BS");
        }
        if( selmedia.equals("CS") || selmedia.equals("ALL") ) {
            reqMedia.add("CS");
        }

        JSONArray mediaobj = new JSONArray();
        for( String media : reqMedia) {
            JSONObject chXXobj = configMan.getChannelsObj(media);
            JSONArray mobj = new JSONArray();
            try {
                JSONArray chobj = chXXobj.getJSONArray("channels");
                for( int i=0; i<chobj.length(); i++ ) {
                    JSONObject channel = chobj.getJSONObject(i);
                    JSONObject resource = channel.getJSONObject("resource");
                    JSONObject ch = new JSONObject()
                            .put("logical_channel_number", channel.getString("logical_channel_number"))
                            .put("broadcast_channel_name", channel.getString("broadcast_channel_name"))
                            .put("resource", new JSONObject()
                                    .put("original_network_id", resource.getInt("original_network_id"))
                                    .put("transport_stream_id", resource.getInt("transport_stream_id"))
                                    .put("service_id", resource.getInt("service_id"))
                            );
                    mobj.put(ch);
                }
            }
            catch(JSONException e) {
                //TODO:forOSS
            }

            mediaobj.put( new JSONObject()
                    .put("type", media)
                    .put("channels", mobj));
        }

        return mediaobj;
    }

    /**
     * get Application-Resource-URL(RestAPI endpointURL).
     * DIAL Application ServiceのRESTAPIのendpointURL(=Application Resource URL)の取得.
     * @return Application Resource URL
     */
    @Override
    public String getDialApplicationResourceURL(){
        String dialApplicationResourceURL = DIALRestHandlerInterface.dialApplicationResourceURL;
        return dialApplicationResourceURL;
    }


    /**
     * DIAL Application Information request Handler by ApplicationResourceURL(Application-URL + "/Hybridcast").
     * Application Resource URL のリクエストに対して、DIAL Application Information を
     * "IPTVFJ STD-0013 7.2.1.1.2 additionalData" の拡張処理を付加してレスポンスとして返す処理ハンドラー.
     * (DIAL で規定されているadditionalDataをHybridcastConnectProtocol仕様でレスポンスとして返す.)
     * @param ctx
     * @param req
     */
    @Override
    public void dialRestServiceAppInfoHandler(ChannelHandlerContext ctx, FullHttpRequest req) {

        if(("application/ld+json").equals(req.headers().get(HttpHeaderNames.CONTENT_TYPE))){
            sendThingDescription(ctx,req); // ThingDescriptionとして返す
        }else{
            sendDialAppInfo(ctx, req); // defaultではDIALINFO-APIのXML
        }
    }

    /**
     * XMLdata genarator for DIAL Application Information response.
     * DIAL Application Information のXMLを生成するメソッド.
     * @param filepath
     * @return XMLString
     */
    public String genDialAppinfoXML(String filepath){
        String XML = "";
        try {
            AssetManager as = WebViewActivity.getContext().getResources().getAssets();
            //read template
            InputStream iStream = as.open(filepath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iStream, "UTF-8"));
            String _line;
            StringBuilder _sb = new StringBuilder();
            while ((_line = reader.readLine()) != null) {
                _sb.append(_line);
                _sb.append("\n");
            }
            reader.close();
            XML = _sb.toString();
        }catch (IOException e) {
            //TODO:forOSS
        }
        return XML;
    }

    /**
     * DIAL Application Information Data Processer.
     * Application InformationのXMLに"IPTVFJ STD-0013 7.2.1.1.2 additionalData" の拡張処理を付加してレスポンスとして返す処理ハンドラー.
     * (DIAL で規定されているadditionalDataをHybridcastConnectProtocol仕様でレスポンスとして返す.)
     * @param ctx
     * @param req
     */
    public void sendDialAppInfo(ChannelHandlerContext ctx, FullHttpRequest req) {

        Log.i("sendDialAppInfo", "" );

        try {
            //set device status & app status
            String appinfo = genDialAppinfoXML("appinfo_template.xml");
            appinfo = appinfo.replace("statexxxxx", WebViewActivity.getAppStatus() );
            appinfo = appinfo.replace("[TVControlURL]", getTVControlURL(req));
            appinfo = appinfo.replace("[WSServerURL]", getWebSocketLocation( ctx.pipeline(), req, websocketPath));

            //[ServerName/Version;ProtocolVersion;MakerId;ModelId;Comment]
            String serverInfoFmt = "%s/%s;%s;%s;%s;%s";
            String protocolVerssion = HyconetHandlerInterface.protocolVersion;
            String serverInfo = String.format( serverInfoFmt, "AnTWapp",
                    (String)configMan.get(Const.Config.versionName.Name) , protocolVersion, "0001", "0001", "Comment");
            appinfo = appinfo.replace("[ServerInfo]", serverInfo);

            //send response
            ByteBuf content = Unpooled.copiedBuffer( appinfo.getBytes() );
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, DIALRestHandlerInterface.DialAppInfoResponseStatus.OK, content);
            res.headers().set(HttpHeaderNames.CONTENT_TYPE, DIALRestHandlerInterface.mimeParameter);  //DIAL Protocol
            HttpUtil.setContentLength(res, content.readableBytes());
            sendHttpResponse(ctx, req, res, false);
        }
        catch (Exception e) {
            //TODO:forOSS
        }
    }


    /**
     * W3C WoTのThingDescriptionを返す
     * @param ctx
     * @param req
     */
    private void sendThingDescription(ChannelHandlerContext ctx, FullHttpRequest req) {
        AssetManager as = WebViewActivity.getContext().getResources().getAssets();
        Log.i("sendThingDescription", "" );

        try {
            //read template
            InputStream iStream = as.open( "json/td.json" );
            BufferedReader reader = new BufferedReader(new InputStreamReader(iStream,"UTF-8"));
            String _line;
            StringBuilder _sb = new StringBuilder();
            while ((_line = reader.readLine()) != null) {
                _sb.append(_line);
                _sb.append("\n");
            }
            reader.close();

            //set device status & app status
            String tdstr = _sb.toString();
            tdstr = tdstr.replace("RESTBASEPREFIX", getTVControlURL(req));
            tdstr = tdstr.replace("WSENDPOINT", getWebSocketLocation( ctx.pipeline(), req, this.websocketPath));

            //[ServerName/Version;ProtocolVersion;MakerId;ModelId;Comment]
            String serverInfoFmt = "%s/%s;%s;%s;%s;%s";
            String protocolVersion = HyconetHandlerInterface.protocolVersion;
            String serverInfo = String.format( serverInfoFmt, "AnTWapp",
                    ((String)configMan.get(Const.Config.versionName.Name)).split("_")[0] , protocolVersion, "0001", "0001", "Comment");
            tdstr = tdstr.replace("DEVICEVERSION", serverInfo);
            tdstr = tdstr.replace("PROTOCOLVERSION", protocolVersion);

            //send response
            ByteBuf content = Unpooled.copiedBuffer( tdstr.getBytes() );
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/ld+json");
            HttpUtil.setContentLength(res, content.readableBytes());
            sendHttpResponse(ctx, req, res, false);
        }
        catch (IOException e) {
            //TODO:forOSS
        }
    }

    /**
     * エラー時の応答
     * @param ctx
     * @param req
     * @param status
     */
    private void sendErrorStatus(ChannelHandlerContext ctx, FullHttpRequest req, HttpResponseStatus status, Boolean needEncrypt, SessionManager.SessionInfo sessInfo) {
        JSONObject resobj = new JSONObject()
                .put("head", new JSONObject()
                        .put("code", status.code())
                        .put("message", status.reasonPhrase())
                )
                .put("body", new JSONObject());
        String resstr = resobj.toString();

        //send response
        ByteBuf content = Unpooled.copiedBuffer(resstr.getBytes());
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, status, content);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(res, content.readableBytes());

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }


    /**
     * Request Header Validation Interface
     *
     * @param ctx
     * @param req
     * @return Boolean params Valid or Not
     */
    @Override
    public Boolean isRequestValid(ChannelHandlerContext ctx, FullHttpRequest req) {
        Boolean isValid = true; //default true
        // write validation logic on Request Header in common or at any request type.
        if(!isRequestPolicyValid(ctx, req)){
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "isRequestValid()", "RequestPolicyValid::RequestRefused", Const.LogRed, "" );
            sendErrorStatus(ctx, req, HyconetHandlerInterface.StartAITResponse.RequestRefused, false, null);
        }else if(!isRequestHeaderValid(ctx, req)){
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "isRequestValid()", "requestHeaderValid::Unzuhorized", Const.LogRed, "" );
            sendErrorStatus(ctx, req, HyconetHandlerInterface.StartAITResponse.Unauthorized, false, null);
        }
        return isValid;
    }

    /**
     * Request Header Validation Interface
     *
     * @param ctx
     * @param req
     * @param taskid
     * @return Boolean params Valid or Not
     */
    private Boolean isRequestValid(ChannelHandlerContext ctx, FullHttpRequest req, String taskid) {
        Boolean isValid = true; //default true
        // write validation logic on Request Header in common or at any request type.

        if(!isRequestPolicyValid(ctx, req)){
            isValid = false;
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "isRequestValid()", "RequestPolicyValid::RequestRefused " + taskid, Const.LogRed, "" );
            sendErrorStatus(ctx, req, HyconetHandlerInterface.StartAITResponse.RequestRefused, false, null);
        }else if(!isRequestHeaderValid(ctx, req)){
            isValid = false;
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "isRequestValid()", "RequestHeaderValid::Unauthorized " + taskid, Const.LogRed, "" );
            sendErrorStatus(ctx, req, HyconetHandlerInterface.StartAITResponse.Unauthorized, false, null);
        }
        return isValid;
    }


    /**
     * Request Policy Validation Interface
     *
     * @param ctx
     * @param req
     * @return Boolean params Valid or Not
     */
    public Boolean isRequestPolicyValid(ChannelHandlerContext ctx, FullHttpRequest req) {
        Boolean isValid = true; //default true
        // write validation Refused logic(policy) in common or at any request type.
        // if not in policy, expect to be "Request Refused"

        return isValid;
    }


    /**
     * Request Header Params Validation Interface
     *
     * @param ctx
     * @param req
     * @return Boolean paramsValid or Not
     */
    public Boolean isRequestHeaderValid(ChannelHandlerContext ctx, FullHttpRequest req) {
        Boolean isValid = true; //default true
        // write validation logic on Request Header in common or at any request type.

        return isValid;
    }


    /**
     * "7.2.3.2.3.2.1 メディア(地上デジタル、BS、CS)利用可否情報の取得" の処理ハンドラー
     * 受信機に設定されている利用可能なメディアをコンパニオンアプリケーションから受信機へ要求を処理してレスポンスを返す.
     *
     * @param ctx
     * @param req
     */
    public void sendAvailableMedia(ChannelHandlerContext ctx, FullHttpRequest req) {
        Boolean isParamGood = true;
        SessionManager.SessionInfo sessInfo = null;

        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "sendAvailableMedia()", "sendAvailableMedia::Process Start", Const.LogBlue, "" );

        if( !isRequestHeaderValid(ctx, req) ) {
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "sendAvailableMedia()", "sendAvailableMedia::BadRequest(parameter)", Const.LogRed, "" );
            sendErrorStatus(ctx, req, Const.HTTP.Unauthorized, false, null);
        }
        else {
            JSONObject resobj = new JSONObject()
                    .put("head", new JSONObject()
                            .put("code", 200)
                            .put("message", "OK")
                    )
                    .put("body", new JSONObject()
                            .put("created_at", getISODateTime())
                            .put("TD", configMan.getMediaStatus("TD"))
                            .put("BS", configMan.getMediaStatus("BS"))
                            .put("CS", configMan.getMediaStatus("CS"))
                    );
            String resstr = resobj.toString();

            //send response
            sendHttpResponseWithJSON( ctx, req, resstr, HttpResponseStatus.OK, sessInfo );

            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "sendAvailableMedia()", "sendAvailableMedia::Response finished", Const.LogGreen, "" );
        }
    }

    /**
     * 	"IPTVFJ STD-0013 7.2.3.2.3.2.2" 編成チャンネル情報の取得” の処理ハンドラー
     * 	受信機に設定されている選局可能な編成チャンネル情報をコンパニオンアプリケーションから受信機へ要求を処理してレスポンスを返す.
     * @param ctx
     * @param req
     * @param params
     */
    public void sendChannelsInfo(ChannelHandlerContext ctx, FullHttpRequest req, String[] params) {
        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "sendChannelsInfo()", "sendChannelsInfo::Process Start", Const.LogBlack, "" );

        Boolean isParamGood = false;
        SessionManager.SessionInfo sessInfo = null;

        String param_media = ""; //"ALL", "TD", "BS", "CS"
        if( params != null ) {
            String[] param = params[0].split("=");
            if( (param.length==2) && (param[0].equals("media")) ) {
                param_media = param[1];
            }
        }

        isParamGood = true;

        if(!isRequestHeaderValid(ctx, req)){
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "sendChannelsInfo()", "sendChannelsInfo::BadRequest(parameter)", Const.LogRed, "" );
            sendErrorStatus(ctx, req, Const.HTTP.Unauthorized, false, null);
        }else{
            if (param_media.equals("ALL") || param_media.equals("TD") ||param_media.equals("BS") ||param_media.equals("CS")) {
                JSONArray mediaobj = getChannelInfo(param_media);
                JSONObject resobj = new JSONObject()
                        .put("head", new JSONObject()
                                .put("code", 200)
                                .put("message", "OK")
                        )
                        .put("body", new JSONObject()
                                .put("created_at", getISODateTime())
                                .put("media", mediaobj)
                        );
                String resstr = resobj.toString();
                sendHttpResponseWithJSON( ctx, req, resstr, HttpResponseStatus.OK, sessInfo );
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "sendChannelsInfo()", "sendChannelsInfo::Response finished", Const.LogGreen, "" );
            } else {
                JSONObject resobj = new JSONObject()
                        .put("head", new JSONObject()
                                .put("code", 400)
                                .put("message", "Bad Request")
                        )
                        .put("body", new JSONObject()
                        );
                Log.i("resobj","resobj" + resobj);
                String resstr = resobj.toString();
                sendErrorStatus(ctx, req, Const.HTTP.BadRequest, false, sessInfo);
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "sendChannelsInfo()", "sendChannelsInfo::BadRequest", Const.LogGreen, "parameter" );
            }
        }
    }




    /**
     * アプリケーション起動::選局処理
     * @param task
     */
    private void launchHcAppsSub(StartAITTask task) {
        Boolean appParamIsOK = true;

        JSONObject resObj = new JSONObject();
        int nwid = 0;
        int tsid = 0;
        int svid = 0;
        int orgid = 0;
        Long appid = 0L;

        JSONObject reqbodyObj = task.getBodyObj();

        try{
            resObj = (JSONObject) reqbodyObj.get("resource");
            nwid = resObj.getInt("original_network_id");
            tsid = resObj.getInt("transport_stream_id");
            svid = resObj.getInt("service_id");
        }
        catch (JSONException e) {
            Log.i("LaunchHCApp: ", String.format("Error: %s", e));
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcAppsSub()", "launchHCApp::BadRequest: resource may be illegal", Const.LogRed, "" );
            appParamIsOK = false;
        }
        catch (RuntimeException e) {
            Log.i("LaunchHCApp: ", String.format("Error: %s", e));
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcAppsSub()", "launchHCApp::BadRequest: resource cound not be got", Const.LogRed, "" );
            appParamIsOK = false;
        }

        //エミュレータ
        if (((String)configMan.get( Const.Config.tuneMode.Name )).equals(Const.Config.tuneMode.Value.Emulator)) {
            Boolean tvTuneEnable = true;    //paramter
            if( !tvTuneEnable ) {
                task.hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Error;
                task.startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.TuningFailed;
            }
            else {
                String aiturl = "";
                String hcurl = "";
                JSONObject hcobj = new JSONObject();

                // tune & HClaunch
                if( task.getMode().equals("tune") ) {
                    task.hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                    task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Done;
                    task.startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.OK;
                }
                else if( task.getMode().equals("app") ) {
                    // TODO: schema-validation
                    // 受付処理でschema-validationしていれば、aiturlがURLフォーマットになっているか、AITXMLのパース、HCアプリのURLValidation以外はいらない。
                    if( reqbodyObj.has("hybridcast") ) {
                        try {
                            hcobj = (JSONObject) reqbodyObj.get("hybridcast");
                            //Check & Get AITURL and HCURL
                            if (hcobj.has("aiturl") && hcobj.has("orgid") && hcobj.has("appid")) {
                                orgid = hcobj.getInt("orgid");
                                if(!isOrgIdOk(orgid)){
                                    appParamIsOK = false;
                                    task.hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                                    task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Error;
                                    task.startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.AITProcessingError;

                                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcAppsSub()", "launchHCApp::ORGID range Error", Const.LogRed, "" );
                                }

                                appid = hcobj.getLong("appid");
                                if (!isAppIdOk(appid)) {
                                    appParamIsOK = false;
                                    task.hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                                    task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Error;
                                    task.startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.AITProcessingError;

                                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcAppsSub()", "launchHCApp::AppID range Error", Const.LogRed, "" );
                                }

                                aiturl = hcobj.getString("aiturl");
                                if (!aiturl.equals("")) {// AITURLがある
                                    if ((Boolean) configMan.get(Const.Config.aitLoad.Name)) {
                                        // 注意：AITが取得できないと、ここのタイムアウトでハングする
                                        hcurl = getHCURLfromAIT(aiturl);
                                    } else {
                                        hcurl = aiturl;
                                    }
                                    Log.i("URL: ", String.format("AITURL: %s, HCURL: %s", aiturl, hcurl));
                                }
                            }
                            else {
                                appParamIsOK = false;
                                task.hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                                task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Error;
                                task.startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.AITProcessingError;

                                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcAppsSub()", "launchHCApp::parameter is missing", Const.LogRed, "" );
                            }
                        } catch (JSONException e) {
                            appParamIsOK = false;
                            task.hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                            task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Error;
                            task.startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.AITProcessingError;

                            Log.i("LaunchHCApp: ", String.format("Error: %s", e));
                            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcAppsSub()", "launchHCApp::resource may be illegal", Const.LogRed, "" );
                        } catch (RuntimeException e) {
                            appParamIsOK = false;
                            task.hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                            task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Error;
                            task.startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.AITProcessingError;

                            Log.i("LaunchHCApp: ", String.format("Error: %s", e));
                            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcAppsSub()", "launchHCApp::resource cound not be got", Const.LogRed, "" );
                        }

                        // change Receiver/HC/LaunchProcess Status
                        if (aiturl.equals("")) {
                            setLastTaskId( task.taskid );
                            task.hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                            task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Error;
                            task.startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.AITProcessingError;

                            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcAppsSub()", "launchHCApp::No AIT URL specified", Const.LogRed, "" );
                            appParamIsOK = false;
                        } else {
                            //Check aitLoad mode
                            if (hcurl.equals("")) {
                                setLastTaskId( task.taskid );
                                task.hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                                task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Error;
                                task.startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.AITProcessingError;

                                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcAppsSub()", "startAIT::AITProcessingError : No HCURL Specified", Const.LogRed, "" );
                                appParamIsOK = false;
                            } else {
                                task.hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.Running;
                                task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Done;
                                task.startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.OK;
                            }
                        }
                    }
                    else {
                        appParamIsOK = false;
                        task.hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                        task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Error;
                        task.startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.AITProcessingError;

                        Log.i("LaunchHCApp: ", String.format("No hybridcast Object in JSONdata"));
                        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcAppsSub()", "launchHCApp::BadRequest No hybridcast data Error", Const.LogRed, "" );
                    }
                }

                if ( appParamIsOK ) {
                    //TV/Hybridcast Status Update
//                    task.hybridcastBrowserStatus  = HyconetHandlerInterface.HybridcastBrowserStatus.Running;
//                    task.startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.Done;
//                    task.setLastTaskId( task.taskid );

                    companion_apps = 1;
                    resourceStatus.put("original_network_id", nwid);
                    resourceStatus.put("transport_stream_id", tsid);
                    resourceStatus.put("service_id", svid);

                    reqbodyObj.put("mode", task.getMode());
                    reqbodyObj.put( Const.Config.hcViewMode.Name, (String)configMan.get( Const.Config.hcViewMode.Name ));
                    ((JSONObject) reqbodyObj.get("hybridcast")).put("hcurl", hcurl);
                    ((JSONObject) reqbodyObj.get("hybridcast")).put("tuneurl", WebViewActivity.tune_url);
                    lastReqObject = reqbodyObj;

                    //set Logo
                    lastReqObject.put("logo_image", "");

                    //Launch HC
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "launchHcAppsSub()", "launchHCApp::startAIT mode=" + task.getMode(), Const.LogBlack, "" );

                    if (task.getMode().equals("app") && !hcurl.equals("")) {//Launch HC App
                        // launch用messageArrayStringObject
                        String[] urlInfo = { String.format("javascript:startAITControlledApp('%s');", reqbodyObj.toString()) , hcurl };
                        activity_sendurl(WebViewActivity.MessageType.URL, urlInfo);
                        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "launchHcAppsSub() url", hcurl, Const.LogBlue, "" );
                    }
                    else {//Tune Only
                        String[] urlInfo = { String.format("javascript:startAITControlledApp('%s');", reqbodyObj.toString()) , WebViewActivity.tune_url };
                        activity_sendurl(WebViewActivity.MessageType.URL, urlInfo);
                        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "launchHcAppsSub() url", WebViewActivity.tune_url, Const.LogBlue, "" );
                    }
                } else{ // appParamsNGのケース
                    //TODO:forOSS
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "launchHcAppsSub()", "app Param Error", Const.LogBlue, "" );
                }
            }
        }

        //Update Status & remove task
        setLastTaskId( task.taskid );
        hybridcastBrowserStatus = task.hybridcastBrowserStatus;
        startAITTaskStatus = task.startAITTaskStatus;
        startAITTaskResult = task.startAITTaskResult;
        removeStartAITTask(task);

        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "launchHcAppsSub()", "launchHCApp::ALL Process finished", Const.LogGreen, "" );
    }

    /**
     * generator TaskId Interface
     *
     * @param req
     * @return String TaskId
     */
    public String genTaskId(FullHttpRequest req) {
        String taskid = "0"; //default true
        // write generation logic of taskid
//        taskid = String.format("%d", this.taskid++);
        taskid = String.format("%d", (new Date()).getTime());

        return taskid;
    }

    /**
     * request params Validation Interface
     *
     * @param ctx
     * @param task
     * @return Boolean body Valid or Not
     */
//    private Boolean isRequestParamsValid(ChannelHandlerContext ctx, FullHttpRequest req) {
    private Boolean isRequestParamsValid(ChannelHandlerContext ctx, StartAITTask task) {
        Boolean isValid = true; //default true
        // write validation logic on Request Params from communication in common or at any request type.

        if(!isRequestQueryParamsValid(ctx, task)){//1-1-1
            isValid = false;
        }else if (!isRequestBodyParamsValid(ctx, task)) {//1-1-2
            isValid = false;
        }else if (!isChannelResourceEntried(ctx, task)) {//1-2
            isValid = false;
        }else if (!isHybridcastConnectObjectValid(ctx, task)) {//1-2-?
            isValid = false;
        }else if(!isReceiverPolicyValid(ctx, task)){//1-1-3
            isValid = false;
        }
        else {

        }

        return isValid;
    }


    /**
     * Request query Parameters Validation Interface
     *
     * @param ctx
     * @param req
     * @return Boolean body Validb or Not
     */
    public Boolean isRequestQueryParamsValid(ChannelHandlerContext ctx, FullHttpRequest req) {
        Boolean isValid = true; //default true
        // write validation logic on Request Body in common or at any request type.
        return isValid;
    }


    /**
     * Request query Parameters Validation Interface
     *
     * @param ctx
     * @param task
     * @return Boolean body Validb or Not
     */
    private Boolean isRequestQueryParamsValid(ChannelHandlerContext ctx, StartAITTask task) {
        Boolean isValid = true; //default true
        // write validation logic on Request Body in common or at any request type.

        String mode = getRequestModeFromQueryString(task.query_params);
        if (mode.equals("none")) {
            isValid = false;
        }
        else {
            task.setMode( mode );
        }

        return isValid;
    }

    /**
     * get request mode from queryParams
     *
     * @param params strings of queryParams
     * @return mode name of string
     */
    public String getRequestModeFromQueryString(String[] params) {
        // mode enum: [tune|app|none]
        String mode = "none";
        if (null == params || 0 == params.length) { // params指定がなかったらapp modeとみなす
            mode = "app"; //default
        } else if (0 < params.length) { // paramsが１つ以上あれば処理する
            String[] param = params[0].split("=");
            if (param[0].equals("mode")) {
                if (param.length == 2) { // param[0]=mode, param[1]=[app | tune | others]
                    if( null == param[1] ) { // null => mode = none
                        mode = "none";
                    }
                    else if( param[1].equals("app") || param[1].equals("tune") ) {
                        mode = param[1];
                    }
                }
            }
        }
        return mode;
    }

    /**
     * Receiver Policy Validation Interface
     *
     * @param ctx
     * @param req
     * @return Boolean body Valid or Not
     */
    public Boolean isReceiverPolicyValid(ChannelHandlerContext ctx, FullHttpRequest req) {
        Boolean isValid = true; //default true
        // write validation logic on Request Body in common or at any request type.

        return isValid;
    }

    /**
     * Receiver Policy Validation Interface
     *
     * @param ctx
     * @param task
     * @return Boolean body Valid or Not
     */
    private Boolean isReceiverPolicyValid(ChannelHandlerContext ctx, StartAITTask task) {
        Boolean isValid = true; //default true
        // write validation logic on Request Body in common or at any request type.

        return isValid;
    }

    /**
     * Request Body Validation Interface
     *
     * @param ctx
     * @param task
     * @return Boolean body Valid or Not
     */
    private Boolean isRequestBodyParamsValid(ChannelHandlerContext ctx, StartAITTask task) {
        Boolean isValid = true; //default true
        // write validation logic on Request Body in common or at any request type.

        JSONObject reqbodyObj = null;
        ByteBuf buf = task.req.content();
        if (buf == null) {
            isValid = false;
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "isRequestBodyParamsValid()", "isRequestBodyParamsValid::BadRequest", Const.LogRed, "no request" );
            sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.BadRequest, false, null);
        }
        else {// requestのbody検査 => json-schemaによるvalidation => Ok もしくは BadRequest
            // TODO: replace to isRequestBValid(body)
            String body = buf.toString(CharsetUtil.UTF_8);
            try  {
                reqbodyObj = new JSONObject(body);
                if( !reqbodyObj.has("resource") ) {
                    isValid = false;
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcApps()", "launchHcApps::BadRequest", Const.LogRed, "No attr: resource Object in application body" );
                    hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                    sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.BadRequest, false, null);
                }
                else if( !reqbodyObj.has("hybridcast") ) {
                    isValid = false;
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcApps()", "launchHcApps::BadRequest", Const.LogRed, "No attr: Hybridcast Object in application body" );
                    hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                    sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.BadRequest, false, null);
                }
            }
            catch (JSONException e) {
                isValid = false;
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcApps()", String.format("launchHcApps::BadRequest"), Const.LogRed, "json error" );
                hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.BadRequest, false, null);
            }
            catch(RuntimeException e){
                isValid = false;
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcApps()", String.format("launchHcApps::RuntimeException"), Const.LogRed, "" );
                hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.BadRequest, false, null);
            }
        }

        if( isValid ) {
            task.setBodyObj(reqbodyObj);
        }

        return isValid;
    }

    /**
     * Channel Resource Body Validation Interface
     *
     * @param ctx
     * @return Boolean body Valid or Not
     */
    public Boolean isChannelResourceEntried(ChannelHandlerContext ctx, FullHttpRequest req){
        Boolean Existed = true; //default true
        // write channel resource parameters are in the list of channel resource list on TV Set in common or at any request type.

        return Existed;
    }


    /**
     * Channel Resource Body Validation Interface
     *
     * @param ctx
     * @param task
     * @return Boolean body Valid or Not
     */
    private Boolean isChannelResourceEntried(ChannelHandlerContext ctx, StartAITTask task) {
        Boolean Existed = true; //default true
        // write channel resource parameters are in the list of channel resource list on TV Set in common or at any request type.

        JSONObject reqbodyObj = task.getBodyObj();
        try {
            JSONObject resObj = (JSONObject) reqbodyObj.get("resource");
            int nwid = resObj.getInt("original_network_id");
            int tsid = resObj.getInt("transport_stream_id");
            int svid = resObj.getInt("service_id");

            JSONObject chobj = configMan.getChannelObj(nwid, tsid, svid);
            if (chobj == null) {
                Existed = false;
                send_loginfo(Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "isChannelResourceEntried()", "isChannelResourceEntried::BadRequest", Const.LogRed, "Available Channel resource not match in CHlist");
                sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.BadRequest, false, null);
            } else {
                Log.i("isChRsrcEntried:", String.format("Resource %d/%d/%d", nwid, tsid, svid));
                send_loginfo(Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "isChannelResourceEntried()", String.format("isChannelResourceEntried::Valid: %d/%d/%d", nwid, tsid, svid), Const.LogGreen, "");
            }
        } catch (JSONException e) {
            Existed = false;
            send_loginfo(Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "isChannelResourceEntried()", String.format("isChannelResourceEntried::BadRequest"), Const.LogRed, "json error");
            sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.BadRequest, false, null);
        } catch (RuntimeException e) {
            Existed = false;
            send_loginfo(Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "isChannelResourceEntried()", String.format("isChannelResourceEntried::RuntimeException"), Const.LogRed, "");
            sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.BadRequest, false, null);
        }

        return Existed;
    }

    /**
     * Hybridcast Mode Enable / Disable Interface
     *
     * @return Boolean body Valid or Not
     */
    public Boolean isHybridcastEnabled() {
        Boolean Enabled = true; //default true
        // write validation logic on Request Body in common or at any request type.
        return Enabled;
    }

    /**
     * Hybridcast Mode Enable / Disable Interface
     * @param ctx
     * @pram task
     * @return Boolean body Valid or Not
     */
    private Boolean isHybridcastEnabled(ChannelHandlerContext ctx, StartAITTask task) {
        Boolean Enabled = true; //default true
        // write validation logic on Request Body in common or at any request type.

        if( task.getMode().equals("app") ) {
            if (!Enabled) {
                send_loginfo(Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "isHybridcastEnabled()", String.format("isHybridcastEnabled::Hybridcast Disabled"), Const.LogRed, "");
                sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.HybridcastDisabled, false, null);
            }
        }

        return Enabled;
    }

    /**
     * Permission of Launch Request Interface
     *
     * @return Boolean body Valid or Not
     */
    public Boolean isStartAitPermitted() {
        Boolean Permitted = false; //default false
        // write validation logic on Request Body in common or at any request type.
        return Permitted;
    }

    /**
     * Permission of Launch Request Interface
     *
     * @param ctx
     * @param task
     * @return Boolean body Valid or Not
     */
    public Boolean isStartAitPermitted(ChannelHandlerContext ctx, StartAITTask task) {
        Boolean Permitted = false; //default false
        // write validation logic on Request Body in common or at any request type.

        int taskpos = getStartAITTaskPosition(task);
        Log.i("task",String.format("isStartAitPermitted: id=%s pos=%d", task.taskid, taskpos));

        if( taskpos == 0 ) {
            Permitted = true;
        }
        else {
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "isStartAitPermitted()", "isStartAitPermitted::Processing Another Request", Const.LogRed, "" );
            sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.ProcessingAnotherRequest, false, null);
        }

        return Permitted;
    }

    /**
     * Request AitUrl Validation Interface
     *
     * @param jsobj
     * @return Boolean body Valid or Not
     */
    public Boolean isAitUrlValid(JSONObject jsobj) {
        Boolean isValid = true; //default true
        // request validation from AITURL Verification Server.
        return isValid;
    }


    /**
     * Request AitUrl Validation Interface
     *
     * @param ctx
     * @param task
     * @return Boolean body Valid or Not
     */
    private Boolean isAitUrlValid(ChannelHandlerContext ctx, StartAITTask task) {
        Boolean isValid = true; //default true
        // request validation from AITURL Verification Server.

        if( task.getMode().equals("app") ) {
            String straitflag = (String) configMan.get(Const.Config.aitVerifierMode.Name);
            String[] verifyAllURL = ((String)(configMan.get(Const.Config.aitVerifierUrl.Name))).split("\\|");
            String verifyURL = null;

            if( straitflag.equals(Const.Config.aitVerifierMode.Value.Internal ) ) {
                verifyURL = verifyAllURL[Const.Config.aitVerifierUrl.Index.Internal];
            }
            else if( straitflag.equals(Const.Config.aitVerifierMode.Value.External ) ) {
                verifyURL = verifyAllURL[Const.Config.aitVerifierUrl.Index.External];
            }
            else if( straitflag.equals(Const.Config.aitVerifierMode.Value.AllOK ) ) {
                verifyURL = verifyAllURL[Const.Config.aitVerifierUrl.Index.AllOK];
            }
            Log.i("isAitUrlValid",String.format("straitflag: %s, verifyUrl: %s", straitflag, verifyURL));

            if( straitflag.equals(Const.Config.aitVerifierMode.Value.AllOK) ) {
                if( verifyURL.equals( Const.Config.aitVerifierUrl.Url.AllOK )) {
                    //AllOK
                }
            }
            else {
                JSONObject hybridcastConnectObj = task.getBodyObj();
                String aiturl = hybridcastConnectObj.getJSONObject("hybridcast").getString("aiturl");

                Log.i("isAitUrlValid",String.format("aiturl: %s", aiturl));

                if (!isAitUrlValidAsURL(aiturl)) {
                    isValid = false;
                } else {
                    // request validation
//                    Log.i("isAitUrlValid",String.format("aiturl: [%s]", verifyURL));
//                    String ret = WebViewActivity.getHTTP(aiturl, (int) configMan.get(Const.Config.aitVerificationTimeout.Name));

                    String ret = WebViewActivity.postHTTP(verifyURL, hybridcastConnectObj.toString(), (int)configMan.get(Const.Config.aitVerificationTimeout.Name));
                    Log.i("isAitUrlValid",String.format("aiturl: [%s]", ret));
                    if (ret.equals("OK")) {
                        isValid = true;
                    } else {
                        isValid = false;
                    }
                }
            }
        }

        if( !isValid ) {
            send_loginfo(Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "isAitUrlValid()", String.format("isAitUrlValid::UnacceptableAITSpecified"), Const.LogRed, "");
            sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.UnacceptableAITSpecified, false, null);
        }

        return isValid;
    }


    /**
     * Validation that URL is OK/not
     * @param aiturl
     * @return Boolean body Valid or Not
     */
    private Boolean isAitUrlValidAsURL(String aiturl) {
        Boolean isValid = true; //default true
        if((aiturl == null) || aiturl.equals("")){
            // BadRequest
            isValid = false;
        }

        return isValid;
    }

    /**
     * Send Success Response against StartAIT Request Interface
     *
     * @param ctx
     * @param task
     * @return
     */
    private void sendSuccessResponseOnStartAitRequest(ChannelHandlerContext ctx, StartAITTask task) {
        // write send response logic against Request

        JSONObject resobj = new JSONObject()
                .put("head", new JSONObject()
                        .put("code", HyconetHandlerInterface.StartAITResponse.Created.code())
                        .put("message", HyconetHandlerInterface.StartAITResponse.Created.reasonPhrase())
                )
                .put("body", new JSONObject()
                        .put("taskid", task.taskid)
                );
        String resstr = resobj.toString();
        send_loginfo(Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "launchHcApps()", String.format("sendSuccessResponseOnStartAitRequest::Response Send: Task Created: %s", task.taskid), Const.LogGreen, "");
        sendHttpResponseWithJSON(ctx, task.req, resstr, HttpResponseStatus.CREATED, null);
    }

    /**
     * Request Body Validation Interface
     *
     * @param ctx
     * @param str
     * @return Boolean body Valid or Not
     */
    public Boolean isHybridcastConnectObjectValid(ChannelHandlerContext ctx, String str) {
        Boolean isValid = true; //default true
        // write validation logic on Request Body in common or at any request type.
        return isValid;
    }


    /**
     * Request Body Validation Interface
     *
     * @param ctx
     * @param task
     * @return Boolean body Valid or Not
     */
//    private Boolean isHybridcastConnectObjectValid(ChannelHandlerContext ctx, String str) {
    private Boolean isHybridcastConnectObjectValid(ChannelHandlerContext ctx, StartAITTask task) {
        Boolean isValid = true; //default true
        ArrayList<String> schemaErrorList = new ArrayList<>();
        // write validation logic on Request Body in common or at any request type.

        //Check orgid/appid/AIT URL
//        JSONObject verificationBody = reqbodyObj;
        JSONObject verificationBody = task.getBodyObj();
        try {
            // Check orgid/appid before AIT verification
            //TODO : json schema validation => json-schemaで事前にBadRequest判定できる
            if(verificationBody.has("hybridcast ")){
                int orgid = verificationBody.getJSONObject("hybridcast").getInt("orgid"); // Stringだった場合、自動的にIntにcastされる
                Long appid = verificationBody.getJSONObject("hybridcast").getLong("appid"); // Stringだった場合、自動的にLongにcastされる

                if(!isOrgIdOk(orgid) || !isAppIdOk(appid)) {
                    isValid = false ;
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "isHybridcastConnectObjectValid()", String.format("isHybridcastConnectObjectValid::orgid/appid range error"), Const.LogRed, "" );
//                    hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                    sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.BadRequest, false, null);
                }
                else {
                    String aiturl = verificationBody.getJSONObject("hybridcast").getString("aiturl");
                    if( getHCURLfromAIT(aiturl) == "" ) {
                        isValid = false ;
                        Log.i("aitUrl-check", String.format("Error: %s", configMan.get( Const.Config.aitVerifierUrl.Name ), verificationBody.toString()));
//                        hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                        sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.BadRequest, false, null);
                    }
                }
            }
        }catch(JSONException e){
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcApps()", String.format("launchHcApps::AITVerification Body has something wrong"), Const.LogRed, "" );
            hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
            sendErrorStatus(ctx, task.req, HyconetHandlerInterface.StartAITResponse.BadRequest, false, null);
        }

        Log.i("isHCCObjectValid", String.format("isValid: %s", (isValid)?"True":"False"));


/*
*
        String schemapath = "./startAIT_request_schema.json";
        try (InputStream inputStream = HCXPCliTool.class.getResourceAsStream(schemapath)) {
			JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
			Schema schema = SchemaLoader.load(rawSchema);

			try {
				schema.validate(new JSONObject(str));
			}catch(JSONException e){
			} catch (ValidationException e) {
				schemaErrorList = e.getCausingExceptions().stream()
						.map(ValidationException::getAllMessages)
						.forEach(Collectors.toList());
			}
		}catch(JSONException e){
		}catch(IOException e){
        }
 *
 */
        return isValid;
    }

    /**
     * get XMLAIT
     *
     * @param aiturl
     * @return String body Valid or Not
     */
    public String getAitXmlFromAitUrl(ChannelHandlerContext ctx, String aiturl) {
        String result = "";

        return result;
    }

    /**
     * validation XMLAIT
     *
     * @param xmlait
     * @return Boolean body Valid or Not
     */
    public Boolean isAitXmlValid(ChannelHandlerContext ctx, String xmlait) {
        Boolean isValid = true; //default true

        return isValid;
    }

    /**
     * comparison orgid/appid between XMLAIT and request params.
     * @param aiturl
     * @return Boolean body Valid or Not
     */
    @Override
    public Boolean isHybridcastAitIDValid(ChannelHandlerContext ctx, String aiturl) {
        Boolean isValid = true; //default true

        return isValid;
    }


    /**
     * "IPTVFJ STD-0013 7.2.3.2.3.3.1 ハイブリッドキャストの起動要求" の処理ハンドラー.
     * POST データで指定する情報に従ってハイブリッドキャストアプリケーションの起動（または選局のみ）を受信機へ要求を処理してレスポンスを返す.
     * @param ctx
     * @param req
     * @param query_params
     */
    public void launchHcApps(ChannelHandlerContext ctx, FullHttpRequest req, String[] query_params) {
        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "launchHcApps()", "launchHcApps::Process Start", Const.LogBlack, "" );

        Boolean isParamGood = false;
        Boolean isCreated = false;
        String mode = "";
        String body = "";

        SessionManager.SessionInfo sessInfo = null;

        String req_taskid = genTaskId(req);

//        Log.i("launchHcApps:", String.format("Process:%s", launchAppInProcess));
        Log.i("launchHcApps:", String.format("Number of Processing Task: %d", getStartAITTaskSize()));

        // TODO: Anotherリクエストがあってもリクエストとしての受付はするように修正する。規格上、Connection refusedとUnauhorized以外はリクエストの受付はする
        // TODO: security的な理由で500 Request Refusedする

        // TODO: isrequestHeaderValid()
        if( !isRequestValid(ctx, req, req_taskid) ) {
            //send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcApps()", "launchHcApps::Reqest Refused", Const.LogRed, "" );
            //sendErrorStatus(ctx, req, HyconetHandlerInterface.StartAITResponse.RequestRefused, false, null);
        }
        else {
            StartAITTask task = new StartAITTask(req_taskid, req, query_params);
            addStartAITTask(task);

            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "launchHcApps()", "launchHcApps::Params Check start", Const.LogBlack, "" );
            if( !isRequestParamsValid(ctx, task) ) {
                removeStartAITTask(task);
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcApps()", "launchHcApps::Params:BadRequest", Const.LogRed, "" );
                sendErrorStatus(ctx, req, Const.HTTP.BadRequest, true, sessInfo);
            }
            else if( !isStartAitPermitted(ctx, task) ) {
                removeStartAITTask(task);
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcApps()", "launchHcApps::StartAit:BadRequest", Const.LogRed, "" );
                sendErrorStatus(ctx, req, Const.HTTP.BadRequest, true, sessInfo);
            }
            else if( !isHybridcastEnabled(ctx, task)) {
                removeStartAITTask(task);
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcApps()", "launchHcApps::HybridcastEnable:BadRequest", Const.LogRed, "" );
                sendErrorStatus(ctx, req, Const.HTTP.BadRequest, true, sessInfo);
            }
            else if( !isAitUrlValid( ctx, task )) {
                removeStartAITTask(task);
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "launchHcApps()", "launchHcApps::AitUrl:BadRequest", Const.LogRed, "" );
                sendErrorStatus(ctx, req, Const.HTTP.BadRequest, true, sessInfo);
            }
            else {
                //Set Status
                setLastTaskId(task.taskid);
                hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                startAITTaskStatus = HyconetHandlerInterface.StartAITTaskStatus.inProcess;
                startAITTaskResult = HyconetHandlerInterface.StartAITTaskResultStatus.OK;

                //Send Responce
                sendSuccessResponseOnStartAitRequest(ctx, task);

                //Tune and Launch HC
                hybridcastBrowserStatus = HyconetHandlerInterface.HybridcastBrowserStatus.NotStarted;
                send_loginfo(Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "launchHcApps()", String.format("launchHcApps::start Tune Process start"), Const.LogBlack, "");

                JSONObject reqbodyObj = task.getBodyObj();
                JSONObject resObj = (JSONObject) reqbodyObj.get("resource");
                int nwid = resObj.getInt("original_network_id");
                int tsid = resObj.getInt("transport_stream_id");
                int svid = resObj.getInt("service_id");
                JSONObject chobj = configMan.getChannelObj(nwid, tsid, svid);

                task.getBodyObj().put("logical_channel_number", chobj.getString("logical_channel_number"));
                task.getBodyObj().put("broadcast_channel_name", chobj.getString("broadcast_channel_name"));

                //Tune with Delay
                int tuneDelay = (int) configMan.get(Const.Config.tuneDelay.Name);
                if (0 < tuneDelay) {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        //                            String mode = null;
//                            JSONObject reqbodyObj = null;
                        StartAITTask task ;

                        public TimerTask setParams(StartAITTask _task) {
//                                this.mode = m;
//                                this.reqbodyObj = req;
                            this.task = _task;
                            return this;
                        }

                        @Override
                        public void run() {
                            launchHcAppsSub( task );
                        }
                    }.setParams(task), tuneDelay);
                } else {
                    launchHcAppsSub( task );
                }
            }
//            }
        }
    }

    /**
     * "IPTVFJ STD-0013 7.2.3.2.3.4.1 起動要求成否の取得” の処理ハンドラー
     * 起動要求(本書 7.2.3.2.3.3.1 節参照)を受理した後の受信機の処理結果を受信機へ要求を処理してレスポンスを返す.
     * @param ctx
     * @param req
     */
    public void sendLaunchHCAppStatus(ChannelHandlerContext ctx, FullHttpRequest req) {
        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "sendLaunchHCAppStatus()", String.format("sendLaunchHCAppStatus::Process Start"), Const.LogBlack, "" );

        Boolean isParamGood = false;
        SessionManager.SessionInfo sessInfo = null;

        isParamGood = true;

        if( !isRequestHeaderValid(ctx, req) ) {
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "sendLaunchHCAppStatus()", String.format("sendLaunchHCAppStatus::BadRequest"), Const.LogRed, "parameter" );
            sendErrorStatus(ctx, req, Const.HTTP.BadRequest, false, null);
        }
        else {
            if( !isLastTaskIdValid() ) {
                //TODO: Internal Server Error
                send_loginfo(Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "sendLaunchHCAppStatus()", String.format("sendLaunchHCAppStatus::Internal Server Error"), Const.LogRed, "");
                sendErrorStatus(ctx, req, Const.HTTP.InternalServerError, false, null);
            }
            else {
                JSONObject resobj = new JSONObject()
                        .put("head", new JSONObject()
                                .put("code", Const.HTTP.OK.code())
                                .put("message", Const.HTTP.OK.reasonPhrase())
                        )
                        .put("body", new JSONObject()
                                .put("taskid", lasttaskid)
                                .put("result", new JSONObject()
                                        .put("status", startAITTaskStatus)
                                        .put("code", startAITTaskResult.code())
                                        .put("message", startAITTaskResult.reasonPhrase())
                                )
                        );
                String resstr = resobj.toString();
                sendHttpResponseWithJSON(ctx, req, resstr, HttpResponseStatus.OK, sessInfo);
                send_loginfo(Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "sendLaunchHCAppStatus()", String.format("sendLaunchHCAppStatus::Response finished"), Const.LogGreen, "");
            }
        }
    }

    /**
     * "IPTVFJ STD0013 7.2.3.2.3.4.2 受信機状態の取得" の処理ハンドラー
     * ハイブリッドキャストアプリケーションエンジンの起動状態、コンパニオンデバイスの接続数、選局中の編成サービスを要求を処理してレスポンスを返す.
     * @param ctx
     * @param req
     */
    public void sendReceiverStatus(ChannelHandlerContext ctx, FullHttpRequest req) {
        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "sendReceiverStatus()", "sendReceiverStatus::Process Start", Const.LogBlack, "" );

        Boolean isParamGood = false;
        SessionManager.SessionInfo sessInfo = null;

        isParamGood = true;

        if( !isRequestHeaderValid(ctx, req) ) {
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "sendReceiverStatus()", "sendReceiverStatus::BadRequest", Const.LogRed, "parameter" );
            sendErrorStatus(ctx, req, Const.HTTP.BadRequest, false, null);
        }
        else {
            JSONObject resobj = new JSONObject()
                    .put("head", new JSONObject()
                            .put("code", 200)
                            .put("message", "OK")
                    )
                    .put("body", new JSONObject()
                            .put("status", new JSONObject()
                                    .put("hybridcast", hybridcastBrowserStatus)
                                    .put("companion_apps", WebSocketFrameHandler.getNumberOfConnections())
                                    .put("resource", new JSONObject()
                                            .put("original_network_id", resourceStatus.get("original_network_id"))
                                            .put("transport_stream_id", resourceStatus.get("transport_stream_id"))
                                            .put("service_id", resourceStatus.get("service_id"))
                                    )
                            )
                    );
            String resstr = resobj.toString();
            sendHttpResponseWithJSON( ctx, req, resstr, HttpResponseStatus.OK, sessInfo );
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "sendReceiverStatus()", "sendReceiverStatus::Response finished", Const.LogGreen, "" );
        }
    }

    /**
     * 受信機内で保持しているsetURLCacheのURLパラメターだけを更新.
     * @param ctx
     * @param req
     */
    private void updateHCSetURL(ChannelHandlerContext ctx, FullHttpRequest req) {
        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "updateHCSetURL()", String.format("updateHCSetURL::Response start"), Const.LogBlack, "" );
        ByteBuf buf = req.content();
        String reqbody = "";
        JSONObject reqjson = null;

        if (buf == null) {
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "updateHCSetURL()", String.format("updateHCSetURL::requestBody Error"), Const.LogRed, "" );
            sendErrorStatus(ctx, req, Const.HTTP.BadRequest, false, null);
        }
        else {
            reqbody = buf.toString(CharsetUtil.UTF_8);
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "updateHCSetURL()", String.format("updateHCSetURL::updateHCSetURL %s",reqbody), Const.LogBlack, "" );

            try {
                configMan.updateSetURL( reqbody );
            }
            catch (JSONException e){
                //TODO:forOSS
            }
        }
        JSONObject resobj = new JSONObject()
                .put("seturl", reqbody);
        String resstr = resobj.toString();

        //send response
        sendHttpResponseWithJSON( ctx, req, resstr, HttpResponseStatus.OK, null );
        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "updateHCSetURL()", String.format("updateHCSetURL::Response finished"), Const.LogGreen, "" );
    }


    /**
     * HybridcastのsetURLAPIと同等の機能の全パラメタを更新
     * @param ctx
     * @param req
     */
    public void updateHCSetURLParams(ChannelHandlerContext ctx, FullHttpRequest req) {
        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "updateHCSetURLParams()", String.format("updateHCSetURLParams::Response start"), Const.LogBlack, "" );

        ByteBuf buf = req.content();
        String reqbody = "";
        JSONObject reqjson = null;

        if (buf == null) {
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "updateHCSetURLParams()", String.format("updateHCSetURLParams::BadParameter"), Const.LogRed, "" );
            sendErrorStatus(ctx, req, Const.HTTP.BadRequest, false, null);
        }
        else {
            reqbody = buf.toString(CharsetUtil.UTF_8);
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "updateHCSetURLParams()", String.format("updateHCSetURLParams::updateSetURLParams %s",reqbody), Const.LogBlack, "" );

            try {
                reqjson = new JSONObject(reqbody);
                configMan.updateSetURLParams( reqjson );
            }
            catch (JSONException e){
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "updateHCSetURLParams()", String.format("updateHCSetURLParams::BadRequest"), Const.LogBlack, "json error" );
                sendErrorStatus(ctx, req, Const.HTTP.BadRequest, false, null);
            }
        }
        String resstr = reqjson.toString();

        //send response
        sendHttpResponseWithJSON( ctx, req, resstr, HttpResponseStatus.OK, null );
        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "updateHCSetURLParams()", String.format("updateHCSetURLParams::Response finished"), Const.LogGreen, "json error" );
    }

    /**
     * 指定されたファイルの内容をHTTPレスポンスとして返す
     * @param ctx
     * @param req
     * @param fname
     * @param type
     */
    public void sendResponse(ChannelHandlerContext ctx, FullHttpRequest req, String fname, String type) {
        ByteBuf content = null;

        try {
            byte[] data = WebViewActivity.readAssetTextFile(fname);
            if( data == null ) {
                //TODO:forOSS
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "sendResponse()", String.format("File read error"), Const.LogRed, "read error" );
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR), false);
            }
            else {
                content = Unpooled.copiedBuffer(data);
                FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
                switch(type) {
                    case "html":
                        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
                        break;
                    case "css":
                        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css; charset=UTF-8");
                        break;
                    case "js":
                        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css; charset=UTF-8");
                        break;
                    case "json":
                        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
                        break;
                    case "xml":
                        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/xml; charset=UTF-8");
                        break;
                    case "ico":
                        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/x-icon");
                        break;
                    case "img":
                        if (0 < fname.indexOf(".png")) {
                            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/png; charset=UTF-8");
                        } else if (0 < fname.indexOf(".gif")) {
                            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/gif; charset=UTF-8");
                        } else if (0 < fname.indexOf(".jpg")) {
                            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/jpeg; charset=UTF-8");
                        }
                        break;
                }
                HttpUtil.setContentLength(res, content.readableBytes());
                sendHttpResponse(ctx, req, res, false);
            }
        }
        catch (FileNotFoundException e ) {
            Log.i("sendResponse:", String.format("FileNotFoundException Error %s", e.toString()));
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "sendResponse()", String.format("FileNotFoundException: %s", fname), Const.LogRed, "read error" );
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR), false);
        }
        catch (IOException e ){
            Log.i("sendResponse:", String.format("Read Error %s", e.toString()));
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "sendResponse()", String.format("IOException: %s", fname), Const.LogRed, "read error" );
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR), false);
        }
    }


    /**
     * HTTP Request Receiver , URI Path Routing Handler
     * @param ctx
     * @param req
     * @throws
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // Handle a bad request.
        Log.i("channelRead0::ReqURI: ", req.uri());
        if (!req.decoderResult().isSuccess()) {
            Log.i("channelRead0:", String.format("decoderResult %s", req.decoderResult().toString()));
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST), false);
            return;
        }

        //パラメータの解析
        String[] parseurl = req.uri().split("\\?");
        String url_loc = parseurl[0];
        String[] url_query = null ;
        if( parseurl.length == 2) {
            url_query = parseurl[1].split("&");
        }

        Log.i("RecvRequest req", req.toString() );
        Log.i("RecvRequest ctx", ctx.toString() );
        Log.i("RecvRequest rip", ctx.channel().remoteAddress().toString().split("/")[1].split(":")[0] );

        String msgbody = "Unknown ";
        if( req.method() == GET ) msgbody = "Get ";
        if( req.method() == POST ) msgbody = "Post ";
        if( req.method() == PUT ) msgbody = "Put ";
        msgbody = msgbody + req.uri();

        if (req.method() == GET) {
            // Send the index page
            // File
            if ( "/".equals(req.uri()) ) {
                sendResponse(ctx, req, "index.html", "html");
            } else if ("/index.html".equals(req.uri())) {
                sendResponse(ctx, req, "index.html", "html");
            } else if ("/console.html".equals(req.uri())) {
                sendResponse(ctx, req, "console.html", "html");
            } else if ("/config.html".equals(req.uri())) {
                sendResponse(ctx, req, "config.html", "html");
            } else if ("/hc.html".equals(req.uri())) {
                sendResponse(ctx, req, "hc.html", "html");
            } else if ("/hcsub.html".equals(req.uri())) {
                sendResponse(ctx, req, "hcsub.html", "html");
            } else if ("/tune.html".equals(req.uri())) {
                sendResponse(ctx, req, "tune.html", "html");
            } else if ("/wsclient.html".equals(url_loc)) {
                sendResponse(ctx, req, "wsclient.html", "html");
            } else if ("/hcxplog.html".equals(url_loc)) {
                sendResponse(ctx, req, "hcxplog.html", "html");
            }
            else if (req.uri().substring(0, 5).equals("/ait/")) {
                String script = "javascript:logdisp('%s');";
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "HTTP Request", msgbody, Const.LogPink, "" );

                sendResponse(ctx, req, req.uri().substring(1), "xml");
            } else if (req.uri().substring(0, 5).equals("/css/")) {
                sendResponse(ctx, req, req.uri().substring(1), "css");
            } else if (req.uri().substring(0, 5).equals("/img/")) {
                sendResponse(ctx, req, req.uri().substring(1), "img");
            } else if (req.uri().substring(0, 4).equals("/js/")) {
                sendResponse(ctx, req, req.uri().substring(1), "js");
            } else if (req.uri().substring(0, 6).equals("/json/")) {
                sendResponse(ctx, req, req.uri().substring(1), "json");
            }
            else if ("/favicon.ico".equals(url_loc)) {
                sendResponse(ctx, req, req.uri().substring(1), "ico");
            }
            else if (("/api/lastappinfo").equals(req.uri())) { sendLastAppInfo(ctx, req); }
            else if (("/api/calldialog").equals(req.uri())) { calldialog(); }
            else if (("/api/antwappconfig").equals(req.uri())) { sendAppConfig(ctx, req); }

            else {
                String script = "javascript:logdisp('%s');";
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "HTTP Request", msgbody, Const.LogPink, "" );

                //Show Access Dialog
                if( (apiPrefix + "/media").equals(req.uri())
                        || (apiPrefix + "/channels").equals(url_loc)
                        || (apiPrefix + "/status").equals(url_loc)
                        || (apiPrefix + "/hybridcast").equals(url_loc)) {

                    String remoteIP  = ctx.channel().remoteAddress().toString().split("/")[1].split(":")[0];
                    String userAgent = req.headers().get("User-Agent");
                    WebViewActivity.getActivity().showAccessDialog( remoteIP, userAgent );
                }

                // DIAL REST Service URL for  for emulator and WoT
                if((DIALRestHandlerInterface.dialApplicationURL + "/antwapp").equals(req.uri()) ){ sendDialAppInfo(ctx, req); } // Application Information request by ApplicationResourceURL(Application-URL + "/antwapp") for Emulator
                else if (getDialApplicationResourceURL().equals(req.uri()) ) { // Application Information request by ApplicationResourceURL(Application-URL + "/Hybridcast")

                    dialRestServiceAppInfoHandler(ctx, req);
                }
                // Hybridcast Connect RESTAPIs
                else if ((apiPrefix + HyconetHandlerInterface.restApiPath.get("media")).equals(req.uri())) { sendAvailableMedia(ctx, req); } // GEt /media
                else if ((apiPrefix + HyconetHandlerInterface.restApiPath.get("channels")).equals(url_loc)) { sendChannelsInfo(ctx, req, url_query); } // GET /channels
                else if ((apiPrefix + HyconetHandlerInterface.restApiPath.get("status")).equals(url_loc)) { sendReceiverStatus(ctx, req); } // GET /status
                else if ((apiPrefix + HyconetHandlerInterface.restApiPath.get("hybridcast")).equals(url_loc)) { sendLaunchHCAppStatus(ctx, req); } // GET /hybridcast

                // Error
                else if(req.uri().startsWith(DIALRestHandlerInterface.dialApplicationURL )){ // if No Application matches, write Specific Error in DIAL Protocol
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "HTTP Request", String.format("DIAL apps not found: %s",req.uri()), Const.LogPink, "" );
                    sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, DIALRestHandlerInterface.DialAppInfoResponseStatus.NotFound), false);
                }
                else {
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "HTTP Request", String.format("not found: %s",req.uri()), Const.LogPink, "" );
                    sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND), false);
                }
            }
        }
        else if (req.method() == POST) {
            String script = "javascript:logdisp('%s');";
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "HTTP Request", msgbody, Const.LogPink, "" );

            Log.i("channelRead0:", String.format("URI %s", req.uri()));

            //Show Access Dialog
            if( (apiPrefix + HyconetHandlerInterface.restApiPath.get("hybridcast")).equals(url_loc)) { // POST /hybridcast
                String remoteIP  = ctx.channel().remoteAddress().toString().split("/")[1].split(":")[0];
                String userAgent = req.headers().get("User-Agent");
                WebViewActivity.getActivity().showAccessDialog( remoteIP, userAgent );
            }

            // webAPI for antwapp
            if ( ("/api/antwappconfig").equals(req.uri()) ) { setAppConfig(ctx, req); }
            else if ( ("/api/aitverifier").equals(req.uri()) ) { verifyAIT(ctx, req); }
            else if ( ("/api/seturl").equals(req.uri()) ) { updateHCSetURL(ctx, req); }
            else if ( ("/api/seturls").equals(req.uri()) ) { updateHCSetURLParams(ctx, req); }

            // Interface API
            else if ( (apiPrefix + HyconetHandlerInterface.restApiPath.get("hybridcast")).equals(url_loc) ) { launchHcApps(ctx, req, url_query ); }

            // Error
            else {
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "HTTP Request", String.format("not found: %s",req.uri()), Const.LogRed, "" );
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND), false);
            }
        }
        else if (req.method() == PUT) {
            String script = "javascript:logdisp('%s');";
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "PUT "+req.uri(), msgbody, Const.LogPink, "" );

            // Interface API
            // Error
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "HTTP Request", String.format("not found: %s",req.uri()), Const.LogRed, "" );
        }

        else {
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, req.uri(), String.format("forbidden: %s",req.uri()), Const.LogRed, "" );
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN),false);
        }
    }
}
