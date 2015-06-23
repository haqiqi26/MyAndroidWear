package com.floo.pedometer;

/**
 * Created by SONY_VAIO on 6/7/2015.
 */
public class UserBadge {
    String userID;
    int platinumBadge;
    int goldBadge;

    public UserBadge(String userID, int platinumBadge, int goldBadge) {
        this.userID= userID;
        this.platinumBadge = platinumBadge;
        this.goldBadge = goldBadge;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID= userID;
    }

    public int getPlatinumBadge() {
        return platinumBadge;
    }

    public void setPlatinumBadge(int platinumBadge) {
        this.platinumBadge = platinumBadge;
    }

    public int getGoldBadge() {
        return goldBadge;
    }

    public void setGoldBadge(int goldBadge) {
        this.goldBadge = goldBadge;
    }
}
