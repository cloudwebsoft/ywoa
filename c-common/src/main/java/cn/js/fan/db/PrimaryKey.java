package cn.js.fan.db;

import java.io.*;
import java.util.*;
import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.util.LogUtil;

public class PrimaryKey implements Serializable, Cloneable {
    public static final int TYPE_STRING = 0;
    public static final int TYPE_INT = 1;
    public static final int TYPE_COMPOUND = 2; // 复合类型
    public static final int TYPE_LONG = 3;

    public static final int TYPE_DATE = 4; // 日期型

    HashMap keys; // 当为复合类型时的key

    public PrimaryKey() {

    }

    public PrimaryKey(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public PrimaryKey(HashMap keys) {
        this.keys = keys;
        this.type = TYPE_COMPOUND;
    }

    public PrimaryKey(String name, Object value) {
        this.name = name;

        if (value.getClass().isInstance(0)) {
            this.type = TYPE_INT;
            this.intValue = (Integer) value;
        }
        else if (value.getClass().isInstance(0L)) {
            this.type = TYPE_LONG;
            this.longValue = (Long) value;
        }
        else if (value.getClass().isInstance(new Date())) {
            this.type = TYPE_DATE;
            this.dateValue = (Date)value;
        }
        else if (value.getClass().isInstance(new String(""))) {
            this.type = TYPE_STRING;
            this.strValue = (String)value;
        }
    }

    @Override
    public Object clone() {
        PrimaryKey o = null;
        try {
            o = (PrimaryKey)super.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        if (keys!=null) {
            o.keys = new HashMap();
            Iterator ir = keys.keySet().iterator();
            while (ir.hasNext()) {
                String key = (String)ir.next();
                KeyUnit ku = (KeyUnit)((KeyUnit)keys.get(key)).clone();
                o.keys.put(key, ku);
            }
        }
        return o;
    }

    public void setValue(Object value) {
        if (value instanceof Integer) {
            this.type = TYPE_INT;
            this.intValue = (Integer) value;
        }
        else  if (value instanceof String) {
            this.type = TYPE_STRING;
            this.strValue = (String)value;
        }
        else if (value instanceof Long) {
            this.type = TYPE_LONG;
            this.longValue = (Long) value;
        }
        else if (value instanceof HashMap) {
            this.type = TYPE_COMPOUND;
            this.keys = (HashMap)value;
        }
        else if (value instanceof Date) {
            this.type = TYPE_DATE;
            this.dateValue = (Date)value;
        }
        else {
            if (value == null) {
                throw new IllegalArgumentException("value is null.");
            }
            else {
                throw new IllegalArgumentException("PrimaryKey setValue: Object value " + value +
                        " are not valid type. value.getClass()=" + value.getClass() + " Object=" + value);
            }
        }
    }

    public void setType(int keyType) {
        this.type = keyType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public int getType() {
        return type;
    }

    // 取得主键的数量
    public int getKeyCount() {
        // if (type==this.TYPE_INT || type==this.TYPE_STRING || type==this.TYPE_LONG || type==this.TYPE_DATE)
        //    return 1;
        if (type== TYPE_COMPOUND) {
            if (keys!=null) {
                return keys.size();
            }
            else {
                return 0;
            }
        }
        else {
            return 1;
        }
    }

    public HashMap getKeys() {
        return keys;
    }

    public String getName() {
        if (type== TYPE_INT || type== TYPE_STRING || type== TYPE_LONG || type== TYPE_DATE) {
            return name;
        } else {
            if (keys!=null) {
                Iterator ir = keys.keySet().iterator();
                String str = "";
                while (ir.hasNext()) {
                    String objname = (String)ir.next();
                    if ("".equals(str)) {
                        str += objname;
                    } else {
                        str += "," + objname;
                    }
                }
                return str;
            }
            else {
                return null;
            }
        }
    }

    public String getStrValue() {
        return strValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public Object getValue() {
        if (type==TYPE_STRING) {
            return strValue;
        } else if (type==TYPE_INT) {
            return intValue;
        } else if (type==TYPE_LONG) {
            return longValue;
        }else if (type==TYPE_DATE) {
            return dateValue;
        } else if (type== TYPE_COMPOUND) {
            if (keys!=null) {
                Iterator ir = keys.keySet().iterator();
                String str = "";
                while (ir.hasNext()) {
                    String keyname = (String)ir.next();
                    KeyUnit ku = (KeyUnit)keys.get(keyname);
                    if (str.equals("")) {
                        str += ku.getStrValue();
                    } else {
                        str += "|" + ku.getStrValue();
                    }
                }
                return str;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public Object getKeyValue(String keyName) {
        if (keys!=null) {
            KeyUnit ku = (KeyUnit)keys.get(keyName);
            if (ku==null) {
                throw new IllegalArgumentException("keyName=" + keyName + " is not exist.");
            }
            return ku.getValue();
        }
        else {
            return null;
        }
    }

    public void setKeyValue(String keyName, Object value) {
        KeyUnit ku = (KeyUnit)keys.get(keyName);
        if (ku!=null) {
            ku.setValue(value);
        }
        else {
            keys.put(keyName, new KeyUnit(value));
        }
    }

    public long getKeyLongValue(String keyName) {
        Object obj = getKeyValue(keyName);
        if (obj==null) {
            return -65536;
        }
        else {
            return (Long) obj;
        }
    }

    public int getKeyIntValue(String keyName) {
        Object obj = getKeyValue(keyName);
        if (obj==null) {
            return -65536;
        }
        else {
            return (Integer) obj;
        }
    }

    public String getKeyStrValue(String keyName) {
        Object obj = getKeyValue(keyName);
        if (obj==null) {
            return null;
        }
        else {
            return (String)obj;
        }
    }

    public Date getKeyDateValue(String keyName) {
        Object obj = getKeyValue(keyName);
        if (obj==null) {
            return null;
        }
        else {
            return (Date)obj;
        }
    }

    public Object[] toObjectArray() {
        if (type == TYPE_STRING) {
            return new Object[] {strValue};
        } else if (type == TYPE_INT) {
            return new Object[] {intValue};
        }
        // 本句只支持JDK1.5以上
        // return new Object[] {Integer.valueOf(intValue)};
        else if (type == TYPE_LONG) {
            return new Object[] {longValue};
        }
        // 本句只支持JDK1.5以上
        // return new Object[] {Long.valueOf(longValue))};
        else if (type==TYPE_DATE) {
            return new Object[] {dateValue};
        }
        else if (type == TYPE_COMPOUND) {
            if (keys != null) {
                Object[] objs = new Object[keys.size()];
                Iterator ir = keys.keySet().iterator();
                int k = 0;
                while (ir.hasNext()) {
                    String keyname = (String) ir.next();
                    KeyUnit ku = (KeyUnit) keys.get(keyname);
                    objs[ku.getOrders()] = ku.getValue();
                    k++;
                }
                return objs;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        if (type==TYPE_STRING) {
            return strValue;
        } else if (type==TYPE_INT) {
            return String.valueOf(intValue);
        } else if (type==TYPE_LONG) {
            return String.valueOf(longValue);
        }
        else if (type==TYPE_DATE) {
            return DateUtil.format(dateValue, "yyyy-MM-dd HH:mm:ss");
        }
        else if (type==TYPE_COMPOUND) {
            if (keys!=null) {
                Iterator ir = keys.keySet().iterator();
                String str = "";
                while (ir.hasNext()) {
                    String keyname = (String)ir.next();
                    KeyUnit ku = (KeyUnit)keys.get(keyname);

                    if (str.equals("")) {
                        str += ku.getStrValue();
                    } else {
                        str += "|" + ku.getStrValue();
                    }
                }
                return str;
            }
            else {
                return "";
            }
        }
        else {
            return "";
        }
    }

    private int type;
    private int intValue;
    private long longValue;
    private String strValue;
    private String name;
    private Date dateValue;
}
