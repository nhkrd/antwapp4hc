/**
 * Antwapp Config
 */

var configNew = null;
var tuneStep = 100;

var attrs = {
    "aitload":0,
    "wsBroadcastMode":1,
    "aitVerify":2,
    "hcViewMode":3,
    "save":4
};
var Url_index = {"Internal":0, "External":1, "AllOK":2};
var aitVerifierUrl = {};

function delayup() {
	var tuneDelay = parseInt(configNew["tuneDelay"]);
	tuneDelay = tuneDelay + tuneStep;
	if( 10000 < tuneDelay ) {
		tuneDelay = 10000;
	}
	configNew["tuneDelay"] = tuneDelay;
	$('#cc_tuneDelay').text( configNew['tuneDelay'] );
}

function delaydown() {
	var tuneDelay = parseInt(configNew["tuneDelay"]);
	tuneDelay = tuneDelay - tuneStep;
	if( tuneDelay < 0 ) {
		tuneDelay = 0;
	}
	configNew["tuneDelay"] = tuneDelay;
	$('#cc_tuneDelay').text( configNew['tuneDelay'] );
}

function dispConfig(data) {
    var configObj = $("#config");
    configObj.empty();

    configObj.append( $('<p class="attrtitle">デバイス、ソフトウェア情報</p>') ) ;
    var envAll = JSON.parse(data)["body"]["Env"];
	if( envAll['MANUFACTURER'] ) { configObj.append($('<p class="attrstr"></p>').text('MANUFACTURER: ' + envAll['MANUFACTURER'])) ; }
	if( envAll['BRAND'] ) { configObj.append($('<p class="attrstr"></p>').text('BRAND: ' + envAll['BRAND'])) ; }
	if( envAll['PRODUCT'] ) { configObj.append($('<p class="attrstr"></p>').text('PRODUCT: ' + envAll['PRODUCT'])) ; }
	if( envAll['IP Address'] ) { configObj.append($('<p class="attrstr"></p>').text('IP Address: ' + envAll['IP Address'])) ; }
	if( envAll['Emulator Version'] ) { configObj.append($('<p class="attrstr"></p>').text('Emulator Version: ' + envAll['Emulator Version'])) ; }

	configObj.append( $('<br>') ) ;
	configObj.append( $('<div class="attrtitle">設定</div>') ) ;

    var configAll = JSON.parse(data)["body"]["config"];
    configNew = {};
    if( configAll['aitload']) { configNew['aitload'] = configAll['aitload']; }
    if( configAll['wsBroadcastMode']) { configNew['wsBroadcastMode'] = configAll['wsBroadcastMode']; }
    if( configAll['aitVerifierMode']) { configNew['aitVerifierMode'] = configAll['aitVerifierMode']; }
    if( configAll['aitVerifierUrl']) { configNew['aitVerifierUrl'] = configAll['aitVerifierUrl']; }
    if( configAll['channelsFrom']) { configNew['channelsFrom'] = configAll['channelsFrom']; }
    if( configAll['tuneMode']) { configNew['tuneMode'] = configAll['tuneMode']; }
    if( configAll['hcViewMode']) { configNew['hcViewMode'] = configAll['hcViewMode']; }
    if( configAll['tuneDelay']) { configNew['tuneDelay'] = configAll['tuneDelay']; }

    var configBase = $('<div class="configBase"></div>');
    configBase.append( $('<p class="attrtitle">各項目はトグル的に変わります。[設定更新]にて反映されます</p>') ) ;
    var configButtons = '<div class="ConfigChange_Btn_Content">'
                    + '<div><button class="CondifChange_Btn focusable">aitload(true, false)</button><span id="cc_aitload"></span></div>'
                    + '<div><button class="CondifChange_Btn focusable">wsBroadcastMode(true, false)</button><span id="cc_wsbroadcastmode"></span></div>'
                    + '<div class=input-group><button class="CondifChange_Btn focusable">aitVerify(External, Internal, AllOK)</button><span id="cc_aitVerifierMode"></span><span> : </span><input id="cc_aitVerifierURL" type="text" value="" ></div>'
                    + '<div><button class="CondifChange_Btn focusable">hcViewMode(Debug, Full, Both)</button><span id="cc_hcviewmode"></span></div>'
                    + '<div><div class="CondifChange_Div">tuneDelay(<span id="cc_tuneDelay"></span> msec)</div><button class="tuneDelayButton focusable" onclick="delayup();">▲</button><button class="tuneDelayButton focusable" onclick="delaydown();">▼</button></div>'
                    + '<div><button class="CondifChange_Btn focusable">設定更新</button></div>'
                    + '</div>';
    configBase.append( configButtons ) ;
    configObj.append( $('<br>') ) ;
    configObj.append( configBase ) ;
    configUpdate();

    $('#dispconfig').focus();
}

function getConfig() {
	$.ajax( antwappconfig_url, {
		type: 'get',
		dataType: 'text'
	})
	.done( function(data) {dispConfig(data);} )
	.fail(function() {
		logdisp( "Read Config Error", "#ff0000");
	});
}

function postConfig() {
//		var config = { "aitload": true };
	$.ajax( antwappconfig_url, {
		type: 'post',
		dataType: 'text',
		data: JSON.stringify(configNew)
	})
	.done(function(data) {
		console.log("sendConfig Done");
		getConfig();
	})
	.fail(function() {
	});
}

function dispIpAddress( antwappconfig_url, tagIpAddress) {
	$.ajax( antwappconfig_url, {
		type: 'get',
		dataType: 'text'
	})
	.done(function(data) {
		var envAll = JSON.parse(data)["body"]["Env"];
		if( envAll["IP Address"] ) {
			var ipObj = $(tagIpAddress);
			ipObj.text( envAll["IP Address"] );
		}
	})
	.fail(function() {
		logdisp( "Read Config Error", "#ff0000");
	});
}

function configUpdate() {
	//Current Config
	$('#None').text( "false" );
    $('#cc_aitload').text( configNew['aitload']? "true":"false" );
    $('#cc_wsbroadcastmode').text( configNew['wsBroadcastMode']? "true":"false" );
	$('#cc_aitVerifierMode').text( configNew['aitVerifierMode'] );
	$('#cc_hcviewmode').text( configNew['hcViewMode'] );
	$('#cc_tuneDelay').text( configNew['tuneDelay'] );

    aitVerifierUrl = configNew['aitVerifierUrl'].split('|');
	$('#cc_aitVerifierURL').attr( "value", aitVerifierUrl[Url_index[configNew['aitVerifierMode']]] );

	//Operation
	$('.CondifChange_Btn').each(function () {
		$(this).on('click', function () {
			var index = $('.CondifChange_Btn').index(this);
			if( index == attrs["aitload"] ) {
				configNew['aitload'] = !configNew['aitload'];
				$('#cc_aitload').text( configNew['aitload']? "true":"false" );
			}
			else if( index == attrs["wsBroadcastMode"] ) {
            	configNew['wsBroadcastMode'] = !configNew['wsBroadcastMode'];
            	$('#cc_wsbroadcastmode').text( configNew['wsBroadcastMode']? "true":"false" );
            }
			else if( index == attrs["aitVerify"] ) {
                if( configNew['aitVerifierMode'] == "External" ) {
                    configNew['aitVerifierMode'] = "Internal";
                }
                else if( configNew['aitVerifierMode'] == "Internal" ) {
                    configNew['aitVerifierMode'] = "AllOK";
                }
                else {
                    configNew['aitVerifierMode'] = "External";
                }
                $('#cc_aitVerifierMode').text( configNew['aitVerifierMode'] );
              	$('#cc_aitVerifierURL').attr( "value", aitVerifierUrl[Url_index[configNew['aitVerifierMode']]] );
			}
			else  if( index == attrs["hcViewMode"] ) {
				if( configNew['hcViewMode'] == "Debug" ) {
					configNew['hcViewMode'] = "Full";
				}
				else if( configNew['hcViewMode'] == "Full" ) {
					configNew['hcViewMode'] = "Both";
				}
				else {
					configNew['hcViewMode'] = "Debug";
				}
				$('#cc_hcviewmode').text( configNew['hcViewMode'] );
			}
			else  if( index == attrs["save"] ) {
				postConfig();
			}
			console.log( "CondifChange_Btn:" + index );
		});
	});
}
