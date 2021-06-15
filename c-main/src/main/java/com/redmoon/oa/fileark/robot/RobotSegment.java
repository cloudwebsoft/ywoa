package com.redmoon.oa.fileark.robot;

public class RobotSegment {
    public static int TYPE_TEXT = 0;
    public static int TYPE_LINK = 1;
    private int type;
    private String text;
    private String link;

    public RobotSegment(int type) {
        this.type = type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getType() {
        return this.type;
    }

    public String getText() {
        return this.text;
    }

    public String getLink() {
        return this.link;
    }
}