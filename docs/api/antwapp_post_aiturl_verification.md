# AITURI-VerificationAPI

AITURI可否判定API.

AITファイルのURIが放送サービスの許可するAITファイルのURIかどうかを判定する「AITURI可否判定サーバー」へリクエストするAPI.

### Abstract

AITファイルのURIが放送サービスの許可するAITファイルのURIかどうかを判定するAPI.
受信機は「AITURI可否判定サーバー」へリクエストし、そのレスポンス値により判定OKか判定NGかを把握する.

- 判定OKの場合は、以後HCアプリ起動プロセスを継続し、AITURIを使ってHCアプリを起動する
- 判定NGの場合は、HCアプリ起動プロセスを破棄して、HCアプリ起動要求APIのレスポンスにエラーとして、「403 Unacceptable AIT Specified」を返す

"[IPTVFJ STD-0013](http://www.iptvforum.jp/download/input.html) 8.3.1.1.1.3 可否判定リクエスト"を参照.


## AnTwapp(Receiver/Server-Side


### WebAPI(Method, endpoint, Parameter, Response)

#### Request


```
HTTP/1.1
POST <AITURI-VerifierURL>

{
    "resource": {
        "original_network_id": 32726,
        "transport_stream_id": 32726,
        "service_id": 1024
    },
    "hybridcast": {
        "aiturl": "https://example.com/ait/example.ait",
        "orgid": 1,
        "appid": 1
    }
}
```

- [AITURI-VerifierURL]はRequestBodyを受けてAITURI可否判定ができるサーバーのURL

- [AITURI-VerifierURL]は規格としてはHTTPSであるが、AntwappではHTTP/HTTPSどちらでもよい

- RequestBody

    [ハイブリッドキャストアプリ起動要求API](./antwapp_post_hybridcast.md)のリクエストボディのスキーマと同じ. [startAIT_request_schema.json](../../app/src/main/assets/json-schema/startAIT_request_schema.json)

フィールド | 型 | 説明
--- | --- | ---
resource | Object | original_network_id/transport_stream_id/service_idを含むオブジェクト
original_network_id | Number | オリジナルネットワーク識別子(範囲は[0..65535])
transport_stream_id | Number | トランスポートストリーム識別子(範囲は[0..65535])
service_id | Number | サービス識別子(範囲は[0..65535])
hybridcast | Object | aiturl/orgid/ appidを含むオブジェクト
aiturl | String | 実行対象となるハイブリッドキャストアプリケーションのXML-AITのURL
orgid | Number | 事業者ID (範囲は[0..65535])
appid | Number | アプリケーションID (範囲は[0..4294967295])


#### Response

```
HTTP/1.1 200 OK

```

- ステータスコード

「200 OK」以外は判定結果NGとみなす.

200以外のエラー系のステータスコードやメッセージは判定サーバーの仕様による.

code | message | 説明
--- | --- | ---
200 | OK | AITURIが受信機で提示中の放送サービスにとって妥当
400系 | --- | AITURIが受信機で提示中の放送サービスにとって妥当でない
500系 | --- | AITURIが受信機で提示中の放送サービスにとって妥当でない


