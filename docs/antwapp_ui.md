## About Application View on Antwapp

[Japanese](./antwapp_ui_jp.md)

### Application View

![antwapp_Application_view](./imgs/antwapp_config_app_screen.jpg)

"Antwapp4hc" has sevral views for various purposes such as showing debug messages and changing configuration. Views of "Antwapp4hc" are displayed as an Android application.

- Log Area(ログ表示)

  Messages related to reception, processing, and response by "antwapp4hc" are shown. Debug messages are also shown here. Clasification of messages and their colors are listed below.

  | Color | Meaning of the messages |
  | :---: | :--- |
  | <font color="Red">Red</font> | - Error for the call of APIs in Hybridcast-Connect Protocol. |
  | <font color="Green">Green</font> | - Completion of the process of the APIs called in Hybridcast-Connect Protocol. |
  | <font color="Blue">Blue</font> | - Received message by the APIs in Hybridcast-Connect Protocol. |
  | <font color="Black">Black</font> | - Detail information on the process of the APIs called in Hybridcast-Connect Protocol. |
  | <font color="Yellow">Yellow</font> | - Messages related to the process in response to the websocket messages of Hybridcast-Connect protocol. |


- startAIT Request Information Area

  When tunig and/or launching an Hybridcast application is requested, contents of the request, such as target channel or URL of AIT of target Hybridcast application, is shown here.

  - Mode:
    - request type(tuning only/tuning and launching)
  - Resource:
    - infomation on target channel
      - NWID: original_network_id
      - TSID: transport_stream_id
      - SVID: service_id
  - Hybridcast: 
    - AITURL: URL of AIT for the target Hybridcast application in the request message
    - HCURL: URL of the Hybridcast Application described in the AIT
    - ORGID:  "orgid" defined in IPTVFJ STD-0010
    - APPID:  "appid" defined in IPTVFJ STD-0010

- Hybridcast application Area(Hybridcast想定HTML表示画面)

  - When the request is tuning only, built-in HTML document is shown to indicate completion of tuning.
  - When the rewuest is tuning and launching, the launched Hybridcast application is shown.


#### Tabs on the Window

You can change the content by seleting tabs.
- Log (ログ表示)

  Messages related to reception, processing, and response by "antwapp4hc" are shown. Debug messages are also shown here.

- Configuration (設定情報)

  - aitload
    - Select target infomation to be acquired by given URL, tune only or launch of Hybridcast application. When setting to true, "Antwapp4hc" retrieves the AIT. When setting to false, "Antwapp4hc" retrieves the Hybridcast application directly without reading the AIT. Note that behaviour of "Antwapp4hc" for false is not compliant with "Hybridcast-Connect" specification. It is provided only for easiness of application development.
  - wsBroadcastMode
    - Select enable(true) or disable(false) to echo the received messages via Websocet by "Antwapp4hc". The echo message is delivered to all the conected devices with "Antwapp4hc" except the orignal sender.
  - aitVerify
    - Select service integrity check server to be used.
     - External: Use the service integrity check server designated by the URL in the right field.
     - Internal: Use the built-in service integrity check server in "Antwapp4hc".
     - AllOK: Disable service integrity check; tuning and launching Hybridcast application are always allowed.
  - hcViewMode
    - There are 2 types of presentation of Hybridcast application, with or without debug messages. Selection of presentation type is controled by a button on a remote. hcViewMode is to select the subject of continuous update among the 2 types.
     - Debug: Only Hybridcast application with debug messages is updated.
     - Full: Only full screen presented Hybridcast application is updated.
     - Both: Presentation of both types is updated.
  - tuneDelay(milliseconds)
    - Intentional delay time to start tuning process after reception of tuning request.
- App screen (アプリ画面へ遷移)
  - TBD
- Clear the log (ログクリア)
  - Clear the messages in log area.