package websocket;


import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumeMessage implements Runnable {

    private static volatile ConcurrentHashMap<String, Queue<String>> messageQueue = new ConcurrentHashMap<String, Queue<String>>();

    public boolean isRun = true;

    //订阅者集合
    private static volatile ConcurrentHashMap<String, ArrayList<WebSocketTest>> subscribes = new ConcurrentHashMap<String, ArrayList<WebSocketTest>>();

    public ConsumeMessage(ConcurrentHashMap<String, Queue<String>>  messageQueue, ConcurrentHashMap<String, ArrayList<WebSocketTest>> subscribes) {
        this.messageQueue = messageQueue;
        this.subscribes = subscribes;
    }

    public void run() {

        while (isRun) {
            for (Map.Entry<String, Queue<String>> entry : messageQueue.entrySet()) {
                //判断messageQueue 里channel是否为null
                if (entry.getKey() != null) {
                    while (entry.getValue() != null && entry.getValue().size() != 0) {
                        //判断接收端是否存在对应channel
                        if (subscribes.containsKey(entry.getKey())) {
                            if (subscribes.get(entry.getKey()) != null && subscribes.get(entry.getKey()).size() != 0) {
                                for (int i = 0; i < subscribes.get(entry.getKey()).size(); i++) {
                                    try {
                                        subscribes.get(entry.getKey()).get(i).session.getBasicRemote().sendText(entry.getKey()+"-"+entry.getValue().peek());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        System.out.println(entry.getKey()+"-"+entry.getValue().peek());
                                    }
                                }
                                entry.getValue().poll();
                            }
                        }
                    }
                }

            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
