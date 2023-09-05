package com.redmoon.oa.basic;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.web.Global;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloudweb.oa.api.IBasicDataService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;

import java.sql.SQLException;

import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.db.SequenceManager;

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
public class SelectDb extends ObjectDb {
    /**
     * 列表型
     */
    public static int TYPE_LIST = 0;
    /**
     * 树形
     */
    public static int TYPE_TREE = 1;

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getOrders() {
        return orders;
    }

    public SelectDb() {
        init();
    }

    public SelectDb(String code) {
        this.code = code;
        init();
        load();
    }

    @Override
    public void initDB() {
        tableName = "oa_select";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_STRING);
        objectCache = new SelectCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into oa_select (code,name,orders,sel_type,kind) values (?,?,?,?,?)";
        QUERY_SAVE = "update oa_select set name=?,orders=?,sel_type=?,kind=? where code=?";
        QUERY_LIST =
                "select code,name,orders,sel_type,kind from oa_select order by kind, orders";
        QUERY_DEL = "delete from " + tableName + " where code=?";
        QUERY_LOAD = "select code,name,orders,sel_type,kind from oa_select where code=? order by orders";
    }

    @Override
    public boolean create() throws ErrMsgException {
        JdbcTemplate jt = new JdbcTemplate();
        boolean re;
        try {
            re = jt.executeUpdate(QUERY_CREATE, new Object[]{code,name, orders, type, kind})==1;
            if (re) {
                SelectCache rc = new SelectCache(this);
                rc.refreshCreate();

                if (type==SelectDb.TYPE_TREE) {
                    IBasicDataService basicDataService = SpringUtil.getBean(IBasicDataService.class);
                    basicDataService.initTreeSelect(code, name);
                }
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            throw new ErrMsgException("数据库出错，编码可能重复!");
        }
        return re;
    }

    public Vector<SelectOptionDb> getOptions() {
        return options;
    }

    public Vector<SelectOptionDb> getOptions(JdbcTemplate jt) {
        return getOptions(jt, "");
    }

    public Vector<SelectOptionDb> getOptions(JdbcTemplate jt, String key) {
        Vector<SelectOptionDb> v = new Vector<>();
        String sql = "select * from oa_select_option where code=? order by orders";
        if (!"".equals(key)) {
            sql = "select * from oa_select_option where code=? and name like " + StrUtil.sqlstr("%" + key + "%") + " order by orders";
        }
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[]{code});
            while (ri.hasNext()) {
                ResultRecord rr = ri.next();
                SelectOptionDb sod = new SelectOptionDb();
                sod.setId(rr.getInt("id"));
                sod.setCode(rr.getString("code"));
                sod.setName(rr.getString("name"));
                sod.setValue(rr.getString("value"));
                sod.setOrders(rr.getInt("orders"));
                sod.setDefault(rr.getBoolean("is_default"));
                sod.setColor(StrUtil.getNullStr(rr.getString("color")));
                sod.setOpen(rr.getInt("is_open") == 1);
                v.add(sod);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        options = v;
        return v;
    }

    public SelectDb getSelectDb(String code) {
        return (SelectDb)getObjectDb(code);
    }

    @Override
    public ListResult listResult(String sql, int curPage, int pageSize) throws ErrMsgException {
        int total = 0;
        ResultSet rs;
        Vector<SelectDb> result = new Vector<>();
        ListResult lr = new ListResult();
        lr.setResult(result);
        lr.setTotal(total);
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(sql);
            // 取得总记录条数
            PreparedStatement ps = conn.prepareStatement(countsql);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
            }

            if (ps!=null) {
                ps.close();
            }

            // 防止受到攻击时，curPage被置为很大，或者很小
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages) {
                curPage = totalpages;
            }
            if (curPage <= 0) {
                curPage = 1;
            }

            conn.prepareStatement(sql);
            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

            rs = conn.executePreQuery();
            if (rs == null) {
                return lr;
            } else {
                SelectDb sd = new SelectDb();
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (!rs.absolute(absoluteLocation)) {
                    return lr;
                }
                do {
                    result.addElement(sd.getSelectDb(rs.getString("code")));
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException("数据库出错！");
        } finally {
            conn.close();
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    public void setOptions(Vector<SelectOptionDb> options) {
        this.options = options;
    }

    private String code;
    private String name;
    private int orders = 0;

    @TableField(exist=false)
    private Vector<SelectOptionDb> options;

    private boolean loaded = false;

    /**
     * 类别，列表型或树型
     */

    private int type = TYPE_LIST;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int kind;

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }


    /**
     * 只有列表状的才有默认值
     *
     * @return String
     */
    public String getDefaultValue() {
        if (getType() == SelectDb.TYPE_LIST) {
            Vector<SelectOptionDb> v = getOptions(new JdbcTemplate());
            for (SelectOptionDb sod : v) {
                if (sod.isDefault()) {
                    return sod.getValue();
                }
            }
        }
        return "";
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public void load() {
        String sql = "select code,name,orders,sel_type,kind from oa_select where code=? order by orders";
        JdbcTemplate jt = new JdbcTemplate();
        jt.setAutoClose(false);
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[]{code});
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                setCode(rr.getString("code"));
                setName(rr.getString("name"));
                setOrders(rr.getInt("orders"));
                setType(rr.getInt("sel_type"));
                setKind(rr.getInt("kind"));
                setLoaded(true);

                getOptions(jt);

                loaded = true;
                primaryKey.setValue(rr.getString("code"));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            jt.close();
        }
    }

    @Override
    public boolean save() throws ErrMsgException, ResKeyException {
        JdbcTemplate jt = new JdbcTemplate();
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_SAVE, new Object[]{name, orders, type, kind, code})==1;
            if (re) {
                SelectCache rc = new SelectCache(this);
                primaryKey.setValue(code);
                rc.refreshSave(primaryKey);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return re;
    }

    @Override
    public boolean del() throws ErrMsgException, ResKeyException {
        boolean re = false;
        if (getType()==SelectDb.TYPE_LIST) {
            String sql = "delete from oa_select where code=?";
            String sql2 = "delete from oa_select_option where code=?";
            JdbcTemplate jt = new JdbcTemplate();
            jt.setAutoClose(false);
            try {
                re = jt.executeUpdate(sql, new Object[]{code})==1;
                if (re) {
                    SelectCache rc = new SelectCache(this);
                    primaryKey.setValue(code);
                    rc.refreshDel(primaryKey);

                    jt.executeUpdate(sql2, new Object[]{code});
                }
            }
            catch (SQLException e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            }
            finally {
                jt.close();
            }
        }
        else {
            String sql = "delete from oa_select where code=?";
            JdbcTemplate jt = new JdbcTemplate();
            try {
                re = jt.executeUpdate(sql, new Object[]{code})==1;
                if (re) {
                    SelectCache rc = new SelectCache(this);
                    primaryKey.setValue(code);
                    rc.refreshDel(primaryKey);

                    IBasicDataService basicDataService = SpringUtil.getBean(IBasicDataService.class);
                    basicDataService.delTreeSelect(code);
                }
            }
            catch (SQLException e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            }
        }
        return re;
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new SelectDb(pk.getStrValue());
    }
}
