package cn.js.fan.base;

import java.sql.*;
import java.util.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: 对象类用于直接存取数据库</p>
 *
 * <p>Description: 应用本类，是直接存取数据库，而ObjectDb则与缓存配套使用</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public abstract class ObjectRaw {

    public String connname = "";
    public String QUERY_LIST;
    public String QUERY_LOAD;
    public String QUERY_DEL;
    public String QUERY_SAVE;
    public String QUERY_ADD;
    public String QUERY_CREATE;

    public ObjectRaw() {
        init();
    }

    /**
     * 初始化，需在构造函数中调用，用来初始化数据库的查询语句等
     */
    public void init() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("默认数据库名不能为空");
        setQueryCreate();
        setQuerySave();
        setQueryDel();
        setQueryLoad();
        setQueryList();
        initDB();
    }

    /**
     * 初始化QUERY_CREATE
     */
    public void setQueryCreate() {};
    /**
     * 初始化QUERY_SAVE
     */
    public void setQuerySave() {};
    /**
     * 初始化QUERY_DEL
     */
    public void setQueryDel() {};
    /**
     * 初始化QUERY_LOAD
     */
    public void setQueryLoad() {};
    /**
     * 初始化QUERY_LIST
     */
    public void setQueryList() {};

    /**
     * 初始化数据库，在init()函数中调用
     */
    public void initDB() {}

    /**
     * 分页函数（抽象）
     * @param sql String sql语句
     * @param curPage int 当前页
     * @param pageSize int 每页的记录数
     * @return ListResult
     * @throws ErrMsgException
     */
    abstract public ListResult list(String sql, int curPage, int pageSize) throws
            ErrMsgException;

    /**
     * 列出全部记录
     * @return Vector
     * @throws SQLException
     */
    abstract public Vector list() throws SQLException;

    /**
     * 是否从数据库中加载成功
     * @return boolean
     */
    public boolean isLoaded() {
        return loaded;
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * 从数据库中加载
     */
    abstract public void load();
    /**
     * 保存对象至数据库
     * @return boolean
     */
    abstract public boolean save();
    /**
     * 从数据库中删除对象
     * @return boolean
     */
    abstract public boolean del();

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean loaded = false;
    protected String tableName;
}
