package com.redmoon.oa.mobileskins;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.jcs.access.exception.CacheException;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.ui.menu.WallpaperDb;

public class MobileSkinsDb extends QObjectDb {
	public MobileSkinsDb getMobileSkinsDb(String code){
		return (MobileSkinsDb)getQObjectDb(code);
		
	}
	  /**
	   * 写文件
	   * @param request
	   * @param fu
	   * @param file_path
	   * @return
	   * @throws ErrMsgException
	   */
	   public String writeFile(HttpServletRequest request, FileUpload fu,String file_path) throws ErrMsgException {
	       if (fu.getRet() == FileUpload.RET_SUCCESS) {
	            Vector v = fu.getFiles();
	            Iterator ir = v.iterator();
	           
	            // 置路径
	            fu.setSavePath(file_path);
	            if (ir.hasNext()) {
	                FileInfo fi = (FileInfo) ir.next();
	                // 使用随机名称写入磁盘
	                fi.write(fu.getSavePath(), true);
	                return fi.getDiskName();
	            }
	        }
	        return "";
	    }
	   /**
	    * 选中皮肤的详细信息
	    * @return
	    */
	   public ResultRecord getIsUsedInfoDetail(String sql){
		   JdbcTemplate jt = new JdbcTemplate();
			try {
			 ResultIterator ri = jt.executeQuery(sql);
			 while(ri.hasNext()){
				
				 ResultRecord record = (ResultRecord) ri.next();
				 return record;
			 }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	 }

	   /**
	    * (增 删 改 查 操作)
	    * @param sql
	    * @return
	    */
	   public boolean updateIsUsedStatus(String sql){
		   boolean flag = false;
		   boolean countDetail = getCountInfo();
		   JdbcTemplate jt = new JdbcTemplate();
		   try {
			   if(!countDetail){
				  
				   int result = jt.executeUpdate(sql);
				    if(result > 0){
						flag = true;
					} 
			   }else{
				   flag = true;
			   }

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			RMCache rm = RMCache.getInstance();
			try {
				rm.clear();
			} catch (CacheException e) {
				e.printStackTrace();
			}
		}
		return flag;
	   }
	   /**
	    * 查询皮肤记录数
	    * @return
	    */
	   public boolean  getCountInfo(){
		   boolean flag = false;
		   String sql = "select count(*) from mobile_skins where is_used = '1'";
		   JdbcTemplate jt = new JdbcTemplate();
		   try {
			ResultIterator ri =jt.executeQuery(sql);
			while(ri.hasNext()){
				  ResultRecord rr = (ResultRecord) ri.next();
				  int count = rr.getInt(1);
				  if( count == 0){
					  flag = true;
				  }
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   return flag;
	   }
	   
	
}
