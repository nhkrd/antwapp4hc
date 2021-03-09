# Antwapp4hc

A "Hybridcast-Connect" emulator that processes its protcols for Android TV. 

[Japanese](./README_JP.md)

## Overview

"antwapp4hc" is an AndroidTV App as an example of application control module for "Hybridcast-Connect" that was standardized in Sep. 2018 at IPTV Forum Japan. The application can work together with "Hybridcast-Connect" reference client(SDK) "[hyconet4j](https://github.com/nhkrd/hyconet4j)". "Hybridcast-Connect" enables to control some of the broadcast reception functions on a TV Set from an application on the companion devices. It also enables communication between a Hybridcast application or a TV set and an application on a companion device by text messages.
 
The OSS, "antwapp4hc", works as an emulator of "Hybridcast-Connect" ready TV and is a part of the conformance test tool for "Hybridcast-Conect" protocols. By using this emulator, you can see how the process is working on the "Hybridcast-Connect" protocol.
For more details on "Hybridcast-Conect", see [About "Hybridcast-Connect"](./HybridcastConnect.md). For use of "antwapp4hc", see [LICENSE](./LICENSE.txt) and [NOTICE](./NOTICE.txt).

!["Hybridcast-Connect" Overview](./docs/imgs/hybridcast-connect-overview-oss.png)

- Reference
    - [About "Hybridcast-Connect"](./HybridcastConnect.md)
    - [IPTVFJ STD-0013 "Hybridcast Operational Guideline"](https://www.iptvforum.jp/download/input.html)
    - [W3C TPAC2018 Media & Entertainment IG "Recent Achievement Of Hybridcast in TPAC2018"](https://www.w3.org/2011/webtv/wiki/images/4/45/RecentAchievementHybridcast_TPAC20181022.pdf)
    - [W3C TPAC2019 Media & Entertainment IG "Recent Achievement Of Hybridcast in TPAC2019"](https://www.w3.org/2011/webtv/wiki/images/d/d1/MediaTimedEventsInHybridcast_TPAC20190916.pdf)
    - [W3C TPAC2020 Media & Entertainment IG "Recent Achievement Of Hybridcast in TPAC2020"](https://www.w3.org/2011/webtv/wiki/images/2/22/RecentUpdateHybridcast_TPAC20201021_%281%29.pdf)

## Environment

- Android(TV) OS
  - AndroidTVOS
    - AndroidTV OS 7.0
    - AndroidTV OS 8.0
  - FireOS
    - FireTVStick（2nd Gen）: Fire OS 5.2.6.6(AndroidOS:5.1 base)
    - FireTVStick（3rd Gen）: Fire OS 6.2.6.6(AndroidOS:7.0 base)

- Dependencies (for more dettail, see file "build.gradle")
  - commons-codec-1.10.jar
  - jzlib-1.1.3.jar
  - netty-buffer-4.1.48.Final.jar
  - netty-codec-4.1.48.Final.jar
  - netty-codec-http-4.1.48.Final.jar
  - netty-common-4.1.48.Final.jar
  - netty-resolver-4.1.48.Final.jar
  - netty-transport-4.1.48.Final.jar
  - netty-handler-4.1.48.Final.jar
  - JSON-java-20170220.jar (NOTICE: see [License](#license))
  - Android SDK/packages
    - com.android.tools.build:gradle:3.5.3
    - com.android.support:leanback-v17:21.0.3
    - com.google.android.gms:play-services:8.3.0

---

## Build


### By Android Studio

Use Android Studio to build.

### By docker

Use docker to build.
"docker-compose" is also available to build.

```bash
$ ./make_docker_image.sh
$ ./build_in_docker.sh
```

or

```
$ docker-compose build
$ docker-compose up
```

## Directories

### ./app/libs

- Dependencies
  - JSON-java-20170220.jar (NOTICE: see [License](#license))



### ./docs

Documents on APIs, instruction, and others.

---

## Installation

This section describes how to install "antwapp4hc". To install the app "antwapp4hc.apk", you can transfer the apk file by Android Studio or adb command directly.

### For AndroidTV OS

- Configure Network
- Install
    - Set "Developer mode" in configuration menu.
    - Set ADB Debug to "Enabled".
    - Install by adb command.
  ```
  adb connect [IPAddress of AndroidTV]
  adb install [apk file name of AndriudTV]
  ```

### For FireOS

- Configure Network and Account.
- Install
  - Follow [Setting] -> [MyFireTV] -> [Developer Option] and set items as follows. For menu structure, see FireTV manual.
    - ADB Debug to "Enabled".
    - Unknown App to "Enabled".
  - Install by adb command. IP address is shown by [Setting] -> [MyFireTV] -> [VersionInfo] -> [Network]. See FireTV manual.

  ```
  adb connect [IPAddress of FireTVStick]
  adb install [apk file name of FireTVStick]
  ```

### For Android OS

TBD

---

## How To Use

---

### Launch

After installing this software to the device (AndroidTV Receiver or FireStick, etc), find the app icon "antwapp" on the home menu of the device and press it.

- When launching by adb command

  Use the command below. (It's the same when restarting)

  ```bash
  adb shell am start -n "jp.or.nhk.rd.antwapp4hc/.WebViewActivity" -s
  ```

### Application View

"antwapp4hc" can show debug messages and configure tha settings of "antwapp4hc". Screen comprises of following 3 areas. For more details, see [antwapp_ui.md](./docs/antwapp_ui.md).

- Log Area
- startAIT Request Information Area
- WebBrowser(Hybridcast) Display Area

![Application_View](./docs/imgs/antwapp_console_tune_screen.jpg)



#### Tabs on the Window

You can change the content by seleting tabs. For more details, see [antwapp_ui.md](./docs/antwapp_ui.md)

- Log (ログ表示)

  Messages related to reception, processing, and response by "antwapp4hc" are shown. Debug messages are also shown here.

- Configuration (設定情報)

  You can change forlowng settings.

  - AITload
  - wsBroadcastMode
  - aitVerify
  - hcViewMode
  - Delay

- App screen (アプリ画面へ遷移)

- Clear the log (ログクリア) 
  
  Clear the messages in log area

---
## HTML

"antwapp4hc" provides html documents for console view of AndroidTV App and other prsentable infomation by a Web browser. Some of the documents are also samples for checking data and APIs.

```
-- assets
  |-- config.html  <-- configuration form
  |-- console.html <-- debug console(same as home view of android app)
  |-- hc.html      <-- full view for browser window like hybridcast
  |-- hcsub.html   <-- small view for browser window like hybridcast
  |-- hcxplog.html <-- log
  |-- hybridcast   <-- directory for hybridcast applcation (sample)
  |-- index.html   <-- toppage for antwapp4hc
  |-- tune.html    <-- status of tuning
  `-- wsclient.html<-- websocket client for checking data and APIs
```

- To access these HTML documents from Web browser

  ```
  http://[IPAddress]:8887/[html-file-name]
  
  Ex. http://[IPAddress]:8887/console.html
  ```

---

## [APIs](./docs/apidocs.md)

"antwapp4hc" offers following 2 kinds of APIs in either REST or Websocket. 

For more details, see [API documents](./docs/apidocs.md).

- REST APIs and Websocket APIs defined in "[IPTVFJ STD-0013](./HybridcastConnect.md#iptvfj-std-0013)" 
- REST APIs and Websocket APIs to control and manage "antwapp4hc"


## Rererence implementation for a W3C WoT Device

"antwapp4hc" implements some of the following functions as the reference implementation in W3C Web of Things(WoT) IG/WG. "antwapp4hc" contributes to the Plugfest(Interoperability TestEvent) held by WoT Group.

- WoT Discovery: mDNS-SD.
- WoT ThingDescription: ThingDescription for Hybridcast-Connect implemented TVSet.


# License

See [LICENSE.txt](./LICENSE.txt) and [NOTICE.txt](./NOTICE.txt).


---

"antwapp4hc" includes other oss packages due to some reasons.

- JSON-java-20170220.java ([License](https://github.com/stleary/JSON-java/blob/master/LICENSE))

    - Repository: https://github.com/stleary/JSON-java
    - LICENSE: https://github.com/stleary/JSON-java/blob/master/LICENSE

    This is the common Java implementation for JSON processing. In case of the use on Android, there's [confliction problem between JSON-java and android](https://github.com/stleary/JSON-java/wiki/JSON-Java-for-Android-developers). To solve this problem, "antwapp4hc" includes the modified version in which  package name is chaned from "org.json" to "JSON-java". See [License](https://github.com/stleary/JSON-java/blob/master/LICENSE).

- focus-manager.js (BSD-3-Clause)

  "antwapp4hc" uses [focus-manager.js](./app/src/main/assets/js/focus-manager.js) for focus contorol on the display of a device(AndroidTV/FireStick,etc). See License in [focus-manager.js](./app/src/main/assets/js/focus-manager.js).
