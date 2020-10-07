package jp.or.nhk.rd.antwapp4hc ;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Constant Settings and Parameters for Antwapp4hc (emulator).
 * Antwappの設定やパラメターの定数
 */
class Const {

	final static String AntwappTcpPort = "8887";
	final static String console_url    = "http://127.0.0.1:" + AntwappTcpPort + "/console.html";
	final static String hybridcast_url = "http://127.0.0.1:" + AntwappTcpPort + "/hc.html";
	final static String hcsub_url      = "http://127.0.0.1:" + AntwappTcpPort + "/hcsub.html";
	final static String tune_url       = "http://127.0.0.1:" + AntwappTcpPort + "/tune.html";
	final static String config_path    = "json/config.json";

	static class Status {
		public int Code;
		public String Message;

		public Status(int c, String m) {
			this.Code = c;
			this.Message = m;
		}
	}

	final static class DebugInfo {
		final static class Type {
			final static String HCXPLog = "HCXPLog";
		}
		final static class Status {
			final static String Error = "Error";
			final static String Success = "Success";
			final static String Warning = "Warning";
			final static String PASS = "PASS";
			final static String Notice = "Notice";
		}
	}

	final static String LogBlack = "#000000";
	final static String LogRed   = "#ff0000";
	final static String LogGreen = "#009900";
	final static String LogBlue  = "#0000ff";
	final static String LogPink   = "#ff0080";

	final static class Command {
		final static String accessDialog = "accessDialog" ;
		final static String tune = "tune" ;
	}

	// future: it is DIAL Protocol parameters so that move to DIALRestHandlerInterface
	final static class AntwappStatus {
		final static String stopping = "stopping" ;
		final static String running  = "running" ;
		final static String hidden   = "hidden" ;
	}

	final static class Config {
		final static class versionName {
			final static String Name = "versionName" ;
		}

		final static class userAgent {
			final static String Name  = "userAgent" ;
			final static String Value = "Hybridcast/1.0 (;0;AnTWhapp;2;2;)" ;
		}

		final static class SetUrl {
			final static String Name = "seturl" ;
		}

		final static class SetUrlAutoStart {
			final static String Name = "auto_start" ;
		}

		final static class SetUrlAppTitle {
			final static String Name = "app_title" ;
		}

		final static class SetUrlAppDesc {
			final static String Name = "app_desc" ;
		}

		final static class SubProtocol {
			final static String Name = "subprotocol";
			final static String HCConnect = "Hybridcast" ; // 規格で定められたWSsubprotocol
			final static String HCApp = "HCApp" ; // HCapp側が接続する場合の識別subprotocol
			final static String HCXPLog = "HCXPLog" ; // HCapp側が接続する場合の識別subprotocol
		}

		final static class WSBroadcastMode {
			final static String Name = "wsBroadcastMode" ;

		}

		final static class aitVerifierMode {
			final static String Name = "aitVerifierMode" ;
			final static class Value {
				final static String Internal = "Internal";
				final static String External = "External";
				final static String AllOK = "AllOK";
			}
		}

		final static class aitVerifierUrl {
			final static String Name = "aitVerifierUrl" ;
			final static class Value {
				final static String Internal = "Internal";
				final static String External = "External";
				final static String AllOK = "AllOK";
			}
			final static class Index {
				final static int Internal = 0;
				final static int External = 1;
				final static int AllOK = 2;
			}
			final static class Url {
				final static String Internal = "http://127.0.0.1:: " + Const.AntwappTcpPort + "/api/aitverifier";
				final static String External = "https://example.com/api/validation";
				final static String AllOK = "PASS";
				static String getAllUrl() {
					return (Internal + "|" + External + "|" + AllOK);
				}
			}
		}

		final static class aitVerificationTimeout {
			final static String Name = "aitVerificationTimeout" ;
			final static int DefaultValue = 5000 ;
		}

		final static class aitRequestTimeout {
			final static String Name = "aitRequestTimeout" ;
			final static int DefaultValue = 5000 ;
		}


		final static class tuneDelay {
			final static String Name = "tuneDelay" ;
			final static int DefaultValue = 2000 ;
		}

		final static class tuneMode {
			final static String Name = "tuneMode" ;
			final static class Value {
				final static String Emulator = "Emulator" ;
			}
		}

		final static class hcViewMode {
			final static String Name = "hcViewMode" ;
			final static class Value {
				final static String Debug = "Debug";
				final static String Full = "Full";
				final static String Both = "Both";
			}
		}

		final static class aitLoad {
			final static String Name = "aitload" ;
		}

		final static class media {
			final static String Name = "media" ;
////			final static class Value {
////				final static String Device = "Device";
////			}
		}

		final static class channels {
			final static String Name = "channels" ;
////			final static class Value {
////				final static String Device = "Device";
////			}
		}

		final static class channelsFrom {
			final static String Name = "channelsFrom" ;
			final static class Value {
				final static String File = "File";
			}
		}
		final static class mDNS {
			final static String Name = "mDNS" ;
		}

	}

	// future: it is Hybridcast Connect Protocol parameters so that move to HyconetHandlerInterface
	final static class HybridcastBrowserStatus {
		final static String Running = "Running" ;
		final static String NotStarted = "NotStarted";
	}

	final static class HTTP {
		final static HttpResponseStatus OK = new HttpResponseStatus( 200, "OK");
		final static HttpResponseStatus BadRequest = new HttpResponseStatus( 400, "Bad Request");
		final static HttpResponseStatus Unauthorized = new HttpResponseStatus( 401, "Unauthorized");
		final static HttpResponseStatus NotFound = new HttpResponseStatus( 404, "Not Found");
		final static HttpResponseStatus InternalServerError = new HttpResponseStatus( 500, "Internal Server Error");
	}
	// future: it is Hybridcast Connect Protocol parameters so that move to HyconetHandlerInterface
	final static class PostHybridcastStatus {
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
	// future: it is Hybridcast Connect Protocol parameters so that move to HyconetHandlerInterface
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
	// future: it is Hybridcast Connect Protocol parameters so that move to HyconetHandlerInterface
	final static class StartAITTaskResultStatus {
		final static HttpResponseStatus OK = new HttpResponseStatus( 200, "OK");
		final static HttpResponseStatus TuningFailed = new HttpResponseStatus( 500, "Tuning Failed");
		final static HttpResponseStatus AITProcessingError = new HttpResponseStatus( 500, "AIT Processing Error");
		final static HttpResponseStatus Internalservererror = new HttpResponseStatus( 500, "Internal server error");
		final static HttpResponseStatus BadRequestInternalProcessing = new HttpResponseStatus( 51400, "Bad Request InternalProcessing");
		final static HttpResponseStatus DenyInternalProcessing = new HttpResponseStatus( 51500, "Deny InternalProcessing");
	}

	// deprecated, move to WotHandlerInterface for the future because these params are defined as a WoT-Discovery in W3C WoT-Architecture
	// see reference, https://github.com/w3c/wot-discovery
	final static class mDNS {
		final static String serviceName = "Antwapp4hc" ;
		final static String serviceType = "_wot._tcp" ;
		final static int port = 8887 ;
		final static Map<String, String> txt = new HashMap<String, String>() {{
			put("type", "Thing");
			put("td", "/td/nhktv.jsonld");
		}};
	}
}
