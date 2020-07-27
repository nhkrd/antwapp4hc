package jp.or.nhk.rd.antwapp4hc ;

import android.util.Log;
import java.util.ArrayList;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.HEAD;
import static io.netty.handler.codec.http.HttpMethod.OPTIONS;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;

/**
 * HTTPServerInitializer
 */
public class HTTPServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final String WEBSOCKET_PATH = HyconetHandlerInterface.websocketApiEndpoint;
    static final ArrayList<Channel> charray = new ArrayList<Channel>();

    private final SslContext sslCtx;

    /**
     *
     * @param sslCtx
     * @param manufacture
     */
    public HTTPServerInitializer(SslContext sslCtx, String manufacture){
        this.sslCtx = sslCtx;
    }

    /**
     *
     * @param ch
     * @throws Exception
     */
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }

        CorsConfig corsConfig = CorsConfigBuilder
                .forAnyOrigin()
                .allowNullOrigin()
                .allowedRequestMethods(GET,POST,PUT,HEAD,OPTIONS)
                .allowCredentials()
                .build();

        pipeline.addLast(new HttpServerCodec( 4096, 8192, 8192, true));
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new CorsHandler(corsConfig));
        pipeline.addLast(new WebSocketServerCompressionHandler());

//For Websocket
        pipeline.addLast(new WebSocketRequestHandler(WEBSOCKET_PATH, "Hybridcast,HCApp,HCXPLog", true));
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, "Hybridcast,HCApp,HCXPLog" , true));
        pipeline.addLast(new WebSocketFrameHandler(charray));

//For HTTP
        pipeline.addLast(new HTTPFrameHandler(WEBSOCKET_PATH));
    }

    /**
     *
     * @param msg
     */
    public static void sendWSLog(String msg) {

        Log.i("HTTPServerInit Log: ", msg);

        SessionManager sessMan = HTTPFrameHandler.getSessionManager();

        for (int i = 0; i < charray.size(); i++) {
            Channel tmpchannel = charray.get(i);
            try{
                if (tmpchannel.remoteAddress() != null) {
                    String hcAppLogDevId = Const.Config.SubProtocol.HCXPLog + tmpchannel.remoteAddress().toString();
                    Log.i("sendWSLog: ", hcAppLogDevId);

                    SessionManager.SessionInfo sessInfo = sessMan.getSessionByDevID(hcAppLogDevId);
                    if (sessInfo != null) {
                        Log.i("sendWSLog: ", hcAppLogDevId + " send");
                        tmpchannel.writeAndFlush(new TextWebSocketFrame(msg));
                    }
                }
            }catch(NullPointerException e){
                Log.i("sendWSLog: ", e.toString());
            }
        }
    }

}
