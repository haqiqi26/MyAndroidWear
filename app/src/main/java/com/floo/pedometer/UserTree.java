package com.floo.pedometer;

/**
 * Created by SONY_VAIO on 6/21/2015.
 */
public class UserTree {
    String userID;
    int treeCompleted;
    int lastTreeProgress;

    public UserTree(String userID,int treeCompleted,int lastTreeProgress)
    {
        this.userID = userID;
        this.treeCompleted = treeCompleted;
        this.lastTreeProgress = lastTreeProgress;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setTreeCompleted(int treeCompleted) {
        this.treeCompleted = treeCompleted;
    }

    public void setLastTreeProgress(int lastTreeProgress) {
        this.lastTreeProgress = lastTreeProgress;
    }

    public String getUserID() {
        return userID;
    }

    public int getTreesCompleted() {
        return treeCompleted;
    }

    public int getLastTreeProgress() {
        return lastTreeProgress;
    }
}
