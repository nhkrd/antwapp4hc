package jp.or.nhk.rd.antwapp4hc ;

import android.os.Message;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import org.json.JSONjava.JSONException;
import org.json.JSONjava.JSONObject;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * AntWapp Websocket Handler
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private ArrayList<Channel> charray = new ArrayList<Channel>();
    private static int websocket_counter = 0;

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
     *
     */
    public WebSocketFrameHandler() {}
    public WebSocketFrameHandler(ArrayList<Channel> s_array) {
        charray = s_array;
    }

    /**
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        SessionManager sessMan = HTTPFrameHandler.getSessionManager();
        SessionManager.SessionInfo sessInfo = sessMan.getSessionByChannel( ctx.channel() );
        if( sessInfo != null ) {

        }

Log.i("Ctx Channel:", ctx.channel().toString());

        charray.add(ctx.channel());

        String localAddr = ctx.channel().localAddress().toString();
        if( localAddr.indexOf("127.0.0.1") < 0 ) {
            websocket_counter++;

Log.i("WebSocketFrameHandler:", String.format("channelRegistered: %d %s", websocket_counter, ctx.channel().remoteAddress().toString()));
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "channelRegistered()", String.format("WSRegisterd: counter:%d address:%s",websocket_counter, ctx.channel().remoteAddress().toString()), Const.LogBlue, "" );
        }
    }

    /**
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        charray.remove(ctx.channel());

        String localAddr = ctx.channel().localAddress().toString();
        if( localAddr.indexOf("127.0.0.1") < 0 ) {
            websocket_counter--;

Log.i("WebSocketFrameHandler:", String.format("channelRegistered: %d %s", websocket_counter, ctx.channel().remoteAddress().toString()));
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "channelUnregistered()", String.format("WSUnRegisterd: counter:%d address:%s",websocket_counter, ctx.channel().remoteAddress().toString()), Const.LogBlue, "" );
        }
    }

    /**
     *
     * @return
     */
    public static int getNumberOfConnections() {
        int chnum = 0;

        if( 0 < websocket_counter ) {
            chnum = websocket_counter;
        }

        return chnum;
    }

    /**
     *
     * @param requestObj
     * @return
     */
    private String checkWScommand(JSONObject requestObj){
        String wscmd = "";
        try {
            // TODO: jsonValidator
            if (requestObj.has("message")) {
                JSONObject tvctrlObj = (JSONObject) requestObj.get("message");
                if (tvctrlObj.has("sendTextToHostDevice")) {
                    wscmd = "sendTextToHostDevice";
                }
                else if (tvctrlObj.has("sendTextToCompanionDevice")) {
                    wscmd = "sendTextToCompanionDevice";
                }
            }else if (requestObj.has("control")) {
                JSONObject tvctrlObj = (JSONObject) requestObj.get("control");
                if (tvctrlObj.has("request")) {
                    if(((JSONObject)tvctrlObj.get("request")).getString("command").equals("setURLForCompanionDevice")){
                        wscmd = "seturl";
                    }
                }else if (tvctrlObj.has("extensions")) {
                    if (tvctrlObj.has("vendor")) {
                        wscmd = "extensions";
                    }
                }
            }
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "checkWScommand()", String.format("checkWScommand: wscmd:%s",wscmd), Const.LogBlue, "" );

        }
        catch (JSONException e) {
            Log.i("WSJsonFormatError", e.getMessage() );
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "checkWScommand()", "checkWScommand json error", Const.LogRed, "" );
        }

        return wscmd;
    }

    /**
     *
     * @param devid
     * @param sendtext
     * @return
     */
    private JSONObject genSendTextToCompanionDeviceJson(String devid, String sendtext){
         JSONObject jsobj = new JSONObject();
         try {
             jsobj = new JSONObject()
                     .put("message", new JSONObject()
                             .put("devid", devid)
                             .put("sendTextToCompanionDevice", new JSONObject()
                                     .put("text", sendtext)
                             ));
         }catch (JSONException e) {
             Log.i("JsonProcessError", e.getMessage() );
             send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "genSendTextToCompanionDeviceJson()", "genSendTextToCompanionDeviceJson json error", Const.LogRed, "" );
         }
        return jsobj;
    }

    /**
     *
     * @param devid
     * @param sendtext
     * @return
     */
    private JSONObject genSendTextToHostDeviceJson(String devid, String sendtext){
        JSONObject jsobj = new JSONObject();
        try {
            jsobj = new JSONObject()
                    .put("message", new JSONObject()
                            .put("devid", devid)
                            .put("sendTextToHostDevice", new JSONObject()
                                    .put("text", sendtext)
                            ));
        }catch (JSONException e) {
            Log.i("JsonProcessError", e.getMessage() );
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "genSendTextToHostDeviceJson()", "genSendTextToHostDeviceJson json error", Const.LogRed, "" );
        }
        return jsobj;
    }

    /**
     *
     * @param devid
     * @param seturl
     * @param auto_start
     * @param app_title
     * @param app_desc
     * @return
     */
    private JSONObject genSetUrlJson(String devid, String seturl, Boolean auto_start, String app_title, String app_desc ){
        JSONObject jsobj = new JSONObject();
        try {
            jsobj = new JSONObject()
                    .put("control", new JSONObject()
                            .put("devid", devid)
                            .put("setURLForCompanionDevice", new JSONObject()
                                    .put("url", seturl)
                                    .put("options", new JSONObject()
                                            .put("auto_start", auto_start)
                                            .put("app_title", app_title)
                                            .put("app_desc", app_desc)
                                    )
                            )
                    );
        }catch (JSONException e) {
            Log.i("JsonProcessError", e.getMessage() );
            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "genSetUrlJson()", "genSetUrlJson json error", Const.LogRed, "" );
        }
        return jsobj;
    }

    /**
     *
     * @param ctx
     * @param frame
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled

Log.i("WebSocketFrameHandler:", String.format("channelRead0: "));

        if (frame instanceof TextWebSocketFrame) {
            // Send the uppercase string back.
            String request  = ((TextWebSocketFrame) frame).text();
            String requestD = request;

            // 接続channelに対応したsession情報を探す
            SessionManager sessMan = HTTPFrameHandler.getSessionManager();
            SessionManager.SessionInfo sessInfo = sessMan.getSessionByChannel( ctx.channel() );
            if( sessInfo != null ) {
                Log.i("WSSessInfoFound", "SessInfo corresponding to :" + ctx.channel().toString());
                send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "Recv WS frame", String.format("Recv WS frame: %s",requestD), Const.LogBlack, "" );

                // Message Processing
                String wscmd = "";
                JSONObject resobj = new JSONObject();
                try {
                    JSONObject requestObj = new JSONObject(requestD);
                    wscmd = checkWScommand(requestObj);
                    if (wscmd.equals("sendTextToHostDevice")) {// messageがHostDevice向けの時
                        resobj = genSendTextToCompanionDeviceJson(
                                requestObj.getJSONObject("message").getString("devid"),
                                requestObj.getJSONObject("message").getJSONObject("sendTextToHostDevice").getString("text"));
                    }else if (wscmd.equals("sendTextToCompanionDevice")) { // messageがCompanionDevice向けの時
                        resobj = genSendTextToHostDeviceJson(
                                    requestObj.getJSONObject("message").getString("devid"),
                                    requestObj.getJSONObject("message").getJSONObject("sendTextToCompanionDevice").getString("text"));
                    } else if (wscmd.equals("seturl")) { // seturlの時
                        resobj = genSetUrlJson(
                                requestObj.getJSONObject("control").getString("devid"),
                                (String) WebViewActivity.configMan().get(Const.Config.SetUrl.Name),
                                (Boolean) WebViewActivity.configMan().get(Const.Config.SetUrlAutoStart.Name),
                                (String) WebViewActivity.configMan().get(Const.Config.SetUrlAppTitle.Name),
                                (String) WebViewActivity.configMan().get(Const.Config.SetUrlAppDesc.Name)
                        );
                    } else {
                        resobj = requestObj;
                    }
                    if (null == resobj) {
                        Log.i("WSGenResponseJson:", String.format("WebsocketResponseJsonText--NULL: %s", resobj.toString()));
                    } else {
                        Log.i("WSGenResponseJson:", String.format("WebsocketResponseJsonText: %s", resobj.toString()));
                        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "Recv WS frame", String.format("ResponseWSMessage: %s",resobj.toString()), Const.LogBlack, "" );
                    }

                    // Websocket Push

                    String r = resobj.toString();
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(r));

                    // WebsocketBroadcastModeであれば、messageはBroadcastする
                    if ((boolean) WebViewActivity.configMan().get(Const.Config.WSBroadcastMode.Name)) {
                        for (int i = 0; i < charray.size(); i++) {
                            Channel tmpchannel = charray.get(i);
                            String newReq = null;

                            if (tmpchannel != ctx.channel()) { // currentChannel以外のchannelだったらbroadcastPush
                                SessionManager.SessionInfo tmpsessInfo = null;
                                tmpsessInfo = sessMan.getSessionByChannel(tmpchannel);

                                if ( (tmpsessInfo != null) && !(tmpsessInfo.subprotocol.equals(Const.Config.SubProtocol.HCXPLog)) ) {
                                    tmpchannel.writeAndFlush(new TextWebSocketFrame(resobj.toString()));
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.i("WebSocketFrameHandler", e.getMessage());
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "WS Frame Processing", String.format("WS Frame Error: JSONException %s",e.getMessage()), Const.LogBlack, "" );
                }
            }
        }
        else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }
}

