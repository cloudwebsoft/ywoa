package com.redmoon.oa.visual;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.util.*;

import com.cloudwebsoft.framework.util.*;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.*;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;

import cn.js.fan.util.file.FileUtil;
import com.redmoon.oa.base.IFormMacroCtl;

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
public class Render {
    HttpServletRequest request;
    FormDb fd;
    long visualObjId;
    ModuleSetupDb msd;

    public static String FORM_FLEMENT_ID = "visualForm";
    
    /**
     * 不可写字段
     */
    Vector vdisable;
    ModulePrivDb mpd;

    public Render(HttpServletRequest request, FormDb fd) {
        this.request = request;
        this.fd = fd;
    }

    public Render(HttpServletRequest request, long visualObjId, FormDb fd) {
        this.request = request;
        this.fd = fd;
        this.visualObjId = visualObjId;
    }
    
    public String getContentMacroReplaced(FormDAO fdao, String content, Vector fields) {
    	vdisable = new Vector();
    	
        if (msd!=null) {
	    	// 置editable及hidden，因为在宏控件convertToHTML时需用到
	    	String userName = new Privilege().getUser(request);
	        // 取得当前用户的可写字段
	    	String moduleCode = msd.getString("code");
	        mpd = new ModulePrivDb(moduleCode);
	        Iterator ir = null;
	        // 与
	       	String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
	       	if (fieldWrite!=null && !"".equals(fieldWrite)) {
		        String[] fds = StrUtil.split(fieldWrite, ",");
		        if (fds!=null) {
		        	int len = fds.length;
		            // 将不可写的域筛选出
		            ir = fields.iterator();
		            while (ir.hasNext()) {
		                FormField ff = (FormField)ir.next();
	
		                boolean finded = false;
		                for (int i=0; i<len; i++) {
		                    if (ff.getName().equals(fds[i])) {
		                        finded = true;
		                        break;
		                    }
		                }
	
		                if (!finded) {
		                    // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
    	                    vdisable.addElement(ff);
		                    ff.setEditable(false);
		                    // System.out.println(getClass() + " " + ff.getName() + " false");
		                }
		            }     
		        }
	       	}
	        
	       	String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
	        String[] fdsHide = StrUtil.split(fieldHide, ",");   
	        if (fdsHide!=null) {
	        	ir = fields.iterator();
	        	while (ir.hasNext()) {
	        		FormField ffHide = (FormField)ir.next();
		        	for (String hideFieldName : fdsHide) {
		        		if (ffHide.getName().equals(hideFieldName)) {
		        			ffHide.setHidden(true);
		        			break;
		        		}
		        	}
	        	}
	        }        
	        
            // 测试发现需隐藏时，必须得可写，所以要把隐藏字段从不可写字段中去掉
	        // 从不可写字段中去掉隐藏字段
	        if (fdsHide!=null) {
	            ir = vdisable.iterator();
	            while(ir.hasNext()) {
	                FormField ff = (FormField) ir.next();
	                for (int k=0; k<fdsHide.length; k++) {
		                if (ff.getName().equals(fdsHide[k])) {
		                	ir.remove();
		                }
	                }
	            }
	        }	        
        }
            	
        Iterator ir = fields.iterator();
        MacroCtlMgr mm = new MacroCtlMgr();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            // System.out.println("oa.visual.Render -- getContentMacroReplaced:" + ff.getMacroType());
            if (!ff.getMacroType().equals(FormField.MACRO_NOT)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu == null)
                    throw new IllegalArgumentException("Macro ctl " + ff.getTitle() + " is not exist.");
                else {
                    IFormMacroCtl ifmc = mu.getIFormMacroCtl();
                    if (ifmc==null) {
                       throw new IllegalArgumentException("Macro ctl " + ff.getMacroType() + "'s IFormMacroCtl is not found.");
                    }
                    else {
                        // 传入fdao备用
                        ifmc.setVisualFormDAO(fdao);                        	
                        content = ifmc.replaceMacroCtlWithHTMLCtl(request, ff, content);
                    }
                }
            }
        }    	
        return content;
    }    

    public String getContentMacroReplaced(FormDAO fdao, FormDb fd, Vector fields) {
        // 替换content中的宏控件
        String content = fd.getContent();
        // System.out.println("oa.visula.Render -- getContentMacroReplaced:" + content);

        // FileUtil.WriteFile("c:/3.txt", content, "utf-8");


        // FileUtil.WriteFile("c:/4.txt", content, "utf-8");
        return getContentMacroReplaced(fdao, content, fields);
    }

    public String replaceDefaultStr(String content) {
        content = content.replaceFirst("#\\[表单\\]", fd.getName());

        String pat = "\\{\\$rootPath\\}";
        content = content.replaceAll(pat, request.getContextPath());

        return content;
    }
    
    public String rendForAdd() {
    	FormDAO fdao = null;
        String content = getContentMacroReplaced(fdao, fd, fd.getFields());
        return rendForAdd(content, fd.getFields());
    }
    
    /**
     * 添加
     * @param msd
     * @return
     */
    public String rendForAdd(ModuleSetupDb msd) {
    	this.msd = msd;

    	String code = msd.getString("code");
    	String formCode = msd.getString("form_code");
    	// 如果是主表单
    	if (code.equals(formCode)) {
    		// return rendForAdd();
    	}
    	
    	try {
			License.getInstance().checkSolution(formCode);
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			return e.getMessage();
		}    	
    	
    	int viewEdit = msd.getInt("view_edit");
    	if (viewEdit==ModuleSetupDb.VIEW_DEFAULT || viewEdit==ModuleSetupDb.VIEW_EDIT_CUSTOM) {
    		return rendForAdd();
    	}
    	
    	FormViewDb fvd = new FormViewDb();
    	fvd = fvd.getFormViewDb(viewEdit);
    	if (fvd==null) {
    		LogUtil.getLog(getClass()).error("视图ID=" + viewEdit + "不存在");
    	}
    	        
        String form = fvd.getString("form");
        // Vector fields = fdao.getFields();

        String ieVersion = fvd.getString("ie_version");
    	FormParser fp = new FormParser();
    	Vector fields = fp.parseCtlFromView(fvd.getString("content"), ieVersion, fd);
        
        // String content = doc.getContent(1); // 取得表单内容
        String content = getContentMacroReplaced(null, form, fields);    
        
        return rendForAdd(content, fields);
    }
    
    public String rendForAdd(String content, Vector fields) {
        // String content = doc.getContent(1); // 取得表单内容

        // String formId = "flowForm";
        content = replaceDefaultStr(content);
        // 置默认值
        String str = "";

        MacroCtlMgr mm = new MacroCtlMgr();

        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            // str += FormField.getOuterHTMLOfElementWithRAWValue(request, ff);

            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null) {
                    str += mu.getIFormMacroCtl().getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request,
                            ff);
                }
            }
            else {
                str += FormField.getOuterHTMLOfElementsWithRAWValueAndHTMLValueForVisualAdd(request, ff);
            }
        }

        str += "\n<script>\n";
        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                java.util.Date dt = null;
                try {
                	if (ff.getDefaultValueRaw().equals(FormField.DATE_CURRENT)) {
                        dt = new java.util.Date();
                	} else {
                		dt = DateUtil.parse(ff.getDefaultValue(), 
                				FormField.FORMAT_DATE_TIME);
                	}
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error("Render.java rendForAdd:" + e.getMessage());
                }
                String d = DateUtil.format(dt, FormField.FORMAT_DATE);
                String t = DateUtil.format(dt, "HH:mm:ss");
//                str += "setCtlValue('" + ff.getName() + "', '" + ff.getType() +
//                        "', '" +
//                        d + "');\n";
//                str += "setCtlValue('" + ff.getName() + "_time" + "', '" +
//                        ff.getType() +
//                        "', '" +
//                        t + "');\n";
				str += "if (o('" + ff.getName()
						+ "_time')!= null){setCtlValue('" + ff.getName()
						+ "', '" + ff.getType() + "', '" + d + "');\n"
						+ "setCtlValue('" + ff.getName() + "_time" + "', '"
						+ ff.getType() + "', '" + t
						+ "');\n}else{setCtlValue('" + ff.getName() + "', '"
						+ ff.getType() + "', '" + DateUtil.format(dt, FormField.FORMAT_DATE_TIME) + "');\n}";
			} else if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null) {
                    str += mu.getIFormMacroCtl().getSetCtlValueScript(request, null, ff, FORM_FLEMENT_ID);
                }
            } else {
                str += FormField.getSetCtlValueScript(request, null, ff, FORM_FLEMENT_ID);
            }
        }

        str += "</script>\n";

        // 当在nest_sheet_edit_relate.jsp页编辑时，即在流程中编辑嵌套表格2的记录时
        String pageKind = (String)request.getAttribute("pageKind");
    	long actionId = StrUtil.toInt(StrUtil.getNullStr((String)request.getAttribute("actionId")), -1);
        if ("nest_sheet_relate".equals(pageKind) && actionId!=-1) {
			WorkflowActionDb wfa = new WorkflowActionDb();
			wfa = wfa.getWorkflowActionDb((int)actionId);
			try {
				str += rendNestSheetCtlRelated(wfa, fields);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        // 在智能模块中
    	if (actionId==-1) {
    		str += "<script>\n";
    		String userName = new Privilege().getUser(request);
            // 取得当前用户的隐藏字段
    		// 取得当前用户的可写字段
        	String moduleCode = "";
        	// msd在rend(ModuleSetupDb msd)中赋值
        	if (msd!=null) {
        		moduleCode = msd.getString("code");
        	}
        	else {
        		moduleCode = fd.getCode();
        	}
            ModulePrivDb mpd = new ModulePrivDb(moduleCode);    		
           	String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
            String[] fdsHide = StrUtil.split(fieldHide, ","); 
            
            // 测试发现需隐藏时，必须得可写，所以要把隐藏字段从不可写字段中去掉
           	String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
           	if (!"".equals(fieldWrite)) {
                Vector vdisable = new Vector();
    	        String[] fds = StrUtil.split(fieldWrite, ",");
    	        if (fds!=null) {
    	        	int len = fds.length;
    	        	
    	            // 将不可写的域筛选出
    	            ir = fields.iterator();
    	            while (ir.hasNext()) {
    	                FormField ff = (FormField)ir.next();

    	                boolean finded = false;
    	                for (int i=0; i<len; i++) {
    	                    if (ff.getName().equals(fds[i])) {
    	                        finded = true;
    	                        break;
    	                    }
    	                }

    	                if (!finded) {
    	                    vdisable.addElement(ff);
    	                    // 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
    	                    ff.setEditable(false);
    	                }
    	            }        	        	
    	        }
    	        else {
    	        	// 全部不可写
    	        	vdisable = fields;
    	        }
    	        
    	        // 从不可写字段中去掉隐藏字段
    	        if (fdsHide!=null) {
    	            ir = vdisable.iterator();
    	            while(ir.hasNext()) {
    	                FormField ff = (FormField) ir.next();
    	                for (int k=0; k<fdsHide.length; k++) {
    		                if (ff.getName().equals(fdsHide[k])) {
    		                	ir.remove();
    		                }
    	                }
    	            }
    	        }
                
                ir = vdisable.iterator();
                while (ir.hasNext()) { 
                    FormField ff = (FormField) ir.next();
                    LogUtil.getLog(getClass()).info("不可写字段" + ff.getName() + ":" + ff.getTitle());
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu != null) {
                            str += mu.getIFormMacroCtl().getDisableCtlScript(ff, FORM_FLEMENT_ID);
                        }
                    }
                    else {
                        str += FormField.getDisableCtlScript(ff, FORM_FLEMENT_ID);
                    }
                }	        
           	}            
            
            int len = 0;
            if (fdsHide!=null) {
            	len = fdsHide.length;
            }
            for (int i=0; i<len; i++) {
                if (!fdsHide[i].startsWith("nest.")) {
                	FormField ff = fd.getFormField(fdsHide[i]);
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu != null) {
                            str += mu.getIFormMacroCtl().getHideCtlScript(ff, FORM_FLEMENT_ID);
                        }
                    }
                    else {            	
                    	str += FormField.getHideCtlScript(ff, FORM_FLEMENT_ID);
                    }
                }
                else {
                    str += "try{ hideNestTableCol('" + fdsHide[i].substring("nest.".length()) +  "'); }catch(e) {}\n";
                    str += "try{ hideNestSheetCol('" + fdsHide[i].substring("nest.".length()) +  "'); }catch(e) {}\n";
                }
            }    		
    		
            str += "</script>\n";
            
	        // 仅验证可写字段，且需去除隐藏字段
	       	// String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
	       	if (fieldWrite!=null) {
	       		if (!fieldWrite.equals("")) {
	       			String[] fieldAry = StrUtil.split(fieldWrite, ",");
	       			if (fieldAry!=null) {
	       				fields = new Vector();
	       				for (int k=0; k<fieldAry.length; k++) {
	       					fields.addElement(fd.getFormField(fieldAry[k]));
	       				}
	       			}
	       		}
	       	}
	       	// 去除隐藏字段
	       	if (fdsHide!=null) {
	       		for (int i=0; i<fdsHide.length; i++) {	       			
	       			ir = fields.iterator();
	       			while (ir.hasNext()) {
	       				FormField ff = (FormField)ir.next();
	       				if (ff==null) {
	       					continue;
	       				}
	       				if (fdsHide[i].equals(ff.getName())) {
	       					ir.remove();
	       				}
	       			}
	       		}
	       	}
    	}
       	
        str += FormUtil.doGetCheckJS(request, fields);
        
        // 取得函数的参数中所关联的表单域当值发生改变时，重新获取值的脚本
        str += FuncUtil.doGetFieldsRelatedOnChangeJS(fd);
                
        // 显示或隐藏表单中的区域
        str += ModuleUtil.doGetViewJS(request, fd, null, new Privilege().getUser(request), false);

		str += FormUtil.doGetCheckJSUnique(-1, fields);

		// 一米OA签名
        if (License.getInstance().isFree()) {
        	content += License.getFormWatermark();
        }
        
        return content + str;
    }

    /**
     * 可能已经用不着了
     * @return
     */
    public String rend() {
        return rend(FORM_FLEMENT_ID);
    }
    
    /**
     * 编辑时渲染
     * @param msd
     * @return
     */
    public String rend(ModuleSetupDb msd) {    	
    	this.msd = msd;
    	
    	String code = msd.getString("code");
    	String formCode = msd.getString("form_code");
    	// 如果是主表单
    	if (code.equals(formCode)) {
    		// return rend();
    	}
    	
    	try {
			License.getInstance().checkSolution(formCode);
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			return e.getMessage();
		}    	
    			
    	int viewEdit = msd.getInt("view_edit");
    	if (viewEdit==ModuleSetupDb.VIEW_DEFAULT || viewEdit==ModuleSetupDb.VIEW_EDIT_CUSTOM)
    		return rend();
    	
    	FormViewDb fvd = new FormViewDb();
    	fvd = fvd.getFormViewDb(viewEdit);
    	if (fvd==null) {
    		LogUtil.getLog(getClass()).error("视图ID=" + viewEdit + "不存在");
    	}
    	
        FormDAOMgr fdm = new FormDAOMgr(fd);
        FormDAO fdao = fdm.getFormDAO(visualObjId);
        
        String cnt = fvd.getString("content");
        // Vector fields = fdao.getFields();

        String ieVersion = fvd.getString("ie_version");
    	FormParser fp = new FormParser();
    	Vector fields = fp.parseCtlFromView(cnt, ieVersion, fd);
    	
    	// 将fields改为已fdao中已取得值的FormField
    	Vector v = new Vector();
    	Iterator ir = fields.iterator();
    	while (ir.hasNext()) {
    		FormField ff = (FormField)ir.next();
    		v.addElement(fdao.getFormField(ff.getName()));
    	}
    	
    	fields = v;
       	
        // String content = doc.getContent(1); // 取得表单内容
        String content = getContentMacroReplaced(fdao, fvd.getString("form"), fields);   
        String str = rend(fdao, FORM_FLEMENT_ID, content, fields);
        
        return str;
    }
    
    public String rend(FormDAO fdao, String formElementId, String content, Vector fields) {
    	// String formId = "flowForm";
        content = replaceDefaultStr(content);
        // 置用户已操作的值
        String str = "";
        MacroCtlMgr mm = new MacroCtlMgr();

        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            // str += FormField.getOuterHTMLOfElementWithRAWValue(request, ff);
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null) {
                    str += mu.getIFormMacroCtl().getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
                }
            }
            else {
                str += FormField.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
            }
        }

        str += "\n<script>\n";
        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                java.util.Date dt = null;
                if (StrUtil.getNullStr(ff.getValue()).equals("")) {
	                try {
	                	if (ff.getDefaultValueRaw().equals(FormField.DATE_CURRENT)) {
	                        dt = new java.util.Date();
	                	} else {
	                		dt = DateUtil.parse(ff.getDefaultValue(), 
	                				FormField.FORMAT_DATE_TIME);
	                	}
	                } catch (Exception e) {
	                    LogUtil.getLog(getClass()).error("rend1:" + e.getMessage());
	                }
                } else {
                	dt = DateUtil.parse(ff.getValue(), FormField.FORMAT_DATE_TIME);
                }
                String d = DateUtil.format(dt, FormField.FORMAT_DATE);
                String t = DateUtil.format(dt, "HH:mm:ss");
//                str += "setCtlValue('" + ff.getName() + "', '" + ff.getType() +
//                        "', '" +
//                        d + "');\n";
//                str += "setCtlValue('" + ff.getName() + "_time" + "', '" +
//                        ff.getType() +
//                        "', '" +
//                        t + "');\n";
//				str += "if (o('" + ff.getName()
//						+ "_time')!= null){setCtlValue('" + ff.getName()
//						+ "', '" + ff.getType() + "', '" + d + "');\n"
//						+ "setCtlValue('" + ff.getName() + "_time" + "', '"
//						+ ff.getType() + "', '" + t
//						+ "');\n}else{setCtlValue('" + ff.getName() + "', '"
//						+ ff.getType() + "', '" + DateUtil.format(dt, FormField.FORMAT_DATE_TIME) + "');\n}";
                str += "setCtlValue('" + ff.getName() + "', '"
						+ ff.getType() + "', '" + DateUtil.format(dt, FormField.FORMAT_DATE_TIME) + "');\n";
                
            } else if (ff.getType().equals(FormField.TYPE_MACRO)) {
                // 2011-4-13 在制作RatyCtl时，发现缺少对宏控件getSetCtlValueScript的处理
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                LogUtil.getLog(getClass()).info(ff.getTitle() + " name=" + ff.getName() + " macroType=" + ff.getMacroType() + " mu=" + mu);
                if (mu != null) {
                    str += mu.getIFormMacroCtl().getSetCtlValueScript(request, fdao, ff, formElementId);
                }
                else {
                    LogUtil.getLog(getClass()).error(ff.getTitle() + " macroType=" + ff.getMacroType() + " 宏控件不存在！");
                }
            } else {
                str +=
                        FormField.getSetCtlValueScript(request, fdao, ff, formElementId);
            }
        }
        
        // 处理在字段管理中设定的隐藏字段
        ir = fields.iterator();
        while (ir.hasNext()) {
        	FormField ff = (FormField)ir.next();
            if (ff.getHide()!=FormField.HIDE_NONE) {
            	// @task:此处可能有问题，fields字段名不可能含有nest.
                if (!ff.getName().startsWith("nest.")) {
                   	str += FormField.getHideCtlScript(ff, FORM_FLEMENT_ID);
                }
                else {
                    str += "try{ hideNestTableCol('" + ff.getName().substring("nest.".length()) +  "'); }catch(e) {}\n";
                    str += "try{ hideNestSheetCol('" + ff.getName().substring("nest.".length()) +  "'); }catch(e) {}\n";
                }
            }
        }
        
        str += "</script>\n";
                
        // 当在nest_sheet_edit_relate.jsp页编辑时，即在流程中编辑嵌套表格2的记录时
        String pageKind = (String)request.getAttribute("pageKind");
    	long actionId = StrUtil.toInt(StrUtil.getNullStr((String)request.getAttribute("actionId")), -1);
        if ("nest_sheet_relate".equals(pageKind)) {
			if (actionId!=-1) {
				WorkflowActionDb wfa = new WorkflowActionDb();
				wfa = wfa.getWorkflowActionDb((int)actionId);
				try {
					str += rendNestSheetCtlRelated(wfa, fdao.getFields());
				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}  
        }

        if (actionId==-1) {
        	str += "<script>\n";
        	
        	String userName = new Privilege().getUser(request);
            // 取得当前用户的可写字段
        	String moduleCode = "";
        	// msd在rend(ModuleSetupDb msd)中赋值
        	if (msd!=null) {
        		moduleCode = msd.getString("code");
        	}
        	else {
        		moduleCode = fdao.getFormCode();
        	}

        	int len = 0;    

            ir = vdisable.iterator();
            while (ir.hasNext()) { 
                FormField ff = (FormField) ir.next();
                LogUtil.getLog(getClass()).info("不可写字段" + ff.getName() + ":" + ff.getTitle());
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        str += mu.getIFormMacroCtl().getDisableCtlScript(ff, FORM_FLEMENT_ID);
                    }
					else {
						System.out.println("render:" + ff.getTitle() + " " + ff.getName() + " 宏控件不存在");
						LogUtil.getLog(getClass()).error(ff.getTitle() + " " + ff.getName() + " 宏控件不存在");
					}
                }
                else {
                    str += FormField.getDisableCtlScript(ff, FORM_FLEMENT_ID);
                }
            }	        
            
            mpd = new ModulePrivDb(moduleCode);
            
           	String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
           	String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
            String[] fdsHide = StrUtil.split(fieldHide, ",");  
            len = 0;
            if (fdsHide!=null) {
            	len = fdsHide.length;
            }
            for (int i=0; i<len; i++) {
                if (!fdsHide[i].startsWith("nest.")) {
                	FormField ff = fd.getFormField(fdsHide[i]);
                	if (ff==null) {
                		LogUtil.getLog(getClass()).error("隐藏字段：" + fdsHide[i] + "不存在！" );
                		continue;
                	}
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu != null) {
                            str += mu.getIFormMacroCtl().getHideCtlScript(ff, FORM_FLEMENT_ID);
                        }
                    }
                    else {
                    	str += FormField.getHideCtlScript(ff, FORM_FLEMENT_ID);
                    }
                }
                else {
                    str += "try{ hideNestTableCol('" + fdsHide[i].substring("nest.".length()) +  "'); }catch(e) {}\n";
                    str += "try{ hideNestSheetCol('" + fdsHide[i].substring("nest.".length()) +  "'); }catch(e) {}\n";
                }
            }
            
            str += "</script>\n";

        	// 仅验证可写字段，且需去除隐藏字段
			String[] fieldAry = StrUtil.split(fieldWrite, ",");
			if (fieldAry!=null) {
				fields = new Vector();
				for (int k=0; k<fieldAry.length; k++) {
					FormField ffWrite = fd.getFormField(fieldAry[k]);
					if (ffWrite!=null) {
						fields.addElement(fd.getFormField(fieldAry[k]));
					}
				}
			}

	        // 去掉隐藏字段的前台验证
	       	if (fdsHide!=null) {
	       		for (int i=0; i<fdsHide.length; i++) {
	       			ir = fields.iterator();
	       			while (ir.hasNext()) {
	       				FormField ff = (FormField)ir.next();
	       				if (fdsHide[i].equals(ff.getName())) {
	       					ir.remove();
	       				}
	       			}
	       		}
	       	}     
	       	
	        // 去掉图片、文件宏控件的前台验证
			// 因为图片、文件宏控件在编辑时，应该允许为空	       	
	       	ir = fields.iterator();
   			while (ir.hasNext()) {
   				FormField ff = (FormField)ir.next();
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    IFormMacroCtl ifc = mu.getIFormMacroCtl();
                    if ( ifc instanceof AttachmentCtl
                    		|| ifc instanceof AttachmentsCtl
                    		|| ifc instanceof ImageCtl
                    ) {
                    	ir.remove();
                    }
   				}
   			}	       	
        }
        
        // 取得前台校验脚本
        str += FormUtil.doGetCheckJS(request, fields);
        
        // 取得函数的参数中所关联的表单域当值发生改变时，重新获取值的脚本
        str += FuncUtil.doGetFieldsRelatedOnChangeJS(fdao.getFormDb());
        
        // 显示或隐藏表单中的区域
        str += ModuleUtil.doGetViewJS(request, fd, fdao, new Privilege().getUser(request), false);

		str += FormUtil.doGetCheckJSUnique(fdao.getId(), fields);

		// 一米OA签名
        if (License.getInstance().isFree()) {
        	content += License.getFormWatermark();
        }
        
        return content + str;    
    }
    
    /**
     * 禁用嵌套表格2中的不可写字段
     * @param wfa 流程的action
     * @param vNestFormField 嵌套表格中的字段
     * @return String
     */
	public String rendNestSheetCtlRelated(WorkflowActionDb wfa, Vector vNestFormField)
			throws ErrMsgException {
		String fieldWrite = StrUtil.getNullString(wfa.getFieldWrite()).trim();

		String[] fds = fieldWrite.split(",");
		int len = fds.length;

		int nestLen = "nest.".length();
		// 将嵌套表中不可写的域筛选出
		Iterator ir = vNestFormField.iterator();
		Vector vdisable = new Vector();
		while (ir.hasNext()) {
			FormField ff = (FormField) ir.next();

			boolean finded = false;
			for (int i = 0; i < len; i++) {
	            // 如果不是嵌套表格2的可写表单域
                if (!fds[i].startsWith("nest.")) {
                    continue;
                }
                String fName = fds[i].substring(nestLen);
				if (ff.getName().equals(fName)) {
					finded = true;
					break;
				}
			}

			if (!finded) {
				vdisable.addElement(ff);
				// 置为不能编辑，以使得CKEditorCtl初始化时，不转变为编辑器
				ff.setEditable(false);
			}
		}

		String str = "";
		MacroCtlMgr mm = new MacroCtlMgr();
		// 根据用户不可写的表单域的type，name，插入相应的JavaScript，禁止控件
		ir = vdisable.iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField) ir.next();
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu != null) {
					str += mu.getIFormMacroCtl().getDisableCtlScript(ff,
							com.redmoon.oa.visual.Render.FORM_FLEMENT_ID);
				}
			} else {
				str += FormField.getDisableCtlScript(ff, com.redmoon.oa.visual.Render.FORM_FLEMENT_ID);
			}
		}
		
		String fieldHide = StrUtil.getNullString(wfa.getFieldHide()).trim();
		String[] fdsHide = StrUtil.split(fieldHide, ",");
        len = 0;	
        if (fdsHide!=null) {
        	len = fdsHide.length;
        }
        for (int i=0; i<len; i++) {
            if (fdsHide[i].startsWith("nest.")) {
            	String fieldName = fdsHide[i].substring(nestLen);
            	FormField ff = fd.getFormField(fieldName);
            	if (ff==null) {
            		LogUtil.getLog(getClass()).error("嵌套表 " + fd.getName() + " 中字段 " + fieldName + " 不存在！");
            		continue;
            	}
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        str += mu.getIFormMacroCtl().getHideCtlScript(ff, FORM_FLEMENT_ID);
                    }
                }
                else {
                	str += FormField.getHideCtlScript(ff, FORM_FLEMENT_ID);
                }
            }
        }		
		
		str = "<script>\r\n" + str + "</script>\r\n";

		return str;
	}    

    /**
     * 可能已经用不着了，编辑时置表单中的各个输入框的值，置用户已操作的值，渲染显示表单，当用于显示嵌套表单时，需置formElementId为flowForm
     * @param formElementId String
     * @return String
     */
    public String rend(String formElementId) {
        FormDAOMgr fdm = new FormDAOMgr(fd);
        FormDAO fdao = fdm.getFormDAO(visualObjId);
        
        Vector fields = fdao.getFields();
        
        String content = getContentMacroReplaced(fdao, fd, fields);

        return rend(fdao, formElementId, content, fields);
    }
    
    public String report(FormDAO fdao, String content, boolean isNest) {
        Vector fields = fdao.getFields();
        
        content = replaceDefaultStr(content);
        if (!isNest)
            content = "<div id=formDiv name=formDiv>" + content + "</div>";

        // 置用户已操作的值
        String str = "";

        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
        	String val = FuncUtil.renderFieldValue(fdao, ff);
        	ff.setValue(val);
        	
            /*
            if (ff.getType().equals(ff.TYPE_CHECKBOX)) {
                str += FormField.getOuterHTMLOfElementWithRAWValue(request, ff);
            }
            else
            */
                str += FormField.getOuterHTMLOfElementWithHTMLValue(request, ff);
        }

        MacroCtlMgr mm = new MacroCtlMgr();
        
        String userName = new Privilege().getUser(request);
        // 取得当前用户的可写字段
        ModulePrivDb mpd = new ModulePrivDb(fdao.getFormCode());
       	String fieldHide = mpd.getUserFieldsHasPriv(userName, "hide");
        String[] fdsHide = StrUtil.split(fieldHide, ",");
        
        // 使用try、catch的目的是为了在嵌套表单在打印时，不会出现JS错误，因为
        // 嵌套表单在report时，下面的脚本会被包含在formDiv中
        str += "\n<script>";
        // str += "\n<script>\ntry{";
        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            
            // 过滤掉隐藏字段
            boolean isHide = false;
	        if (fdsHide!=null) {
                for (int k=0; k<fdsHide.length; k++) {
	                if (ff.getName().equals(fdsHide[k])) {
	                	isHide = true;
	                	break;
	                }
                }
	        }            
            
	        if (isHide) {
	        	continue;
	        }
	        
            if (ff.getHide()==FormField.HIDE_ALWAYS)
            	continue;            
            
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu != null) {
                    str += mu.getIFormMacroCtl().getReplaceCtlWithValueScript(fdao, ff);
                }
            }
            else {
                /*
                if (ff.getType().equals(ff.TYPE_CHECKBOX)) {
                    str +=
                            FormField.getSetCtlValueScript(request, fdao, ff, FORM_FLEMENT_ID);
                } else
                */           	
                str += FormField.getReplaceCtlWithValueScript(ff);
            }
        }

        int len = 0;
        if (fdsHide!=null) {
        	len = fdsHide.length;
        }
        for (int i=0; i<len; i++) {
            if (!fdsHide[i].startsWith("nest.")) {
            	FormField ff = fd.getFormField(fdsHide[i]);
            	if (ff==null) {
            		LogUtil.getLog(getClass()).error("隐藏字段：" + fdsHide[i] + " 不存在！");
            		continue;
            	}
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        str += mu.getIFormMacroCtl().getHideCtlScript(ff, FORM_FLEMENT_ID);
                    }
                }
                else {            	
                	str += FormField.getHideCtlScript(ff, FORM_FLEMENT_ID);
                }
            }
            else {
                str += "try{ hideNestTableCol('" + fdsHide[i].substring("nest.".length()) +  "'); }catch(e) {}\n";
                str += "try{ hideNestSheetCol('" + fdsHide[i].substring("nest.".length()) +  "'); }catch(e) {}\n";
            }
        }
        
        // 清除其它辅助图片按钮等
        if (!isNest)
            str += "ClearAccessory();\n";
        // str += "}catch(e){}</script>\n";
        
        // 处理隐藏字段
        ir = fields.iterator();
        while (ir.hasNext()) {
        	FormField ff = (FormField)ir.next();
            if (ff.getHide()==FormField.HIDE_ALWAYS) {
                if (!ff.getName().startsWith("nest.")) {
                    str += FormField.getHideCtlScript(ff, FORM_FLEMENT_ID);
                }
                else {
                    str += "hideNestTableCol('" + ff.getName().substring("nest.".length()) +  "');\n";
                }                
            }
        }        
        
        str += "</script>\n";

        // 显示或隐藏表单中的区域
        str += ModuleUtil.doGetViewJS(request, fd, fdao, new Privilege().getUser(request), true);        

        // 一米OA签名
        if (License.getInstance().isFree()) {
        	content += License.getFormWatermark();
        }
        
        return content + str;    	
    }

    /**
     * 为表单生成报表
     * @param isNest boolean 是否为嵌套表单
     * @return String
     */
    public String report(boolean isNest) {
        // String content = doc.getContent(1); // 取得表单内容
        FormDAOMgr fdm = new FormDAOMgr(fd);
        FormDAO fdao = fdm.getFormDAO(visualObjId);
        Vector fields = fdao.getFields();

        String content = getContentMacroReplaced(fdao, fd, fields);
        return report(fdao, content, isNest);
    }

    public String report() {
        return report(false);
    }
    
    /**
     * 查看
     * @param msd
     * @return
     */
    public String report(ModuleSetupDb msd) {
    	String code = msd.getString("code");
    	String formCode = msd.getString("form_code");
    	// 如果是主表单
    	if (code.equals(formCode)) {
    		// return report();
    	}
    	
    	try {
			License.getInstance().checkSolution(formCode);
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			return e.getMessage();
		}   
		
    	int viewShow = msd.getInt("view_show");
    	if (viewShow==ModuleSetupDb.VIEW_DEFAULT || viewShow==ModuleSetupDb.VIEW_SHOW_CUSTOM)
    		return report();
    	
    	FormViewDb fvd = new FormViewDb();
    	fvd = fvd.getFormViewDb(viewShow);
    	if (fvd==null) {
    		LogUtil.getLog(getClass()).error("视图ID=" + viewShow + "不存在");
    	}
    	
        FormDAOMgr fdm = new FormDAOMgr(fd);
        FormDAO fdao = fdm.getFormDAO(visualObjId);
        Vector fields = fdao.getFields();
        
        String form = fvd.getString("form");

        String content = getContentMacroReplaced(fdao, form, fields);
        boolean isNest = false;
        return report(fdao, content, isNest);
        
    }
}
