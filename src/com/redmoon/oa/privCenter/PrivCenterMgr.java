package com.redmoon.oa.privCenter;

import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.person.UserCache;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.RolePrivCache;
import com.redmoon.oa.pvg.RolePrivDb;
import com.redmoon.oa.visual.ModulePrivDb;

import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

public class PrivCenterMgr {
	Logger logger = Logger.getLogger(PrivCenterMgr.class.getName());
	
	//增加权限用户
	public void addUsers(String users,String myPriv,
			String module_code, String privs) throws ErrMsgException{
		if(myPriv.equals("root")){//myPriv=root表示的是文件柜根目录管理权限，与其他普权限不一样，操作表：dir_priv
			addDirPrivs(users,LeafPriv.TYPE_USER);
			return;
		}
		JdbcTemplate rmconn = new JdbcTemplate();
		String sql = null;
		try {
			rmconn.beginTrans();
			//先删除之前所选择的用户
			clearUsers(rmconn,myPriv,module_code,privs);
			//然后添加新的用户
			String[] usersArr = StrUtil.split(users, ",");
			for(String user : usersArr){
				//增加这个人关联（privs）权限
				if(privs!=null && !privs.equals("")){
					addRelatePrivs(rmconn,user,myPriv,privs,"user_priv","username");
				}
				//增加这个人模块（module_code）权限
				if(module_code!=null && !module_code.equals("")){
					addModulePrivs(rmconn,user, module_code,ModulePrivDb.TYPE_USER);
				}
				if(myPriv!=null && !myPriv.equals("")){
					sql = (new StringBuilder("delete from user_priv where username= ")).append(StrUtil.sqlstr(user)).append(" and priv=").append(StrUtil.sqlstr(myPriv)).toString();
					rmconn.executeUpdate(sql);
					sql = (new StringBuilder("insert into user_priv (username,priv) values (")).append(StrUtil.sqlstr(user)).append(", ").append(StrUtil.sqlstr(StrUtil.UnicodeToUTF8(myPriv))).append(")").toString();
					rmconn.executeUpdate(sql);
					//权限用户缓存
			        UserDb userDb = new UserDb();
			        userDb = userDb.getUserDb(user);
					UserCache uc = new UserCache(userDb);
		            uc.refreshPrivs(user);
				}
			}
			rmconn.commit();
		} catch (SQLException e) {
			rmconn.rollback();
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}
	}
	
	//增加权限角色
	public void addRoles(String roles,String myPriv,
			String module_code, String privs) throws ErrMsgException{
		if(myPriv.equals("root")){//myPriv=root表示的是文件柜根目录管理权限，与其他普权限不一样，操作表：dir_priv
			addDirPrivs(roles,LeafPriv.TYPE_ROLE);
			return;
		}
		JdbcTemplate rmconn = new JdbcTemplate();
		String sql = null;
		try {
			rmconn.beginTrans();
			//先删除之前所选择的角色
			clearRoles(rmconn,myPriv,module_code,privs);
			//然后添加新的角色
			String[] rolesArr = StrUtil.split(roles, ",");
			for(String role : rolesArr){
				//增加这个人关联（privs）权限
				if(privs!=null && !privs.equals("")){
					addRelatePrivs(rmconn,role,myPriv,privs,"user_role_priv","roleCode");
				}
				//增加这个人或这个角色模块（module_code）权限
				if(module_code!=null && !module_code.equals("")){
					addModulePrivs(rmconn,role, module_code,ModulePrivDb.TYPE_ROLE);
				}
				if(myPriv!=null && !myPriv.equals("")){
					sql = (new StringBuilder("delete from user_role_priv where roleCode= ")).append(StrUtil.sqlstr(role)).append(" and priv=").append(StrUtil.sqlstr(myPriv)).toString();
					rmconn.executeUpdate(sql);
					sql = (new StringBuilder("insert into user_role_priv (roleCode,priv) values (")).append(StrUtil.sqlstr(role)).append(", ").append(StrUtil.sqlstr(StrUtil.UnicodeToUTF8(myPriv))).append(")").toString();
					rmconn.executeUpdate(sql);
					//权限角色缓存
			        RolePrivDb rp = new RolePrivDb();
			        rp = rp.getRolePrivDb(role, myPriv);
			        RolePrivCache rc = new RolePrivCache(rp);
			        PrimaryKey primaryKey = new PrimaryKey(new HashMap());
		            rc.refreshDel(primaryKey);
				}
			}
			rmconn.commit();
		} catch (SQLException e) {
			rmconn.rollback();
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}
	}
	
	//增加文件柜根目录管理权限
	private void addDirPrivs(String userOrRoles,int type) throws ErrMsgException{
		JdbcTemplate rmconn = new JdbcTemplate();
		String sql = null;
		try {
			rmconn.beginTrans();
			//先删除之前所选择的用户或角色
			clearDirPrivs(rmconn,type);
			//然后添加新的用户或角色
			String[] userOrRolesArr = StrUtil.split(userOrRoles, ",");
			for(String userOrRole : userOrRolesArr){
				//查询表dir_priv，根目录管理权限是否赋予了这个人或这个角色
				sql = (new StringBuilder("delete from dir_priv where dir_code = 'root' and name = ")).append(StrUtil.sqlstr(userOrRole)).toString();
				rmconn.executeUpdate(sql);
				int id = (int)SequenceManager.nextID(SequenceManager.FILEARK_PRIV);
				sql = (new StringBuilder("insert into dir_priv(id,name,priv_type,dir_code,examine,see,append,del,dir_modify) values(")).append(id).append(",").append(StrUtil.sqlstr(userOrRole)).append(",").append(type).append(",'root',1,0,0,0,0)").toString();
				rmconn.executeUpdate(sql);
			}
			rmconn.commit();
		} catch (SQLException e) {
			rmconn.rollback();
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}
	}
	
	//删除权限用户
	public void deleteUser(String user,
			String myPriv, String module_code, String privs) throws ErrMsgException{
		if(myPriv.equals("root")){//myPriv=root表示的是文件柜根目录管理权限，与其他普权限不一样，操作表：dir_priv
			removeDirPrivs(user,LeafPriv.TYPE_USER);
			return;
		}
		//删除这个人的普通（myPriv）权限
		JdbcTemplate rmconn = new JdbcTemplate();
		try {
			rmconn.beginTrans();
			//删除这个人的关联（privs）权限
	        if(privs!=null && !privs.equals("")){
	        	removeRelatePrivs(rmconn,user,myPriv,privs,"user_priv","username");
			}
	        //删除这个人的模块（module_code）权限
	        if(module_code!=null && !module_code.equals("")){
	        	removeModulePrivs(rmconn,user,module_code,ModulePrivDb.TYPE_USER);
			}
	        if(myPriv!=null && !myPriv.equals("")){
				String sql = (new StringBuilder("delete from user_priv ")).append(" where priv = ").append(StrUtil.sqlstr(myPriv)).append(" and username= ").append(StrUtil.sqlstr(user)).toString();
				rmconn.executeUpdate(sql);
				//清除权限用户缓存
		        UserDb userDb = new UserDb();
		        userDb = userDb.getUserDb(user);
				UserCache uc = new UserCache(userDb);
	            uc.refreshPrivs(user);
			}
	        rmconn.commit();
		} catch (SQLException e) {
			rmconn.rollback();
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}
	}
	
	//删除权限角色
	public void deleteRole(String role,
			String myPriv, String module_code, String privs) throws ErrMsgException{
		if(myPriv.equals("root")){//myPriv=root表示的是文件柜根目录管理权限，与其他普权限不一样，操作表：dir_priv
			removeDirPrivs(role,LeafPriv.TYPE_ROLE);
			return;
		}
		//删除这个角色的普通（myPriv）权限
		JdbcTemplate rmconn = new JdbcTemplate();
		try {
			rmconn.beginTrans();
			//删除这个角色的关联（privs）权限
	        if(privs!=null && !privs.equals("")){
	        	removeRelatePrivs(rmconn,role,myPriv,privs,"user_role_priv","roleCode");
			}
	        //删除这个角色的模块（module_code）权限
	        if(module_code!=null && !module_code.equals("")){
	        	removeModulePrivs(rmconn,role,module_code,ModulePrivDb.TYPE_ROLE);
			}
	        if(myPriv!=null && !myPriv.equals("")){
				String sql = (new StringBuilder("delete from user_role_priv ")).append(" where priv = ").append(StrUtil.sqlstr(myPriv)).append(" and roleCode= ").append(StrUtil.sqlstr(role)).toString();
				rmconn.executeUpdate(sql);
				//清除权限角色缓存
		        RolePrivDb rp = new RolePrivDb();
		        rp = rp.getRolePrivDb(role, myPriv);
		        RolePrivCache rc = new RolePrivCache(rp);
		        PrimaryKey primaryKey = new PrimaryKey(new HashMap());
	            rc.refreshDel(primaryKey);
			}
	        rmconn.commit();
		} catch (SQLException e) {
			rmconn.rollback();
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}
	}
	
	//删除文件柜根目录管理权限
	private void removeDirPrivs(String userOrRole,int type) throws ErrMsgException{
		JdbcTemplate rmconn = new JdbcTemplate();
		String sql = null;
		try {
			//删除拥有根目录权限的这个人或这个角色
			sql = (new StringBuilder("delete from dir_priv where dir_code = 'root' and examine=1 and priv_type = ")).append(type).append(" and name = ").append(StrUtil.sqlstr(userOrRole)).toString();
			rmconn.executeUpdate(sql);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}
	}

	//清空权限用户
	public void clearUsers(JdbcTemplate conn,String myPriv,String module_code, String privs) throws ErrMsgException{
		if(myPriv.equals("root")){//myPriv=root表示的是文件柜根目录管理权限，与其他普权限不一样，操作表：dir_priv
			clearDirPrivs(conn,LeafPriv.TYPE_USER);
			return;
		}
		JdbcTemplate rmconn = null;
		if(conn == null){
			rmconn = new JdbcTemplate();
		}else{
			rmconn = conn;
		}
		ResultIterator ri = null;
		ResultRecord rr = null;
		String sql = null;
		String userOrRole = null;
		try {
			if(myPriv!=null && !myPriv.equals("")){
				sql = (new StringBuilder("select distinct username from user_priv where priv= ")).append(StrUtil.sqlstr(myPriv)).toString();
			}else if((myPriv==null || myPriv.equals("")) && !module_code.equals("")){
				sql = (new StringBuilder("select distinct name from visual_module_priv where manage = 1 and form_code= ")).append(StrUtil.sqlstr(module_code)).append(" and priv_type = ").append(ModulePrivDb.TYPE_USER).toString();
			}
			if(conn == null){
				rmconn.beginTrans();
			}
			ri = rmconn.executeQuery(sql);
			while (ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				userOrRole = rr.getString(1);
				//删除这个人的关联（privs）权限
		        if(privs!=null && !privs.equals("")){
		        	removeRelatePrivs(rmconn,userOrRole,myPriv,privs,"user_priv","username");
				}
		        //删除这个人的模块（module_code）权限
		        if(module_code!=null && !module_code.equals("")){
		        	removeModulePrivs(rmconn,userOrRole,module_code,ModulePrivDb.TYPE_USER);
				}
		        if(myPriv!=null && !myPriv.equals("")){
					sql = (new StringBuilder("delete from user_priv ")).append(" where priv = ").append(StrUtil.sqlstr(myPriv)).append(" and username= ").append(StrUtil.sqlstr(userOrRole)).toString();
					rmconn.executeUpdate(sql);
					//清除权限用户缓存
			        UserDb userDb = new UserDb();
			        userDb = userDb.getUserDb(userOrRole);
					UserCache uc = new UserCache(userDb);
		            uc.refreshPrivs(userOrRole);
				}
			}
			if(conn == null){
				rmconn.commit();
			}
		} catch (SQLException e) {
			rmconn.rollback();
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}
	}

	//清空权限角色
	public void clearRoles(JdbcTemplate conn,String myPriv,String module_code, String privs) throws ErrMsgException{
		if(myPriv.equals("root")){//myPriv=root表示的是文件柜根目录管理权限，与其他普权限不一样，操作表：dir_priv
			clearDirPrivs(conn,LeafPriv.TYPE_ROLE);
			return;
		}
		JdbcTemplate rmconn = null;
		if(conn == null){
			rmconn = new JdbcTemplate();
		}else{
			rmconn = conn;
		}
		ResultIterator ri = null;
		ResultRecord rr = null;
		String sql = null;
		String userOrRole = null;
		try {
			if(myPriv!=null && !myPriv.equals("")){
				sql = (new StringBuilder("select distinct roleCode from user_role_priv where priv= ")).append(StrUtil.sqlstr(myPriv)).toString();
			}else if((myPriv==null || myPriv.equals("")) && !module_code.equals("")){
				sql = (new StringBuilder("select distinct name from visual_module_priv where manage = 1 and form_code= ")).append(StrUtil.sqlstr(module_code)).append(" and priv_type = ").append(ModulePrivDb.TYPE_ROLE).toString();
			}
			if(conn == null){
				rmconn.beginTrans();
			}
			ri = rmconn.executeQuery(sql);
			while (ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				userOrRole = rr.getString(1);
				//删除这个角色的关联（privs）权限
		        if(privs!=null && !privs.equals("")){
		        	removeRelatePrivs(rmconn,userOrRole,myPriv,privs,"user_role_priv","roleCode");
				}
		        //删除这个角色的模块（module_code）权限
		        if(module_code!=null && !module_code.equals("")){
		        	removeModulePrivs(rmconn,userOrRole,module_code,ModulePrivDb.TYPE_ROLE);
				}
		        if(myPriv!=null && !myPriv.equals("")){
					sql = (new StringBuilder("delete from user_role_priv ")).append(" where priv = ").append(StrUtil.sqlstr(myPriv)).append(" and roleCode= ").append(StrUtil.sqlstr(userOrRole)).toString();
					rmconn.executeUpdate(sql);
					//清除权限角色缓存
			        RolePrivDb rp = new RolePrivDb();
			        rp = rp.getRolePrivDb(userOrRole, myPriv);
			        RolePrivCache rc = new RolePrivCache(rp);
			        PrimaryKey primaryKey = new PrimaryKey(new HashMap());
		            rc.refreshDel(primaryKey);
				}
			}
			if(conn == null){
				rmconn.commit();
			}
		} catch (SQLException e) {
			rmconn.rollback();
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}
	}
	
	//清空文件柜根目录管理权限
	private void clearDirPrivs(JdbcTemplate conn,int type) throws ErrMsgException{
		JdbcTemplate rmconn = null;
		if(conn == null){
			rmconn = new JdbcTemplate();
		}else{
			rmconn = conn;
		}
		String sql = null;
		try {
			//清空拥有根目录权限的用户或角色
			sql = (new StringBuilder("delete from dir_priv where dir_code = 'root' and examine=1 and priv_type = ")).append(type).toString();
			rmconn.executeUpdate(sql);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}		
	}
	
	//增加这个人或这个角色关联（privs）权限
	private void addRelatePrivs(JdbcTemplate rmconn,String userOrRole,String myPriv,String privs,String table,String column) throws ErrMsgException{
		String sql = null;
		String[] privsArr = StrUtil.split(privs, "\\|");
		try {
			for(String childPriv : privsArr){
				if(myPriv!=null && !myPriv.equals("")){
					sql = (new StringBuilder("delete from ")).append(table).append(" where ").append(column).append("=").append(StrUtil.sqlstr(userOrRole)).append(" and priv=").append(StrUtil.sqlstr(childPriv)).toString();
					rmconn.executeUpdate(sql);
					sql = (new StringBuilder("insert into ")).append(table).append("(").append(column).append(",priv) values (").append(StrUtil.sqlstr(userOrRole)).append(", ").append(StrUtil.sqlstr(StrUtil.UnicodeToUTF8(childPriv))).append(")").toString();
					rmconn.executeUpdate(sql);
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}
	}

	//增加这个人或这个角色模块（module_code）权限
	private void addModulePrivs(JdbcTemplate rmconn,String userOrRole,String module_code,int type) throws ErrMsgException{
		String sql = null;
		String[] moduleArr = StrUtil.split(module_code, "\\|");
		try {
			for(String module : moduleArr){
				sql = (new StringBuilder("delete from visual_module_priv where name=")).append(StrUtil.sqlstr(userOrRole)).append(" and form_code=").append(StrUtil.sqlstr(module)).toString();
				rmconn.executeUpdate(sql);
				int id = (int)SequenceManager.nextID(66);
				sql = (new StringBuilder("insert into visual_module_priv (id,form_code,name,priv_type,manage,see,append,modify,view) values (")).append(id).append(",").append(StrUtil.sqlstr(StrUtil.UnicodeToUTF8(module))).append(",").append(StrUtil.sqlstr(userOrRole)).append(",").append(type).append(",1,0,0,0,0)").toString();
				rmconn.executeUpdate(sql);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}
	}
	
	//删除这个人或这个角色关联（privs）权限
	private void removeRelatePrivs(JdbcTemplate rmconn,String userOrRole,String myPriv,String privs,String table,String column) throws ErrMsgException{
		String sql = null;
		String[] privsArr = StrUtil.split(privs, "\\|");
		for(String childPriv : privsArr){
			sql = (new StringBuilder("delete from ")).append(table).append(" where priv = ").append(StrUtil.sqlstr(childPriv)).append(" and ").append(column).append(" = ").append(StrUtil.sqlstr(userOrRole)).toString();
			try {
				rmconn.executeUpdate(sql);
			} catch (SQLException e) {
				logger.error(e.getMessage());
				throw new ErrMsgException("操作失败！");
			}
		}
	}
	
	//删除这个人或这个角色模块（module_code）权限
	private void removeModulePrivs(JdbcTemplate rmconn,String userOrRole,String module_code,int type) throws ErrMsgException{
		String sql = null;
		String[] moduleArr = StrUtil.split(module_code, "\\|");
		try {
			for(String module : moduleArr){
				sql = (new StringBuilder("delete from visual_module_priv where manage=1 and name=")).append(StrUtil.sqlstr(userOrRole)).append(" and form_code=").append(StrUtil.sqlstr(module)).append(" and priv_type = ").append(type).toString();
				rmconn.executeUpdate(sql);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new ErrMsgException("操作失败！");
		}
	}
}
