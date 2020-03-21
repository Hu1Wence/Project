package websocket;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/websocket")
public class WebSocketTest {


    private static volatile int onlineCount = 0;

    //客户端集合
    private static CopyOnWriteArraySet<WebSocketTest> client = new CopyOnWriteArraySet<WebSocketTest>();

    //channel 和 message
    private static ConcurrentHashMap<String, Queue<String>> messageQueue = new ConcurrentHashMap<String, Queue<String>>();

    //订阅者集合
    private static ConcurrentHashMap<String, ArrayList<WebSocketTest>> subscribes = new ConcurrentHashMap<String, ArrayList<WebSocketTest>>();

    //上线的学生端集合
    private static ArrayList<WebSocketTest> OnLineStu = new ArrayList<WebSocketTest>();


    ConsumeMessage consumeMessage =  new ConsumeMessage(messageQueue, subscribes);
    Thread thread1 = new Thread(consumeMessage);

    public Session session;

    @OnOpen
    public void onOpen(Session session) {
        thread1.start();
        this.session = session;
        System.out.println(this);
        client.add(this);
        addOnlineCount();
        System.out.println("有新的连接加入!当前在线人数为" + getOnlineCount());
    }

    @OnClose
    public void onClose() {
        OnLineStu.remove(this);
        client.remove(this);
        subOnlineCount();
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    @OnMessage
    public synchronized void onMessage(String message, Session session) {
        System.out.println(message);
        //如果消息为studentState，代表是学生端上线
        if (message.equals("studentState")) {
            OnLineStu.add(this);
        } else if (message.equals("getStudentState")) {
            //如果消息为getStudentState，代表教师端获取上线学生信息
            try {
                if (OnLineStu == null || OnLineStu.size() == 0) {
                    this.session.getBasicRemote().sendText("stuState-离线");
                } else {
                    this.session.getBasicRemote().sendText("stuState-在线");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (message.split("-")[1].equals("subscribe")) {
            //message的 type 是"channel:消息"或者"channel:subscribe";
            //如果消息带subscribe代表为接收端
            // 判断是否客户端为接收端
            // 是否此频道为第一次订阅
            if (subscribes.containsKey(message.split("-")[0])) {
                subscribes.get(message.split("-")[0]).add(this);
            } else {
                ArrayList<WebSocketTest> list = new ArrayList<WebSocketTest>();
                list.add(this);
                subscribes.put(message.split("-")[0], list);
            }
        } else {
            //如果不是接收端则代表是发送端
            if (messageQueue.containsKey(message.split("-")[0])) {
                messageQueue.get(message.split("-")[0]).offer(message.split("-")[1]);
            } else {
                Queue<String> queue = new LinkedList<String>();
                queue.offer(message.split("-")[1]);
                messageQueue.put(message.split("-")[0], queue);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        consumeMessage.isRun = false;
        OnLineStu.remove(this);
        System.out.println("发生错误");
        error.printStackTrace();
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketTest.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketTest.onlineCount--;

    }
}