package com.redmoon.oa.visual.func;

import java.util.ArrayList;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.base.IFuncImpl;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.person.UserDb;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

/**
 * 
 * @author fgf
 * $getUserInfo(user_name, mobile)，user_name为表单中的用户字段，mobile表示所取的信息
 */
public class UserFuncImpl implements IFuncImpl {

	@Override
	public String func(IFormDAO fdao, String[] func) throws ErrMsgException {
		String[] params = StrUtil.split(func[1], ",");
		if (params==null || params.length!=2) {
			throw new ErrMsgException("参数必须为2个！");
		}
		
		String userField = params[0].trim();
		String infoField = params[1].trim();
				
		String val;
		if (userField==null || "".equals(userField)) {
			LogUtil.getLog(getClass()).error(userField + "不存在！");			
			val = "";
		}
		else {
			val = fdao.getFieldValue(userField);
		}
		if (!"".equals(val)) {
			UserDb user = new UserDb();
			user = user.getUserDb(val);
			if (user.isLoaded()) {
				if ("email".equalsIgnoreCase(infoField)) {
					val = user.getEmail();
				}
				else if ("mobile".equalsIgnoreCase(infoField)) {
					val = user.getMobile();
				}
				else if ("qq".equalsIgnoreCase(infoField)) {
					val = user.getQQ();
				}
				else if ("idcard".equalsIgnoreCase(infoField)) {
					val = user.getIDCard();
				}
				else if ("address".equalsIgnoreCase(infoField)) {
					val = user.getAddress();
				}
				else if ("phone".equalsIgnoreCase(infoField)) {
					val = user.getPhone();
				}
				return val;
			}
			else {
				return "";
			}
		}
		else {
			return "";
		}
	}
	
	public ArrayList<String> getFieldsRelated(String[] func, FormDb fd) {
		String fieldName = func[1];		
		ArrayList<String> list = new ArrayList<String>();
		list.add(fieldName);
		return list;
	}	
}
