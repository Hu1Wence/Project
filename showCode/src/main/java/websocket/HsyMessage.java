package websocket;

import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class HsyMessage implements Runnable {

    //历史消息集合
    private static ConcurrentHashMap<String, Queue<String>> historyMsg = new ConcurrentHashMap<String, Queue<String>>();

    public HsyMessage(ConcurrentHashMap<String, Queue<String>> historyMsg) {
        this.historyMsg = historyMsg;
    }

    public void run() {
        while (true) {
            for (Map.Entry<String, Queue<String>> entry : historyMsg.entrySet()) {
                if (new Date().getTime() - 86400000 >= Long.parseLong(entry.getValue().peek().split("-")[0])) {
                    historyMsg.remove(entry.getKey());
                }
            }
        }
    }
}
