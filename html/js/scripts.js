var option, dataSetA, labels;
$(document).ready(function() {
	var data = {
		labels : [ "", "", "", "", "", "", "", "", "", "", "", "", "","", "", "", "", "", "", "", "", "", "", "", "", "", "","", "", "" ],
		
		datasets : [{
			fillColor : "rgba(255,255,255,0.1)",
			strokeColor : "rgba(220,220,220,1)",
			pointColor : "rgba(220,220,220,1)",
			pointStrokeColor : "#fff",
			data : [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0 ]
		},/*{
			fillColor : "rgba(255,255,255,0.1)",
			strokeColor : "rgba(220,220,220,1)",
			pointColor : "rgba(220,220,220,1)",
			pointStrokeColor : "#fff",
			data : [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0 ]
		}*/]
	}
	
	// 置入資料序列
	var updateData = function(oldData) {
		labels = oldData["labels"];
		
		dataSetA = oldData["datasets"][0]["data"];
		//dataSetB = oldData["datasets"][1]["data"];
		
		labels.shift();
		labels.push("");
		//var newDataA = dataSetA[9]+ (20 - Math.floor(Math.random() * (41)));
		
		dataSetA.push(read());
		//dataSetB.push(read()-1);
		
		
		dataSetA.shift();
		//dataSetB.shift();
	};
	
	option = {
		pointDot : false,
		animation : false,
		scaleOverride: true,
		scaleSteps: 25,
	    scaleStepWidth: 10,
	    scaleStartValue: 0,
		scaleGridLineColor : "rgba(255,255,255,.1)",
		datasetStrokeWidth : 0.5,
		
	}
	
	var ctx = document.getElementById("myChart").getContext("2d");
	var optionsNoAnimation = {
		animation : false
	}
	var myNewChart = new Chart(ctx);
	myNewChart.Line(data, option);
	
	setInterval(function() {
		updateData(data);
		myNewChart.Line(data, option);
	}, 500);

});

var d, s;
function getDate() {
	d = new Date()
	$("#counter").html(s);
	$("#dater").html(formatDate(d, "yyyy/MM/dd"));
	$("#timer").html(formatDate(d, "HH:mm:ss"));
	option.scaleStepWidth=Math.ceil(s/25);
	option.scaleStartValue=Math.ceil(s/3);
}

function formatDate(date, format) {
	if (!date)
		return;
	if (!format)
		format = "yyyy-MM-dd";
	switch (typeof date) {
	case "string":
		date = new Date(date.replace(/-/, "/"));
		break;
	case "number":
		date = new Date(date);
		break;
	}
	if (!date instanceof Date)
		return;
	var dict = {
		"yyyy" : date.getFullYear(),
		"M" : date.getMonth() + 1,
		"d" : date.getDate(),
		"H" : date.getHours(),
		"m" : date.getMinutes(),
		"s" : date.getSeconds(),
		"MM" : ("" + (date.getMonth() + 101)).substr(1),
		"dd" : ("" + (date.getDate() + 100)).substr(1),
		"HH" : ("" + (date.getHours() + 100)).substr(1),
		"mm" : ("" + (date.getMinutes() + 100)).substr(1),
		"ss" : ("" + (date.getSeconds() + 100)).substr(1)
	};
	return format.replace(/(yyyy|MM?|dd?|HH?|ss?|mm?)/g, function() {
		return dict[arguments[0]];
	});
}

var finish;
function read() {	
	$.ajax({
		url : "data.json",
		type : 'GET',
		dataType : 'json',
		async : false,
		success : function(d) {
			s = d.num;
			$("#values").html(d.num);
			if(d.stat=="true"){				
				$("#status").html("<a href='all_data.json' download>儲存裝置已自動儲存</a>");
				if(!finish)
				startDownload();
			}else{
				finish=false;
				$("#status").html("儲存裝置尚未連結");
			}
			
		},
		//error : function() {}
	});
	s = pow(s);
	getDate();
	return s;
}

function startDownload(){
	finish=true;
	//document.getElementById('save').click();
}

function pow(n) {
	Math.round(n)
	// n=Math.Round(100.1,1)
	n = n.toString().slice(0, -2);
	n += "00";
	$("#realer").html(n);
	//$(".dial").knob();
	$('.dial').val(((n-4000)/20000)*100).trigger('change');
	
	
	if (n >= 4000 && n <= 8000) {
		n = Math.pow(10, 1 + ((n - 4000) * 0.00025));
	}
	if (n > 8000 && n <= 12000) {
		n = Math.pow(10, 2 + ((n - 8000) * 0.00025));
	}
	if (n > 12000 && n <= 16000) {
		n = Math.pow(10, 3 + ((n - 12000) * 0.00025));
	}
	if (n > 16000 && n <= 21000) {
		n = Math.pow(10, 4 + ((n - 16000) * 0.00025));
	}
	
	
	return Math.round(n);
}