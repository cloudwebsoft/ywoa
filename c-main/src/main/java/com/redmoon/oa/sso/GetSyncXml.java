package com.redmoon.oa.sso;

import cn.js.fan.util.DateUtil;
import java.util.*;

public class GetSyncXml {

    public GetSyncXml() {

    }

    public String orgSynchronize(String operCode, String operType,
                                           String key, String code, String name,
                                           String description,
                                           String parentCode, String rootCode,
                                           int childCount, int orders,
                                           Date addDate, int deptType,
                                           int layer, int id,
                                           String normalCode, int isDeleted,
                                           int deptLevel, int kind,
                                           int deptRank, String opUser) {
        java.util.Date d = new java.util.Date();
        String timeStamp = DateUtil.format(d, "yyyyMMddHHmmss");
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("<TXL>");
        sb.append("<header>");
        sb.append("<oper_code>" + operCode + "</oper_code>");
        sb.append("<version>1.0.0</version>");
        sb.append("<key>" + key + "</key>");
        sb.append("<TimeStamp>" + timeStamp + "</TimeStamp>");
        sb.append("</header>");
        sb.append("<body>");
        sb.append("<oper_type>" + operType + "</oper_type>");
        sb.append("<code>" + code + "</code>");
        sb.append("<name>" + name + "</name>");
        sb.append("<description>" + description + "</description>");
        sb.append("<parent_code>" + parentCode + "</parent_code>");
        sb.append("<root_code>" + rootCode + "</root_code>");
        sb.append("<orders>" + orders + "</orders>");
        sb.append("<child_count>" + childCount + "</child_count>");
        sb.append("<add_date>" + addDate + "</add_date>");
        sb.append("<dept_type>" + deptType + "</dept_type>");
        sb.append("<layer>" + layer + "</layer>");
        sb.append("<id>" + id + "</id>");
        sb.append("<normal_code>" + normalCode + "</normal_code>");
        sb.append("<is_deleted>" + isDeleted + "</is_deleted>");
        sb.append("<dept_level>" + deptLevel + "</dept_level>");
        sb.append("<kind>" + kind + "</kind>");
        sb.append("<dept_rank>" + deptRank + "</dept_rank>");
        sb.append("<op_user>" + opUser + "</op_user>");
        sb.append("</body>");
        sb.append("</TXL>");
        return sb.toString();
    }

    /**
     * 同步用户
     * @param operCode
     * @param operType
     * @param key
     * @param userName
     * @param realName
     * @param pwd
     * @param email
     * @param deptCode
     * @param gender 0-男性 1-女性
     * @param opUser
     * @return
     */
    public String userSynchronize(String operCode, String operType,
                                           String key, String userName,
                                           String realName,
                                           String pwd,
                                           String email,
                                           String deptCode,
                                           String opUser,
                                           int gender,
                                           int deptUserOrder) {
        java.util.Date d = new java.util.Date();
        String timeStamp = DateUtil.format(d, "yyyyMMddHHmmss");
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("<TXL>");
        sb.append("<header>");
        sb.append("<oper_code>" + operCode + "</oper_code>");
        sb.append("<version>1.0.0</version>");
        sb.append("<key>" + key + "</key>");
        sb.append("<TimeStamp>" + timeStamp + "</TimeStamp>");
        sb.append("</header>");
        sb.append("<body>");
        sb.append("<oper_type>" + operType + "</oper_type>");
        sb.append("<user_name>" + userName + "</user_name>");
        sb.append("<pwd>" + pwd + "</pwd>");
        sb.append("<real_name>" + realName + "</real_name>");
        sb.append("<email>" + email + "</email>");
        sb.append("<dept_code>" + deptCode + "</dept_code>");
        sb.append("<op_user>" + opUser + "</op_user>");
        sb.append("<gender>" + gender + "</gender>");
        sb.append("<dept_user_order>" + deptUserOrder + "</dept_user_order>");        
        sb.append("</body>");
        sb.append("</TXL>");
        return sb.toString();
    }
    
    /**
     * @Description: 全部删除
     * @param operCode
     * @return
     */
    public String allDelete(String operCode, String key) {
    	java.util.Date d = new java.util.Date();
		String timeStamp = DateUtil.format(d, "yyyyMMddHHmmss");
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sb.append("<TXL>");
		sb.append("<header>");
		sb.append("<oper_code>" + operCode + "</oper_code>");
		sb.append("<version>1.0.0</version>");
        sb.append("<key>" + key + "</key>");
		sb.append("<TimeStamp>" + timeStamp + "</TimeStamp>");
		sb.append("</header>");
		sb.append("<body>");
		sb.append("</body>");
		sb.append("</TXL>");
		return sb.toString();
    }

}
