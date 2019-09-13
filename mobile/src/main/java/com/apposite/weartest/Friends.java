package com.apposite.weartest;

class Friends {

    public String uid;

    public Friends() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Friends(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
