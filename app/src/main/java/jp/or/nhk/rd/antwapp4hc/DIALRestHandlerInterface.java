package jp.or.nhk.rd.antwapp4hc ;

import java.util.Map;
import java.util.HashMap;
import org.json.JSONjava.JSONObject;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * DIAL Rest Service Handler Interface.
 * Hybridcast Connect Refers DIAL Protocol.
 * See "IPTVFJ STD-0013 7.2.1 Discovery" and DIALProtocol Specification version1.7.2 in http://www.dial-multiscreen.org/dial-protocol-specification "
 */
public interface DIALRestHandlerInterface {


	public static final String applicationName = "Hybridcast";  // Registry in IANA, For Hybridcast Connect.
	public static final String dialApplicationURL = "/apps"; // Dont include suffix "/"
	public static final String dialApplicationResourceURL = dialApplicationURL + "/" + applicationName;

	public static final String mimetype = "text/xml";
	public static final String charset = "charset=UTF-8";
	public static final String mimeParameter = mimetype + "; " + charset;
	public static final String accessControlAllowOrigin = "";

	/**
	 * DIAL Rest Service APIs
	 */

	/**
	 * get Application-Resource-URL(RestAPI endpointURL).
	 * DIAL Application ServiceのRESTAPIのendpointURL(=Application Resource URL)の取得.
	 *
	 * @return Application Resource URL
	 */
	public String getDialApplicationResourceURL();

	/**
	 * DIAL Application Information request Handler by ApplicationResourceURL(Application-URL + "/Hybridcast").
	 * Application Resource URL のリクエストに対して、DIAL Application Information を
	 * "IPTVFJ STD-0013 7.2.1.1.2 additionalData" の拡張処理を付加してレスポンスとして返す処理ハンドラー.
	 * (DIAL で規定されているadditionalDataをHybridcastConnectProtocol仕様でレスポンスとして返す.)
	 *
	 * @param ctx
	 * @param req
	 */
	public void dialRestServiceAppInfoHandler(ChannelHandlerContext ctx, FullHttpRequest req);

	/**
	 * XMLdata genarator for DIAL Application Information response.
	 * DIAL Application Information のXMLを生成するメソッド.
	 *
	 * @param filepath
	 * @return XMLString
	 */
	public String genDialAppinfoXML(String filepath);



	/**
	 * DIAL Rest Service Response Status
	 */


	public final static class DialApplicationState {
		final static String Running = "running";
		final static String Stopped = "stopped";
		final static String Installable = "installable";
	}

	public final static class DialAppInfoResponseStatus {
		final static HttpResponseStatus OK = new HttpResponseStatus( 200, "OK");
		final static HttpResponseStatus NotFound = new HttpResponseStatus( 404, "Not Found");
	}

}
