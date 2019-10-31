package net.sakuragasaki46.coriplus.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userToken;
    private String displayName;

    public LoggedInUser(String userToken, String displayName) {
        this.userToken = userToken;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userToken.split(":")[0];
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUserToken(){
        return userToken;
    }
}
