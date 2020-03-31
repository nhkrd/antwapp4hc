## About Application View on Antwapp

### Application View

![antwapp_Application_view](./imgs/antwapp_config_app_screen.jpg)

"Antwapp4hc" has application views(views as Android App) to display debug messages and to set configuration that changes functions of antwapp4hc. These views are shown on a display of device and you can check log message of "Antwapp4hc" as debug information and change configuration to debug Hybridcast Connect protocol.

"Antwapp4hc"は、デバッグメッセージの表示や設定変更のためのアプリ画面をAndroidアプリとしてを用意しています。デバイス上のディスプレイにてアプリの画面として表示し、デバッグのためのlog表示、設定変更ができます。以下はその説明です。

- Log Area

  Show log information on the display of the Antwapp4hc android application view.Here is log level and the color of the messages corresponding to each loglevel.

  Antwapp4hcが通信した際の受信・処理・応答に関するメッセージをログとして表示する.


  | Color | Meaning of the messages |
  | :---: | :--- |
  | <font color="Red">Red</font> | - Error message for the webAPI in Hybridcast Connect Protocol. <br/> - ハイコネプロトコル対応APIに対するエラーメッセージ. |
  | <font color="Green">Green</font> | - Message that indicates completing the processing of the webAPI in Hybridcast Connect Protocol.<br/>- ハイコネプロトコル対応APIに対する処理完了メッセージ. |
  | <font color="Blue">Blue</font> | - Received message for the webAPI in Hybridcast Connect Protocol.<br/>- ハイコネプロトコル対応APIに対する受信メッセージ. |
  | <font color="Black">Black</font> | - Detail information on the webAPI in Hybridcast Connect Protocol.<br/>- ハイコネプロトコル対応APIに対する詳細情報. |
  | <font color="Yellow">Yellow</font> | - Messages related with the process of websocketAPI.<br/>- ハイコネプロトコルの連携端末通信websocketに関係する受信・処理・応答に関するメッセージ. |


- startAIT Request Information Area

  Show current channel information and the properties of the received message body that a client application requests.

  選局・HC起動APIがクライアントから要求された際に、リクエストの内容(チャンネル情報・HCのAITURLなど)を表示する.

  - Mode:
    - startAIT API request mode(tune/app)
    - 選局・ハイブリッドキャスト起動APIのモード
  - Resource: channel information | 編成チャンネル情報
    - NWID: original_network_id
    - TSID: transport_stream_id
    - SVID: service_id
  - Hybridcast: 
    - AITURL: AITURL to launch| 起動要求に含まれるハイブリッドキャストのAITURL
    - HCURL:  Hybridcast App URL to launch | 起動要求に含まれるハイブリッドキャストのAITURL内に記述されているURL
    - ORGID:  "orgid" defined in IPTVFJ STD-0013
    - APPID:  "appid" defined in IPTVFJ STD-0013

- WebBrowser(Hybridcast) Display Area

  - show HTML(web)application that is expected as Hybridacast application.
  - クライアントから要求された選局・HC起動APIに記述されたHTMLを表示する画面.選局要求では、放送波を想定してアプリ内部に実装してあるhtmlを要求された放送局と仮定して表示する.HC起動要求では、選局要求の動作に加えて、指定されたAITURLに記述されているHCアプリのHTMLを表示する.


#### Tabs on the Window

It can be button to switch window.

このタブはボタンとして機能し、以下の画面に切り替えることができる。

- "ログ表示" -- Log

  Antwapp4hcアプリが通信した際の受信・処理・応答に関するメッセージ表示するタブ.debugメッセージも出力する.

- "設定情報" -- Config

  - AITload
    - Switch On/Off(true/false) to ignore processing Verification of a AITURL that is defined in IPTVFJ STD-0013.
    - 選局・HC起動APIを受信した時の指定されたURLで取得するファイルタイプを変更できる（AITまたはHTML）
  - wsBroadcastMode
    - if true, antwapp4hc responds(sends) the same message as the message received from a client over websocket.
    - websocketでメッセージを受信した時に受信したメッセージと同じメッセージを返す（送信する）機能のOn/Off.
  - aitVerify
    - Select the AITURL-Verification-Server URL to verify the AITURL requested from client. If "ALL OK" mode selected, the process of the AITURL-Verification will be ignored, so that no request to verify.
    - AITURL可否判定サーバーのURLの指定をすることができる。External/InternalのモードへのURL設定はbuild時のパラメタで決まる。"ALL OK"を指定するとAITURL判定処理が無視され、判定リクエストもされない。
  - hcViewMode
    - Switch display-mode between Debug/Full/Both. In DEBUG mode, show all area on the display. In FULL mode, show only WebBrowser Display Area. In Both mode, switch to show areas in DEBUG mode and FULL mode.
    - ログ・設定画面と選局・Hybridcast想定画面の表示方法を設定

    | DebugMode | Log Area | StartAIT Area | WebBrowser Area | Full WebBrowser Area<br/>Only in another window |
    | :---: | :---: | :---: | :---: | :---: |
    | Debug | O | O | O | - |
    | Full | - | - | - | O |
    | Both | O | O | O | O |

  - Delay(milliseconds)
    - Delay time-interval until excuting tune process in startAIT.
    - 選局APIを受信した際に、選局処理実行の前に入れることができる遅延時間

- "アプリ画面へ遷移"
  - TBD
  - 未実装

- "ログクリア" -- Clear message in log area
  - Clear message displayed in Log Area.
  - ログ画面をクリアする


  
