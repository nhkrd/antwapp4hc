# APIDocs for antwapp4hc

API document for antwapp4hc


# Abstract

"antwapp4hc" is the android application that serves many APIs as the Rest and Websocket server. "antwapp4hc" has two kinds of the APIs below.The part of these APIs are standardized in "[IPTVFJ STD-0013](#iptvfj-std-0013)".

- RESTAPIs and WebsocketAPIs standardized as "Hybridcast-Connect"(protocol).

  Thease are the APIs standardized in "[IPTVFJ STD-0013](#iptvfj-std-0013) 7.2 Hybridcast-Connect Protocol". Some of these include the functions that are dependent on each implementation. See Interfaces [HyconetHandlerInterface.java](../app/src/main/java/jp/or/nhk/rd/antwapp4hc/HyconetHandlerInterface.java) and [HyconetHandlerInterface.java](../app/src/main/java/jp/or/nhk/rd/antwapp4hc/DIALRestHandlerInterface.java) that are functions instructed on the standardized specification.

- RESTAPIs and WebsocketAPIs that the settings of "antwapp4hc" can be controlled by.


---

antwapp4hcは、Rest/Websocketサーバーとして動作し、以下２種類のwebAPIを提供する。このAPIの一部は[IPTVFJ STD-0013](#iptvfj-std-0013)にて規定されている。

- ハイコネの規格仕様通りのRESTAPIおよびwebsocketAPI

  "[IPTVFJ STD-0013](#iptvfj-std-0013) 7.2 連携端末通信プロトコル"にて規定されているAPI。規格では明記していないが、実装として必須であるAPIを含む。

- antwapp4hcの管理・制御のためのRESTAPI・websocketAPI


# ハイコネ規格仕様API

## 1. RESTAPI

"ハイコネ規格仕様[IPTVFJ STD-0013](#iptvfj-std-0013) 7.2 連携端末通信プロトコル"に基づいてインターフェース
[HyconetHandlerInterface.java](../app/src/main/java/jp/or/nhk/rd/antwapp4hc/HyconetHandlerInterface.java)と[DIALRestHandlerInterface.java](../app/src/main/java/jp/or/nhk/rd/antwapp4hc/DIALRestHandlerInterface.java)
を[HTTPFrameHandler.java](../app/src/main/java/jp/or/nhk/rd/antwapp4hc/HTTPFrameHandler.java)にて以下のように実装。

---

### 1.1 機器発見API

"[IPTVFJ STD-0013](#iptvfj-std-0013) 7.2.1 機器発見"（Discovery）に基づき、機器サーチ、デバイス情報取得のために提供するRESTAPI。

- DIALProtocol規定のDialApplicationInfomationのRESTAPI

  DIALのApplicationResourceURLにてサービスごとの状態(DialApplicationInformation）を返すために以下の仕様のRESTAPIを提供。

  詳細は、インターフェース
[DIALRestHandlerInterface.java](../app/src/main/java/jp/or/nhk/rd/antwapp4hc/DIALRestHandlerInterface.java)
を参照。

  ```
  curl -X GET [IPADDRESS]:8887/apps/Hybridcast
  ```

- RESTAPIのendpointURLのprefix

  DialInfoAPIで取得するXML内に記述される、サービスごと（ここではHybridcast-Connect）の情報。

  ```
  http://[IPADDRESS]:8887/apps/Hybridcast/tvcontrol
  ```

- websocketAPIのendpointURL

  DialInfoAPIで取得するXML内に記述される、サービスごと（ここではHybridcast-Connect）の情報。

  ```
  http://[IPADDRESS]:8887/antwapp/websocket
  ```

---

### 1.2 ハイコネ外部起動API

"[IPTVFJ STD-0013](#iptvfj-std-0013) 7.2.3.2 コンパニオンデバイス起点の通信"に規定されているAPI。これは、受信機に相当する機能の制御用APIであるが、antwapp4hcでは、受信機に相当する機能をもたないため、APIの検査やconsole画面への挙動の擬似的な反映を実行してエミュレートする。詳細はインターフェース[HyconetHandlerInterface.java](../app/src/main/java/jp/or/nhk/rd/antwapp4hc/HyconetHandlerInterface.java)と実装クラス[HTTPFrameHandler.java](../app/src/main/java/jp/or/nhk/rd/antwapp4hc/HTTPFrameHandler.java)を参照。

以下全てのRESTAPIのendpointURLにはprefixとして、DiscoveryAPIで取得したRESTAPIのendpoint-prefixを付加する。

```
http://[IPADDRESS]:8887/apps/Hybridcast/tvcontrol
```

| endpointURLPath | queryParams | method | request schema | response schema | description | 
| --- | --- | --- | --- |--- | --- |
| /media | media | GET | --- | [availableMedia_schema.json](../app/src/main/assets/json-schema/availableMedia_schema.json)  | [detail](./api/antwapp_get_media.md) |
| /chanels | --- | GET | --- | [channels_schema.json](../app/src/main/assets/json-schema/channels_schema.json)  | [detail](./api/antwapp_get_channels.md) |
| /hybridcast | mode | POST | [startAIT_request_schema.json](../app/src/main/assets/json-schema/startAIT_request_schema.json)  | [startAIT_response_schema.json](../app/src/main/assets/json-schema/startAIT_response_schema.json)  | [detail](./api/antwapp_post_hybridcast.md) |
| /hybridcast | --- | GET | --- | [taskStatus_schema.json](../app/src/main/assets/json-schema/taskStatus_schema.json)  | [detail](./api/antwapp_get_hybridcast.md) |
| /status | --- | GET | --- | [receiverStatus_schema.json](../app/src/main/assets/json-schema/receiverStatus_schema.json)  | [detail](./api/antwapp_get_status.md) |

---

## 2. 連携端末通信API

- websocketAPIのendpontURL

"[IPTVFJ STD-0013](#iptvfj-std-0013) 7.2.3.1 受信機起点の通信"に規定されているAPI。受信機（サーバー）および受信機上のHybridcastアプリ（HTML）と、クライアントのアプリケーションを通信するためのwebsocketAPIであり、
通信するメッセージにてメッセージ送信や情報リクエストを実現するものである。
antwapp4hcでは、受信機に相当する機能をもたないため、APIの検査やconsole画面への挙動の擬似的な反映を実行してエミュレートする。

- websocketAPIのendpointはDiscovery時のendpointURL

```curl
http://[IPADDRESS]:8887/antwapp/websocket
```

- message type as a function

| Function | requestJsonSchema | responseJsonSchema | description |
| --- | --- | --- | --- |
| sendTextToCompaonionDevice | [ws_sendTextToCompaonionDevice_schema.json](../app/src/main/assets/json-schema/ws_sendTextToCompaonionDevice_schema.json) | --- | [detail](./api/antwapp_websocket_companion.md#sendtexttoCompaniondevice)  |
| sendTextToHostDevice | [ws_sendTextToHostDevice_schema.json](../app/src/main/assets/json-schema/ws_sendTextToHostDevice_schema.json) | --- | [detail](./api/antwapp_websocket_companion.md#sendtexttohostdevice) | 
| setURLForCompanionDevice | [ws_setURLForCompanionDevice_schema.json](../app/src/main/assets/json-schema/ws_setURLForCompanionDevice_schema.json) | --- | [detail](./api/antwapp_websocket_companion.md#seturlforcompaniondevice) | 
| requestURL | [ws_requestURL_schema.json](../app/src/main/assets/json-schema/ws_requestURL_schema.json) | [ws_setURLForCompanionDevice_schema.json](../app/src/main/assets/json-schema/ws_setURLForCompanionDevice_schema.json) | [detail](./api/antwapp_websocket_companion.md#request) |
| extensionCommand | [ws_extensionCommand_schema.json](../app/src/main/assets/json-schema/ws_extensionCommand_schema.json) | --- | [detail](./api/antwapp_websocket_companion.md#extensions) | 


# antwapp4hc管理制御API

## 1. RESTAPI

### ***configAPI***

Application CofigurationのUIのドキュメント[antwapp_ui.md](./antwapp_ui.md)も参照。

- endponstURL

  - getConfig

    現在のantwappに関するconfigurationを取得。

    ```bash
    curl -X GET http://[IPADDRESS]:8887/api/appconfig
    ```

  - setConfig

    config.jsonにセットしたpropertyのみを更新する。

    ```bash
    curl -X POST -d @config.json http://[IPADDRESS]:8887/api/appconfig
    ```

- requestbody/responsebody: (config.json)

  configのデフォルトは[assets/json/config.json](../app/src/main/assets)で指定可能。（変更可能なプロパティのみ記載）

  | property | syntax | description |
  | :--- | :--- | --- |
  | aitload | boolean | |
  | wsBroadcastMode | boolean | |
  | aitVerifierMode | String | External/Internal/ALLOK |
  | hcViewMode | String | Debug/Full/Both |
  | tuneDelay | String | milliseconds |
  | media | String | filepath |
  | channels | String | filepath |

#### Detail properties in configAPI

- aitload

  AIT(Appilication Information Table)のXMLデータの中身を取得せずに、指定されたhybridcastのURLを起動するモードの設定。

- wsBroadcastMode

  websocketでメッセージを受信した時に受信したメッセージと同じメッセージを返す（送信する）機能のOn/Off.

- aitVerifierMode

  "IPTVFJ STD-0013 4.1 起動シーケンス", "IPTVFJ STD-0013 4.2 外部起動", "IPTVFJ STD-0013 8.3.1.1.1 AIT の URI 可否判定の動作"におけるAITURI可否判定を行うサーバーのURLの設定項目。antwapp4hcはデバッガーとしての機能もあるため、以下３パターンの設定項目を用意し、webAPIやwebUIにて簡単に切替ができる。
    - External: 外部のAITURI可否判定サーバーのURL(判定APIEndpoint)を利用する際の設定項目
    - Internal: Antwapp4hc自身が内部にAITURI可否判定サーバーのAPIを想定した内部URL(判定URIEndpoint)を指定を想定した設定項目
    - ALLOK: デバッグのためにエミュレータではAITURI可否判定をしない



- hcViewMode

  デバッグやデモを目的とした、以下モードを選択してandroidアプリの画面のモード切替が可能。
  - debug: ログviewや設定view、および選局状態・結果view、Hybridacast相当のwebブラウザviewを一画面に表示
  - full: 選局状態・結果view、Hybridacast相当のwebブラウザviewをフル画面で表示
  - both: debug画面とfull画面をボタンで切替えることができる。

    | DebugMode | Log Area | StartAIT Area | WebBrowser Area | Full WebBrowser Area<br/>Only in another window |
    | :---: | :---: | :---: | :---: | :---: |
    | Debug | O | O | O | - |
    | Full | - | - | - | O |
    | Both | O | O | O | O |

- tuneDelay

  StartAITによる外部起動制御にて選局もしくはHybridcastアプリ起動相当の機能をantwappが実行する。その際に選局処理機能が実行されるまでのDelayを調整することができる。

- media

  受信機想定のAntwappが提供する受信機相当が対応するメディア(対応チューナー）の情報のファイルパス設定。デフォルトの編成チャンネルは[/app/src/main/assets/json/media.json](../app/src/main/assets/json/media.json)。json-schemaは"[IPTVFJ STD-0013](#iptvfj-std-0013) 7.2.3.2.3.2.1 メディア(地上デジタル、BS、CS)利用可否情報の取得"で規定されたschema([media_schema.json](../../app/src/main/assets/json-schema/availableMedia_schema.json))である。[antwapp_get_media.md](./api/antwapp_get_media.md)も参照。

- channels

  受信機想定のAntwappが提供する編成チャンネル情報のファイルパス設定。デフォルトの編成チャンネルは[/app/src/main/assets/json/channels.json](../app/src/main/assets/json/channels.json)。json-schemaは"[IPTVFJ STD-0013](#iptvfj-std-0013) 7.2.3.2.3.2.2 編成チャンネル情報の取得"で規定されたschema([channels_schema.json](../../app/src/main/assets/json-schema/channels_schema.json))である。[antwapp_get_channels.md](./api/antwapp_get_channels.md)も参照。

  - （参考）[ARIB TR-B14 "OPERATIONAL GUIDELINES FOR
DIGITAL TERRESTRIAL TELEVISION BROADCASTING"](http://www.arib.or.jp/english/html/overview/doc/8-TR-B14v6_0-5p5-E1.pdf) ,9.2 List of Identifier (Table 9-1, page7-76)
  - （参考）[ARIB TR-B15 "OPERATIONAL GUIDELINES FOR DIGITAL
SATELLITE BROADCASTING"](http://www.arib.or.jp/english/html/overview/doc/8-TR-B15v4_6-3p4-E1.pdf) ,8.2.1 TS_id list/8.2.2 service_id list(page 7-57,7-58)

### ***seturlAPI***


受信機搭載のHybridcastブラウザ上のwebアプリ(Hybridcastアプリ)が受信機に設定した"setURLObject"情報を、擬似的に設定するAPI.

- endpointURL

  ```bash
  curl -X POST -d @seturl.json [IPADDRESS]:8887/api/seturls
  ```

- requestBody(seturl.json)

  [ws_setURLForCompanionDevice_schema.json](../app/src/main/assets/json-schema/ws_setURLForCompanionDevice_schema.json)および[連携端末APIの説明](./api/antwapp_websocket_companion.md#seturlforcompaniondevice)を参照.

  ```json
  {
    "control" : {
    "devid" : "DEVICEID",
    "setURLForCompanionDevice" : {
        "url" : "HybridcastAppURL",
        "options" : {
                "auto_start" : BOOL,
                "app_title" : "HybridcastAppTITLE",
                "app_desc" : "HybridcastAppDescription"
            }
        }
    }
  }
  ```
    
## 2. WebsocketAPI

### ***LogAPI***

logAPIを使ったlogViewer: [hcxplog.html](../app/src/main/assets/hcxplog.html)を参照。

- endpointURL

  ```curl
  [IPADDRESS]:8887/websocket 
  ```

- subprotocol

  ```
  HCXPLog
  ```

- messages

  ```json
  {
    "message":"Get /apps/Hybridcast",
    "status":"Notice",
    "time":"2020-03-30T16:47:38",
    "type":"HCXPLog",
    "remark":"",
    "color":"#ff0080",
    "codepoint":"HTTP Request"
  }
  ```

---

## References

### IPTVFJ STD-0013

[IPTVFJ STD-0013](http://www.iptvforum.jp/download/input.html)  "Hybridcast Operational Guideline Version 2.8", [http://www.iptvforum.jp/download/input.html](http://www.iptvforum.jp/download/input.html)