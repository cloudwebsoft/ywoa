package com.redmoon.oa.hr;

import com.redmoon.oa.visual.IModuleChecker;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.visual.FormDAO;

import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserCheck;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.post.*;
import com.redmoon.kit.util.FileUpload;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletResponse;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * @Description: 人员基本信息表的校验
 * @author: 古月圣
 * @Date: 2016-4-14下午04:26:02
 */
public class PersonbasicChecker implements IModuleChecker {
	private final static String AT_WORK = "在职";
	
	public PersonbasicChecker() {
		super();
	}

	public boolean validateUpdate(HttpServletRequest request, FileUpload fu,
			FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {
		return true;
	}

	public boolean validateCreate(HttpServletRequest request, FileUpload fu,
			Vector fields) throws ErrMsgException {

			String userName = StrUtil.getNullStr(fu.getFieldValue("user_name"));
			String mobile = StrUtil.getNullStr(fu.getFieldValue("mobile"));
			
		    UserCheck.chkName(userName);
		    UserCheck.chkMobile(mobile);
		    
		    return true;
	}

	public boolean validateDel(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		return true;
	}

	public boolean onDel(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		// 判断配置中是否设置了同步帐户
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");

		if (!isArchiveUserSynAccount) {
		    return true;
		}
		
		String userName = StrUtil.getNullStr(fdao.getFieldValue("user_name"));
		UserDb ud = new UserDb();
		if (ud.isExist(userName)) {
			ud = ud.getUserDb(userName);
			ud.setValid(UserDb.VALID_FIRED);
			ud.save();
		}
		PostUserMgr puMgr = new PostUserMgr();
		puMgr.setUserName(userName);
		PostUserDb puDb = puMgr.postByUserName();
		if (puDb != null && puDb.isLoaded()) {
			try {
				puDb.del();
			} catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			}
		}
		return true;
	}

	public boolean onCreate(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		String status = StrUtil.getNullStr(fdao.getFieldValue("zzqk"));
		if (status.equals("")) {
			fdao.setFieldValue("zzqk", "1");
			fdao.save();
		}
		// 判断配置中是否设置了同步帐户
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");

		if (!isArchiveUserSynAccount) {
		    return true;
		}
		
		String userName = StrUtil.getNullStr(fdao.getFieldValue("user_name"));
		int jobLevel = StrUtil.toInt(fdao.getFieldValue("job_level"), 0);
		
		UserDb ud = new UserDb();
		if (!ud.isExist(userName)) {
			// 为新增用户自动创建帐户
			com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();		  
			// 默认密码
			String defaultPwd = scfg.getInitPassword();

			String realName = StrUtil.getNullStr(fdao.getFieldValue("realname"));
			String mobile = StrUtil.getNullStr(fdao.getFieldValue("mobile"));
			
			String deptCode = StrUtil.getNullStr(fdao.getFieldValue("dept"));
			
			DeptDb dd = new DeptDb(deptCode);
			String unitCode = DeptDb.ROOTCODE;
			if (dd != null && dd.isLoaded()) {
				DeptDb unitDd = dd.getUnitOfDept(dd);
				unitCode = unitDd.getCode();
			}
			ud.create(userName, realName, defaultPwd, mobile, unitCode);
			ud = ud.getUserDb(userName);
			String sex = StrUtil.getNullStr(fdao.getFieldValue("sex"));
			ud.setGender(sex.equals("男") ? UserDb.GENDER_MAN : UserDb.GENDER_WOMAN);
			ud.setIDCard(StrUtil.getNullStr(fdao.getFieldValue("idcard")));
			ud.setAddress(StrUtil.getNullStr(fdao.getFieldValue("address")));
			ud.setBirthday(DateUtil.parse(fdao.getFieldValue("csrq"), "yyyy-MM-dd"));
			String personNo = StrUtil.getNullStr(fdao.getFieldValue("person_no"));
			if (personNo.equals("")) {
				ud.setPersonNo(UserDb.getNextPersonNo());
			} else {
				ud.setPersonNo(personNo);
			}
			Date entryDate = DateUtil.parse(fdao.getFieldValue("entry_date"), "yyyy-MM-dd");
			ud.setEntryDate(entryDate);
			ud.save();
			
			if (dd != null && dd.isLoaded()) {
				DeptUserDb dub = new DeptUserDb();
				dub.create(deptCode, userName, "");
			}
		}
		
		setPostUser(userName, jobLevel);
		return true;
	}

	public boolean onNestTableCtlAdd(HttpServletRequest request,
			HttpServletResponse response, JspWriter out) {
		return false;
	}

	public boolean onUpdate(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		String status = StrUtil.getNullStr(fdao.getFieldValue("zzqk"));
		if (status.equals("")) {
			fdao.setFieldValue("zzqk", "1");
			fdao.save();
		}
		String userName = StrUtil.getNullStr(fdao.getFieldValue("user_name"));
		int jobLevel = StrUtil.toInt(fdao.getFieldValue("job_level"), 0);
		
		// 判断配置中是否设置了同步帐户
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");

		if (!isArchiveUserSynAccount) {
		    return true;
		}

		String realName = StrUtil.getNullStr(fdao.getFieldValue("realname"));
		String mobile = StrUtil.getNullStr(fdao.getFieldValue("mobile"));
		String deptCode = StrUtil.getNullStr(fdao.getFieldValue("dept"));
		Date entryDate = DateUtil.parse(fdao.getFieldValue("entry_date"), "yyyy-MM-dd");
		
		DeptDb dd = new DeptDb(deptCode);
		String unitCode = DeptDb.ROOTCODE;
		if (dd != null && dd.isLoaded()) {
			DeptDb unitDd = dd.getUnitOfDept(dd);
			unitCode = unitDd.getCode();
		}
		String sex = StrUtil.getNullStr(fdao.getFieldValue("sex"));
		
		UserDb ud = new UserDb();
		if (!ud.isExist(userName)) {
			// 为新增用户自动创建帐户
			com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();		  
			// 默认密码
			String defaultPwd = scfg.getInitPassword();

			ud.create(userName, realName, defaultPwd, mobile, unitCode);
			ud = ud.getUserDb(userName);
			ud.setGender(sex.equals("男") ? UserDb.GENDER_MAN : UserDb.GENDER_WOMAN);
			ud.setAddress(StrUtil.getNullStr(fdao.getFieldValue("idcard")));
			ud.setBirthday(DateUtil.parse(fdao.getFieldValue("csrq"), "yyyy-MM-dd"));
			ud.setValid(status.equals(AT_WORK) ? UserDb.VALID_WORKING : UserDb.VALID_FIRED);
			ud.setEntryDate(entryDate);
			
			ud.save();
			
			if (dd != null && dd.isLoaded()) {
				DeptUserDb dud = new DeptUserDb();
				dud.create(deptCode, userName, "");
			}
		} else {
			ud = ud.getUserDb(userName);
			ud.setRealName(realName);
			ud.setMobile(mobile);
			ud.setUnitCode(unitCode);
			ud.setGender(sex.equals("男") ? UserDb.GENDER_MAN : UserDb.GENDER_WOMAN);
			ud.setAddress(StrUtil.getNullStr(fdao.getFieldValue("idcard")));
			ud.setBirthday(DateUtil.parse(fdao.getFieldValue("csrq"), "yyyy-MM-dd"));
			String personNo = StrUtil.getNullStr(fdao.getFieldValue("person_no"));
			if (personNo.equals("")) {
				ud.setPersonNo(UserDb.getNextPersonNo());
			} else {
				ud.setPersonNo(personNo);
			}
			ud.setValid(status.equals(AT_WORK) ? UserDb.VALID_WORKING : UserDb.VALID_FIRED);
			ud.setEntryDate(entryDate);
			ud.save();
			
			if (dd != null && dd.isLoaded()) {
				DeptUserDb dud = new DeptUserDb(userName);
				if (dud == null || !dud.isLoaded()) {
					dud = new DeptUserDb();
					dud.create(deptCode, userName, "");
				} else {
					dud.setDeptCode(deptCode);
					dud.save();
				}
			}
		}

		setPostUser(userName, jobLevel);
		return true;
	}

	private void setPostUser(String userName, int jobLevel) {
		if (jobLevel > 0) {
			PostUserDb puDb = new PostUserDb();
			PostUserMgr puMgr = new PostUserMgr();
			puMgr.setUserName(userName);
			try {
				if (puMgr.isExist()) {
					puDb.save(new JdbcTemplate(), new Object[] { jobLevel,
							userName, 0, puMgr.getId() });
				} else {
					puDb.create(new JdbcTemplate(), new Object[] { jobLevel,
							userName, 0 });
				}
			} catch (SQLException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			} catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			}
		}
	}
}
