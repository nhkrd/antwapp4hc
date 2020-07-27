"use strict"

/////////////////////////
// 手動で設定する変数
/////////////////////////

//※注意 デフォルトBaseURLは手動設定可能あります。
const BaseURL = "http://192.168.250.27:8887/apps"
const DDXMLURL = "http://192.168.250.27:8887/dd.xml"
const APPLOCATIONURL = "http://192.168.250.27:8887/apps"

/////////////////////////
// 関数の定義
/////////////////////////

window.appLauncher = window.appLauncher || { // std0013 7.1.7
    baseurl : BaseURL,
    ddxml_url: DDXMLURL,
    app_location_url: APPLOCATIONURL,
    getBaseURL : function(){},
    getAvailableMediaFromHostDevice : null, // std0013 7.1.7.1.1
    getChannelInfoFromHostDevice : null, // std0013 7.1.7.1.2
    startAITControlledAppToHostDevice : null, // std0013 7.1.7.2.1
    getTaskStatusFromHostDevice : null, // std0013 7.1.7.3.1
    addTaskStatusListener: function(callback) {callback()},
    removeTaskStatusListener: function(callback) {callback()},
    getReceiverStatusFromHostDevice : null,  // std0013 7.1.7.3.2 
    addReceiverStatusListener : function(callback){callback()},
    removeReceiverStatusListener : function(callback){callback()},
};

//動作未確認　テスト用に作成した関数 受信機のBASEURLの取得
appLauncher.getBaseURL = function(path) {

    //const companionDeviceExt = window.companionDeviceExt || {};

    const companionDeviceExt = window.companionDeviceExt || {
        getConnectingDeviceInfo: function (){return {"check": "ok", "maker": "", "ipaddress": "192.168.250.2:8887"}}
    }

    const devInfo = companionDeviceExt.getConnectingDeviceInfo();
    
    let targetURL = "http://" + devInfo.ipaddress + path; //XMLを取得するURI
    
    console.log(targetURL);
 
    fetch(targetURL , {
        mode:"no-cors",
        method: "GET",
    }).then((response) => {

    const responseBodyPromise = response.text();

    return responseBodyPromise.then(body => ({ body: body, responseOk: response.ok }))

    })
    .then(({ body, responseOk }) => {

        console.log("body:",body)
        // ここで正常なリクエスト完了だと判定
        if (responseOk) {
        return body
        }
        // サーバとのやりとりが出来ている40x系、50x系はここ
        console.log("SERVER-ERR :", body)
        throw new Error(body || "リクエストに失敗しました")
    })

}

//メディア利用可否取得
appLauncher.getAvailableMediaFromHostDevice =
        appLauncher.getAvailableMediaFromHostDevice || function(cache){
        console.log("execute:appLauncher.getAvailableMediaFromHostDevice")
        let retPromise = null;
        let url = this.baseurl + "/media";
        console.log("URL:",url)
        retPromise = fetch(url, {
                        method: "GET",
                    })
        return retPromise;        

}


// 選局情報一覧の取得  media = "ALL","TD","BS" or "CS"
appLauncher.getChannelInfoFromHostDevice = 
    window.appLauncher.getChannelInfoFromHostDevice || function(media, cache){
    console.log("execute:appLauncher.getChannelInfoFromHostDevice")
    const querymedia = media ? media : "ALL"
    let url = this.baseurl + "/channels?media=" + querymedia;
    let retPromise = null;
    console.log("URL:",url)
    retPromise = fetch(url, {
        method: "GET",
    })
    return retPromise;

}

// アプリ起動リクエスト状態の取得
appLauncher.getTaskStatusFromHostDevice =
        window.appLauncher.getTaskStatusFromHostDevice || function(){
            let retPromise = null;
            let url = this.baseurl + "/hybridcast";
            console.log("URL:",url)
            retPromise = fetch(url, {
            method: "GET",
            })
            return retPromise;
        }

appLauncher.startAITControlledAppToHostDevice =
            window.appLauncher.startAITControlledAppToHostDevice || function(mode, reqJson){
            console.log("execute:appLauncher.startAITControlledAppToHostDevice")
            /*
            resJson := {
                resource: {
                    "original_network_id": 0x7fe1, // should be express as Number
                    "transport_stream_id": 0x7fe1, // should be express as Number
                    "service_id": 0x0408  // should be express as Number
                },
                hybridcast: {
                    "aiturl": "http://example.com/ait/example.ait",
                    "orgid": 16,
                    "appid": 1
                }
            }
            */
            let retPromise = null
            let url = this.baseurl + "/hybridcast?mode=" + mode
            console.log("URL:",url)
            let body = {
                resource: reqJson.resource,
                hybridcast: reqJson.hybridcast 
            }
            console.log("body:",body)
            
            retPromise = fetch(url, {
                        method: "POST",
                        body: JSON.stringify(body) 
                    })
            return retPromise;
        }

appLauncher.getReceiverStatusFromHostDevice = 
    window.appLauncher.getReceiverStatusFromHostDevice || function(callback) {
            let retPromise = null;
            let url = this.baseurl + "/status";
            console.log("URL:",url)
            retPromise = fetch(url, {
            method: "GET",
            })
            return retPromise;
        }
 


appLauncher.addTaskStatusListener = 
        window.appLauncher.addTaskStatusListener || function(callback){
            // registCallback(callback)
        }

appLauncher.removeTaskStatusListener = 
        window.appLauncher.removeTaskStatusListener || function(callback) {
            // removeCallback(callback)
        }

appLauncher.addReceiverStatusListener = 
        window.appLauncher.addReceiverStatusListener || function(callback){
            // registCallback(callback)
        }

appLauncher.removeReceiverStatusListener = 
        window.appLauncher.removeReceiverStatusListener || function(callback) {
            // removeCallback(callback)
        }
