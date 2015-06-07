package com.floo.pedometer;

/**
 * Created by SONY_VAIO on 6/7/2015.
 */
public class UserBadge {
    String userName;
    int platinumBadge;
    int goldBadge;

    public UserBadge(String userName, int platinumBadge, int goldBadge) {
        this.userName = userName;
        this.platinumBadge = platinumBadge;
        this.goldBadge = goldBadge;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
