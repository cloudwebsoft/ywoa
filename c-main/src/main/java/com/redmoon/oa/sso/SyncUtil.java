package com.redmoon.oa.sso;
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
import java.io.StringReader;

import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.entity.DeptUser;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class SyncUtil {
    public static final String ORGSYNC = "org_sync"; //组织结构同步

    public static final String CREATE = "01";
    public static final String MODIFY = "10";
    public static final String DEL = "02";
    public static final String HIDE = "20";
    public static final String MOVE_UP = "03";
    public static final String MOVE_DOWN = "30";

    public static final String USERSYNC = "user_sync";
    
    public static final String ALLDEL = "all_del";

    public SyncUtil() {
    }

    public boolean orgSync(Department department, String opType, String opUser) {
    	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    	if (!cfg.getBooleanProperty("isLarkUsed")) {
    		return true;
    	}
        com.redmoon.oa.sso.Config config = new com.redmoon.oa.sso.Config();
        String desKey = config.get("key");
        boolean re = false;
        try {
            com.redmoon.oa.sso.GetSyncXml xml = new com.redmoon.oa.sso.GetSyncXml();
            String xmlStr = xml.orgSynchronize(SyncUtil.ORGSYNC,
                    opType, desKey, department.getCode(), department.getName(),
                    department.getDescription(), department.getParentCode(),
                    department.getRootCode(), 0, department.getOrders(),
                    new java.util.Date(), department.getDeptType(),
                    department.getLayer(), department.getId(),
                    "", 0, 0,
                    0, 0, opUser);
            re = executeHttpRequest(xmlStr);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
            return re;
        }
        return re;
    }

    public boolean userSync(String userName, String deptCode, String opType, String opUser) {
    	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    	if (!cfg.getBooleanProperty("isLarkUsed")) {
    		return true;
    	}

        IUserService usersService = SpringUtil.getBean(IUserService.class);
    	User user = usersService.getUser(userName);

        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        DeptUser deptUser = deptUserService.getDeptUser(user.getName(), deptCode);
    	
    	int orders = 0;
    	if (deptUser!=null) {
            orders = deptUser.getOrders();
        }
    	
        com.redmoon.oa.sso.Config config = new com.redmoon.oa.sso.Config();
        String desKey = config.get("key");
        boolean re = false;
        try {
            com.redmoon.oa.sso.GetSyncXml xml = new com.redmoon.oa.sso.GetSyncXml();
            String xmlStr = xml.userSynchronize(SyncUtil.USERSYNC,
                    opType, desKey, user.getName(), user.getRealName(), user.getPwdRaw(), user.getEmail(), deptCode , opUser, user.getGender()?0:1, orders);
            re = executeHttpRequest(xmlStr);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
            return re;
        }
        return re;
    }
    
    public boolean allDelete() {
    	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    	if (!cfg.getBooleanProperty("isLarkUsed")) {
    		return true;
    	}
    	
        com.redmoon.oa.sso.Config config = new com.redmoon.oa.sso.Config();
        String desKey = config.get("key");
        boolean re = false;
        try {
            com.redmoon.oa.sso.GetSyncXml xml = new com.redmoon.oa.sso.GetSyncXml();
            String xmlStr = xml.allDelete(SyncUtil.ALLDEL, desKey);
            re = executeHttpRequest(xmlStr);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
            return re;
        }
        return re;
    }

    public boolean executeHttpRequest(String xml){
        com.redmoon.oa.sso.Config config = new com.redmoon.oa.
                sso.Config();
        String descUrl = config.get("targetUrl");
        HttpClient httpclient = new HttpClient();
        PostMethod post = new PostMethod(descUrl);
        try {
            post.setRequestHeader("Content-type", "text/xml; charset=utf-8");
            RequestEntity entity = new StringRequestEntity(xml, "text/xml", "utf-8");
            post.setRequestEntity(entity);
            httpclient.executeMethod(post);
            // 打印服务器返回的状态
            String status = "";
            String result = post.getResponseBodyAsString();
            if (post.getStatusCode() == HttpStatus.SC_OK) {
                result = new String(post.getResponseBody());
                if (result.equals("")) {
                	return true;
                }

                StringReader read = new StringReader(result);
                // 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
                InputSource source = new InputSource(read);
                
                // 创建一个新的SAXBuilder
                SAXBuilder sBuilder = new SAXBuilder();
                // 通过输入源构造一个Document
                Document doc = sBuilder.build(source);
                // 取的根元素
                Element root = doc.getRootElement();
                // out.print(root.getName());
                // Element header = root.getChild("Head");
                Element body = root.getChild("Body");
                if (body != null) {
                    status = body.getChildText("Status");
                    String OperType = body.getChildText("OperType");
                    String OperCode = root.getChildText("OperCode");
                    if (status.equals("0")) {
                        return true;
                    }
                    else {
                        LogUtil.getLog(getClass()).info("SyncUtil executeHttpRequest: OperCode=" + OperCode + " OperType=" + OperType + " status=" + status);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            post.releaseConnection();
        }

        return false;
    }
}
