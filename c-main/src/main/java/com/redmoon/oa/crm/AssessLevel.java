package com.redmoon.oa.crm;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class AssessLevel {

    public AssessLevel() {
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public double getPercent() {
        return percent;
    }

    public String getDesc() {
        return desc;
    }

    private String code;
    private String name;
    private int score;
    private double percent;
    private String desc;
}
