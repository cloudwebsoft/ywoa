package cn.js.fan.db;

import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.util.LogUtil;

import java.util.*;
import java.sql.Timestamp;
import java.math.BigDecimal;

/**
 * 存储一行数据
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
public class ResultRecord implements java.io.Serializable {
    Vector row;
    HashMap mapIndex;

    public ResultRecord(Vector row, HashMap mapIndex) {
        this.row = row;
        this.mapIndex = mapIndex;
    }

    public HashMap getMapIndex() {
        return mapIndex;
    }

    public Object get(String colname) {
        colname = colname.toUpperCase();
        Integer itg = (Integer)mapIndex.get(colname);
        if (itg==null) {
            throw new IllegalArgumentException(colname + " is not exist.");
        }
        return get(itg.intValue());
    }

    public String getString(String colname) {
        colname = colname.toUpperCase();
        Integer itg = (Integer)mapIndex.get(colname);
        if (itg==null) {
            throw new IllegalArgumentException(colname + " is not exist.");
        }
        return getString(itg.intValue());
    }

    /**
     * 2007.3.14
     * 赋值，但不进行类型检查，也不检查colname是否存在，例：在初始化initQObject后，用put来设置对应colname列的值
     * @param colname String
     * @param value String
     */
    public void put(String colname, Object value) {
        colname = colname.toUpperCase();
        Integer index = (Integer) mapIndex.get(colname);
        if (index!=null) {
            set(index.intValue(), value);
        }
        else
            throw new IllegalArgumentException(colname + " is not exist.");
    }

    /**
     * 赋值，赋值时会检查colname是否存在，并进行类型检查
     * @param colname String
     * @param value Object 可以为null, null不属于任何类型，如： null instanceof String = false;
     */
    public void set(String colname, Object value) {
        // 类型检查
        Object col = get(colname);
        if (col!=null) {
            Class cls = col.getClass(); // 数据库中列的类型
            if (value!=null) {
                if (!cls.isInstance(value)) {
                    if (col instanceof Timestamp) {
                        if (!(value instanceof java.util.Date)) {
                            if (value instanceof String) {
                                value = DateUtil.parse(String.valueOf(value), "yyyy-MM-dd HH:mm:ss");
                            }
                            else {
                                throw new IllegalArgumentException("colname=" +
                                        colname + " class=" +
                                        value.getClass().getName() + " value=" +
                                        value + " is not of " + cls);
                            }
                        }
                    }
                    else if (col instanceof Integer) {
                        if (!(value instanceof Long)) {
                            throw new IllegalArgumentException("colname=" +
                                    colname + " class=" +
                                    value.getClass().getName() + " value=" +
                                         value + " is not of " + cls);
                        }
                    }
                    else if (col instanceof Long) {
                        if (!(value instanceof Integer)) {
                            throw new IllegalArgumentException("colname=" +
                                    colname + " class=" +
                                    value.getClass().getName() + " value=" +
                                         value + " is not of " + cls);
                        }
                    } else if (col instanceof java.sql.Date) {
                        if (!(value instanceof java.util.Date)) {
                            throw new IllegalArgumentException(colname + ":" +
                                    value.getClass() + ":" +
                                    value + " is not of " + cls);
                        }
                    } else if (col instanceof Double) {
                        if (value instanceof Float || value instanceof Long ||
                            value instanceof Integer) {
                            ;
                        } else {
                            throw new IllegalArgumentException(colname + ":" +
                                    value.getClass() + ":" +
                                    value + " is not of " + cls);
                        }
                    }
                    else if (col instanceof Float) {
                        if (value instanceof Double || value instanceof Long ||
                            value instanceof Integer) {
                            ;
                        } else {
                            throw new IllegalArgumentException(colname + ":" +
                                    value.getClass() + ":" +
                                    value + " is not of " + cls);
                        }
                    }
                    else if (col instanceof BigDecimal) {
                        if (value instanceof Double || value instanceof Long ||
                            value instanceof Integer || value instanceof Float) {
                            ;
                        } else {
                            throw new IllegalArgumentException(colname + ":" +
                                    value.getClass() + ":" +
                                    value + " is not of " + cls);
                        }
                    }
                    else {
                        throw new IllegalArgumentException("colname=" +
                                colname +
                                " class=" + value.getClass().getName() +
                                " value=" +
                                value + " is not of " + cls);
                    }
                }
            }
        }
        colname = colname.toUpperCase();
        set(((Integer)mapIndex.get(colname)).intValue(), value);
    }

    public int getInt(String colname) {
        colname = colname.toUpperCase();
        Integer itg = (Integer)mapIndex.get(colname);
        if (itg==null) {
            throw new IllegalArgumentException(colname + " is not exist.");
        }
        return getInt(itg.intValue());
    }

    public long getLong(String colname) {
        colname = colname.toUpperCase();
        Integer itg = (Integer)mapIndex.get(colname);
        if (itg==null) {
            throw new IllegalArgumentException(colname + " is not exist.");
        }
        return getLong(itg.intValue());
    }

    public double getDouble(String colname) {
        colname = colname.toUpperCase();
        Integer itg = (Integer)mapIndex.get(colname);
        if (itg==null) {
            throw new IllegalArgumentException(colname + " is not exist.");
        }
        return getDouble(itg.intValue());
    }

    public float getFloat(String colname) {
        colname = colname.toUpperCase();
        Integer itg = (Integer)mapIndex.get(colname);
        if (itg==null) {
            throw new IllegalArgumentException(colname + " is not exist.");
        }
        return getFloat(itg.intValue());
    }

    public Date getDate(String colname) {
        colname = colname.toUpperCase();
        Integer itg = (Integer)mapIndex.get(colname);
        if (itg==null) {
            throw new IllegalArgumentException(colname + " is not exist.");
        }
        Date d = null;
        try {
            d = getDate(itg.intValue());
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return d;
    }

    public Timestamp getTimestamp(String colname) {
        colname = colname.toUpperCase();
        Integer itg = (Integer)mapIndex.get(colname);
        if (itg==null) {
            throw new IllegalArgumentException(colname + " is not exist.");
        }
        Timestamp d = null;
        try {
            d = getTimestamp(itg.intValue());
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return d;
    }

    public boolean getBoolean(String colname) {
        colname = colname.toUpperCase();
        Integer itg = (Integer)mapIndex.get(colname);
        if (itg==null) {
            throw new IllegalArgumentException(colname + " is not exist.");
        }
        return getBoolean(itg.intValue());
    }

    /**
     *
     * @param i int i从1开始
     * @return Object
     */
    public Object get(int i) {
        return row.elementAt(i-1);
    }

    public boolean getBoolean(int i) {
        boolean r = false;
        Object obj = row.elementAt(i - 1);
        try {
            // logger.info("obj=" + obj);
            r = ((String) obj).equals("1") || ((String) obj).equals("true");
        } catch (ClassCastException e) {
            // if (e.getMessage().equals("java.lang.Integer"))
            //    return ((Integer) obj).intValue()>0?true:false;
            if (obj instanceof Integer) {
                return ((Integer)obj).intValue()==1;
            }
            else if (obj instanceof Long) {
                return ((Long)obj).longValue()==1;
            }
            else if (obj instanceof Boolean) {
                return ((Boolean) obj).booleanValue();
            }
            else if (obj instanceof BigDecimal) {
                return ((BigDecimal)obj).intValue()==1;
            }
            else {
                throw new ClassCastException("obj = " + obj + " class=" + obj.getClass());
            }
        }
        return r;
    }

    public double getDouble(int i) {
        Object obj = row.elementAt(i - 1);
        double k = 0;
        if (obj==null)
            return k;
        try {
            k = ((Double) obj).doubleValue();
        } catch (ClassCastException e) {
            if (obj instanceof Float) {
                k = ((Float)obj).doubleValue();
            }
            else if (obj instanceof Integer) {
                k = ((Integer)obj).doubleValue();
            }
            else if (obj instanceof Long) {
                k = ((Long)obj).doubleValue();
            }
            else if (obj instanceof BigDecimal) {
                k = ((BigDecimal)obj).doubleValue();
            }
            else if (obj instanceof String) {
                try {
                    return Double.parseDouble((String) obj);
                } catch (NumberFormatException e1) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
            else {
                throw new ClassCastException("obj=" + obj + " getDouble=" + obj.getClass());
            }
        }
        return k;
    }

    public float getFloat(int i) {
        Object obj = row.elementAt(i - 1);
        float k = -1;
        if (obj==null)
            return k;
        try {
            k = ((Float) obj).floatValue();
        } catch (ClassCastException e) {
            if (obj instanceof Double) {
                k = ((Double)obj).floatValue();
            }
            else if (obj instanceof Integer) {
                k = ((Integer)obj).floatValue();
            }
            else if (obj instanceof Long) {
                k = ((Long)obj).floatValue();
            }
            else if (obj instanceof BigDecimal) {
                k = ((BigDecimal)obj).floatValue();
            }
            else if (obj instanceof String) {
                try {
                    return Float.parseFloat((String) obj);
                } catch (NumberFormatException e1) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
            else {
                throw new ClassCastException("getFloat:obj=" + obj + " class=" + obj.getClass());
            }
        }
        return k;
    }

    public void set(int i, Object value) {
        row.set(i-1, value);
    }

    public String getString(int i) {
        String str = "";
        Object obj = row.elementAt(i - 1);
        try {
            str = (String)obj;
        }
        catch (ClassCastException e) {
            str = obj.toString();
        }
        return str;
    }

    public int getInt(int i) {
        Object obj = row.elementAt(i - 1);
        int k = -1;
        if (obj==null)
            return k;
        try {
            k = ((Integer) obj).intValue();
        } catch (ClassCastException e) {
            if (obj instanceof Long) {
                return ((Long) obj).intValue();
            }
            else if (obj instanceof java.math.BigDecimal) {
                return ((java.math.BigDecimal)obj).intValue();
            }
            else if (obj instanceof Double) {
                return ((Double) obj).intValue();
            }
            else if (obj instanceof String) {
                try {
                    return Integer.parseInt((String) obj);
                } catch (NumberFormatException e1) {
                    LogUtil.getLog(getClass()).error(e);
                }
            } else {
                throw new ClassCastException("obj = " + obj + " class=" + obj.getClass());
            }
        }
        return k;
    }

    public long getLong(int i) {
        Object obj = row.elementAt(i - 1);
        long k = -1;
        if (obj==null)
            return k;
        try {
            k = ((Long) obj).longValue();
        } catch (ClassCastException e) {
            if (obj instanceof Integer) {
                return ((Integer) obj).intValue();
            } else if (obj instanceof Float) {
                return ((Float) obj).longValue();
            }  else if (obj instanceof Double) {
                return ((Double) obj).longValue();
            }
            else if (obj instanceof java.math.BigDecimal) {
                return ((java.math.BigDecimal)obj).longValue();
            }
            else if (obj instanceof String) {
                try {
                    return Long.parseLong((String) obj);
                } catch (NumberFormatException e1) {
                    throw new ClassCastException("obj = " + obj + " class=" + obj.getClass());
                }
            } else {
                throw new ClassCastException("obj = " + obj + " class=" + obj.getClass());
            }
        }
        return k;
    }

    public Date getDate(int i) {
        Object obj = row.elementAt(i - 1);
        Date d = null;
        try {
            d = (Date) obj;
        } catch (ClassCastException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return d;
    }

    public Timestamp getTimestamp(int i) {
        Object obj = row.elementAt(i - 1);
        Timestamp d = null;
        try {
            d = (Timestamp) obj;
        } catch (ClassCastException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return d;
    }

    public Vector getRow() {
        return row;
    }
}
