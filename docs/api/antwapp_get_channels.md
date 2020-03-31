# ChannelsInfoAPI 

編成チャンネル情報取得API.選局可能な編成チャンネルリスト情報の取得.

## Abstract

受信機に設定されている選局可能な編成チャンネル情報をコンパニオンアプリケ
ーションから受信機へ要求し、取得する. 取得したいメディアの指定が可能.

"[IPTVFJ STD-0013](http://www.iptvforum.jp/download/input.html) 7.2.3.2.3.2.2 編成チャンネル情報の取得"を参照.

## AnTwapp(受信機側)

### endpoint

```
BaseURL <- DialApplicationInformationのXMLにおけるX_Hybridcast_TVControlURL値
EndpointURL = <BaseURL>/channels
```


### WebAPI(Method, endpoint, Parameter, Response)

#### Request

```
HTTP/1.1
GET <BaseURL>/channels?media=MEDIA
```

- BaseURL <- DialApplicationInformationのXMLにおけるX_Hybridcast_TVControlURL値
- MEDIAは、"ALL","TD","BS","CS"のいずれか.クエリが省略された場合は、"ALL"として扱う.

#### Response

```
HTTP/1.1 200 OK

{
    "head": {
        "code": 200,
        "message": "OK"
    },
    "body": {  // エラー時はbodyオブジェクトが空となる
        "created_at": "2018-01-01T00:00:00Z",
        "media": [
            {
                "type": "TD",
                "channels": [
                    {
                        "logical_channel_number": "011",
                        "resource": {
                            "original_network_id": 32726,
                            "transport_stream_id": 32726,
                            "service_id": 1024
                        },
                        "broadcast_channel_name": "NHK総合・東京"
                    },
                    {
                        "logical_channel_number": "021",
                        "resource": {
                            "original_network_id": 32727,
                            "transport_stream_id": 32727,
                            "service_id": 1032
                        },
                        "broadcast_channel_name": "NHK教育・東京"
                    }
                ]
            },
            {
                "type": "BS",
                "channels": [
                ]
            },
            {
                "type": "CS",
                "channels": [
                ]
            }
        ]
    }
}
```

- エラー時は以下レスポンスとなる

```
HTTP/1.1 400 Bad Request

{
    "head": {
        "code": 400,
        "message": "Bad Request"
    },
    "body": {}
}
```


#### schema

- [channels_schema.json](../../app/src/main/assets/json-schema/channels_schema.json)


フィールド | 型 | 説明
-- | -- | --
head | Object | HTTPステータスラインの情報を含むオブジェクト
code | Number | HTTPステータスコード
message | String | HTTPステータスコードの説明句.なお、受信機の状態を示す文字列も含まれる
body | Object | 受信機からのレスポンスデータを含むオブジェクト（codeがエラーを示す場合は空とする）
created_at | String | 当該情報を受信機が応答した日時<br/>タイムゾーンをUTCとし、フォーマットは"YYYY-MM-DDThh:mm:ssZ"形式(ISO 8601)とする
media | Object | 地上デジタル、BS、CSそれぞれの編成チャンネル情報を含むオブジェクト
type | String | 当該データが"TD"(地上デジタル)、"BS"、"CS"のどの編成チャンネル情報かを示す文字列
channels | Array | 編成チャンネル情報を含む配列
logical_channel_number | String | 論理チャンネル番号
resource | Object | original_network_id/transport_stream_id/service_idを含むオブジェクト
original_network_id | Number | オリジナルネットワーク識別子(範囲は[0..65535])
transport_stream_id | Number | トランスポートストリーム識別子(範囲は[0..65535])
service_id | Number | サービス識別子(範囲は[0..65535])
broadcast_channel_name | String | 編成サービス名


## ClientSide(Recommendation)

#### Javascript API

```
Promise getChannelInfoFromHostDevice(media)
```

"[IPTVFJ STD-0013](http://www.iptvforum.jp/download/input.html) 7.1.7.1.2 編成チャンネル情報の取得"を参照.