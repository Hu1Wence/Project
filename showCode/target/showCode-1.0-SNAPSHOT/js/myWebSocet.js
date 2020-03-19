var websock =new WebSocket("ws://120.27.192.235:8080/showCode/websocket");

// 初始化weosocket
function initWebSocket () {
    // ws地址 -->这里是你的请求路径


    this.websock.onclose = function () {
        alert("连接错误")
    }
    this.websock.onopen = function () {
        console.log("连接成功")
        publish("answer-subscribe");
        publish("studentChannel" + Vue.prototype.$channelName + "-subscribe");
        publish("studentResp" + Vue.prototype.$channelName + "-subscribe");
        publish("teacherChannel" + Vue.prototype.$channelName + "-subscribe");

        publish("studentState");

    }

    // 连接发生错误的回调方法
    this.websock.onerror = function () {
        alert("WebSocket连接发生错误")
    }
    window.onbeforeunload = function () {
        closeWebSocket();
    };





}
initWebSocket ();

// // 数据接收
// function subscribe (e) {
//     globalCallback= e.data;
//     console.log(globalCallback);
// }


function publish(e) {
    console.log(e);
    this.websock.send(e);
}

function closeWebSocket() {
    this.websock.close();
}
