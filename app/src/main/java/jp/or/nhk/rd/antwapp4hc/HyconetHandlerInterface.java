package jp.or.nhk.rd.antwapp4hc ;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONjava.JSONObject;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * HyconetHandler Interface
 */
public interface HyconetHandlerInterface extends DIALRestHandlerInterface {

	public static final String protocolVersion = "2.1";
	public static final String restApiEndpoint = "/antwapp/tvcontrol";
	public static final String websocketApiEndpoint = "/antwapp/websocket";
	public static final Map<String, String> restApiPath = new HashMap<String, String>() {
		{
			put("media", "/media");
			put("channels", "/channels");
			put("status", "/status");
			put("hybridcast", "/hybridcast");
		}
	};

	/*
		Hybridcast Connect Interfase for Discovery.
		"IPTVFJ STD-0013 7.2.1 Discovery"
	*/

	/**
	 * DIAL Application Information request Handler by ApplicationResourceURL(Application-URL + "/Hybridcast").
	 * Application Resource URL のリクエストに対して、DIAL Application Information を
	 * "IPTVFJ STD-0013 7.2.1.1.2 additionalData" の拡張処理を付加してレスポンスとして返す処理ハンドラー.
	 * (DIAL で規定されているadditionalDataをHybridcastConnectProtocol仕様でレスポンスとして返す.)
	 * @param ctx
	 * @param req
	 */
	@Override
	public void dialRestServiceAppInfoHandler(ChannelHandlerContext ctx, FullHttpRequest req);

	/**
	 * XMLdata genarator for DIAL Application Information response.
	 * DIAL Application Information のXMLを生成するメソッド.
	 * @param filepath
	 * @return XMLString
	 */
	@Override
	public String genDialAppinfoXML(String filepath);

	/**
	 * DIAL Application Information Data Response.
	 * Application InformationのXMLに"IPTVFJ STD-0013 7.2.1.1.2 additionalData" の拡張処理を付加してレスポンスとして返す処理.
	 * @param ctx
	 * @param req
	 */
	public void sendDialAppInfo(ChannelHandlerContext ctx, FullHttpRequest req);


	/*
		Hybridcast Connect REST Interfase Respose Handler.
		"IPTVFJ STD-0013 7.2.3.2.3 RESTful API"
	*/

	/**
	 * "7.2.3.2.3.2.1 メディア(地上デジタル、BS、CS)利用可否情報の取得" の処理ハンドラー.
	 * 受信機に設定されている利用可能なメディアをコンパニオンアプリケーションから受信機へ要求を処理してレスポンスを返す.
	 *
	 * @param ctx
	 * @param req
	 */
	public void sendAvailableMedia(ChannelHandlerContext ctx, FullHttpRequest req);

	/**
	 * 	"IPTVFJ STD-0013 7.2.3.2.3.2.2" 編成チャンネル情報の取得” の処理ハンドラー.
	 * 	受信機に設定されている選局可能な編成チャンネル情報をコンパニオンアプリケーションから受信機へ要求を処理してレスポンスを返す.
	 * @param ctx
	 * @param req
	 * @param params
	 */
	public void sendChannelsInfo(ChannelHandlerContext ctx, FullHttpRequest req, String[] params);

	/**
	 * "IPTVFJ STD-0013 7.2.3.2.3.3.1 ハイブリッドキャストの起動要求" の処理ハンドラー.
	 * POST データで指定する情報に従ってハイブリッドキャストアプリケーションの起動（または選局のみ）を受信機へ要求を処理してレスポンスを返す.
	 * "IPTVFJ STD-0013 Figure 7-11"
	 * @param ctx
	 * @param req
	 * @param params
	 */
	public void launchHcApps(ChannelHandlerContext ctx, FullHttpRequest req, String[] params);

	/**
	 * "IPTVFJ STD-0013 7.2.3.2.3.4.1 起動要求成否の取得” の処理ハンドラー.
	 * 起動要求(本書 7.2.3.2.3.3.1 節参照)を受理した後の受信機の処理結果を受信機へ要求を処理してレスポンスを返す.”
	 * "IPTVFJ STD-0013 Figure 7-12"
	 * @param ctx
	 * @param req
	 */
	public void sendLaunchHCAppStatus(ChannelHandlerContext ctx, FullHttpRequest req);

	/**
	 * "IPTVFJ STD-0013 7.2.3.2.3.4.2 受信機状態の取得" の処理ハンドラー.
	 * ハイブリッドキャストアプリケーションエンジンの起動状態、コンパニオンデバイスの接続数、選局中の編成サービスを要求を処理してレスポンスを返す.
	 * @param ctx
	 * @param req
	 */
	public void sendReceiverStatus(ChannelHandlerContext ctx, FullHttpRequest req);



	/**
	 * sub Methods on processing Validation and Launching flow.
	 * "7.2.3.2.3.5 受信機からのレスポンスと処理動作"に添ったメソッドのInterfaces.
	 */

	/**
	 * Request Validation Interface.
	 * "[処理1] in IPTVFJ STD-0013 Figure 7-11"
	 *
	 * @param ctx
	 * @param req
	 * @return Boolean paramsValid or Not
	 */
	public Boolean isRequestValid(ChannelHandlerContext ctx, FullHttpRequest req);

	/**
	 * AITURL verification
	 *
	 * @param str
	 * @return
	 */
	public Boolean isAiturlValidOnAitVerification(String str);


	/**
	 * Request Policy Validation Interface
	 * "[処理1] in IPTVFJ STD-0013 Figure 7-11"
	 * @param ctx
	 * @param req
	 * @return Boolean params Valid or Not
	 */
	public Boolean isRequestPolicyValid(ChannelHandlerContext ctx, FullHttpRequest req);

	/**
	 * Request Header Params Validation Interface
	 * "[処理1] in IPTVFJ STD-0013 Figure 7-11"
	 * @param ctx
	 * @param req
	 * @return Boolean paramsValid or Not
	 */
	public Boolean isRequestHeaderValid(ChannelHandlerContext ctx, FullHttpRequest req);

	/**
	 * generator TaskId Interface.
	 *
	 * @param req
	 * @return String TaskId
	 */
	public String genTaskId(FullHttpRequest req);

	/**
	 * request params Validation Interface.
	 * "[処理1] in IPTVFJ STD-0013 Figure 7-11"
	 * @param ctx
	 * @param req
	 * @return Boolean body Valid or Not
	 */
	//public Boolean isRequestParamsValid(ChannelHandlerContext ctx, FullHttpRequest req);


	/**
	 * request query parameters validation interface.
	 * "[処理1] in IPTVFJ STD-0013 Figure 7-11"
	 * @param ctx
	 * @param req
	 * @return boolean body valid or not
	 */
	public Boolean isRequestQueryParamsValid(ChannelHandlerContext ctx, FullHttpRequest req);


	/**
	 * get request mode from queryParams.
	 * "[処理1] in IPTVFJ STD-0013 Figure 7-11"
	 * @param params strings of queryParams
	 * @return mode name of string
	 * *
	 */
	public String getRequestModeFromQueryString(String[] params);

	/**
	 * Receiver Policy Validation Interface.
	 * "[処理1] in IPTVFJ STD-0013 Figure 7-11"
	 * @param ctx
	 * @param req
	 * @return Boolean body Valid or Not
	 */
	public Boolean isReceiverPolicyValid(ChannelHandlerContext ctx, FullHttpRequest req);

	/**
	 * channel resource entried Interface
	 * "[処理1] in IPTVFJ STD-0013 Figure 7-11"
	 * @param req
	 * @return Boolean body Valid or Not
	 */
	public Boolean isChannelResourceEntried(ChannelHandlerContext ctx, FullHttpRequest req);

	/**
	 * Permission of Launch Request Interface
	 * "[処理1] in IPTVFJ STD-0013 Figure 7-11"
	 * @return Boolean body Valid or Not
	 */
	public Boolean isStartAitPermitted();

	/**
	 * Hybridcast Mode Enable / Disable Interface
	 * "[処理2] in IPTVFJ STD-0013 Figure 7-11"
	 * @return Boolean body Valid or Not
	 */
	public Boolean isHybridcastEnabled();


	/**
	 * Request AitUrl Validation Interface
	 * "[処理3] in IPTVFJ STD-0013 Figure 7-11"
	 * @param jsobj
	 * @return Boolean body Valid or Not
	 */
	public Boolean isAitUrlValid(JSONObject jsobj);

	/**
	 * Request Body Validation Interface.
	 * "[処理3] in IPTVFJ STD-0013 Figure 7-11"
	 * @param str
	 * @return Boolean body Valid or Not
	 */
	public Boolean isHybridcastConnectObjectValid(ChannelHandlerContext ctx, String str);

	/**
	 * get XMLAIT from AITURL.
	 * "[処理5] in IPTVFJ STD-0013 Figure 7-11"
	 * @param aiturl
	 * @return String body Valid or Not
	 */
	public String getAitXmlFromAitUrl(ChannelHandlerContext ctx, String aiturl);

	/**
	 * validation XMLAIT.
	 *  "[処理5] in IPTVFJ STD-0013 Figure 7-11"
	 * @param xmlait
	 * @return Boolean body Valid or Not
	 */
	public Boolean isAitXmlValid(ChannelHandlerContext ctx, String xmlait);

	/**
	 * comparison orgid/appid between XMLAIT and request params.
	 *  "[処理5] in IPTVFJ STD-0013 Figure 7-11"
	 * @param aiturl
	 * @return Boolean body Valid or Not
	 */
	public Boolean isHybridcastAitIDValid(ChannelHandlerContext ctx, String aiturl);

	/**
	 * 指定されたXMLAITファイルから起動するHybridcastHTMLApplicationのURLを取得する.
	 *  "[処理5] in IPTVFJ STD-0013 Figure 7-11"
	 * @param aiturl
	 * @return
	 */
	public String getHCURLfromAIT(String aiturl);


	/*
		Hybridcast Connect REST Interfase Respose Status.
		"IPTVFJ STD-0013 7.2.3.2.3 RESTful API"
	 */
	final static class HTTPResponse {
		final static HttpResponseStatus OK = new HttpResponseStatus( 200, "OK");
		final static HttpResponseStatus Created = new HttpResponseStatus( 201, "Created");
		final static HttpResponseStatus BadRequest = new HttpResponseStatus( 400, "Bad Request");
		final static HttpResponseStatus Unauthorized = new HttpResponseStatus( 401, "Unauthorized");
		final static HttpResponseStatus InternalServerError = new HttpResponseStatus( 500, "Internal Server Error");
	}

	final static class HybridcastBrowserStatus {
		final static String Running = "Running" ;
		final static String NotStarted = "NotStarted";
	}

	final static class StartAITResponse {
		final static HttpResponseStatus OK = new HttpResponseStatus( 200, "OK");
		final static HttpResponseStatus Created = new HttpResponseStatus( 201, "Created");
		final static HttpResponseStatus BadRequest = new HttpResponseStatus( 400, "Bad Request");
		final static HttpResponseStatus Unauthorized = new HttpResponseStatus( 401, "Unauthorized");
		final static HttpResponseStatus UnacceptableAITSpecified = new HttpResponseStatus( 403, "Unacceptable AIT Specified");
		final static HttpResponseStatus ProcessingAnotherRequest = new HttpResponseStatus( 500, "Processing Another Request");
		final static HttpResponseStatus RequestRefused = new HttpResponseStatus( 500, "Request Refused");
		final static HttpResponseStatus HybridcastDisabled = new HttpResponseStatus( 503, "Hybridcast Disabled");
		final static HttpResponseStatus InternalServerError = new HttpResponseStatus( 500, "Internal Server Error");
		final static HttpResponseStatus BadRequestInternalProcessing = new HttpResponseStatus( 51400, "Bad Request InternalProcessing");
		final static HttpResponseStatus DenyInternalProcessing = new HttpResponseStatus( 51500, "Deny InternalProcessing");
	}

	final static class StartAITTaskStatus {
		final static String inProcess = "InProcess" ;
		final static String Done = "Done";
		final static String Error= "Error";

		final static HttpResponseStatus OK = new HttpResponseStatus( 200, "OK");
		final static HttpResponseStatus TuningFailed = new HttpResponseStatus( 500, "Tuning Failed");
		final static HttpResponseStatus AITProcessingError = new HttpResponseStatus( 500, "AIT Processing Error");
		final static HttpResponseStatus Internalservererror = new HttpResponseStatus( 500, "Internal server error");
		final static HttpResponseStatus BadRequestInternalProcessing = new HttpResponseStatus( 51400, "Bad Request InternalProcessing");
		final static HttpResponseStatus DenyInternalProcessing = new HttpResponseStatus( 51500, "Deny InternalProcessing");
	}

	final static class StartAITTaskResultStatus {
		final static HttpResponseStatus OK = new HttpResponseStatus( 200, "OK");
		final static HttpResponseStatus TuningFailed = new HttpResponseStatus( 500, "Tuning Failed");
		final static HttpResponseStatus AITProcessingError = new HttpResponseStatus( 500, "AIT Processing Error");
		final static HttpResponseStatus Internalservererror = new HttpResponseStatus( 500, "Internal server error");
		final static HttpResponseStatus BadRequestInternalProcessing = new HttpResponseStatus( 51400, "Bad Request InternalProcessing");
		final static HttpResponseStatus DenyInternalProcessing = new HttpResponseStatus( 51500, "Deny InternalProcessing");
	}

	/*
		Hybridcast Connect BIA mode .
	 */
	final static class StartAITmodeBIA {
		final static int original_network_id = 0;
		final static int transport_stream_id = 0;
		final static int tlv_stream_id = 0;
		final static int service_id = 0;
		final static String logical_channel_number = "";
		final static String broadcast_channel_name = "BIA";
	}
}
