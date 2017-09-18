package com.df.dto;

import java.io.Serializable;

public class FriendMessage extends UserMessage implements Serializable {
    String remark, message;
    int oline;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remarks) {
        this.remark = remarks;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getOline() {
        return oline;
    }

    public void setOline(int oline) {
        this.oline = oline;
    }

}
