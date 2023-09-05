package cn.js.fan.db;

import java.io.Serializable;
import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.util.LogUtil;

public class KeyUnit implements Serializable,Cloneable {
    /**
     * 只能用于单一主键的情况，复合主键则必须用KeyUnit(type, orders)，一定要有序号
     * @param type int
     */
    public KeyUnit(int type) {
        this.type = type;
    }

    public KeyUnit(Object value) {
        if (value instanceof Integer)
            this.type = PrimaryKey.TYPE_INT;
        else if (value instanceof String) {
            this.type = PrimaryKey.TYPE_STRING;
        }
        else if (value instanceof Long) {
            this.type = PrimaryKey.TYPE_LONG;
        }
        else if (value instanceof java.util.Date) {
            this.type = PrimaryKey.TYPE_DATE;
        }
        this.value = value;
    }

    /**
     * 用于复合主键,在QDBConfig的getQDBTable用到，以便在primaryKey.toObjectArray()中可以按照次序来排列key，在initDB中注意一定
     * 要设置orders(顺序)，否则会在list的时候，比如：sql=select pk1, pk2 from table where cond=*的时候，就可能会因为次序都为0，而使得出错
     * 在QObjectDb中不会出现这样的问题，因为是从XML文件中读取时就赋予了顺序
     * @param value Object
     * @param orders int
     */
    public KeyUnit(int type, int orders) {
        this.type = type;
        this.orders = orders;
    }

    /**
     * 注意value只是影子克隆，克隆后value还是引用
     * @return Object
     */
    public Object clone() {
        KeyUnit o = null;
        try {
            o = (KeyUnit)super.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return o;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getStrValue() {
        if (type==PrimaryKey.TYPE_STRING)
            return (String)value;
        else if (type==PrimaryKey.TYPE_INT)
            return "" + ((Integer)value).intValue();
        else if (type==PrimaryKey.TYPE_LONG)
            return "" + ((Long)value).longValue();
        else if (type==PrimaryKey.TYPE_DATE)
            return "" + ((java.util.Date)value).getTime();
        else
            return null;
    }

    public int getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    private int type;
    private Object value;
    private int orders;
}
