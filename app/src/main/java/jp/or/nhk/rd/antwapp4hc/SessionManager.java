package jp.or.nhk.rd.antwapp4hc ;

/**
 * セッション管理
 */
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import io.netty.channel.Channel;

public class SessionManager {
	private List<SessionInfo> sessList = new ArrayList<SessionInfo>() ;

	/**
	 *
	 */
	SessionManager() {}

	/**
	 * getSessionByChannel
	 * @param ch
	 * @return
	 */
	public SessionInfo getSessionByChannel(Channel ch) {
		SessionInfo session = null;
		for( SessionInfo ss : sessList ) {
			if( ss.ctx_channel == ch ) {
				session = ss;
				break;
			}
		}

		return session;
	}

	/**
	 * getSessionByDevID
	 * @param devID
	 * @return
	 */
	public SessionInfo getSessionByDevID(String devID) {
		SessionInfo session = null;
		for( SessionInfo ss : sessList ) {
			if( ss.devID.equals(devID) ) {
				session = ss;
				break;
			}
		}
		return session;
	}

	/**
	 * getSessionBySubProtocol
	 * @param subprotocol
	 * @return List<SessionInfo>
	 */
	public List<SessionInfo> getSessionBySubProtocol(String subprotocol) {
		SessionInfo session = null;
		List<SessionInfo> sessionList = new ArrayList<SessionInfo>() ;
		for( SessionInfo ss : sessList ) { // subprotocolが一致したsessionを全てリストにつっこむ
			if( ss.subprotocol.equals(subprotocol) ) {
				sessionList.add(ss);
			}
		}
		return sessionList;
	}

	/**
	 * デバイスの追加(HCApp用)
	 * @param appname appName
	 */
	public SessionInfo addHCSession(String appname, String appID, String devID) {
		SessionInfo newSess = new SessionInfo(this, appname, appID, devID);

		sessList.add(newSess);

		return newSess;
	}


	/**********************************************************************************************
	 * class SessionInfo
	 *********************************************************************************************/
	public class SessionInfo {
		private SessionManager sessMgr = null;
		public String appName;				//－アプリケーション名
		public String devID ;				//－端末ID：アプリケーションIDに受信機側がユニークな値を付加する
		public Channel ctx_channel;
		public String subprotocol;

		SessionInfo(SessionManager sessMgr, String appname, String appID, String devID) {
			this.sessMgr = sessMgr;
			this.devID = devID;
			this.appName = appname;

			this.subprotocol = "Hybridcast"; // defaultは規格通り
		}

		SessionInfo(SessionManager sessMgr, String appname, String sessid) {
			this.sessMgr = sessMgr;
			this.devID = "";
			this.appName = appname;
			this.subprotocol = "Hybridcast"; // defaultは規格通り
		}

		/**
		 * setChannel
		 * @param ch
		 */
		public void setChannel(Channel ch) {
			this.ctx_channel = ch;

			Log.i("setChannel: ", this.ctx_channel.toString());
		}

		/**
		 * setSubProtocol
		 * @param subprotocol
		 */
		public void setSubProtocol(String subprotocol) {
			this.subprotocol = subprotocol;

			Log.i("setSubProtocol: ", this.subprotocol);
		}
	}
}
