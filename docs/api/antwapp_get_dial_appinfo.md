# DialApplicationInfomationAPI

AntWapp4hc情報取得API.AntWapp4hcが提供するAPIのURLやバージョン情報の取得

## Abstract

AntWapp4hcのミドルウェアとしてのApplicationInforamtionを返す.
ハイコネプロトコルがリファーして利用する、[DIALProtocol]()のUPnP/SSDPによるDiscoveryのプロセス.

"[IPTVFJ STD-0013](http://www.iptvforum.jp/download/input.html) 7.2.1 機器発見"（Discovery）を参照.


---

## AnTwapp(Receiver/Server-Side

### endpoint

DIALProtocolのUPnP/SSDPによるDiscoveryのプロセスにおいて、dd.xml取得時にApplication-URLとして取得できる.

最終的なendpointURLはApplicationResourceURLと呼び、以下で定義される.

```
ApplicationResourceURL = [Application-URL]/[ApplicationName]
```

- antwappのendpointURL

```
/apps/antwapp
```

- ハイコネ規格のendpointURL

```
/apps/Hybridcast
```

### WebAPI(Method, endpoint, Parameter, Response)

#### Request

```
HTTP/1.1
GET /apps/antwapp
```

#### Response

```
HTTP/1.1 200 OK

<?xml version="1.0" encoding="UTF-8"?>
<service xmlns="urn:dial-multiscreen-org:schemas:dial"
xmlns:iptv="urn:iptv:HybridcastApplication:2015" dialVer="2.1">
    <name>Hybridcast Application</name>
    <options allowStop="false"/>
    <state>stop</state>
    <additionalData>
        <iptv:X_Hybridcast_TVControlURL>  // (Protocol標準)TVControl用APIのベースURL
           [TVControlBaseURL]
        </iptv:X_Hybridcast_TVControlURL>
        <iptv:X_Hybridcast_App2AppURL>  // (Protocol標準)App連携用websocketAPIendpoint
           [WSServerURL]
        </iptv:X_Hybridcast_App2AppURL>
        <iptv:X_Hybridcast_ServerInfo>    // (Protocol標準)プロトコルバージョン
          [ServerName/Version;ProtocolVersion;MakerId;ModelId;Comment]
        </iptv:X_Hybridcast_ServerInfo>
    </additionalData>
</service>
```


- プロトコルバージョンについて

規格上の意味とAntwappにおける値をリストにする

```
[ServerName/Version;ProtocolVersion;MakerId;ModelId;Comment]
```

| ラベル | 規格上の意味(IPTVFJ STD-0013 7.2.x節) | Antwappで指定する値 |
| --- | --- | --- |
| ServerName/Version | 提供ベンダーの情報 | Antwapp/[build.gradle指定のantwappアプリバージョン] |
| ProtocolVersion | 端末連携拡張プロトコルversion | 2.0 |
| MakerID | ブラウザのUAと同じ値 | NHK |
| ModelID : ブラウザのUAと同じ値 | Antwapp |

---

## クライアント側

### Java API

TBD

### Javascript API

TBD
