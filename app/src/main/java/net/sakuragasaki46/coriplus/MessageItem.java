package net.sakuragasaki46.coriplus;

import org.json.JSONObject;

public class MessageItem {
    public final int id;
    public final String content;
    public final int privacy;
    public final double timestamp;
    public final JSONObject userInfo;

    public MessageItem(int id, JSONObject userInfo, String content, double timestamp, int privacy) {
        this.id = id;
        this.userInfo = userInfo;
        this.content = content;
        this.privacy = privacy;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return content;
    }
}