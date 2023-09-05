package com.redmoon.oa.job;

import java.sql.SQLException;

import com.cloudwebsoft.framework.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.quartz.*;
import com.redmoon.oa.ui.menu.Leaf;
import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;
import org.springframework.scheduling.quartz.QuartzJobBean;


/**
 * 
 * @Description: 将OA_MENU表code 与 mobile_app_icon_config code同步
 * 目前仅有“督办”需要同步
 * 20200403弃用，从AppInit中去除
 * @author: 罗珠敏
 * @Date: 2016-2-16上午10:04:53
 */
//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class SetMenuDutyMobileAppJob extends QuartzJobBean {

	// 督办，定位签到
	private final static String[] CODES_ARR = {"supervis","435441682"};
	private final static String[] PARENT_CODES_ARR = {"administration", "administration"};
	private final static String[] NAMES_ARR = {"部门工作", "签到管理"};
	
    public SetMenuDutyMobileAppJob() {
    }

    /**
     * execute
     *
     * @param jobExecutionContext JobExecutionContext
     * @throws JobExecutionException
     */
    @Override
	public void executeInternal(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
    	executeJob();
    }

	public void executeJob() {
    	for (int i = 0; i < CODES_ARR.length; i++) {
    		if(mobileMenuIsExistByCode(CODES_ARR[i])){
    			Leaf leaf = new Leaf(CODES_ARR[i]);
            	if(leaf == null || !leaf.isLoaded() || CODES_ARR[i].equals(CODES_ARR[1])){
            		String new_code = StrUtil.getNullStr(queryOaMenuCodeByName(PARENT_CODES_ARR[i], NAMES_ARR[i]));
            		if(!new_code.equals("")){
            			updateMobileAppCode(CODES_ARR[i], new_code);
            		}
            	}
    		}
		}
    }

	/**
	 * 根据code 查看手机端菜单是否存在
	 * @Description: 
	 * @param code
	 * @return
	 */
	public boolean mobileMenuIsExistByCode(String code){
		boolean flag = false;
		String sql = "select count(id) from mobile_app_icon_config where code = ?";
	 	JdbcTemplate jt = null;
		try {
			jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql,new Object[]{code});
			while (ri.hasNext()) {
				ResultRecord record = (ResultRecord) ri.next();
				int res = record.getInt(1);
				if(res >0 ){
					flag = true;
				}
			}
		} catch (SQLException e) {
			LogUtil.getLog(SetMenuDutyMobileAppJob.class).error(
					" SQLException:" + e.getMessage());
		}
		return flag;
	}

    /**
     * 根据 parentCode、菜单名称 返回菜单 code
     * @Description: 
     * @param parentCode
     * @param name
     * @return
     */
    public String queryOaMenuCodeByName(String parentCode,String name){
    	String sql = "SELECT code FROM oa_menu WHERE name = ? and parent_code = ?";
    	JdbcTemplate jt = null;
		String code = "";
		try {
			jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql,new Object[]{name,parentCode});
			while (ri.hasNext()) {
				ResultRecord record = (ResultRecord) ri.next();
				code = record.getString(1);
			}
		} catch (SQLException e) {
			LogUtil.getLog(SetMenuDutyMobileAppJob.class).error(
					" SQLException:" + e.getMessage());
		}
		return code;
    }

    /**
     * 更新 手机应用表 的code 为oa_menu的code
     * @Description: 
     * @param oldCode
     * @param newCode
     * @return
     */
    public boolean updateMobileAppCode(String oldCode,String newCode){
    	String sql = "UPDATE mobile_app_icon_config SET code = ? WHERE code = ?";
    	JdbcTemplate jt = null;
    	boolean flag = false;
    	
    	try {
    		jt = new JdbcTemplate();
			int res = jt.executeUpdate(sql, new Object[]{newCode,oldCode});
			if(res == 1){
				flag = true;
				// 清缓存
				RMCache rmcache = RMCache.getInstance();
				rmcache.clear();
				
			}
		} catch (SQLException | CacheException e) {
			LogUtil.getLog(SetMenuDutyMobileAppJob.class).error("updateMobileAppCode  SQLException:" + e.getMessage());
		}

		return flag;
    }
  
}
