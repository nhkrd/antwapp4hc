package jp.or.nhk.rd.antwapp4hc ;

import java.util.Map;
import java.util.HashMap;
import org.json.JSONjava.JSONObject;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Web of Things(WoT) Handler Interface.
 * WotHandlerInterface are defined as a recommendation in W3C WoT WG.
 * See https://github.com/w3c/wot 
 */
public interface WotHandlerInterface extends WotDiscoveryInterface, WotScriptingInterface {

	public static final String deviceServiceName = "Hybridcast";  // Registry in IANA, For Hybridcast Connect.

	public static final String mimetype = "application/ld+json";
	public static final String charset = "charset=UTF-8";
	public static final String mimeParameter = mimetype + "; " + charset;
	public static final String accessControlAllowOrigin = "";

	/**
	 * WoT Handler APIs
	 */

}
