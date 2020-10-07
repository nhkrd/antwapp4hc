package jp.or.nhk.rd.antwapp4hc ;

import java.util.Map;
import java.util.HashMap;
import org.json.JSONjava.JSONObject;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Web of Things(WoT) Discovery Handler Interface.
 * WotDiscoveryInterface are defined as a recommendation in W3C WoT WG.
 * See https://github.com/w3c/wot-discovery
 */
public interface WotDiscoveryInterface {


	/**
	 * WoT Discovery Handler APIs
	 */
	public String genThingDescription(String jsonldTemplate);
	/**
	 * mDNS-SD static variables
	 */

	public final static class mDNS {
				final static String serviceName = "Antwapp4hc" ;
				final static String serviceType = "_wot._tcp" ;
				final static int port = 8887 ;
				final static Map<String, String> txt = new HashMap<String, String>() {{
					put("type", "Thing");
					put("td", "/td/nhktv.jsonld");
				}};
			}
}
