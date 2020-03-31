# MediaAvailabilityAPI

メディア利用可否情報API.受信機が受信可能なメディアの情報取得.

## Abstract

受信機に設定されている利用可能なメディアをコンパニオンアプリケーションから受信機へ要求し、取得する.

"[IPTVFJ STD-0013](http://www.iptvforum.jp/download/input.html) 7.2.3.2.3.2.1 メディア(地上デジタル、BS、CS)利用可否情報の取得"を参照.

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
    "body": {  // エラーの場合、Bodyオブジェクトは空
        "created_at": "2018-01-01T00:00:00Z",
        "TD": "Available",
        "BS": "NotAvailable",
        "CS": "NotAvailable"
    }
}
```

- エラーの場合のレスポンス

```
HTTP/1.1 500 Internal Server Error

{
    "head": {
        "code": 500,
        "message": "Internal Server Error"
    },
    "body": {}
}
```

#### schema

- [availableMedia_schema.json](../../app/src/main/assets/json-schema/availableMedia_schema.json)


フィールド | 型 | 説明
-- | -- | --
head | Object | HTTPステータスラインの情報を含むオブジェクト
code | Number | HTTPステータスコード
message | String | HTTPステータスコードの説明句.なお、受信機の状態を示す文字列も含まれる
body | Object | 受信機からのレスポンスデータを含むオブジェクト（codeがエラーを示す場合は空とする）
created_at | String | 当該情報を受信機が応答した日時<br/>タイムゾーンをUTCとし、フォーマットは"YYYY-MM-DDThh:mm:ssZ"形式(ISO 8601)とする
TD | String | 地上デジタルの選局可否<br/>"Available"：選局可能、"NotAvailable"：選局不可のいずれかの文字列
BS | String | BSの選局可否<br/>"Available"：選局可能、"NotAvailable"：選局不可のいずれかの文字列
CS | String | CSの選局可否<br/>"Available"：選局可能、"NotAvailable"：選局不可のいずれかの文字列


## ClientSide(Recommendation)

#### Javascript API

```
Promise getAvailableMediaFromHostDevice()
```

"[IPTVFJ STD-0013](http://www.iptvforum.jp/download/input.html) 7.1.7.1.1 メディア(地上デジタル、BS、CS)利用可否情報の取得"を参照.