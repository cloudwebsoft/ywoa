package com.redmoon.forum;

import java.io.Serializable;

/**
 * <p>Title:灌水宝贝的价格 </p>
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
public class TreasurePrice implements Serializable {
    public String scoreCode;
    public double value;

    public String getScoreCode() {
        return scoreCode;
    }

    public double getValue() {
        return value;
    }
}
