var option, dataSetA,dataSetB, labels, warn=4000;
$("#warnArea").click(function() {
	$("#warn").slider("enable");//點擊後開放編輯
	setTimeout("$('#warn').slider('disable');", 5000);//5秒後鎖定編輯
});

var data = {
	
	labels : [ "", "", "", "", "", "", "", "", "", "", "", "", "","", "", "", "", "", "", "", "", "", "", "", "", "", "","", "", "" ],
	datasets : [
		{
			borderColor:["rgba(51,204,51, 1)",],		
			data : [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0 ]
		},
		
		{
			borderColor:["rgba(245,78,0, 0.5)",],		
			data : [ warn, warn, warn, warn, warn, warn, warn, warn, warn, warn, warn, warn, warn, warn,warn, warn, warn, warn, warn, warn, warn, warn, warn, warn, warn, warn, warn, warn, warn, warn ],
			//borderDash: [10, 5],			
			borderWidth : 6,
	        //pointRadius: 15,
	        //pointHoverRadius: 10,			
		},
		
	
	]
}

var showdata;
var total=0, sum=0;

function updateAlertData(){	
	data["datasets"][0]["borderColor"]=["rgba(255,0,0,1)",];
	chart.update();
}

function updateNoAlertData(){	
	data["datasets"][0]["borderColor"]=["rgba(51,204,51,1)",];
	chart.update();
}

function updateData(oldData) {	
	labels = oldData["labels"];
	pushdata=(total/sum);	
	/*if(pushdata<1500){
		pushdata=pushdata*20;
	}*/		
	dataSetA = oldData["datasets"][0]["data"];	
	dataSetB= oldData["datasets"][1]["data"];		
	labels.shift();
	labels.push("");	
	//$("#realer").html(pushdata+", "+warn);	
	dataSetA.push(pushdata);
	dataSetB.push(warn);	
	dataSetA.shift();
	dataSetB.shift();	
	total=0;
	sum=0;
};

option = {
	animation:{
		duration: 0
	},
	elements:{
		line:{tension: 0},
		point:{radius:0}
	},
	scales: {
        yAxes: [{
            ticks: {
                beginAtZero:true,
                display: true,
                //stepSize: 1000,
                suggestedMin: 0,
                suggestedMax: 10000,
            }
        }],

        xAxes: [{
            display: false,
        }],
        
    },

    legend: { 
        display: false 
    },
    responsive: true,
}

var ctx = document.getElementById("myChart").getContext("2d");
var chart = new Chart(ctx, {
    type: 'line',
    data: data,
    options: option,	    
});

function doSec(){
	warn=$("#warn").val();
	read();
	
	if(showdata>warn){
		updateAlertData();
		$("#counter").css({ 'color': 'rgba(255,0,0,1)'});
	}else{
		updateNoAlertData();
		$("#counter").css({ 'color': 'rgba(255,255,255,1)'});
	}
	
	total+= showdata;
	sum++;
}

function doMin() {
	updateData(data);
	chart.update();
}

chart.update();
setInterval(doSec, 1000);//每秒執行doSec
setInterval(doMin, 3000);//每分執行doMin

var date, sec_real;
var endtime = '2017/10/18 10:30:00'; //設定結束時間格式 

function getDate() {	
	showdata=pow(sec_real);
	//$("#realer").html(showdata+"mA/<1k");		
	$("#counter").html(showdata);	
	date = new Date();	
	if(date.getTime() > Date.parse(endtime).valueOf()){
		alert("您使用的是未經授權的軟體, 請洽供應商更新授權, 謝謝!");
	}
	$("#dater").html(formatDate(date, "yyyy/MM/dd"));
	$("#timer").html(formatDate(date, "HH:mm:ss"));
}

var finish;
var used,usedp;
function read(){
	$.ajax({
		url : "data.json",
		type : 'GET',
		dataType : 'json',
		async : false,
		success : function(newData) {
			sec_real = newData.n;
			used=((newData.u/1024)/1024).toFixed(2);
			usedp=((used/1024)*100).toFixed(2);
			$("#values").html(newData.n);
			$("#f1").html(used+"MB/1024MB");
			$("#f2").html(usedp+"%");
			$("#f3").css( "width", usedp+"%" );
			$("#alertLine").html(warn);
			if(newData.s=="true"){				
				$("#status").html("<a href='all_data.json' download>儲存裝置已自動儲存</a>");
				if(!finish)
				startDownload();
			}
			if(newData.s=="false"){
				finish=false;
				//$("#realer").html(newData.s);	
				//$("#status").html("儲存裝置尚未連結");
				//$("#status").html("");
			}
			/*if(d.stat=="max"){
				finish=false;
				$("#status").html("<font color='red'>1000小時未儲存</font>");
			}*/
		},
		//error : function() {}
	});	
	getDate();
	//return sec_real;
}

function startDownload(){
	finish=true;
	//document.getElementById('save').click();
}

function pow(n) {
	//$("#realer").html(n+"mA/<1k");	
	if(n<1200){
		
		if (n <= 250) {			
			return Math.round(Math.pow(10, 1 + (n * 0.004)));
		}
		if (n > 250 && n <= 500) {
			return Math.round(Math.pow(10, 2 + ((n - 250) * 0.004)));
		}
		if (n > 500 && n <= 750) {
			return Math.round(Math.pow(10, 3 + ((n - 500) * 0.004)));
		}
		if (n > 750 && n <= 1000) {
			return Math.round(Math.pow(10, 4 + ((n - 750) * 0.004)));
		}
		/*if (n > 800 && n <= 1000) {
			return Math.round(Math.pow(10, 4 + ((n - 800) * 0.005)));			
		}	*/
		//return "ERROR";
	}else{
		if (n >= 4000 && n <= 8000) {
			//n = Math.pow(10, 1 + ((n - 4000) * 0.00025));
			return Math.round(Math.pow(10, 1 + ((n - 4000) * 0.00025)));
		}
		if (n > 8000 && n <= 12000) {
			//n = Math.pow(10, 2 + ((n - 8000) * 0.00025));
			return Math.round(Math.pow(10, 2 + ((n - 8000) * 0.00025)));
		}
		if (n > 12000 && n <= 16000) {
			//n = Math.pow(10, 3 + ((n - 12000) * 0.00025));
			return Math.round(Math.pow(10, 3 + ((n - 12000) * 0.00025)));
		}
		if (n > 16000 && n <= 21000) {
			//n = Math.pow(10, 4 + ((n - 16000) * 0.00025));
			return Math.round(Math.pow(10, 4 + ((n - 16000) * 0.00025)));
		}
	}	
	//$("#realer").html((n/1000).toFixed(1)+"mA");//四捨五入至1
	//$(".dial").knob();
	//alert(n);
	return 0;
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

