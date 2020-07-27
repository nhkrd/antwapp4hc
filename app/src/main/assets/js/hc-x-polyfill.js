/* eslint-disable */
"use strict"

/////////////////////////
// HybridcastオブジェクトとAPIの定義
/////////////////////////
let _setURLForCompanionDeviceObj = function (url, jsonobj) {
  /* // 実際のwebsocketで流れるTextMessage
  const m= {"control": {
              "devid":"hostdevice",
              "setURLForCompanionDevice": {
                  "url": url,
                  "options" : {
                      "auto_start": jsobj.auto_start,
                      "app_title": jsonobj.app_title,
                      "app_desc": jsonobj.app_desc
          } } } } 
      this.wsobj.send(JSON.stringify(m))
  */
  const mo = {
    "url": url,
    "options": {
      "auto_start": jsobj.auto_start,
      "app_title": jsonobj.app_title,
      "app_desc": jsonobj.app_desc
    }
  }
  return mo
}

// エミュレーターの提供するAPIへのマッピング
let _mapApiUrl = function (baseurl, path) {
  // 本polyfillがAntwappなどのエミュレーター(localhost)で動作している場合は、エミュレーターのAPIを使う
  if (baseurl.startsWith("127.0.0.1") || baseurl.startsWith("localhost")) {
    return function (baseurl, path) {
      return "http://" + baseurl + path
    }
  } else { // エミュレーター以外はそのまま返す
    return baseurl
  }
}

// App2AppURLを入力する
const receiverurl = "192.168.150.2:8887" // 自由に設定してもいい
const wsurl = "ws://192.168.150.2:8887/websocket"
// window.navigator.receiverDevice.baseurl = receiverurl
// window.navigator.receiverDevice.connect(wsurl,"Hybridcast")

// 本物のAPIがあれば本物を使う。なければpolyfillで吸収する。
// 返すデータなどがない場合は、Dummyを返しStubとして機能させる。
export function getReceiverDevice() {
  return {
        baseurl: "127.0.0.1:8887",
        wsurl: null,
        isConnected: false,
        wsobj: null,
        connect(wsurl, protocol) {
          if (this.wsobj == null) {
            console.log("connect start")
            this.wsurl = wsurl
            this.wsobj = new WebSocket(this.wsurl, protocol);
            this.wsobj.onopen = function (ev) {
              this.isConnected = true;
              console.log("websocket opened")
            }
            this.wsobj.onclose = function (ev) {
              this.isConnected = false;
              console.log("websocket closed")
            }
            this.wsobj.onerror = function (ev) {
              this.isConnected = false;
              console.log("websocket error occured")
            }
          }
        },

        addCompanionDeviceTextMessageListener(cb) {
          if (this.wsobj) {
            this.wsobj.onmessage = function (ev) {
              console.log("onmessage")
              console.log(ev)
  
              if (ev && ev.data) {
                cb(ev.data, "1")
                // var jsm = JSON.parse(event.data)
                // if(jsm.sendTextToHostDevice){
                //    console.log("sendtext onmessage;" + jsm.sendTextToCompanionDevice.text)
                //   cb(jsm.sendTextToHostDevice.text, jsm.devid)
              }
            }
          } else {
            return
          }
        },
        getSystemInformation() {
          const res = {
            makerid: 'makerid',
            browsername: 'browsername',
            browserversion: 'browserversion',
            modelname: 'modelname'
          }
          return res
        },
        getCurrentEventInformation() {
          const res = {
            on_id: 'on_id',
            ts_id: 'ts_id',
            service_id: 'service_id',
            event_id: 'event_id',
            name: 'name',
            desc: 'desc',
            start_time: 'start_time',
            duration: 'duration',
            f_event_id: 'f_event_id',
            f_name: 'f_name',
            f_desc: 'f_desc',
            f_start_time: 'f_start_time',
            f_duration: 'f_duration'
          }
          return res
        },
        streamEvent: {
          addEventIDUpdateListener() {
            console.log('call addEventIDUpdateListener from Polyfill')
          },
          addGeneralEventMessageListener() {
            console.log('call addGeneralEventMessageListener from Polyfill')
          }
        },
        getCompanionDeviceList(devlist) {
          var str = "callback of getCompanionDeviceList(): devlist.length=" + devlist.length
          for (let i = 0; i < devlist.length; i++) {
            str += " " + i + ":" + devlist[i]
          }
          return str
        },
        setURLForCompanionDevice(url, jsonobj) {
          const res = {
            "url": url,
            "options": {
              "auto_start": jsonobj.auto_start,
              "app_title": jsonobj.app_title,
              "app_desc": jsonobj.app_desc
            }
          }
          return res
        }

  }
}

export function getBmlCompat() {
  return {
    browserPseudo: {
      Greg: [
        'gregValue0',
        'gregValue1',
        'gregValue2',
        'gregValue3'
      ],
      readPersistentArray(filename) {
        return ['return Array from polyfill']
      },
      writePersistentArray(filename, structure, data) {
        const num = 12345
        return num
      }
    }
  }
}
