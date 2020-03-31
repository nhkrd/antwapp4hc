# ReceiverStatusAPI

受信機状態取得API.

## Abstract

受信機に関する状態、特に現在チャンネル、Webブラウザ起動(HCサービス起動)状態、コンパニオンアプリ接続数を返す.

"[IPTVFJ STD-0013](http://www.iptvforum.jp/download/input.html) 7.2.3.2.3.4.2 受信機状態の取得"を参照.

## AnTwapp(Receiver/Server-Side)

### WebAPI(Method, endpoint, Parameter, Response)

#### Request

```
HTTP/1.1
GET <BASEURL>/media
```

- BaseURL <- DialApplicationInformationのXMLにおけるX_Hybridcast_TVControlURL値

#### Response

```
HTTP/1.1 200 OK

{
    "head": {
        "code": 200,
        "message": "OK"
        },
    "body": {
        "status": {
            "hybridcast": "Running",
            "companion_apps": 1,
            "resource": {
                "original_network_id": 32727,
                "transport_stream_id": 32727,
                "service_id": 1032
            }
        }
    }
}
```

- エラーの時は以下を返す

```
HTTP/1.1 500

{
    "head": {
        "code": 500,
        "message": "Internal Server Error"
        },
    "body": {}
}
```

#### schema

- [receiverStatus_schema.json](../../app/src/main/assets/json-schema/receiverStatus_schema.json)

status | Object | 下記の受信機の各動作状態を含むオブジェクト
-- | -- | --
head | Object | HTTPステータスラインの情報を含むオブジェクト
code | Number | HTTPステータスコード
message | String | HTTPステータスコードの説明句.なお、受信機の状態を示す文字列も含まれる
body | Object | 受信機からのレスポンスデータを含むオブジェクト（codeがエラーを示す場合は空とする）
hybridcast | String | ハイブリッドキャストアプリケーションエンジンの起動状態を示す文字列.下記のいずれかが指定される.<br/>"NotStarted": 起動されていない/"Running": すでに起動されている
companion_apps | Number | 受信機に接続しているコンパニオンアプリケーションの数
resource | Object | 選局中の編成サービスを示すオブジェクト<br/>original_network_id/transport_stream_id/service_idを含む.「選局中の編成サービス」は、受信機が本APIにより受信機動作状態の要求を受信した際に提示中の放送局の編成サービスと規定する.<br/>なお、受信機が放送サービスを提示していない状態（外部入力の表示中や電源OFF・スタンバイ状態など）の場合、<br/>original_network_id/transport_stream_id/service_idの値はそれぞれ0が返却される.
original_network_id | Number | オリジナルネットワーク識別子(範囲は[0..65535])
transport_stream_id | Number | トランスポートストリーム識別子(範囲は[0..65535])
service_id | Number | サービス識別子(範囲は[0..65535])


### APIについての補足

---

- 放送サービスがAndroidTVアプリ（または受信機画面）に提示されていない場合は、body.status.resourceオブジェクト内の全ての値を0とする

body.status.reourceのTripletsの値 | 受信機動作状態
-- | --
ARIBにて規程されたTriplets | AndroidTVアプリ（または受信機画面）に放送サービス（映像・音声）が提示されている
全て 0 | 放送サービスがAndroidTVアプリ（または受信機画面）に提示されていない場合


- body.status.hybridcastの値

body.status.<br/>hybridcastの値 | 説明 | 変化点
-- | :--: | --
NotStarted | HCブラウザ未起動<br/>(HCサービス未起動)| リソース切替（選局）実行要求時<br/>（選局処理前にWeb(HC)ブラウザを落とす必要があるため） 
Running | HCブラウザ起動中<br/>(HCサービス起動中) | HTMLブラウザ起動実行要求が成功した後<br/>（起動要求にエラーがなかった場合）<br/>HTMLアプリケーション起動完了時ではない

- 状態の例

例えば、選局/HC起動要求時に発生しうるエラーとそのHCブラウザの前後状態は以下である.

HCブラウザ事前状態 | エラーポイント | エラー後の状態
--- | -- | --
HC未起動時(NotStarted) | HCアプリ起動要求を受信した場合にHCブラウザ起動要求がエラー | 未起動(NotStarted)
HC起動時(Running) | 選局要求を受信した場合に選局要求がエラー | 未起動(NotStarted)
HC起動時(Running) | HCアプリ起動要求を受信した場合にHCブラウザ起動要求がエラー | 未起動(NotStarted)
HC起動時(Running) | HCアプリ起動要求を受信した場合に選局前の処理<br/>（引数チェックやAITURL可否判定など）でエラー | 起動中を維持(Running))


## ClientSide(Recommendation)

#### Javascript API

```
Promise getReceiverStatusFromHostDevice()
```

"[IPTVFJ STD-0013](http://www.iptvforum.jp/download/input.html) 7.1.7.3.2 受信機状態の取得"を参照.
