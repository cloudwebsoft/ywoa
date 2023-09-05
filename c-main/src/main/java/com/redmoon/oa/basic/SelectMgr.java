package com.redmoon.oa.basic;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import cn.js.fan.db.*;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;

import java.sql.SQLException;
import java.util.logging.Logger;

import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.db.SequenceManager;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.SkinUtil;

/**
 * <p>Title: 管理select控件</p>
 *
 * <p>Description: 对select控件及其option进行管理</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SelectMgr {
    public SelectMgr() {
        super();
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        String code = ParamUtil.get(request, "code");
        if (!StrUtil.isSimpleCode(code)) {
            throw new ErrMsgException("编码请使用英文字母或英文字母加数字的形式！");
        }
        String name = ParamUtil.get(request, "name");
        int orders = ParamUtil.getInt(request, "orders", 0);
        if ("".equals(name)) {
            throw new ErrMsgException("请输入名称！");
        }
        int type = ParamUtil.getInt(request, "type");
        String kind = ParamUtil.get(request, "kind");
        return create(code, name, orders, type, kind);
    }

    public boolean create(String code, String name, int orders, int type, String kind) throws ErrMsgException {
        SelectDb selectDb = new SelectDb();
        selectDb.setCode(code);
        selectDb.setName(name);
        selectDb.setOrders(orders);
        selectDb.setType(type);
        selectDb.setKind(StrUtil.toInt(kind, -1));
        return selectDb.create();
    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        String code = ParamUtil.get(request, "code");
        String name = ParamUtil.get(request, "name");
        int orders = ParamUtil.getInt(request, "orders", 0);
        if ("".equals(name)) {
            throw new ErrMsgException("请输入名称！");
        }
        int type = ParamUtil.getInt(request, "type");
        String kind = ParamUtil.get(request, "kind");
        try {
            return modify(code, name, orders, type, kind);
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
    }

    public boolean modify(String code, String name, int orders, int type, String kind) throws ResKeyException {
        SelectDb selectDb = new SelectDb();
        selectDb = selectDb.getSelectDb(code);
        selectDb.setName(name);
        selectDb.setOrders(orders);
        selectDb.setType(type);
        selectDb.setKind(StrUtil.toInt(kind));
        return selectDb.save();
    }

    public boolean createOption(HttpServletRequest request) throws ErrMsgException {
        String code = ParamUtil.get(request, "code");
        String name = ParamUtil.get(request, "name");
        String value = ParamUtil.get(request, "value");
        boolean isDefault = ParamUtil.getBoolean(request, "isDefault", false);
        int orders = ParamUtil.getInt(request, "orders", 0);
        if ("".equals(name) || "".equals(value)) {
            throw new ErrMsgException("值或名称不能为空！");
        }
        String color = ParamUtil.get(request, "color");
        
        boolean isOpen = ParamUtil.getInt(request, "isOpen", 1)==1;
        
        // 检查值是否有重复
        String sql = "select id from oa_select_option where code=? and value=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
			ResultIterator ri = jt.executeQuery(sql, new Object[]{code, value});
			if (ri.size()>0) {
				throw new ErrMsgException("值不能有重复！");
			}
		} catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
		}
        
        // 检查名称是否有重复
        sql = "select id from oa_select_option where code=? and name=?";
        try {
			ResultIterator ri = jt.executeQuery(sql, new Object[]{code, name});
			if (ri.size()>0) {
				throw new ErrMsgException("名称不能有重复！");
			}
		} catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
		}        
        
        return createOption(code, name, value, isDefault, orders, color, isOpen);
    }

    public boolean createOption(String code, String name, String value,
                                boolean isDefault, int orders, String color, boolean isOpen) {
        String sql = "insert into oa_select_option (id,code,name,value,is_default,orders,color,is_open) values (?,?,?,?,?,?,?,?)";
        JdbcTemplate jt = new JdbcTemplate();
        boolean re = false;
        int id = (int)SequenceManager.nextID(SequenceManager.OA_SELECT_OPTION);
        try {
            if (isDefault) {
                String sql2 = "update oa_select_option set is_default=0 where code=?";
                jt.executeUpdate(sql2, new Object[] {code});
            }
            re = jt.executeUpdate(sql, new Object[] {id, code, name, value, isDefault ? 1 : 0,
                    orders, color, isOpen?1:0}) == 1;
            if (re) {
                SelectDb selectDb = new SelectDb();
                selectDb = selectDb.getSelectDb(code);
                SelectCache selectCache = new SelectCache(selectDb);
                selectCache.refreshSave(selectDb.getPrimaryKey());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return re;
    }

    public boolean modifyOption(HttpServletRequest request) throws
            ErrMsgException {
        int id = ParamUtil.getInt(request, "id", -1);
        if (id==-1) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_id"));
        }
        String name = ParamUtil.get(request, "name");
        String value = ParamUtil.get(request, "value");
        String code = ParamUtil.get(request, "code");
        boolean isDefault = ParamUtil.getBoolean(request, "isDefault", false);
        int orders = ParamUtil.getInt(request, "orders", 0);
        if ("".equals(name)) {
            throw new ErrMsgException("请输入名称！");
        }
        String color = ParamUtil.get(request, "color");
        boolean isOpen = ParamUtil.getInt(request, "isOpen", 1)==1;
        
        // 检查值是否有重复
        String sql = "select id from oa_select_option where code=? and value=? and id<>" + id;
        JdbcTemplate jt = new JdbcTemplate();
        try {
			ResultIterator ri = jt.executeQuery(sql, new Object[]{code, value});
			if (ri.size()>0) {
				throw new ErrMsgException("值不能有重复！");
			}
		} catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
		}        
        // 检查名称是否有重复
        sql = "select id from oa_select_option where code=? and name=? and id<>" + id;
        try {
			ResultIterator ri = jt.executeQuery(sql, new Object[]{code, name});
			if (ri.size()>0) {
				throw new ErrMsgException("名称不能有重复！");
			}
		} catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
		}   		

        return modifyOption(code, id, name, value, isDefault, orders, color, isOpen);
    }

    public boolean modifyOption(String code, int id, String name, String value, boolean isDefault, int orders, String color, boolean isOpen) {
        String sql = "update oa_select_option set name=?,value=?,is_default=?,orders=?,color=?,is_open=? where id=?";
        JdbcTemplate jt = new JdbcTemplate();
        boolean re = false;
        try {
            if (isDefault) {
                String sql2 = "update oa_select_option set is_default=0 where code=?";
                jt.executeUpdate(sql2, new Object[] {code});
            }
            re = jt.executeUpdate(sql, new Object[]{name,value, isDefault ? 1 : 0, orders, color, isOpen?1:0, id})==1;
            if (re) {
                SelectDb selectDb = new SelectDb();
                selectDb = selectDb.getSelectDb(code);
                SelectCache selectCache = new SelectCache(selectDb);
                selectCache.refreshSave(selectDb.getPrimaryKey());
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return re;
    }

    public boolean delOption(int id) {
        String sql = "delete from oa_select_option where id=?";
        JdbcTemplate jt = new JdbcTemplate();
        boolean re = false;
        try {
            String code = "";
            ResultIterator ri = jt.executeQuery("select code from oa_select_option where id=?", new Object[]{id});
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                code = rr.getString(1);
            }
            re = jt.executeUpdate(sql, new Object[]{id})==1;
            if (re) {
                SelectDb selectDb = new SelectDb();
                selectDb = selectDb.getSelectDb(code);
                SelectCache selectCache = new SelectCache(selectDb);
                selectCache.refreshSave(selectDb.getPrimaryKey());
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return re;
    }

    public Vector getAllSelect() {
    	return getAllSelect(-1);
    }
    
    /**
     * 取得所有的select控件
     * @return SelectDb
     */
    public Vector getAllSelect(int kind) {
        String sql = "select code,name,orders,sel_type,kind from oa_select order by kind, orders";
        if (kind!=-1) {
            sql = "select code,name,orders,sel_type,kind from oa_select where kind=" + kind + " order by orders";
        }
        Vector v = new Vector();
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = ri.next();
                SelectDb sd = new SelectDb();
                sd.setCode(rr.getString("code"));
                sd.setName(rr.getString("name"));
                sd.setOrders(rr.getInt("orders"));
                sd.getOptions(jt);
                sd.setType(rr.getInt("sel_type"));
                sd.setKind(rr.getInt("kind"));
                v.add(sd);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return v;
    }

    /**
     * 列出树形的基础数据
     * @return
     */
    public List<SelectDb> listByTree() {
        List<SelectDb> list = new ArrayList<>();
        String sql = "select code,name,orders,sel_type,kind from oa_select where sel_type=? order by kind, orders";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[]{SelectDb.TYPE_TREE});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                SelectDb sd = new SelectDb();
                sd.setCode(rr.getString("code"));
                sd.setName(rr.getString("name"));
                sd.setOrders(rr.getInt("orders"));
                sd.getOptions(jt);
                sd.setType(rr.getInt("sel_type"));
                sd.setKind(rr.getInt("kind"));
                list.add(sd);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return list;
    }

    public static String getSqlList(HttpServletRequest request) {
        String sql = "select code,name,orders,sel_type,kind from oa_select where 1=1";

        String code = ParamUtil.get(request, "code");
        if (!"".equals(code)) {
            sql += " and (code like " + StrUtil.sqlstr("%" + code + "%")
                    + " or name like " + StrUtil.sqlstr("%" + code + "%") + ")";
        }
        int kind = ParamUtil.getInt(request, "kind", -1);
        if (kind != -1) {
            sql += " and kind=" + kind;
        }

        sql += " order by kind asc, orders desc";
        return sql;
    }
    
    public Vector<SelectDb> list(HttpServletRequest request) {
		String sql = "select code,name,orders,sel_type,kind from oa_select where 1=1";

		String code = ParamUtil.get(request, "code");
		if (!"".equals(code)) {
			sql += " and (code like " + StrUtil.sqlstr("%" + code + "%")
					+ " or name like " + StrUtil.sqlstr("%" + code + "%") + ")";
		}
		int kind = ParamUtil.getInt(request, "kind", -1);
		if (kind!=-1) {
			sql += " and kind=" + kind;
		}
		
		sql += " order by kind asc, orders desc";
		Vector<SelectDb> v = new Vector<>();
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = ri.next();
                SelectDb sd = new SelectDb();
                sd.setCode(rr.getString("code"));
                sd.setName(rr.getString("name"));
                sd.setOrders(rr.getInt("orders"));
                sd.getOptions(jt);
                sd.setType(rr.getInt("sel_type"));
                sd.setKind(rr.getInt("kind"));
                v.add(sd);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return v;		
    }

    public SelectDb getSelect(String code) {
        SelectDb sd = new SelectDb();
        return sd.getSelectDb(code);
    }
}
