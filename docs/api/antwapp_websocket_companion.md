# CompanionCommunicationAPI

端末連携通信API.
websocket通信のメッセージを使って、通信する端末間の操作やリクエストを実行するAPI

## Abstract

Websocket通信の接続により端末連携通信を開始し、テキストメッセージをsend/receiveするためのwebsocketAPI.
端末連携拡張プロトコルでは、通信による操作などはテキストメッセージの中身によって定義されているため、
本ドキュメントにそのメッセージフォーマット記載する.

# AnTwapp(Receiver/Server-Side)

## endpoint

```
WSEndpointURL <- DialApplicationInformationのXMLにおける"X_Hybridcast_App2AppURL"の値
```

## websocketAPI(Method, endpoint, Parameter, Response)


### Send message (as a API) over websocket From Receiver Device


受信機が受信機外の端末へ送信する際のwebsocket通信上のメッセージオブジェクト.

---

#### ***sendTextToCompanionDevice***

受信機搭載のHybridcastブラウザ上のwebアプリ(Hybridcastアプリ)が受信機外の端末へ送信するための、
websocket通信上のメッセージオブジェクト.

```json
{
    "message" : {
        "devid" : "DEVICEID",
        "sendTextToCompanionDevice" : {
            "text" : "TEXTMESSAGE"
        }
    }
}
```

#### ***setURLForCompanionDevice***

受信機搭載のHybridcastブラウザ上のwebアプリ(Hybridcastアプリ)が受信機に設定した"setURLObject"情報を、
受信機が受信機外の端末へ送信するためのメッセージオブジェクト.

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

### Revceive message (as a API) over websocket From Companion Device(Outside of a Receiver Device)

受信機外の端末から受信機へwebsocket通信で送信するメッセージオブジェクト.

---

#### ***sendTextToHostDevice***

受信機外の端末から受信機搭載のHybridcastブラウザ上のwebアプリ(Hybridcastアプリ)に文字列を送信するためのメッセージオブジェクト.

```json
{
    "message" : {
        "devid" : "DEVICEID",
        "sendTextToHostDevice" : {
            "text" : "TEXTMESSAGE"
        }
    }
}
```

#### ***request***

受信機搭載のHybridcastブラウザ上のwebアプリ(Hybridcastアプリ)が受信機に設定した"setURLObject"情報を、
受信機外の端末が受信機へ取得リクエストするためのメッセージオブジェクト.

```json
{
    "control" : {
        "devid" : "DEVICEID",
        "request" : {
            "command" : "setURLForCompanionDevice"
        }
    }
}

```

#### ***Extensions***

受信機外の端末が受信機に対して制御コマンドなどをリクエストする時のメッセージオブジェクト.

```json
{
    "control" : {
        "devid" : "DEVICEID",
        "extensions" : {
            "vendor" : "iptvf",
            "sendKeyCodeToHostDevice" : {
                "keyCode" : "VK_1"
            }
        }
    }
}
```
