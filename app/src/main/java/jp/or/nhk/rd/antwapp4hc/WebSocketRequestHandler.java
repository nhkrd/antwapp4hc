package jp.or.nhk.rd.antwapp4hc ;

import android.os.Message;
import android.util.Log;

import org.json.JSONjava.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

/**
 * AntWapp Websocket Handler
 */
class WebSocketRequestHandler extends ChannelInboundHandlerAdapter {
    private final String websocketPath;
    private final String subprotocols;
    private final boolean checkStartsWith;

    WebSocketRequestHandler(String websocketPath, String subprotocols, boolean checkStartsWith) {
        this.websocketPath = websocketPath;
        this.subprotocols = subprotocols;
        this.checkStartsWith = checkStartsWith;
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
     * HCApp想定のアプリとのWebsocketSubprotocolかどうかを判定
     * @param ctx
     * @param req
     * @param subprotocol
     */
    private boolean isWSSubProtocol(final ChannelHandlerContext ctx, FullHttpRequest req, String subprotocol) {
        String reqProtocol = req.headers().get("Sec-WebSocket-Protocol");
        Log.i("isWSSubProtocol:", String.format("WSSubProtocol: %s", reqProtocol));
        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "SubProtocol", String.format("SubProtocol: %s",reqProtocol), Const.LogBlue, "" );

        boolean isProtocol = false;
        // websocketRequestのsubprotocolが指定のsubprotocolあればtrue。
        if(subprotocol.equals(reqProtocol)){
            isProtocol = true;
        }
        return isProtocol;
    }

    /**
     * channelRead
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            final FullHttpRequest req = (FullHttpRequest) msg;

            // 拡張プロトコルの通信識別
            if( checkStartsWith) {
                // websocketAPIのendpointかどうかを確認。WSendpointでなければスルー
                if (!req.uri().startsWith(websocketPath)) {
                    ctx.fireChannelRead(msg);
                    return;
                }
                // websocketAPIはHTTP GETからのアップグレード以外は認めない
                if (req.method() != GET) {
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "SubProtocol", String.format("SubProtocol: 403 Forbidden "), Const.LogRed, "" );
                    sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
                    return;
                }

                // subprotocol判定
                // HCC外部アプリかHCApp(app/log)の判定と識別を実施、WSメッセージのハンドリングができるようにする
                if( !isWSSubProtocol(ctx, req, Const.Config.SubProtocol.HCApp) && !isWSSubProtocol(ctx, req, Const.Config.SubProtocol.HCXPLog) ) { // "HCApp"subptorocolではない時は規格通り

                    if( !(isWSSubProtocol(ctx,req,Const.Config.SubProtocol.HCConnect)) ){ // SubProtocolが”Hybridcast"かどうかチェック。warningだけだす。
                        // DebugのためにWarningだけ表示する。
                        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "SubProtocol", String.format("SubProtocol: SubProtocol is not specified as Hybridcast"), Const.LogRed, "" );
                        //SubProtocolが規格通りでなければ401を返すことも可能
                    }

                    SessionManager sessMan = HTTPFrameHandler.getSessionManager();
                    String hcAppDevId = Const.Config.SubProtocol.HCConnect + ctx.channel().remoteAddress().toString();
                    SessionManager.SessionInfo sessInfo = sessMan.getSessionByDevID(hcAppDevId);
                    if (sessInfo == null) {
                        // HybridcastのSubprotocolのための特別なsession
                        Log.i("AddHCEXpSession:", String.format("AddHCConnectSession: %s", ""));
                        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "SubProtocol", String.format("SubProtocol: addHCConnectSession %s", hcAppDevId), Const.LogGreen, "" );

                        sessInfo = sessMan.addHCSession("Hybridcast", Const.Config.SubProtocol.HCConnect, hcAppDevId);
                        sessInfo.setChannel(ctx.channel());
                        sessInfo.setSubProtocol(Const.Config.SubProtocol.HCConnect);
                    }

                } else if(isWSSubProtocol(ctx, req, Const.Config.SubProtocol.HCApp)) {
                    //　エミュレーター側のHCアプリ想定Webアプリからのリクエスト(subprotocolが"HCApp"の時）
                    Log.i("HCAppSubProtocol:", String.format("HCAppRequest: %s", ""));
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Success, "SubProtocol", String.format("SubProtocol: HCAppSubProtocolProcess %s", "start"), Const.LogGreen, "" );
                    SessionManager sessMan = HTTPFrameHandler.getSessionManager();
                    String hcAppDevId = Const.Config.SubProtocol.HCApp + ctx.channel().remoteAddress().toString();
                    SessionManager.SessionInfo sessInfo = sessMan.getSessionByDevID(hcAppDevId);
                    if(sessInfo == null){
                        // HybridcastのSubprotocolのための特別なsession
                        Log.i("AddHCAppSession:", String.format("AddHCAppSession: %s", ""));
                        send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "SubProtocol", String.format("SubProtocol: addHCSession %s", hcAppDevId), Const.LogBlack, "" );

                        sessInfo = sessMan.addHCSession("HCApp", Const.Config.SubProtocol.HCApp, hcAppDevId);
                        sessInfo.setChannel(ctx.channel());
                        sessInfo.setSubProtocol(Const.Config.SubProtocol.HCApp);
                    }

                } else if(isWSSubProtocol(ctx, req, Const.Config.SubProtocol.HCXPLog)){
                    // Log収集したいWebアプリからのリクエスト
                    //　エミュレーターのLog表示クライアントからのリクエスト(subprotocolが"HCAppLog"の時）
                    Log.i("HCAppLogSubProtocol:", String.format("HCAppLogAPIRequest: %s", ""));
                    send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Notice, "SubProtocol", String.format("SubProtocol: HCAppLogSubProtocolProcess %s", "start"), Const.LogBlack, "" );
                    SessionManager sessMan = HTTPFrameHandler.getSessionManager();
                    String hcAppLogDevId = Const.Config.SubProtocol.HCXPLog + ctx.channel().remoteAddress().toString();
                    SessionManager.SessionInfo sessInfo = sessMan.getSessionByDevID(hcAppLogDevId);
                    if(sessInfo == null){
                        Log.i("HCAppLogSubProtocol:", String.format("HCAppLogAPIRequest: %s", hcAppLogDevId));
                        // HybridcastのSubprotocolのための特別なsession
                        sessInfo = sessMan.addHCSession("HCAppLog", Const.Config.SubProtocol.HCXPLog, hcAppLogDevId);
                        sessInfo.setChannel(ctx.channel());
                        sessInfo.setSubProtocol(Const.Config.SubProtocol.HCXPLog);
                    }
                }
            }

        }
        catch(ClassCastException e) {
            Log.i("WSReqHandler:", String.format("channelRead: %s", e.getMessage()));
//            send_loginfo( Const.DebugInfo.Type.HCXPLog, Const.DebugInfo.Status.Error, "chanelRead", String.format("WSReqHandler::channelRead-ClassCastException  %s", e.getMessage()), Const.LogRed, "" );
        }
        ctx.fireChannelRead(msg);
     }

    /**
     * sendHttpResponse
     * @param ctx
     * @param req
     * @param res
     */
     private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
         ChannelFuture f = ctx.channel().writeAndFlush(res);
         if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
             f.addListener(ChannelFutureListener.CLOSE);
         }
     }
}