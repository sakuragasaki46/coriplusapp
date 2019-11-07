package net.sakuragasaki46.coriplus;

import org.json.JSONObject;

public class ProfileItem {
    public final int id;
    public final String username;
    public final String full_name;
    public final int followers_count;

    public ProfileItem(int id, String username, String full_name, int followers_count) {
        this.id = id;
        this.username = username;
        this.full_name = full_name;
        this.followers_count = followers_count;
    }

    @Override
    public String toString() {
        return full_name + " (+" + username + ")";
    }
}