package com.redmoon.oa.visual;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import nl.bitwalker.useragentutils.DeviceType;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import SuperDog.DogStatus;
import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.flow.FormViewDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MatchUserException;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.macroctl.AttachmentCtl;
import com.redmoon.oa.flow.macroctl.AttachmentsCtl;
import com.redmoon.oa.flow.macroctl.ImageCtl;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.macroctl.SQLCtl;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.superCheck.CheckSuperKey;
import com.redmoon.oa.util.BeanShellUtil;
import com.redmoon.oa.util.RequestUtil;

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
public class FormDAOMgr {
    String formCode;
    FormDb fd;
    FileUpload fu;
    long visualObjId;
    
    private ArrayList<FormDAO> prjUserDao;
    
    FormDAO fdao;

    public long getVisualObjId() {
		return visualObjId;
	}

	public void setVisualObjId(int visualObjId) {
		this.visualObjId = visualObjId;
	}
	
	public FormDAO getFormDAO() {
		return fdao;
	}
	
	// Logger logger = Logger.getLogger(FormDAOMgr.class.getName());

	public ArrayList<FormDAO> getPrjUserDao() {
		return prjUserDao;
	}

	public void setPrjUserDao(ArrayList<FormDAO> prjUserDao) {
		this.prjUserDao = prjUserDao;
	}

	public FormDAOMgr(FormDb fd) {
    	checkSuper();
        this.formCode = fd.getCode();
        this.fd = fd;
        fu = new FileUpload();
    }

	public FormDAOMgr(String formCode) {
    	checkSuper();
        this.formCode = formCode;
        fd = new FormDb();
        fd = fd.getFormDb(formCode);
        fu = new FileUpload();
    }
    /**
     * 校验超级狗
     */
    private void checkSuper(){
    	//校验超级狗
    	CheckSuperKey csdk = CheckSuperKey.getInstance();
    	com.redmoon.oa.Config oacfg = new com.redmoon.oa.Config();
    	try {
	    	int status = csdk.checkKey();
			//验证失败
			if (status != DogStatus.DOG_STATUS_OK){
				oacfg.put("systemIsOpen", "false");
				oacfg.put("systemStatus", "请使用正版授权系统");
			}
    	}catch (Exception e) {
			// TODO Auto-generated catch block
    		oacfg.put("systemIsOpen", "false");
    		oacfg.put("systemStatus", "请使用正版授权系统");
		}
    }
    public boolean doUpload(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        // String[] extnames = {"jpg", "gif", "xls", "rar", "doc", "rm", "avi",
        //                    "bmp", "swf"};
        // fu.setValidExtname(extnames); // 设置可上传的文件类型
    	// if (formCode.equals("project"))
    	// 	fu.setDebug(true);
    	
        fu.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K
        int ret = 0;
        try {
        	// fu.setDebug(true);
            ret = fu.doUpload(application, request);
            if (ret!=FileUpload.RET_SUCCESS)
                throw new ErrMsgException(fu.getErrMessage());
        }
        catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + StrUtil.trace(e));
            e.printStackTrace();
            throw new ErrMsgException(e.getMessage());
        }
        return true;
    }
    
    public String getFieldValue(String fieldName) {
    	return getFieldValue(fieldName, fu);
    }

    public static String getFieldValue(String fieldName, FileUpload fu) {
    	String val = "";
    	try {
    		val = StrUtil.getNullStr(fu.getFieldValue(fieldName, false));
    	}
    	catch (ClassCastException e) {
    		// 使表单域选择宏控件支持多选的时候，这里得到的应是数组，将其拼装成,号分隔的字符串
    		String[] ary = fu.getFieldValues(fieldName);
    		for (String str : ary) {
    			if ("".equals(val)) {
    				val = str;
    			}
    			else {
    				val += "," + str;
    			}
    		}
    	}
    	return val;
    }

    /**
     * 根据fields获取request中所有域的值
     * @param request HttpServletRequest
     */
    public static Vector<FormField> getFieldsByForm(HttpServletRequest request, FormDb fd, FileUpload fu, Vector<FormField> fields) throws ErrMsgException {
        Iterator<FormField> ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
				UserAgent ua = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
		        OperatingSystem os = ua.getOperatingSystem();
		        boolean isMobile = false;
		        if(DeviceType.MOBILE.equals(os.getDeviceType())) {
		            isMobile = true;
		        }
		        if (!isMobile) {              	
	                String d = getFieldValue(ff.getName(), fu) + " " + getFieldValue(ff.getName()+"_time", fu);
	                // logger.info("getFieldsByForm:" + d);
	                ff.setValue(d);
		        }
		        else {
		        	ff.setValue(getFieldValue(ff.getName(), fu));
		        }
            }
            else {
                ff.setValue(getFieldValue(ff.getName(), fu));
            }
        }
        return fields;
    }

    /**
     * 根据fields获取request中所有域的值
     * @param request HttpServletRequest
     */
    public Vector<FormField> getFieldsByForm(ServletContext application, HttpServletRequest request, Vector fields) throws ErrMsgException {
        doUpload(application, request);
        return getFieldsByForm(request, fd, fu, fields);
    }
    
    public boolean create(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        Vector fields = fd.getFields();
        return create(application, request, fields, fd.getCode());
    }  
    
    public boolean createPrjMember(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        Vector fields = fd.getFields();
        return createPrjMember(application, request, fields, fd.getCode());
    }
    
    public boolean create(ServletContext application, HttpServletRequest request, ModuleSetupDb msd) throws ErrMsgException {
    	String code = msd.getString("code");
    	String formCode = msd.getString("form_code");
    	// 如果是主表单
    	if (code.equals(formCode)) {
    		// return create(application, request);
    	}
    	
    	FormDb fd = new FormDb();
    	fd = fd.getFormDb(formCode);
    	
    	int viewEdit = msd.getInt("view_edit");
    	if (viewEdit==ModuleSetupDb.VIEW_DEFAULT || viewEdit==ModuleSetupDb.VIEW_EDIT_CUSTOM) {
    		return create(application, request);
    	}
    	
    	FormViewDb fvd = new FormViewDb();
    	fvd = fvd.getFormViewDb(viewEdit);
    	if (fvd==null) {
    		throw new ErrMsgException("视图ID=" + viewEdit + "不存在");
    	}
    	
    	String content = fvd.getString("content");
    	String ieVersion = fvd.getString("ie_version");
    	
    	FormParser fp = new FormParser();
    	Vector fields = fp.parseCtlFromView(content, ieVersion, fd);   
    	
    	return create(application, request, fields, code);
    }    

    /**
     * 创建
     * @param request HttpServletRequest
     * @return boolean
     */
    public boolean create(ServletContext application, HttpServletRequest request, Vector fields, String moduleCode) throws ErrMsgException {
    	long actionId = StrUtil.toInt(StrUtil.getNullStr((String)request.getAttribute("actionId")), -1);
        String userName = new Privilege().getUser(request);

        // 当嵌套表格2添加时，formCode为父表单的编码，当前嵌套表的编码则为formCodeRelated
        String parentFormCode = "";
        String pageType = (String)request.getAttribute("pageType");
        if (pageType==null) {
        	pageType = ParamUtil.get(request, "pageType");
        }
        // 仅module_add_relate.jsp中pageType为add，而nest_sheet_add_relate.jsp中为add_relate   
        // flow表示自于手机端，此时的flowId为url后的参数
        if ("add_relate".equals(pageType) || "add".equals(pageType) || "flow".equals(pageType)) {
            parentFormCode = ParamUtil.get(request, "formCode");        	
        }

    	if (actionId==-1 || actionId==0) {
	        ModulePrivDb mpd = new ModulePrivDb(formCode);
	       	String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
	       	if (fieldWrite!=null) {
	       		if (!fieldWrite.equals("")) {
	       			String[] fieldAry = StrUtil.split(fieldWrite, ",");
	       			if (fieldAry!=null) {
	       				fields = new Vector();
	       				for (int k=0; k<fieldAry.length; k++) {
	       					try {
	       						// 需clone，否则会导致fields中字段被更新后，影响fd.getFormField(...)取得的原始值
	       						// 致嵌套表格2在智能模块添加后，立刻向流程中添加出现缓存问题，这时会看到表单中出现了上一次添加的值
								FormField ff = fd.getFormField(fieldAry[k]);
								if (ff==null) {
									continue;
								}
	       						fields.addElement(ff.clone());
							} catch (CloneNotSupportedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	       				}
	       			}
	       		}
	       	}
	       	// 不去掉验证，因为二开的时候有些字段需隐藏
/*	       	// 去掉隐藏字段的验证
	       	String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
	        String[] fdsHide = StrUtil.split(fieldHide, ",");        	
	       	if (fdsHide!=null) {
	       		for (int i=0; i<fdsHide.length; i++) {
	       			Iterator ir = fields.iterator();
	       			while (ir.hasNext()) {
	       				FormField ff = (FormField)ir.next();
	       				if (fdsHide[i].equals(ff.getName())) {
	       					ir.remove();
	       				}
	       			}
	       		}
	       	}	 */      	
    	}
    	else { 
			WorkflowActionDb wfa = new WorkflowActionDb();
			wfa = wfa.getWorkflowActionDb((int)actionId);
			String fieldWrite = StrUtil.getNullString(wfa.getFieldWrite()).trim();

			String[] fds = fieldWrite.split(",");
			int len = fds.length;

            MacroCtlMgr mm = new MacroCtlMgr();
			int nestLen = "nest.".length();
			// 将嵌套表中可写的域筛选出
			Iterator ir = fields.iterator();
			while (ir.hasNext()) {
				FormField ff = (FormField) ir.next();
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
		            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		            // 跳过SQL控件，因为当SQL控件不可写时，仍需记录其值
		            if (mu.getIFormMacroCtl() instanceof SQLCtl) {
		            	continue;
		            }
				}

				boolean isFound = false;
				for (int i = 0; i < len; i++) {
		            // 如果不是嵌套表格2的可写表单域
	                if (!fds[i].startsWith("nest.")) {
	                    continue;
	                }
	                String fName = fds[i].substring(nestLen);
					if (ff.getName().equals(fName)) {
						isFound = true;
						break;
					}
				}
				if (!isFound) {
					ir.remove();
				}
			}
			
    	}
	
    	fields = getFieldsByForm(application, request, fields);

        // 对表单进行有效性验证
        com.redmoon.oa.flow.FormDAOMgr.validateFields(request, fu, fields, null, false);

        // 通过检查接口对fields中的值进行有效性判断
        Config cfg = new Config();
        IModuleChecker ifv = cfg.getIModuleChecker(formCode);
        // logger.info("ifv=" + ifv + " formCode=" + formCode);
        boolean re = true;
        if (ifv!=null)
            re = ifv.validateCreate(request, fu, fields);
        if (!re)
            throw new ErrMsgException("表单验证非法！");

        FormDAO fdao = new FormDAO(fd);
        fdao.setFields(fields);
        
        // 对函数型的表单域赋值
        Iterator irFunc = fields.iterator();
        while (irFunc.hasNext()) {
        	FormField ff = (FormField)irFunc.next();
        	if (ff.isFunc()) {
        		ff.setValue(String.valueOf(FuncUtil.render(ff, ff.getDefaultValueRaw(), fdao)));
        	}
        }

        Privilege privilege = new Privilege();
        
        ModuleSetupDb vsd = new ModuleSetupDb();
        vsd = vsd.getModuleSetupDbOrInit(moduleCode);
        
        // 模块验证
        String validateProp = StrUtil.getNullStr(vsd.getString("validate_prop"));
        if (!"".equals(validateProp)) {
        	String cond = ModuleUtil.parseValidate(request, fu, fdao, moduleCode, validateProp);
	    	ScriptEngineManager manager = new ScriptEngineManager();
	        ScriptEngine engine = manager.getEngineByName("javascript");
	        try {
	        	Boolean ret = (Boolean)engine.eval(cond);
	        	if (!ret.booleanValue()) {
	        		throw new ErrMsgException(StrUtil.getNullStr(vsd.getString("validate_msg")));
	        	}
	        }
	        catch (ScriptException ex) {
	        	ex.printStackTrace();
	        }        	
        }
        
        // 执行验证脚本
		String script = vsd.getScript("validate");
		if (script != null && !script.equals("")) {
			Interpreter bsh = new Interpreter();
			try {
				StringBuffer sb = new StringBuffer();

				BeanShellUtil.setFieldsValue(fdao, sb);

				// 赋值用户
				sb.append("userName=\"" + privilege.getUser(request) + "\";");
				bsh.eval(BeanShellUtil.escape(sb.toString()));

				bsh.set("action", "create");				
				bsh.set("fdao", fdao);
				bsh.set("request", request);
				bsh.set("fileUpload", fu);

				bsh.eval(script);
				Object obj = bsh.get("ret");
				if (obj != null) {
					boolean ret = ((Boolean) obj).booleanValue();
					if (!ret) {
						String errMsg = (String) bsh.get("errMsg");
						if (errMsg != null)
							throw new ErrMsgException("验证非法：" + errMsg);
						else {
							throw new ErrMsgException("验证非法，但errMsg为空！");
						}
					}
				} else {
					// 需要判断action进行检测，因为delete事件中可能要验证，而当创建验证时，该情况下脚本中未写ret值
					// throw new ErrMsgException("该节点脚本中未配置ret=...");
				}
			} catch (EvalError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}        
        
        fdao.setCreator(userName);
        // 置单位
        Privilege pvg = new Privilege();
        fdao.setUnitCode(pvg.getUserUnitCode(request));

        // 置用于关联模块的cws_id的值
        String cwsId = StrUtil.getNullStr(fu.getFieldValue("cws_id"));
        fdao.setCwsId(cwsId);
        
        // 置父模块编码，仅当前表单为嵌套表格或关联的子表时
        fdao.setCwsParentForm(parentFormCode);
        
        // 流程中添加嵌套表格2记录，nest_sheet_add_relate.jsp，表单中上传了cwsStatus，且为STATUS_NOT状态
        int cwsStatus = StrUtil.toInt(fu.getFieldValue("cwsStatus"), com.redmoon.oa.flow.FormDAO.STATUS_DONE);
        fdao.setCwsStatus(cwsStatus);
        
        int flowId = StrUtil.toInt(fu.getFieldValue("flowId"), com.redmoon.oa.visual.FormDAO.NONEFLOWID);
        // 如果是来自于手机端
        if ("flow".equals(pageType)){
        	flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);
        }
        fdao.setFlowId(flowId);
        
        re = fdao.create(request, fu);
        if (re) {
        	this.fdao = fdao;
        	
        	visualObjId = fdao.getId();
        	
            if (ifv!=null) {
            	try {
            		re = ifv.onCreate(request, fdao);
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }
            
            // 如果需要记录历史
            if (fd.isLog()) {
            	FormDAO.log(userName, FormDAOLog.LOG_TYPE_CREATE, fdao);
            }

            // 为关联模块中的表单型（单条记录）生成一条记录
            ModuleRelateDb mrd = new ModuleRelateDb();
            Iterator ir = mrd.getModulesRelated(formCode).iterator();
            while (ir.hasNext()) {
                mrd = (ModuleRelateDb)ir.next();
                if (mrd.getInt("relate_type")==ModuleRelateDb.TYPE_SINGLE) {
                    FormDb fdRelate = new FormDb();
                    fdRelate = fdRelate.getFormDb(mrd.getString("relate_code"));
                    FormDAO fdaoRelate = new FormDAO(fdRelate);
                    fdaoRelate.setCwsId(getRelateFieldValue(fdao.getId(), mrd.getString("relate_code")));
                    fdaoRelate.createEmptyForm();
                }
                else {
                    FormDb fdRelate = new FormDb();
                    fdRelate = fdRelate.getFormDb(mrd.getString("relate_code"));
                    FormDAO fdaoRelate = new FormDAO();
                    ifv = cfg.getIModuleChecker(fdRelate.getCode());

                	// 检查从模块是否有加入表单的临时temp_cws_ids_formCode
                    String fName = FormDAO.NAME_TEMP_CWS_IDS + "_" + mrd.getString("relate_code");
                    String[] ary = fu.getFieldValues(fName);
                    if (ary!=null) {
                    	for (int i=0; i<ary.length; i++) {
                    		fdaoRelate = fdaoRelate.getFormDAO(StrUtil.toLong(ary[i]), fdRelate);
                    		if (fdaoRelate == null || !fdaoRelate.isLoaded()) {
                    			continue;
                    		}
                            fdaoRelate.setCwsId(getRelateFieldValue(fdao.getId(), mrd.getString("relate_code")));
                            fdaoRelate.save();
                            
                            if (ifv!=null)
                            	try {
                            		re = ifv.onCreate(request, fdaoRelate);
                            	} catch (Exception e) {
                            		e.printStackTrace();
                            	}
                    	}
                    }
                }
            }
            
            // 如果表单中含有“表单域选择宏控件”，且当前模块是该宏控件对应的表单的从模块，则自动关联
            // 例如：单独添加合同付款，而不是先打开合同，再添加付款记录，此时则自动关联合同
            
            // 宏控件为智能模块表单型，如其formCode不为空，则表单编码为其formCode，如为空，则由控件自行去解析
            // 相应地需要在宏控件中增加虚方法getFormCode()
            
            // 如果cwsId不为空，则表示在添加时，是从主模板入口进入的
            if ("".equals(cwsId)) {     	
	    		MacroCtlMgr mm = new MacroCtlMgr();            
	            ir = fields.iterator();
	            while (ir.hasNext()) {
	            	FormField ff = (FormField)ir.next();
	                if(ff.getType().equals(FormField.TYPE_MACRO)) {
	                	if (ff.getValue()==null || "".equals(ff.getValue())) {
	                		continue;
	                	}
	                	
	                	// 如果不是long类型的，则说明字段中存储的不是ID，字段对应的不可能是主模块
	                	long mainFormId = StrUtil.toLong(ff.getValue(), -1);
	                	if (mainFormId==-1) {
	                		continue;
	                	}
	                	                	
	    				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
	    				if (mu.isForm()) {
	    					String mainFormCode = mu.getFormCode();
	    					if ("".equals(mainFormCode)) {
	    						mainFormCode = StrUtil.getNullStr(mu.getIFormMacroCtl().getFormCode(request, ff));
	    					}
	    					if ("".equals(mainFormCode)) {
	    						continue;
	    					}
	    					
	    					boolean isRelateToMainForm = false;
	    					// 检查该mainFormCode是否为当前表单的主表单
	    	            	Iterator irRelated = mrd.getModulesRelated(mainFormCode).iterator();            	
	    	            	while (irRelated.hasNext()) {
	    	            		mrd = (ModuleRelateDb)irRelated.next();
	    	            		if (mrd.getString("relate_code").equals(formCode)) {
	    	            			isRelateToMainForm = true;
	    	            			break;
	    	            		}
	    	            	}
	    	            	if (isRelateToMainForm) {
	    	            		FormDAOMgr fdmMain = new FormDAOMgr(mainFormCode);
	    	            		String relateId = fdmMain.getRelateFieldValue(mainFormId, mrd.getString("relate_code"));
	    	            		fdao.setCwsId(relateId);
	                            fdao.save();
	    	            	}
	    				}
	                }
	            }
            }
            // 发送消息
			sendRemindMsg(request, vsd, fdao, "create");
					
			// 添加事件脚本处理
			script = vsd.getScript("create");
			if (script != null) {
				Interpreter bsh = new Interpreter();
				try {
					StringBuffer sb = new StringBuffer();
					
					BeanShellUtil.setFieldsValue(fdao, sb);

					// 赋值当前用户
					sb.append("userName=\"" + privilege.getUser(request)
							+ "\";");
					// 这样的方法，会使得fdao被解释为字符串，即使强制类型转换都不行
					// sb.append("fdao=\"" + fdao + "\";");

					bsh.set("fdao", fdao);
					bsh.set("request", request);
					bsh.set("fileUpload", fu);

					bsh.eval(BeanShellUtil.escape(sb.toString()));

					bsh.eval(script);
					Object obj = bsh.get("ret");
					if (obj != null) {
						boolean ret = ((Boolean) obj).booleanValue();
						if (!ret) {
							String errMsg = (String) bsh.get("errMsg");
							LogUtil.getLog(getClass()).error(
									"create bsh errMsg=" + errMsg);
						}
					}
				} catch (EvalError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}                          
            
        }
        return re;
    }
    
    public void sendRemindMsg(HttpServletRequest request, ModuleSetupDb vsd, FormDAO fdao, String eventType) {
    	String msgProp = vsd.getString("msg_prop");
    	Element action = null;
    	boolean isFound = false;
		if (msgProp!=null && !"".equals(msgProp)){
			try {
				SAXBuilder parser = new SAXBuilder();
				org.jdom.Document docu = parser.build(new InputSource(new StringReader(msgProp)));
				Element root = docu.getRootElement();
				List actions = root.getChildren("action");
				Iterator irAction = null;
				if (actions!=null)
					irAction = actions.iterator();
				while (irAction!=null && irAction.hasNext()) {
					action = (Element)irAction.next();
					String attEventType = action.getAttributeValue("eventType");
					if (eventType.equals(attEventType)){
						isFound = true;
						break;
					}
				}
			}
			catch (JDOMException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(getClass()).error("parse msgProp field error");						
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(getClass()).error("parse msgProp field error");						
				e.printStackTrace();
			}
		}     
		
		if (!isFound) {
			return;
		}
    	
		String deptFields = action.getChildText("deptFields");
		String userFields = action.getChildText("userFields");
		String users = action.getChildText("users");
		String roles = action.getChildText("roles");
		
		String title = action.getChildText("title");
		String content = action.getChildText("content");
		
		Privilege pvg = new Privilege();
		UserDb user = new UserDb();
		user = user.getUserDb(pvg.getUser(request));
		
		title = title.replaceFirst("\\$fromUser", user.getRealName());		
		content = content.replaceFirst("\\$fromUser", user.getRealName());
		
		MacroCtlMgr mm = new MacroCtlMgr();

		RequestUtil.setFormDAO(request, fdao);
        // 处理表单域
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(title);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            FormField ff = fd.getFormField(fieldName);
            if(ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu!=null) {
					m.appendReplacement(sb, mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
				}
				else {
					m.appendReplacement(sb, "控件不存在");
				}
            }
            else {
            	m.appendReplacement(sb, fdao.getFieldValue(fieldName));
            }
        }
        m.appendTail(sb);
        title = sb.toString();
        
        m = p.matcher(content);
        sb = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            FormField ff = fd.getFormField(fieldName);
            if(ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu!=null) {
					m.appendReplacement(sb, mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
				}
				else {
					m.appendReplacement(sb, "控件不存在");
				}
            }
            else {
            	m.appendReplacement(sb, fdao.getFieldValue(fieldName));
            }
        }
        m.appendTail(sb);
        content = sb.toString();        
		
		boolean isMsg = "true".equals(action.getChildText("isMsg"));
		boolean isMail = "true".equals(action.getChildText("isMail"));
		boolean isSms = "true".equals(action.getChildText("isSms"));
		
		UserMgr um = new UserMgr();
		
		Vector v = new Vector();
		Map map = new HashMap();

		// 取出部门
		DeptUserDb dud = new DeptUserDb();
		String[] dNames = StrUtil.split(deptFields, ",");
		if (dNames!=null) {
			for (int i=0; i<dNames.length; i++) {
				Vector vt = dud.list(dNames[i]);
				Iterator ir = vt.iterator();
				while (ir.hasNext()) {
					dud = (DeptUserDb)ir.next();
					if (!map.containsKey(dud.getUserName())) {
						v.addElement(um.getUserDb(dud.getUserName()));
						map.put(dud.getUserName(), "");
					}					
				}
			}
		}
		
		// 取出用户域
		String[] uNames = StrUtil.split(userFields, ",");
		if (uNames!=null) {
			for (int i=0; i<uNames.length; i++) {
				if (!map.containsKey(uNames[i])) {
					v.addElement(um.getUserDb(uNames[i]));
					map.put(uNames[i], "");
				}				
			}
		}		
		
		// 取出角色
		String[] roleAry = StrUtil.split(roles, ",");
		if (roleAry!=null) {
			RoleDb rd = new RoleDb();
			for (int i=0; i<roleAry.length; i++) {
				rd = rd.getRoleDb(roleAry[i]);
				if (rd==null) {
					rd = new RoleDb();
					continue;
				}
				Iterator ir = rd.getAllUserOfRole().iterator();
				while (ir.hasNext()) {
					UserDb ud = (UserDb)ir.next();
					if (!map.containsKey(ud.getName())) {
						v.addElement(ud);
						map.put(ud.getName(), "");
					}	
				}
			}
		}
		
		// 取出用户
		uNames = StrUtil.split(users, ",");
		if (uNames!=null) {
			for (int i=0; i<uNames.length; i++) {
				if (!map.containsKey(uNames[i])) {
					v.addElement(um.getUserDb(uNames[i]));
					map.put(uNames[i], "");
				}				
			}
		}		
		
        String charset = Global.getSmtpCharset();
        cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
        String senderName = StrUtil.GBToUnicode(Global.AppName);
        senderName += "<" + Global.getEmail() + ">";	
        String mailserver = Global.getSmtpServer();
        int smtp_port = Global.getSmtpPort();
        String name = Global.getSmtpUser();
        String pwd_raw = Global.getSmtpPwd();
        boolean isSsl = Global.isSmtpSSL();
        try {
            sendmail.initSession(mailserver, smtp_port, name,
                                 pwd_raw, "", isSsl);
        } catch (Exception ex) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
        }
        
		// String tail = getFormAbstractTable(wf);
        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
		MessageDb md = new MessageDb();
		
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			UserDb ud = (UserDb) ir.next();

			if (isSms && SMSFactory.isUseSMS()) {
				IMsgUtil imu = SMSFactory.getMsgUtil();
				if (imu != null) {
					try {
						imu.send(ud, title, MessageDb.SENDER_SYSTEM);
					} catch (ErrMsgException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			// 发送信息
			if (isMsg) {
				try {
					md.sendSysMsg(ud.getName(), title, content, "");
				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (isMail && ud.getEmail() != null && !ud.getEmail().equals("")) {				
				UserSetupDb usd = new UserSetupDb(ud.getName());
				String mailCont = content; // + tail;
				
				sendmail.initMsg(ud.getEmail(), senderName, title, mailCont, true);
				sendmail.send();
				sendmail.clear();
			}
		}
		
    }    
    
    /**
     * 创建
     * @param request HttpServletRequest
     * @return boolean
     */
    public boolean createPrjMember(ServletContext application, HttpServletRequest request, Vector fields, String moduleCode) throws ErrMsgException {
    	if (!formCode.equals("project_members")) {
    		return create(application, request, fields, fd.getCode());
    	}
    	
    	fields = getFieldsByForm(application, request, fields);
    	
    	Iterator it = fields.iterator();
    	String prjUser = "";
    	while (it.hasNext()) {
    		FormField ff = (FormField) it.next();
    		if (ff.getName().equals("prj_user")) {
    			prjUser = ff.getValue();
    			break;
    		}
    	}
    	
    	String[] prjUsers = prjUser.split(",");

        boolean re = true;
        prjUserDao = new ArrayList<FormDAO>();
        
    	for (String user : prjUsers) {
    		Vector prjVec = new Vector();
    		fu.setFieldValue("prj_user", user);
    		it = fields.iterator();
    		while (it.hasNext()) {
        		FormField ff = (FormField) it.next();
        		if (ff.getName().equals("prj_user")) {
        			ff.setValue(user);
        		}
        		try {
					prjVec.add(ff.clone());
				} catch (CloneNotSupportedException e) {
					throw new ErrMsgException("创建失败！");
				}
        	}
    		// 对表单进行有效性验证
            com.redmoon.oa.flow.FormDAOMgr.validateFields(request, fu, prjVec, null ,false);

            // 通过检查接口对fields中的值进行有效性判断
            Config cfg = new Config();
            IModuleChecker ifv = cfg.getIModuleChecker(formCode);
            // logger.info("ifv=" + ifv + " formCode=" + formCode);
            if (ifv!=null)
                re = ifv.validateCreate(request, fu, prjVec);
            if (!re)
                throw new ErrMsgException("表单验证非法！");

            FormDAO fdao = new FormDAO(fd);
            fdao.setFields(prjVec);

            Privilege privilege = new Privilege();
            
            ModuleSetupDb vsd = new ModuleSetupDb();
            vsd = vsd.getModuleSetupDbOrInit(moduleCode);
            
            // 执行验证脚本
    		String script = vsd.getScript("validate");
    		if (script != null && !script.equals("")) {
    			Interpreter bsh = new Interpreter();
    			try {
    				StringBuffer sb = new StringBuffer();

    				BeanShellUtil.setFieldsValue(fdao, sb);

    				// 赋值用户
    				sb.append("userName=\"" + privilege.getUser(request) + "\";");
    				bsh.eval(BeanShellUtil.escape(sb.toString()));

    				bsh.set("action", "create");				
    				bsh.set("fdao", fdao);
    				bsh.set("request", request);
    				bsh.set("fileUpload", fu);

    				bsh.eval(script);
    				Object obj = bsh.get("ret");
    				if (obj != null) {
    					boolean ret = ((Boolean) obj).booleanValue();
    					if (!ret) {
    						String errMsg = (String) bsh.get("errMsg");
    						if (errMsg != null)
    							throw new ErrMsgException("验证非法：" + errMsg);
    						else {
    							throw new ErrMsgException("验证非法，但errMsg为空！");
    						}
    					}
    				} else {
    					// 需要判断action进行检测，因为delete事件中可能要验证，而当创建验证时，该情况下脚本中未写ret值
    					// throw new ErrMsgException("该节点脚本中未配置ret=...");
    				}
    			} catch (EvalError e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}        
            
            String userName = privilege.getUser(request);
            fdao.setCreator(userName);
            // 置单位
            Privilege pvg = new Privilege();
            fdao.setUnitCode(pvg.getUserUnitCode(request));

            // 置用于关联模块的cws_id的值
            String cwsId = StrUtil.getNullStr(fu.getFieldValue("cws_id"));
            fdao.setCwsId(cwsId);
            
            // 流程中添加嵌套表格2记录，nest_sheet_add_relate.jsp，表单中上传了cwsStatus，且为STATUS_NOT状态
            int cwsStatus = StrUtil.toInt(fu.getFieldValue("cwsStatus"), com.redmoon.oa.flow.FormDAO.STATUS_DONE);
            fdao.setCwsStatus(cwsStatus);
            
            int flowId = StrUtil.toInt(fu.getFieldValue("flowId"), com.redmoon.oa.visual.FormDAO.NONEFLOWID);
            fdao.setFlowId(flowId);
            
            re = fdao.create(request, fu);
            if (re) {
            	prjUserDao.add(fdao);
            	
            	this.fdao = fdao;
            	
            	visualObjId = fdao.getId();
            	
                if (ifv!=null)
                	try {
                		re = ifv.onCreate(request, fdao);
                	} catch (Exception e) {
                		e.printStackTrace();
                	}
                
                // 如果需要记录历史
                if (fd.isLog())
                	FormDAO.log(userName, FormDAOLog.LOG_TYPE_CREATE, fdao);            

                // 为关联模块中的表单型（单条记录）生成一条记录
                ModuleRelateDb mrd = new ModuleRelateDb();
                Iterator ir = mrd.getModulesRelated(formCode).iterator();
                while (ir.hasNext()) {
                    mrd = (ModuleRelateDb)ir.next();
                    if (mrd.getInt("relate_type")==ModuleRelateDb.TYPE_SINGLE) {
                        FormDb fdRelate = new FormDb();
                        fdRelate = fdRelate.getFormDb(mrd.getString("relate_code"));
                        FormDAO fdaoRelate = new FormDAO(fdRelate);
                        fdaoRelate.setCwsId(getRelateFieldValue(fdao.getId(), mrd.getString("relate_code")));
                        fdaoRelate.createEmptyForm();
                    }
                    else {
                        FormDb fdRelate = new FormDb();
                        fdRelate = fdRelate.getFormDb(mrd.getString("relate_code"));
                        FormDAO fdaoRelate = new FormDAO();
                        ifv = cfg.getIModuleChecker(fdRelate.getCode());

                    	// 检查从模块是否有加入表单的临时temp_cws_ids_formCode
                        String fName = FormDAO.NAME_TEMP_CWS_IDS + "_" + mrd.getString("relate_code");
                        String[] ary = fu.getFieldValues(fName);
                        if (ary!=null) {
                        	for (int i=0; i<ary.length; i++) {
                        		fdaoRelate = fdaoRelate.getFormDAO(StrUtil.toLong(ary[i]), fdRelate);
                        		if (fdaoRelate == null || !fdaoRelate.isLoaded()) {
                        			continue;
                        		}
                                fdaoRelate.setCwsId(getRelateFieldValue(fdao.getId(), mrd.getString("relate_code")));
                                fdaoRelate.save();
                                
                                if (ifv!=null)
                                	try {
                                		re = ifv.onCreate(request, fdaoRelate);
                                	} catch (Exception e) {
                                		e.printStackTrace();
                                	}
                        	}
                        }
                    }
                }

    			script = vsd.getScript("create");
    			if (script != null) {
    				Interpreter bsh = new Interpreter();
    				try {
    					StringBuffer sb = new StringBuffer();
    					
    					BeanShellUtil.setFieldsValue(fdao, sb);

    					// 赋值当前用户
    					sb.append("userName=\"" + privilege.getUser(request)
    							+ "\";");
    					sb.append("fdao=\"" + fdao + "\";");

    					bsh.set("request", request);
    					bsh.set("fileUpload", fu);

    					bsh.eval(BeanShellUtil.escape(sb.toString()));

    					bsh.eval(script);
    					Object obj = bsh.get("ret");
    					if (obj != null) {
    						boolean ret = ((Boolean) obj).booleanValue();
    						if (!ret) {
    							String errMsg = (String) bsh.get("errMsg");
    							LogUtil.getLog(getClass()).error(
    									"create bsh errMsg=" + errMsg);
    						}
    					}
    				} catch (EvalError e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}                          
                
            }
    	}

        return re;
    }

    public FormDAO getFormDAO(long visualObjId) {
        FormDAO fdao = new FormDAO();
        return fdao.getFormDAO(visualObjId, fd);
    }
    
    public boolean update(ServletContext application, HttpServletRequest request, ModuleSetupDb msd) throws ErrMsgException {
    	String code = msd.getString("code");
    	String formCode = msd.getString("form_code");
    	// 如果是主表单
    	if (code.equals(formCode)) {
    		// return update(application, request);
    	}
    	
    	FormDb fd = new FormDb();
    	fd = fd.getFormDb(formCode);
    	
    	int viewEdit = msd.getInt("view_edit");
    	if (viewEdit==ModuleSetupDb.VIEW_DEFAULT || viewEdit==ModuleSetupDb.VIEW_EDIT_CUSTOM)
    		return update(application, request);
    	
    	FormViewDb fvd = new FormViewDb();
    	fvd = fvd.getFormViewDb(viewEdit);
    	if (fvd==null) {
    		throw new ErrMsgException("视图ID=" + viewEdit + "不存在");
    	}
    	
    	// String form = fvd.getString("form");
    	String ieVersion = fvd.getString("ie_version");
    	
    	FormParser fp = new FormParser();
    	Vector fields = fp.parseCtlFromView(fvd.getString("content"), ieVersion, fd);

    	return update(application, request, fields, code);
    }    

    public boolean update(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        Vector<FormField> fields = fd.getFields();
        return update(application, request, fields, fd.getCode());
    }
    
    public boolean update(ServletContext application, HttpServletRequest request, Vector<FormField> fields, String moduleCode) throws ErrMsgException {
        Privilege privilege = new Privilege();

    	// 从request中获取表单中域的值
        fields = getFieldsByForm(application, request, fields);
        
    	String userName = privilege.getUser(request);
    	long actionId = StrUtil.toInt(StrUtil.getNullStr((String)request.getAttribute("actionId")), -1);
    	
    	// 复制一份用于校验
    	Vector<FormField> fieldsForCheck = new Vector<FormField>();
    	Iterator<FormField> irF = fields.iterator();
    	while (irF.hasNext()) {
    		FormField ff = null;
			try {
				ff = (FormField)(irF.next()).clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
    		fieldsForCheck.addElement(ff);
    	}
    	
    	if (actionId==-1) {
	        ModulePrivDb mpd = new ModulePrivDb(formCode);
	       	String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
	       	if (fieldWrite!=null) {
	       		if (!fieldWrite.equals("")) {
	       			String[] fieldAry = StrUtil.split(fieldWrite, ",");
	       			if (fieldAry!=null) {
	       				Iterator<FormField> ir = fieldsForCheck.iterator();
	       				while (ir.hasNext()) {
	       					FormField ff = ir.next();
	       					boolean isWrite = false;
		       				for (int k=0; k<fieldAry.length; k++) {
		       					if (ff.getName().equals(fieldAry[k])) {
		       						isWrite = true;
		       						break;
		       					}
		       				}
		       				if (!isWrite) {
		       					ir.remove();
		       				}
	       				}
	       			}
	       		}
	       	}
	       	// 20171209 二开的时候可能需要用到隐藏字段，所以不隐藏
/*	       	// 去掉隐藏字段的验证
	       	String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
	        String[] fdsHide = StrUtil.split(fieldHide, ",");        	
	       	if (fdsHide!=null) {
	       		for (int i=0; i<fdsHide.length; i++) {
	       			Iterator<FormField> ir = fieldsForCheck.iterator();
	       			while (ir.hasNext()) {
	       				FormField ff = (FormField)ir.next();
	       				if (fdsHide[i].equals(ff.getName())) {
	       					ir.remove();
	       				}
	       			}
	       		}
	       	}	*/       	
    	}
    	else {
    		WorkflowActionDb wfa = new WorkflowActionDb();
			wfa = wfa.getWorkflowActionDb((int)actionId);
			String fieldWrite = StrUtil.getNullString(wfa.getFieldWrite()).trim();

			String[] fds = fieldWrite.split(",");
			int len = fds.length;

			MacroCtlMgr mm = new MacroCtlMgr();
			int nestLen = "nest.".length();
			// 将嵌套表中可写的域筛选出
			Iterator<FormField> ir = fieldsForCheck.iterator();
			while (ir.hasNext()) {
				FormField ff = (FormField) ir.next();
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
		            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		            // 跳过SQL控件，因为当SQL控件不可写时，仍需记录其值
		            if (mu.getIFormMacroCtl() instanceof SQLCtl) {
		            	continue;
		            }
				}				
				boolean isFound = false;
				for (int i = 0; i < len; i++) {
		            // 如果不是嵌套表格2的可写表单域
	                if (!fds[i].startsWith("nest.")) {
	                    continue;
	                }
	                String fName = fds[i].substring(nestLen);
					if (ff.getName().equals(fName)) {
						isFound = true;
						break;
					}
				}
				if (!isFound) {
					ir.remove();
				}
			}
    	}
                
        visualObjId = StrUtil.toInt(fu.getFieldValue("id"), -1);
        if (visualObjId==-1) {
        	// 20170227 手机端的明细表中编辑时可能未传过来
        	visualObjId = ParamUtil.getInt(request, "id", -1);
        	if (visualObjId==-1) {
        		throw new ErrMsgException("缺少ID！");
        	}
        }
        
        FormDAO fdao = getFormDAO(visualObjId);
        // 对表单中的可写表单域（且不隐藏的）进行有效性验证
        com.redmoon.oa.flow.FormDAOMgr.validateFields(request, fu, fieldsForCheck, fdao, false);        
        
        boolean re = true;
        // 通过检查接口对fields中的值进行有效性判断
        Config cfg = new Config();
        IModuleChecker ifv = cfg.getIModuleChecker(formCode);
        if (ifv!=null)
            re = ifv.validateUpdate(request, fu, fdao, fields);
        if (!re)
            throw new ErrMsgException("表单验证非法！");
        
        
        ModuleSetupDb vsd = new ModuleSetupDb();
        vsd = vsd.getModuleSetupDb(moduleCode);
        
        // 模块验证
        String validateProp = StrUtil.getNullStr(vsd.getString("validate_prop"));
        if (!"".equals(validateProp)) {
        	String cond = ModuleUtil.parseValidate(request, fu, fdao, moduleCode, validateProp);
	    	ScriptEngineManager manager = new ScriptEngineManager();
	        ScriptEngine engine = manager.getEngineByName("javascript");
	        try {
	        	Boolean ret = (Boolean)engine.eval(cond);
	        	if (!ret.booleanValue()) {
	        		throw new ErrMsgException(StrUtil.getNullStr(vsd.getString("validate_msg")));
	        	}
	        }
	        catch (ScriptException ex) {
	        	ex.printStackTrace();
	        }        	
        }
        // 执行验证脚本
		String script = vsd.getScript("validate");
		if (script != null && !script.equals("")) {
			Interpreter bsh = new Interpreter();
			try {
				StringBuffer sb = new StringBuffer();

				BeanShellUtil.setFieldsValue(fdao, sb);

				// 赋值用户
				sb.append("userName=\"" + privilege.getUser(request) + "\";");
				bsh.eval(BeanShellUtil.escape(sb.toString()));

				bsh.set("action", "save");
				bsh.set("fdao", fdao);
				bsh.set("request", request);
				bsh.set("fileUpload", fu);

				bsh.eval(script);
				Object obj = bsh.get("ret");
				if (obj != null) {
					boolean ret = ((Boolean) obj).booleanValue();
					if (!ret) {
						String errMsg = (String) bsh.get("errMsg");
						if (errMsg != null)
							throw new ErrMsgException("验证非法：" + errMsg);
						else {
							throw new ErrMsgException("验证非法，但errMsg为空！");
						}
					}
				} else {
					// throw new ErrMsgException("该节点脚本中未配置ret=...");
				}
			} catch (EvalError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}        

        fdao.setFields(fields);
        
        // 对函数型的表单域赋值
        Iterator irFunc = fields.iterator();
        while (irFunc.hasNext()) {
        	FormField ff = (FormField)irFunc.next();
        	if (ff.isFunc()) {
        		ff.setValue(String.valueOf(FuncUtil.render(ff, ff.getDefaultValueRaw(), fdao)));
        	}
        }

        fdao.setCreator(userName);
        
        re = fdao.save(request, fu);
        if (re) {
            if (ifv!=null)
                re = ifv.onUpdate(request, fdao);
            
            // 如果需要记录历史
            if (fd.isLog()) {
            	FormDAO.log(userName, FormDAOLog.LOG_TYPE_EDIT, fdao);
            }
            
            // 如果表单中含有“表单域选择宏控件”，且当前模块是该宏控件对应的表单的从模块，则自动关联
            if (true) {     	
                ModuleRelateDb mrd = new ModuleRelateDb();            	
	    		MacroCtlMgr mm = new MacroCtlMgr();            
	            Iterator ir = fields.iterator();
	            while (ir.hasNext()) {
	            	FormField ff = (FormField)ir.next();
	                if(ff.getType().equals(FormField.TYPE_MACRO)) {
	                	if (ff.getValue()==null || "".equals(ff.getValue())) {
	                		continue;
	                	}
	                	
	                	// 如果不是long类型的，则说明字段中存储的不是ID，字段对应的不可能是主模块
	                	long mainFormId = StrUtil.toLong(ff.getValue(), -1);
	                	if (mainFormId==-1) {
	                		continue;
	                	}
	                	                	
	    				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
	    				if (mu.isForm()) {
	    					String mainFormCode = mu.getFormCode();
	    					if ("".equals(mainFormCode)) {
	    						mainFormCode = StrUtil.getNullStr(mu.getIFormMacroCtl().getFormCode(request, ff));
	    					}
	    					if ("".equals(mainFormCode)) {
	    						continue;
	    					}
	    					
	    					boolean isRelateToMainForm = false;
	    					// 检查该mainFormCode是否为当前表单的主表单
	    	            	Iterator irRelated = mrd.getModulesRelated(mainFormCode).iterator();            	
	    	            	while (irRelated.hasNext()) {
	    	            		mrd = (ModuleRelateDb)irRelated.next();
	    	            		if (mrd.getString("relate_code").equals(formCode)) {
	    	            			isRelateToMainForm = true;
	    	            			break;
	    	            		}
	    	            	}
	    	            	if (isRelateToMainForm) {
	    	            		FormDAOMgr fdmMain = new FormDAOMgr(mainFormCode);
	    	            		String relateId = fdmMain.getRelateFieldValue(mainFormId, mrd.getString("relate_code"));
	    	            		if (relateId!=null && !relateId.equals(fdao.getCwsId())) {
		    	            		fdao.setCwsId(relateId);
		                            fdao.save();
	    	            		}
	    	            	}
	    				}
	                }
	            }
            }            
            
            // 发送消息
            sendRemindMsg(request, vsd, fdao, "save");
            
			script = vsd.getScript("save");
			LogUtil.getLog(getClass()).info(
					"update bsh formCode=" + formCode + " script=" + script);			
			if (script != null) {
				Interpreter bsh = new Interpreter();
				try {
					StringBuffer sb = new StringBuffer();
					BeanShellUtil.setFieldsValue(fdao, sb);

					// 赋值当前用户
					sb.append("userName=\"" + privilege.getUser(request)
							+ "\";");

					bsh.set("fdao", fdao);
					bsh.set("request", request);
					bsh.set("fileUpload", fu);

					bsh.eval(BeanShellUtil.escape(sb.toString()));

					bsh.eval(script);
					Object obj = bsh.get("ret");
					if (obj != null) {
						boolean ret = ((Boolean) obj).booleanValue();
						if (!ret) {
							String errMsg = (String) bsh.get("errMsg");
							LogUtil.getLog(getClass()).error(
									"update bsh errMsg=" + errMsg);
						}
					}
				} catch (EvalError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}                  
        }
        return re;
    }
    
    public boolean del(HttpServletRequest request) throws ErrMsgException {
    	return del(request, false, formCode);
    }

    public boolean del(HttpServletRequest request, boolean isNestSheet, String moduleCode) throws ErrMsgException {
        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(moduleCode);
        String userName = privilege.getUser(request);
        // 不检查，以免未配置权限时，嵌套表在流程中被删除时判断为权限非法，降低使用难度 2015-1-5
        // 如果是嵌套表，则根本配置是否需要检查权限来处理2015-1-16
        // isNestSheetCheckPrivilege已经不知道什么时候在config_oa.xml被删除了，所以下面的权限判断的代码已经无用了
        if (isNestSheet) {
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");
            if (isNestSheetCheckPrivilege) {
            	if (!mpd.canUserDel(userName) && !mpd.canUserManage(userName)) {
                    throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            	}
            }
        }
        else if (!mpd.canUserDel(userName) && !mpd.canUserManage(userName)) {
            throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
        }

        String ids = ParamUtil.get(request, "id");
        String[] aryIds = StrUtil.split(ids, ",");
        if (aryIds==null)
            throw new ErrMsgException("缺少标识！");

        ModuleSetupDb vsd = new ModuleSetupDb();
        vsd = vsd.getModuleSetupDbOrInit(formCode);
		
        int len = aryIds.length;
        boolean re = true;
        for (int i=0; i<len; i++) {
            visualObjId = StrUtil.toInt(aryIds[i]);
            FormDAO fdao = getFormDAO(visualObjId);
            if (!fdao.isLoaded()) {
                throw new ErrMsgException("该项已不存在！");
            }
            Config cfg = new Config();
            IModuleChecker imc = cfg.getIModuleChecker(formCode);
            if (imc != null)
                re = imc.validateDel(request, fdao);
            if (!re) {
                throw new ErrMsgException("验证非法！");
            }
            
            // 执行验证脚本
    		String script = vsd.getScript("validate");
    		if (script != null && !script.equals("")) {
    			Interpreter bsh = new Interpreter();
    			try {
    				StringBuffer sb = new StringBuffer();

    				BeanShellUtil.setFieldsValue(fdao, sb);

    				// 赋值用户
    				sb.append("userName=\"" + privilege.getUser(request) + "\";");
    				bsh.eval(BeanShellUtil.escape(sb.toString()));

    				bsh.set("action", "delete");
    				bsh.set("fdao", fdao);
    				bsh.set("request", request);
    				bsh.set("fileUpload", fu);

    				bsh.eval(script);
    				Object obj = bsh.get("ret");
    				if (obj != null) {
    					boolean ret = ((Boolean) obj).booleanValue();
    					if (!ret) {
    						String errMsg = (String) bsh.get("errMsg");
    						if (errMsg != null)
    							throw new ErrMsgException("验证非法：" + errMsg);
    						else {
    							throw new ErrMsgException("验证非法，但errMsg为空！");
    						}
    					}
    				} else {
    					// throw new ErrMsgException("该节点脚本中未配置ret=...");
    				}
    			} catch (EvalError e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}                    
            
            re = fdao.del();
            if (re) {
                // 如果需要记录历史
                if (fd.isLog()) {
                	FormDAO.log(userName, FormDAOLog.LOG_TYPE_DEL, fdao);  
                }
                
                if (imc != null) {
                    re = imc.onDel(request, fdao);
                }
                
                // 发送消息
                sendRemindMsg(request, vsd, fdao, "del");
                
        		script = vsd.getScript("del");
    			if (script != null) {
    				Interpreter bsh = new Interpreter();
    				try {
    					StringBuffer sb = new StringBuffer();
    					BeanShellUtil.setFieldsValue(fdao, sb);

    					// 赋值当前用户
    					sb.append("userName=\"" + privilege.getUser(request)
    							+ "\";");

    					bsh.set("fdao", fdao);
    					bsh.set("request", request);
    					bsh.set("fileUpload", fu);

    					bsh.eval(BeanShellUtil.escape(sb.toString()));

    					bsh.eval(script);
    					Object obj = bsh.get("ret");
    					if (obj != null) {
    						boolean ret = ((Boolean) obj).booleanValue();
    						if (!ret) {
    							String errMsg = (String) bsh.get("errMsg");
    							LogUtil.getLog(getClass()).error(
    									"del bsh errMsg=" + errMsg);
    						}
    					}
    				} catch (EvalError e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}                
            }
        }
        return re;
    }
    
    public boolean batchOperate(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(formCode);
        String userName = privilege.getUser(request);
        // 需根据模块中配置的自定义按钮的权限判断
        // if (!mpd.canUserManage(userName)) {
        //    throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
        // }

        String ids = ParamUtil.get(request, "id");
        String[] aryIds = StrUtil.split(ids, ",");
        if (aryIds==null)
            throw new ErrMsgException("缺少标识！");

        ModuleSetupDb vsd = new ModuleSetupDb();
        vsd = vsd.getModuleSetupDbOrInit(formCode);
        
        String batchField = ParamUtil.get(request, "batchField");
        String batchValue = ParamUtil.get(request, "batchValue");
		
        int len = aryIds.length;
        boolean re = true;
        for (int i=0; i<len; i++) {
            visualObjId = StrUtil.toInt(aryIds[i]);
            FormDAO fdao = getFormDAO(visualObjId);
            if (!fdao.isLoaded()) {
                throw new ErrMsgException("该项已不存在！");
            }
            
            fdao.setFieldValue(batchField, batchValue);
            re = fdao.save();
            if (re) {
                // 如果需要记录历史
                if (fd.isLog()) {
                	FormDAO.log(userName, FormDAOLog.LOG_TYPE_EDIT, fdao);  
                }
            }
        }
        return re;
    }    

    /**
     * 取得与子模块相关的父模块的字段的值 @task:暂时无用
     * @param parentId int
     * @param mainField String
     * @return String
     */
    public String getFieldValueOfMain(long parentId, String mainField) {
        com.redmoon.oa.visual.FormDAO fdaoParent = getFormDAO(parentId);
        return fdaoParent.getFieldValue(mainField);
    }

    /**
     * 根据描述字符串，获得映射字段的值
     * @param fdao FormDAO
     * @param otherFieldDesc String
     * @return String
     */
    public static String getFieldValueOfOther(HttpServletRequest request, FormDAO fdao, String otherFieldDesc) {
        String[] ary = otherFieldDesc.split(":");
        String fieldName = ary[1];
        String fieldValue;
        if ("cws_id".equals(fieldName)) {
        	fieldValue = fdao.getCwsId();
        }
        else if ("id".equals(fieldName)) {
        	fieldValue = String.valueOf(fdao.getId());
        }
        else {
        	fieldValue = fdao.getFieldValue(fieldName);
        }
        
        // 如果沒有多重映射，即隻有一道映射，則允許顯示多條記錄，以逗號分隔
        if (ary.length == 5) {
	        String formCode = "";
	        String showFieldName = "";
	        for (int i=2; i<ary.length; i+=3) {
	            formCode = ary[i];
	            fieldName = ary[i+1];
	            showFieldName = ary[i+2];
	        }        
	        FormDb fd = new FormDb();
	        fd = fd.getFormDb(formCode);
            return getFieldValueOfOtherMulti(request, fd, fieldValue, fieldName, showFieldName);
        }
        else {
	        String formCode = "";
	        String showFieldName = "";
	        for (int i=2; i<ary.length; i+=3) {
	            formCode = ary[i];
	            fieldName = ary[i+1];
	            showFieldName = ary[i+2];
	            // System.out.println(FormDAOMgr.class + "  i=" + i + " fieldValue=" + fieldValue + " formCode=" + formCode + " fieldName=" + fieldName + " showFieldName=" + showFieldName);
	        	// cws_id=null:nr fieldName=cws_id fieldValue=null showFieldName=nr
	
	            FormDAOMgr fdaom = new FormDAOMgr(formCode);
	            fieldValue = fdaom.getFieldValueOfOther(fieldValue, fieldName, showFieldName);
	        }
	
	        LogUtil.getLog(FormDAOMgr.class).info("getFieldValueOfOther:formCode=" +
	                                              formCode + " fieldName=" + fieldName +
	                                                  " ary.length=" + ary.length);
	        if (!showFieldName.equals("id")) {
	            FormDb fd = new FormDb();
	            fd = fd.getFormDb(formCode);
	
	            FormField ff = fd.getFormField(showFieldName);
	            if (ff!=null && ff.getType().equals(FormField.TYPE_MACRO)) {
	                String macroType = ff.getMacroType();
	                MacroCtlMgr mcm = new MacroCtlMgr();
	                MacroCtlUnit mcu = mcm.getMacroCtlUnit(macroType);
	        		RequestUtil.setFormDAO(request, fdao);	                
	                fieldValue = mcu.getIFormMacroCtl().converToHtml(request, ff,
	                        fieldValue);
	            }
	        }
	        return StrUtil.getNullStr(fieldValue);
        }
    }
    
    public static String getFieldValueOfOtherMulti(HttpServletRequest request, FormDb fd, String fieldValue, String fieldName, String showFieldName) {
        Object[] objs = new Object[1];

        StringBuffer ids = new StringBuffer();
        long id = -1;
        try {
            FormField ff = null;
            if (!"id".equals(fieldName) && !"cws_id".equals(fieldName)) {
	            ff = fd.getFormField(fieldName);
	            if (ff==null) {
	            	return "字段" + fieldName + "不存在！";
	            }
            }
            
            String sql = "select id from " + fd.getTableNameByForm() +
                         " where " + fieldName + "=? order by id desc";
            if (fieldName.equals("id")) {
                long myId = StrUtil.toLong(fieldValue, -65536);
                if (myId==-65536)
                    return null;
                objs[0] = new Long(myId);
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getLong(1);
                    StrUtil.concat(ids, ",", String.valueOf(id));
                }
            }
            else if (fieldName.equals("cws_id")) {
                objs[0] = new String(fieldValue);
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getLong(1);
                    StrUtil.concat(ids, ",", String.valueOf(id));                    
                }            	
            }
            else if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR ||
                ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
                objs[0] = fieldValue;
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getInt(1);
                    StrUtil.concat(ids, ",", String.valueOf(id));                    
                }
            } else if (ff.getFieldType() == FormField.FIELD_TYPE_DATE) {
                objs[0] = DateUtil.parse(fieldValue, "yyyy-MM-dd");
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getInt(1);
                    StrUtil.concat(ids, ",", String.valueOf(id));                    
                }
            } else if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
                objs[0] = DateUtil.parse(fieldValue, "yyyy-MM-dd HH:mm:ss");
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getInt(1);
                    StrUtil.concat(ids, ",", String.valueOf(id));                    
                }
            } else {
                sql = "select id from " + fd.getTableNameByForm() + " where " +
                      fieldName + "=" + fieldValue;
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql);
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getInt(1);
                    StrUtil.concat(ids, "，", String.valueOf(id));                    
                }
            }
        }
        catch(SQLException e) {
            LogUtil.getLog(FormDAOMgr.class).error(StrUtil.trace(e));
        }
        if (id==-1)
            return null;
        
        StringBuffer sb = new StringBuffer();
        String[] ary = StrUtil.split(ids.toString(), ",");      
        FormDAO formDAO = new FormDAO();
        for (String strId : ary){ 
        	id = StrUtil.toLong(strId);
	        FormDAO fdao = formDAO.getFormDAO(id, fd);
	        String val = fdao.getFieldValue(showFieldName);
	        if (!showFieldName.equals("id")) {
	            FormField ff = fd.getFormField(showFieldName);
	            if (ff!=null && ff.getType().equals(FormField.TYPE_MACRO)) {
	                String macroType = ff.getMacroType();
	                MacroCtlMgr mcm = new MacroCtlMgr();
	                MacroCtlUnit mcu = mcm.getMacroCtlUnit(macroType);
	    			RequestUtil.setFormDAO(request, fdao);
	                val = mcu.getIFormMacroCtl().converToHtml(request, ff, val);
	            }
	        }
	        StrUtil.concat(sb, ",", val);
        }
        return sb.toString();
    }    

    /**
     * 取得表中字段为指定值的其它字段的值，用于在模块列表中显示与其相关的其它表的字段的值
     * @param fieldValue String 指定字段的值
     * @param fieldName String 指定字段的名称
     * @param showFieldName String 将要显示的字段的名称
     * @return String
     */
    public String getFieldValueOfOther(String fieldValue, String fieldName, String showFieldName) {
        LogUtil.getLog(getClass()).info(fieldName + "=" + fieldValue + ":" + showFieldName);

        Object[] objs = new Object[1];

        long id = -1;
        try {
            FormField ff = null;
            if (!"id".equals(fieldName) && !"cws_id".equals(fieldName)) {
	            ff = fd.getFormField(fieldName);
	            if (ff==null) {
	            	LogUtil.getLog(getClass()).error("getFieldValueOfOther:表单：" + fd.getCode() + "中的字段" + fieldName + " 不存在！");
	            	return "字段" + fieldName + "不存在！";
	            }
            }
            
            String sql = "select id from " + fd.getTableNameByForm() +
                         " where " + fieldName + "=? order by id desc";
            if (fieldName.equals("id")) {
                long myId = StrUtil.toLong(fieldValue, -65536);
                if (myId==-65536)
                    return null;
                objs[0] = new Long(myId);
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getLong(1);
                }
            }
            else if (fieldName.equals("cws_id")) {
                objs[0] = new String(fieldValue);
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getLong(1);
                }            	
            }
            else if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR ||
                ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
                objs[0] = fieldValue;
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getInt(1);
                }
            } else if (ff.getFieldType() == FormField.FIELD_TYPE_DATE) {
                objs[0] = DateUtil.parse(fieldValue, "yyyy-MM-dd");
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getInt(1);
                }
            } else if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
                objs[0] = DateUtil.parse(fieldValue, "yyyy-MM-dd HH:mm:ss");
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, objs);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getInt(1);
                }
            } else {
                sql = "select id from " + fd.getTableNameByForm() + " where " +
                      fieldName + "=" + fieldValue;
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    id = rr.getInt(1);
                }
            }
        }
        catch(SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        if (id==-1)
            return null;

        visualObjId = id;
        
        FormDAO fdao = getFormDAO(id);
        return fdao.getFieldValue(showFieldName);
    }

    /**
     * 取得父模块中被关联字段的值
     * @param parentId int 父模块记录的ID
     * @param moduleCodeRelated String 关联模块的编码
     * @return String
     */
    public String getRelateFieldValue(long parentId, String moduleCodeRelated) {
        String relateFieldValue = null;

        ModuleSetupDb msdParent = new ModuleSetupDb();
        msdParent = msdParent.getModuleSetupDbOrInit(formCode);

        com.redmoon.oa.visual.FormDAO fdaoParent = getFormDAO(parentId);
        // LogUtil.getLog(getClass()).info("relateField=" + relateField + " relateLen=" + relateLen);
        ModuleRelateDb mrd = new ModuleRelateDb();
        Iterator ir = mrd.getModulesRelated(formCode).iterator();
        while (ir.hasNext()) {
            mrd = (ModuleRelateDb)ir.next();
            // 201807 fgf 改为了可以关联副模块
            if (mrd.getString("relate_code").equals(moduleCodeRelated)) {
                String field = mrd.getString("relate_field");
                if (field.equals("id")) {
                    relateFieldValue = "" + fdaoParent.getId();
                } else if (field.equals("cws_id")) {
                    relateFieldValue = "" + fdaoParent.getCwsId();
                } else {
                    relateFieldValue = fdaoParent.getFieldValue(field);
                }
                break;
            }
        }

        return relateFieldValue;
    }

}
