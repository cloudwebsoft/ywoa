package com.redmoon.forum;

import java.io.Serializable;
import java.util.Vector;
import com.redmoon.forum.plugin.base.ITreasure;
import org.apache.log4j.Logger;

/**
 *
 * <p>Title:每个灌水宝贝的信息 </p>
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
public class TreasureUnit implements Serializable {
    transient Logger logger = Logger.getLogger(this.getClass().getName());

    public TreasureUnit(String code) {
        this.code = code;
    }

    public void renew() {
        if (logger==null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setPrice(Vector price) {
        this.price = price;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setDay(int day) {
        this.day = day;
    }


    public String getCode() {
        return code;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Vector getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public int getCount() {
        return count;
    }

    public int getDay() {
        return day;
    }

    public ITreasure getTreasure() {
        ITreasure ipu = null;
        try {
            ipu = (ITreasure) Class.forName(className).newInstance();
        } catch (Exception e) {
            logger.error("getTreasure:" + e.getMessage());
        }
        return ipu;
    }

    private String code;
    private String className;
    private String name;
    private String desc;
    private Vector price;
    private String image;
    private int count;
    private int day;

}
