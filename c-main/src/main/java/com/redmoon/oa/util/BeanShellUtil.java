package com.redmoon.oa.util;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class BeanShellUtil {

	public static String escape(String str) {
		// 替换换行符
		str = str.replaceAll("\\r\\n", "\\\\r\\\\n");
		
		// ie10
		str = str.replaceAll("\\n", "\\\\n");
		
		// str = str.replaceAll("abstract=", "abstractC=");
		return str.replaceAll("\\r", "\\\\r");
	}
	
	/**
	 * 给FileUpload赋值，用于runValidateScript测试
	 * @Description: 
	 * @param fdao
	 * @param fu
	 */
	public static void setFieldsValue(IFormDAO fdao, FileUpload fu) {
		fu.setFields(new Hashtable());
        // @task:fields中可能有重复的域
        for (FormField ff : fdao.getFields()) {
            String val;
            if (!ff.getMacroType().equals(FormField.MACRO_NOT)) {
                MacroCtlMgr mm = new MacroCtlMgr();
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu == null) {
                    throw new IllegalArgumentException("Macro ctl type=" +
                            ff.getMacroType() +
                            " is not exist.");
                }
                int fieldType = mu.getIFormMacroCtl().getFieldType(ff);
                if (FormField.isNumeric(fieldType)) {
                    val = fdao.getFieldValue(ff.getName());
                } else {
                    val = fdao.getFieldValue(ff.getName());
                    val = val.replaceAll("\"", "\\\\\"");
                }
            } else {
                int fType = ff.getFieldType();
                if (fType == FormField.FIELD_TYPE_INT) {
                    int v = StrUtil.toInt(fdao.getFieldValue(ff.getName()), -65536);
                    if (v == -65536) {
                        val = "-65536";
                    } else {
                        val = fdao.getFieldValue(ff.getName());
                    }
                } else if (fType == FormField.FIELD_TYPE_DOUBLE ||
                        fType == FormField.FIELD_TYPE_FLOAT ||
                        fType == FormField.FIELD_TYPE_LONG ||
                        fType == FormField.FIELD_TYPE_PRICE) {
                    double v = StrUtil.toDouble(fdao.getFieldValue(ff.getName()),
                            -65536);
                    if (v == -65536) {
                        // LogUtil.getLog(getClass()).info(ff.getName() + "=" + fdao.getFieldValue(ff.getName()) + " v=" + v);
                        val = "-65536";
                    } else {
                        val = fdao.getFieldValue(ff.getName());
                    }
                } else {
                    val = fdao.getFieldValue(ff.getName());
                    val = val.replaceAll("\"", "\\\\\"");
                }
            }
            fu.setFieldValue(ff.getName(), val);
        }				
	}

    /**
     * 给表单域赋值，仅限于指定的表单域fields
     * @param fdao
     * @param fields
     * @param sb
     */
    public static void setFieldsValue(IFormDAO fdao, Vector<FormField> fields, StringBuffer sb) {
        // @task:fields中可能有重复的域
        for (FormField ff : fields) {
            if (!ff.getMacroType().equals(FormField.MACRO_NOT)) {
                MacroCtlMgr mm = new MacroCtlMgr();
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu == null) {
                    throw new IllegalArgumentException("Macro ctl " + ff.getTitle() + " type=" +
                            ff.getMacroType() + " is not exist.");
                }

                int fieldType = mu.getIFormMacroCtl().getFieldType(ff);
                if (FormField.isNumeric(fieldType)) {
                    String v = fdao.getFieldValue(ff.getName());
                    if (v == null || "".equals(v)) {
                        v = "0";
                    }
                    sb.append("$" + ff.getName() + "=" + v + ";");
                }
                else {
                    String val = fdao.getFieldValue(ff.getName());
                    // val = val.replaceAll("\"", "\\\\\""); // "全球能源企业250强" 替换后变成了 \\"全球能源企业250强\\"，但依然会导致脚本运行报错
                    val = val.replaceAll("\"", "'");
                    // 转换脚本中的\为\\， 否则，如：互联网解决方案（MES\ERP，其中\E会导致报错：Encountered: "E" (69), after :互联网解决方案（MES\
                    val = val.replaceAll("\\\\", "\\\\\\\\");
                    sb.append("$" + ff.getName() + "=\"" + val + "\";");
                }
            } else {
                int fType = ff.getFieldType();
                if (fType == FormField.FIELD_TYPE_INT) {
                    int v = StrUtil.toInt(fdao.getFieldValue(ff.getName()), -65536);
                    if (v == -65536) {
                        sb.append("$" + ff.getName() + "=-65536;");
                    } else {
                        sb.append("$" + ff.getName() + "=" + fdao.getFieldValue(ff.getName()) + ";");
                    }
                } else if (fType == FormField.FIELD_TYPE_DOUBLE ||
                        fType == FormField.FIELD_TYPE_FLOAT ||
                        fType == FormField.FIELD_TYPE_LONG ||
                        fType == FormField.FIELD_TYPE_PRICE) {
                    double v = StrUtil.toDouble(fdao.getFieldValue(ff.getName()), -65536);
                    if (v == -65536) {
                        // LogUtil.getLog(getClass()).info(ff.getName() + "=" + fdao.getFieldValue(ff.getName()) + " v=" + v);
                        sb.append("$" + ff.getName() + "=-65536;");
                    } else {
                        sb.append("$" + ff.getName() + "=" + fdao.getFieldValue(ff.getName()) + ";");
                    }
                }
                else if (FormField.TYPE_CHECKBOX.equals(ff.getType())) {
                    String v = fdao.getFieldValue(ff.getName());
                    if (v == null || "".equals(v)) {
                        v = "0";
                    }
                    sb.append("$" + ff.getName() + "=" + v + ";");
                }
                else {
                    String val = fdao.getFieldValue(ff.getName());
                    // val = val.replaceAll("\"", "\\\\\""); // "全球能源企业250强" 替换后变成了 \\"全球能源企业250强\\"，但依然会导致脚本运行报错
                    val = val.replaceAll("\"", "'");
                    // 转换脚本中的\为\\， 否则，如：互联网解决方案（MES\ERP，其中\E会导致报错：Encountered: "E" (69), after :互联网解决方案（MES\
                    val = val.replaceAll("\\\\", "\\\\\\\\");
                    sb.append("$" + ff.getName() + "=\"" + val + "\";");
                }
            }
        }
    }

	/**
	 * 给流程表单域赋值
	 * @param fdao
	 * @param sb
	 */
	public static void setFieldsValue(IFormDAO fdao, StringBuffer sb) {
		Iterator ir = fdao.getFields().iterator();
        // @task:fields中可能有重复的域
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (!ff.getMacroType().equals(FormField.MACRO_NOT)) {
                MacroCtlMgr mm = new MacroCtlMgr();
                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                if (mu == null) {
                    throw new IllegalArgumentException("Macro ctl " + ff.getTitle() + " type=" +
                            ff.getMacroType() + " is not exist.");
                }

                int fieldType = mu.getIFormMacroCtl().getFieldType(ff);
                if (FormField.isNumeric(fieldType)) {
                    String v = fdao.getFieldValue(ff.getName());
                    if (v==null || "".equals(v)) {
                        v = "0";
                    }
                    sb.append("$" + ff.getName() + "=" + v + ";");
                } else {
                    String val = fdao.getFieldValue(ff.getName());
                    // val = val.replaceAll("\"", "\\\\\""); // "全球能源企业250强" 替换后变成了 \\"全球能源企业250强\\"，但依然会导致脚本运行报错
                    val = val.replaceAll("\"", "'");
                    // 转换脚本中的\为\\， 否则，如：互联网解决方案（MES\ERP，其中\E会导致报错：Encountered: "E" (69), after :互联网解决方案（MES\
                    val = val.replaceAll("\\\\", "\\\\\\\\");
                    sb.append("$" + ff.getName() + "=\"" + val + "\";");
                }
            } else {
                int fType = ff.getFieldType();

                if (fType == FormField.FIELD_TYPE_INT) {
                    int v = StrUtil.toInt(fdao.getFieldValue(ff.getName()), -65536);
                    if (v == -65536) {
                        sb.append("$" + ff.getName() + "=-65536;");
                    } else {
                        sb.append("$" + ff.getName() + "=" + fdao.getFieldValue(ff.getName()) + ";");
                    }
                } else if (fType == FormField.FIELD_TYPE_DOUBLE ||
                           fType == FormField.FIELD_TYPE_FLOAT ||
                           fType == FormField.FIELD_TYPE_LONG ||
                           fType == FormField.FIELD_TYPE_PRICE) {
                    double v = StrUtil.toDouble(fdao.getFieldValue(ff.getName()), -65536);
                    if (v == -65536) {
                        // LogUtil.getLog(getClass()).info(ff.getName() + "=" + fdao.getFieldValue(ff.getName()) + " v=" + v);
                        sb.append("$" + ff.getName() + "=-65536;");
                    }
                    else if (fType == FormField.FIELD_TYPE_LONG) {
                        // 默认类型是 int，如果值为长整型，如：622908573055651012，如果末尾不加L就会报错：Error or number too big for integer type
                        sb.append("$" + ff.getName() + "=" + fdao.getFieldValue(ff.getName()) + "L;");
                    }
                    else if (fType == FormField.FIELD_TYPE_FLOAT) {
                        // 如果浮点常量不带后缀，则默认为双精度常量
                        sb.append("$" + ff.getName() + "=" + fdao.getFieldValue(ff.getName()) + "F;");
                    }
                    else if (fType == FormField.FIELD_TYPE_DOUBLE || fType == FormField.FIELD_TYPE_PRICE) {
                        sb.append("$" + ff.getName() + "=" + fdao.getFieldValue(ff.getName()) + "D;");
                    }
                } else {
                    String val = fdao.getFieldValue(ff.getName());
                    // val = val.replaceAll("\"", "\\\\\""); // "全球能源企业250强" 替换后变成了 \\"全球能源企业250强\\"，但依然会导致脚本运行报错
                    val = val.replaceAll("\"", "'");
                    // 转换脚本中的\为\\， 否则，如：互联网解决方案（MES\ERP，其中\E会导致报错：Encountered: "E" (69), after :互联网解决方案（MES\
                    val = val.replaceAll("\\\\", "\\\\\\\\");
                    sb.append("$" + ff.getName() + "=\"" + val + "\";");
                }
            }
        }		
	}
	
	public static void main(String[] args) {
		String str22 = "String str = \"it is\r\n goto\";";
		
		str22 += "LogUtil.getLog(getClass()).info(str);";
		
		str22 = str22.replaceAll("\\r\\n", "\\\\r\\\\n");
		// LogUtil.getLog(getClass()).info(str);
		str22 += "LogUtil.getLog(getClass()).info(str);";
		
		str22 = "date=\"2014-01-10\";fkfs=\"\";sgdh=\"\";clxqb=\"\";hjryj=\"\";picker=\"admin\";ysbspyj=\"\";cgjlspyj=\"\";cwbfzrsp=\"\";add_button=\"\";contact_no=\"\";project_name=\"5\";provide_name=\"1\";contact_money=\"\";manager_comment=\"11          管理员   2014-01-10 13:47:48\";project_comment=\"\";flowId=6460;";

        Interpreter bsh = new Interpreter();
        try {
			bsh.eval(str22);
		} catch (EvalError e) {
            LogUtil.getLog(BeanShellUtil.class).error(e);
		}
	}
}
