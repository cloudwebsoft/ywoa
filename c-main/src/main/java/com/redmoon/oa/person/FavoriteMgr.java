package com.redmoon.oa.person;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.menu.Leaf;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FavoriteMgr {
    public FavoriteMgr() {
        super();
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        int f_type = ParamUtil.getInt(request, "f_type");
        String item = ParamUtil.get(request, "item");
        if (item.equals("")) {
            if (f_type==FavoriteDb.TYPE_MENU)
                throw new ErrMsgException("请选择菜单项");
            else
                throw new ErrMsgException("请输入链接");
        }
        String target = ParamUtil.get(request, "target");
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        int orders = ParamUtil.getInt(request, "orders", 0);
        String title = ParamUtil.get(request, "title");
        String icon = ParamUtil.get(request, "icon");
        if (f_type==FavoriteDb.TYPE_MENU) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(item);
            icon = lf.getIcon();
        }
        if (title.equals(""))
            throw new ErrMsgException("标题不能为空！");
        FavoriteDb ufd = new FavoriteDb();
        int id = (int) SequenceManager.nextID(SequenceManager.USER_FAVORITE);
        boolean re = false;
        try {
            re = ufd.create(new JdbcTemplate(), new Object[] {
                new Integer(id), userName, new Integer(f_type), item,
                        new Integer(orders), target, title, icon, null 
            });
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }


    public boolean save(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");

        FavoriteDb ufd = new FavoriteDb();
        ufd = (FavoriteDb)ufd.getQObjectDb(new Integer(id));

        int f_type = ufd.getInt("f_type");
        String item = ParamUtil.get(request, "item");
        if (item.equals("")) {
            if (f_type==FavoriteDb.TYPE_MENU)
                throw new ErrMsgException("请选择菜单项");
            else
                throw new ErrMsgException("请输入链接");
        }
        String target = ParamUtil.get(request, "target");
        int orders = ParamUtil.getInt(request, "orders", 0);
        String title = ParamUtil.get(request, "title");
        if (title.equals(""))
            throw new ErrMsgException("标题不能为空！");
        String icon = ParamUtil.get(request, "icon");

        boolean re = false;
        try {
            re = ufd.save(new JdbcTemplate(), new Object[] {
                item,new Integer(orders), target, title,icon,new Integer(id)
            });
        } catch (SQLException e) {
            throw new ErrMsgException(e.getMessage());
        }
        return re;
    }
    /**
     * 快捷菜单修改
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean update(HttpServletRequest request) throws ErrMsgException {
    	 int id = ParamUtil.getInt(request, "id");
    	 String title= ParamUtil.get(request, "title");
    	 String item = ParamUtil.get(request, "item");
    	 String icon = ParamUtil.get(request, "icon");
    	 FavoriteDb ufd = new FavoriteDb();
    	 String sql = "select id from " + ufd.getTable().getName() + " where id=" + id;
    	 Vector v = ufd.list(sql);
    	 
    	 Iterator irfav = v.iterator();
    	 while (irfav.hasNext()) {
			ufd = (FavoriteDb) irfav.next();
    	 }
    	 boolean re = false;
    	 try {
    		 re = ufd.save(new JdbcTemplate(), new Object[] {
                 item, ufd.getInt("orders"), ufd.get("target"), title,icon, id
             });
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return false;
    }

    /**
     * 初始化用户快速入口内容
     * @return
     */
    public boolean initQuickMenu4User(String userName) {
    	//删除该用户所有快速入口数据
    	this.deleteQuickMenu4User(userName);
    	//初始化数据
    	FavoriteDb ufd = new FavoriteDb();
    	String sql = "select id from " + ufd.getTable().getName() + " where user_name='system'";
    	Vector v = ufd.list(sql);
    	Iterator irfav = v.iterator();
    	boolean re = false; 
    	 while (irfav.hasNext()) {
 			ufd = (FavoriteDb) irfav.next();
 			int id = (int) SequenceManager.nextID(SequenceManager.USER_FAVORITE);
 			
 	        try {
 	            re = ufd.create(new JdbcTemplate(), new Object[] {
 	                new Integer(id), userName, ufd.getInt("f_type"), ufd.get("item"),
 	               ufd.getInt("orders"), ufd.get("target"), ufd.get("title"), ufd.get("icon"), 1 
 	            });
 	        } catch (ResKeyException e) { 	           
 	        	break;
 	        }
     	 }
    	 return re;
    }
    /**
     * 根据用户名删除对应快速入口内容
     * @param userName
     * @return
     */
    public boolean deleteQuickMenu4User(String userName)
    {
    	FavoriteDb ufd = new FavoriteDb();
    	String sql = "delete from " + ufd.getTable().getName() + " where user_name=" + StrUtil.sqlstr(userName);
		JdbcTemplate jt = new JdbcTemplate();
		boolean re = false;
		try {
			re = jt.executeUpdate(sql)==1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(getClass()).error(e);
		}
    	
		return re;
    }
   
}
