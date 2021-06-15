package cn.js.fan.util;

import java.util.Vector;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FormRule {
    public FormRule() {
    }

    public Vector getRules() {
        return rules;
    }

    public Vector getUnionRules() {
        return unionRules;
    }

    public boolean isOnErrorExit() {
        return onErrorExit;
    }

    public String getRes() {
        return res;
    }

    public void setRules(Vector rules) {
        this.rules = rules;
    }

    public void setUnionRules(Vector unionRules) {
        this.unionRules = unionRules;
    }

    public void setOnErrorExit(boolean onErrorExit) {
        this.onErrorExit = onErrorExit;
    }

    public void setRes(String res) {
        this.res = res;
    }

    private Vector rules;
    private Vector unionRules;
    private boolean onErrorExit = false;
    private String res;
}
