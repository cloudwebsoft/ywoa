package cn.js.fan.util;

import java.util.Vector;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.util.IPUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;

import java.util.HashMap;

import cn.js.fan.security.SecurityUtil;

import java.text.SimpleDateFormat;

import cn.js.fan.web.SkinUtil;

import java.util.Date;

import com.cloudwebsoft.framework.base.ISequence;

/**
 * <p>Title: </p>
 *
 * <p>Description:
 * 单独验证并取值置于HashMap中：类型(String int Date String[] int[])，域名，描述, null=(not|empty|allow|current|ip),format=(yyyy-mm-dd), email=(true|false), min(>|>=)20, max(<|<=)20,isNotCN=true|false 如果出错则退出验证,sql=SQLSERVER|MYSQL|ORACLE(用于SQL语句)
 * 联合验证： 表单域名称1 >= | <= | = 表单域名称2
 * 例：String, code, 编码, not, email=true, min<=20 max=30, isNotCN=true, exclude=#|;|", sql=sqlserver 表示类型为string型的变量code，名称为编码，不允许为空......
 * format=yyyy-MM-dd HH:mm:ss
 * 固定格式：第一个必须是类型，第一个必须是域名，第三个是域的描述，第四个必须是能否为null，字符串中一律使用小写
 * null=not|empty　not表示不能为空empty置其为空字符串
 * 字符串类型时min表示最小长度，max表示最大长度
 * int类型时min表示最小值，max表示最大值
 * Date类型时 current表示当前时间 min表示日期的最小值, max表示日期的最大值
 * 说明：对于大小写不敏感，如RegName,将会被转换为regname
 * 域的描述格式：　#资源名称
 * 非#开头，表示直接使用
 * 资源文件中的key使用小写
 * </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ParamChecker {
    int sourceType;

    HttpServletRequest request;
    FileUpload fu;

    String formResource = "";

    String res = "res.checker";
    
    public static String TYPE_DATE = "Date";
    public static String TYPE_INT = "int";
    public static String TYPE_STRING = "String";
    public static String TYPE_STRING_ARRAY = "String[]";
    public static String TYPE_INT_ARRAY = "int[]";
    public static String TYPE_BOOLEAN = "boolean";
    public static String TYPE_LONG = "long";

    public static String TYPE_DOUBLE = "double";
    public static String TYPE_FLOAT = "float";

    public static int SOURCE_TYPE_REQUEST = 0;
    public static int SOURCE_TYPE_FILEUPLOAD = 1;

    HashMap fields = new HashMap(); // 用来存取域中所有的元素及其值

    boolean onErrorExit = false;

    public Vector msgs = new Vector(); // 存储出错信息

    public ParamChecker(HttpServletRequest request) {
        this.request = request;
        this.sourceType = SOURCE_TYPE_REQUEST;
    }

    public ParamChecker(HttpServletRequest request, Object sourceObj) {
        this.request = request;
        if (sourceObj instanceof HttpServletRequest) {
            this.sourceType = SOURCE_TYPE_REQUEST;
        } else {
            this.sourceType = SOURCE_TYPE_FILEUPLOAD;
            this.fu = (FileUpload) sourceObj;
        }
    }

    public String getFieldValue(String field) {
        if (sourceType == SOURCE_TYPE_REQUEST) {
            return ParamUtil.get(request, field, false);
        } else {
            String val = "";
            try {
                val = StrUtil.getNullStr(fu.getFieldValue(field, false));
            } catch (ClassCastException e) {
                // 使表单域选择宏控件支持多选的时候，这里得到的应是数组，将其拼装成,号分隔的字符串
                String[] ary = fu.getFieldValues(field);
                for (String str : ary) {
                    if ("".equals(val)) {
                        val = str;
                    } else {
                        val += "," + str;
                    }
                }
            }
            return val;
        }
    }

    public String[] getFieldValues(String field) {
        if (sourceType == SOURCE_TYPE_REQUEST) {
            return ParamUtil.getParameters(request, field);
        } else {
            return fu.getFieldValues(field);
        }
    }

    /**
     * 切分规则，并对其中的转义字符&comma;作处理
     *
     * @param str String
     * @return String[]
     */
    public static String[] split(String str) {
        str = str.replaceAll("，", ","); // 替换全角
        String[] r = StrUtil.split(str, ",");
        if (r != null) {
            int len = r.length;
            for (int i = 0; i < len; i++) {
                r[i] = r[i].trim().replaceAll("\\\\comma", ",");
            }
        }
        return r;
    }

    public Object getValue(String fieldName) throws ErrMsgException {
        if (fields.containsKey(fieldName)) {
            // LogUtil.getLog(getClass()).info("getValue:" + ((Field) fields.get(fieldName)).value);
            // LogUtil.getLog(getClass()).info(getClass() + "g etValue:" + ((Field) fields.get(fieldName)).value);
            return ((Field) fields.get(fieldName)).value;
        } else {
            throw new ParamCheckerException(fieldName + " is not found in the form rule.", ParamCheckerException.TYPE_PARAM_NOT_SET_IN_FORM_RULE); // 调试用，所以不调用资源文件
        }
    }

    public void setValue(String fieldName, String desc, Object value) {
        Field f = new Field(fieldName, desc, value);
        fields.put(fieldName, f);
    }

    public String getString(String fieldName) throws ErrMsgException {
        return (String) getValue(fieldName);
    }

    public int getInt(String fieldName) throws ErrMsgException {
        Integer it = (Integer) getValue(fieldName);
        if (it == null)
            return -1;
        else
            return it.intValue();
    }

    public Date getDate(String fieldName) throws ErrMsgException {
        Date d = (Date) getValue(fieldName);
        return d;
    }

    public String[] getStringValues(String fieldName) throws ErrMsgException {
        return (String[]) getValue(fieldName);
    }

    public int[] getIntValues(String fieldName) throws ErrMsgException {
        return (int[]) getValue(fieldName);
    }

    public boolean getBoolean(String fieldName) throws ErrMsgException {
        Boolean b = (Boolean) getValue(fieldName);
        if (b == null)
            return false;
        else
            return b.booleanValue();
    }

    public long getLong(String fieldName) throws ErrMsgException {
        Long v = (Long) getValue(fieldName);
        if (v == null)
            return -1;
        else
            return v.longValue();
    }

    public double getDouble(String fieldName) throws ErrMsgException {
        Double v = (Double) getValue(fieldName);
        if (v == null)
            return -1;
        else
            return v.doubleValue();
    }

    public float getFloat(String fieldName) throws ErrMsgException {
        Float v = (Float) getValue(fieldName);
        if (v == null)
            return -1;
        else
            return v.floatValue();
    }

    /**
     * 获取UnionCond中的成结的Field的值
     *
     * @param rulePairStr String
     * @param token       String
     * @return String[]
     */
    public boolean checkUnionCond(String rulePairStr, String token) {
        boolean isValid = true;
        String[] pairs = rulePairStr.split(token);
        if (pairs.length < 2) {
            addMsg("err_format", new String[]{rulePairStr});
            isValid = false;
        }
        String leftFieldName = pairs[0].trim();
        String rightFieldName = pairs[1].trim();

        // LogUtil.getLog(getClass()).info("checkUnionCond leftField name=" + leftFieldName + " rightFieldName=" + rightFieldName);

        Field leftField = (Field) fields.get(leftFieldName);
        Field rightField = (Field) fields.get(rightFieldName);
        if (leftField == null) {
            addMsg("err_not_in_result", new String[]{leftFieldName});
            isValid = false;
            return false;
        }
        if (rightField == null) {
            addMsg("err_not_in_result", new String[]{rightFieldName});
            isValid = false;
            return false;
        }

        String leftFieldDesc = leftField.desc;
        String rightFieldDesc = rightField.desc;

        // LogUtil.getLog(getClass()).info("checkUnionCond leftField name=" + leftField.name + " value=" + leftField.value + " type=" + leftField.type);
        // LogUtil.getLog(getClass()).info("checkUnionCond rightField name=" + rightField.name + " value=" + rightField.value + " type=" + rightField.type);
        if (leftField.value == null || rightField.value == null)
            return false;

        if (!leftField.type.equals(rightField.type)) {
            isValid = false;
            addMsg("err_type_not_match", new String[]{leftFieldDesc, "" + rightFieldDesc});
        }

        if (leftField.type.equals(this.TYPE_STRING) || leftField.type.equals(this.TYPE_INT) || leftField.type.equals(this.TYPE_DATE) || leftField.type.equals(this.TYPE_LONG))
            ;
        else {
            addMsg("err_can_not_compare", new String[]{leftFieldDesc, "" + rightFieldDesc});
            isValid = false;
            return isValid;
        }
        if (token.equals(">=")) {
            if (leftField.type.equals(this.TYPE_INT)) {
                int a = ((Integer) leftField.value).intValue();
                int b = ((Integer) rightField.value).intValue();
                if (a >= b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_more_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_DATE)) {
                Date a = (Date) leftField.value;
                Date b = (Date) rightField.value;
                if (DateUtil.compare(a, b) == 1 || DateUtil.compare(a, b) == 0)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_more_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_LONG)) {
                long a = ((Long) leftField.value).longValue();
                long b = ((Long) rightField.value).longValue();
                if (a >= b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_more_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_DOUBLE)) {
                double a = ((Double) leftField.value).doubleValue();
                double b = ((Double) rightField.value).doubleValue();
                if (a >= b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_more_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_FLOAT)) {
                float a = ((Float) leftField.value).floatValue();
                float b = ((Float) rightField.value).floatValue();
                if (a >= b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_more_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            }
        } else if (token.equals(">")) {
            if (leftField.type.equals(this.TYPE_INT)) {
                int a = ((Integer) leftField.value).intValue();
                int b = ((Integer) rightField.value).intValue();
                if (a > b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_more", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_DATE)) {
                Date a = (Date) leftField.value;
                Date b = (Date) rightField.value;
                if (DateUtil.compare(a, b) == 1)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_more", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_LONG)) {
                long a = ((Long) leftField.value).longValue();
                long b = ((Long) rightField.value).longValue();
                if (a > b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_more", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_DOUBLE)) {
                double a = ((Double) leftField.value).doubleValue();
                double b = ((Double) rightField.value).doubleValue();
                if (a > b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_more", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_FLOAT)) {
                float a = ((Float) leftField.value).floatValue();
                float b = ((Float) rightField.value).floatValue();
                if (a > b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_more", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            }
        } else if (token.equals("<=")) {
            if (leftField.type.equals(this.TYPE_INT)) {
                int a = ((Integer) leftField.value).intValue();
                int b = ((Integer) rightField.value).intValue();
                if (a <= b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_less_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_DATE)) {
                Date a = (Date) leftField.value;
                Date b = (Date) rightField.value;
                if (DateUtil.compare(a, b) == 2 || DateUtil.compare(a, b) == 0)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_less_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_LONG)) {
                long a = ((Long) leftField.value).longValue();
                long b = ((Long) rightField.value).longValue();
                if (a <= b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_less_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_DOUBLE)) {
                double a = ((Double) leftField.value).doubleValue();
                double b = ((Double) rightField.value).doubleValue();
                if (a <= b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_less_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_FLOAT)) {
                float a = ((Float) leftField.value).floatValue();
                float b = ((Float) rightField.value).floatValue();
                if (a <= b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_less_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            }

        } else if (token.equals("<")) {
            if (leftField.type.equals(this.TYPE_INT)) {
                int a = ((Integer) leftField.value).intValue();
                int b = ((Integer) rightField.value).intValue();
                if (a < b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_less", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_DATE)) {
                Date a = (Date) leftField.value;
                Date b = (Date) rightField.value;
                if (DateUtil.compare(a, b) == 2)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_less", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_LONG)) {
                long a = ((Long) leftField.value).longValue();
                long b = ((Long) rightField.value).longValue();
                if (a < b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_less", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_DOUBLE)) {
                double a = ((Double) leftField.value).doubleValue();
                double b = ((Double) rightField.value).doubleValue();
                if (a < b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_less", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_FLOAT)) {
                float a = ((Float) leftField.value).floatValue();
                float b = ((Float) rightField.value).floatValue();
                if (a < b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_less", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            }
        } else if (token.equals("=")) {
            if (leftField.type.equals(this.TYPE_INT)) {
                int a = ((Integer) leftField.value).intValue();
                int b = ((Integer) rightField.value).intValue();
                if (a == b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_DATE)) {
                Date a = (Date) leftField.value;
                Date b = (Date) rightField.value;
                if (DateUtil.compare(a, b) == 0)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_LONG)) {
                long a = ((Long) leftField.value).longValue();
                long b = ((Long) rightField.value).longValue();
                if (a == b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_BOOLEAN)) {
                boolean a = ((Boolean) leftField.value).booleanValue();
                boolean b = ((Boolean) rightField.value).booleanValue();
                if (a == b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_STRING)) {
                String a = (String) leftField.value;
                String b = (String) rightField.value;
                if (a.equals(b))
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_DOUBLE)) {
                double a = ((Double) leftField.value).doubleValue();
                double b = ((Double) rightField.value).doubleValue();
                if (a == b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_FLOAT)) {
                float a = ((Float) leftField.value).floatValue();
                float b = ((Float) rightField.value).floatValue();
                if (a == b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_equal", new String[]{leftFieldDesc, "" + rightFieldDesc});
                }
            }

        } else if (token.equals("!=")) {
            if (leftField.type.equals(this.TYPE_INT)) {
                int a = ((Integer) leftField.value).intValue();
                int b = ((Integer) rightField.value).intValue();
                if (a != b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_not_equal", new String[]{leftFieldDesc,
                            rightFieldDesc});
                }
            } else if (leftField.type.equals(TYPE_DATE)) {
                Date a = (Date) leftField.value;
                Date b = (Date) rightField.value;
                // LogUtil.getLog(getClass()).info(getClass() + " a=" + a + " b=" + b);
                if (DateUtil.compare(a, b) != 0)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_not_equal", new String[]{leftFieldDesc,
                            rightFieldDesc});
                }
            } else if (leftField.type.equals(TYPE_LONG)) {
                long a = ((Long) leftField.value).longValue();
                long b = ((Long) rightField.value).longValue();
                if (a != b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_not_equal", new String[]{leftFieldDesc,
                            rightFieldDesc});
                }
            } else if (leftField.type.equals(TYPE_BOOLEAN)) {
                boolean a = ((Boolean) leftField.value).booleanValue();
                boolean b = ((Boolean) rightField.value).booleanValue();
                if (a != b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_not_equal", new String[]{leftFieldDesc,
                            "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_STRING)) {
                String a = (String) leftField.value;
                String b = (String) rightField.value;
                // LogUtil.getLog(getClass()).info(getClass() + " a=" + a + " b=" + b);

                if (!a.equals(b))
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_not_equal", new String[]{leftFieldDesc,
                            "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_DOUBLE)) {
                double a = ((Double) leftField.value).doubleValue();
                double b = ((Double) rightField.value).doubleValue();
                if (a != b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_not_equal", new String[]{leftFieldDesc,
                            "" + rightFieldDesc});
                }
            } else if (leftField.type.equals(this.TYPE_FLOAT)) {
                float a = ((Float) leftField.value).floatValue();
                float b = ((Float) rightField.value).floatValue();
                if (a != b)
                    ;
                else {
                    isValid = false;
                    addMsg("err_need_not_equal", new String[]{leftFieldDesc,
                            "" + rightFieldDesc});
                }
            }
        } else {
            isValid = false;
            addMsg("err_format", new String[]{rulePairStr});
        }
        return isValid;
    }

    /**
     * 检查联合条件，如两个表单域之间的相等、大于、小于
     * 如：onerror=exit|resume,pwd=pwd1,date1>date2,num1<=num2
     */
    public void checkUnionCond(Vector rules) throws CheckErrException {
        int len = rules.size();
        // 先找到onerror
        boolean isValid = true;

        for (int i = 0; i < len; i++) {
            isValid = true;
            String rule = (String) rules.get(i);
            rule = rule.trim();
            if (rule.indexOf(">=") != -1) {
                isValid = this.checkUnionCond(rule, ">=");
            } else if (rule.indexOf("<=") != -1) {
                isValid = this.checkUnionCond(rule, "<=");
            } else if (rule.indexOf(">") != -1) {
                isValid = this.checkUnionCond(rule, ">");
            } else if (rule.indexOf("<") != -1) {
                isValid = this.checkUnionCond(rule, "<");
            } else if (rule.indexOf("!=") != -1) {
                isValid = this.checkUnionCond(rule, "!=");
            } else if (rule.indexOf("=") != -1) {
                isValid = this.checkUnionCond(rule, "=");
            } else {
                addMsg("err_format", new String[]{rule});
                if (onErrorExit) {
                    break;
                }
            }
            if (!isValid) {
                if (onErrorExit) {
                    break;
                }
            }
        }
    }

    public void doCheck(FormRule fr) throws CheckErrException, ErrMsgException {
        if (fr == null) {
            throw new ErrMsgException(LoadString("err_formrule_none", new String[]{}));
        }
        onErrorExit = fr.isOnErrorExit();
        formResource = fr.getRes();
        check(fr.getRules());
        checkUnionCond(fr.getUnionRules());

        if (msgs.size() != 0) {
            throw new ErrMsgException(getMessage(false));
        }
    }

    public void check(Vector rules) throws CheckErrException, ErrMsgException {
        Iterator ir = rules.iterator();
        while (ir.hasNext()) {
            String rule = (String) ir.next();
            checkField(rule);
        }
    }

    public void checkField(String rule) throws CheckErrException, ErrMsgException {
        rule = rule.trim();

        if (rule.startsWith(TYPE_STRING_ARRAY)) {
            checkFieldStringArray(rule);
        } else if (rule.startsWith(TYPE_INT_ARRAY)) {
            checkFieldIntArray(rule);
        } else if (rule.startsWith(TYPE_STRING)) {
            checkFieldString(rule);
        } else if (rule.startsWith(TYPE_INT)) {
            checkFieldInt(rule);
        } else if (rule.startsWith(TYPE_DATE)) {
            checkFieldDate(rule);
        } else if (rule.startsWith(TYPE_LONG)) {
            checkFieldLong(rule);
        } else if (rule.startsWith(TYPE_BOOLEAN)) {
            checkFieldBoolean(rule);
        } else if (rule.startsWith(TYPE_DOUBLE)) {
            checkFieldDouble(rule);
        } else if (rule.startsWith(TYPE_FLOAT)) {
            checkFieldFloat(rule);
        } else {
            throw new ErrMsgException(LoadString("err_type", new String[]{rule}));
        }
    }

    public String parseFieldDesc(String desc) {
        // LogUtil.getLog(getClass()).info("parseFieldDesc desc=" + desc + " formResource=" + formResource);
        if (formResource == null || formResource.equals("")) {
            return desc;
        }
        if (!desc.startsWith("#")) {
            return desc;
        } else {
            String key = desc.substring(1, desc.length()).trim();
            return SkinUtil.LoadString(request, formResource, key);
        }
    }

    public void checkFieldIntArray(String ruleStr) throws CheckErrException {
        String fieldName = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        fieldName = rule[1];
        String fieldDesc = parseFieldDesc(rule[2]);

        String[] values = getFieldValues(fieldName);
        int[] intValues = null;
        len = 0;
        if (values != null) {
            len = values.length;
            intValues = new int[len];
        }
        for (int i = 0; i < len; i++) {
            try {
                intValues[i] = Integer.parseInt(values[i]);
            } catch (Exception e) {
                addMsg("err_not_num", new String[]{fieldDesc});
                break;
            }
        }

        Field f = new Field(fieldName, fieldDesc, intValues, TYPE_INT_ARRAY);
        fields.put(fieldName, f);

        String NULL = rule[3];
        if (values == null) {
            if (NULL.equalsIgnoreCase("not")) {
                addMsg("err_want", new String[]{fieldDesc});
                return;
            }
        }
    }

    public void checkFieldStringArray(String ruleStr) throws CheckErrException {
        String fieldName = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }
        fieldName = rule[1];
        String fieldDesc = parseFieldDesc(rule[2]);

        String[] values = getFieldValues(fieldName);
        Field f = new Field(fieldName, fieldDesc, values, TYPE_STRING_ARRAY);
        fields.put(fieldName, f);

        String NULL = rule[3];

        if (values == null) {
            if (NULL.equalsIgnoreCase("not")) {
                addMsg("err_want", new String[]{fieldDesc});
            } else if (NULL.equals("allow"))
                ;
            else {
                addMsg("err_format", new String[]{NULL});
            }
        }
    }

    public void checkFieldBoolean(String ruleStr) throws CheckErrException {
        String fieldName = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }
        fieldName = rule[1];

        String value = getFieldValue(fieldName);

        checkFieldBoolean(ruleStr, value);
    }

    public void checkFieldBoolean(String ruleStr, String value) throws CheckErrException {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }
        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        String NULL = rule[3];

        if (value == null) {
            if (NULL.equalsIgnoreCase("not")) {
                addMsg("err_want", new String[]{fieldDesc});
                return;
            } else if (NULL.equals("allow"))
                ;
            else {
                value = NULL;
            }
        }

        Boolean bu;

        if (value != null && value.equals("true") || value.equals("1"))
            bu = new Boolean(true);
        else
            bu = new Boolean(false);

        boolean isValid = true;

        // 存储field值
        Field f = new Field(fieldName, fieldDesc, bu, type);
        fields.put(fieldName, f);

        // 验证

        // ...

        if (!isValid) {
            if (onErrorExit) {
                throw new CheckErrException(msgs);
            }
        }
    }

    public void checkFieldDate(String ruleStr) throws CheckErrException {
        String fieldName = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }
        fieldName = rule[1];

        String value = getFieldValue(fieldName);

        checkFieldDate(ruleStr, value);
    }

    public void checkFieldDate(String ruleStr, String value) throws CheckErrException {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }
        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        String NULL = rule[3];

        if (value == null || value.equals("")) {
            if (NULL.equalsIgnoreCase("not")) {
                addMsg("err_want", new String[]{fieldDesc});
                return;
            } else if (NULL.equals("allow"))
                ;
            else if (NULL.equals("current")) {
                // 如果默认为当前时间
                Field f = new Field(fieldName, fieldDesc, new Date(), type);
                fields.put(fieldName, f);
                return;
            } else {
                addMsg("err_format", new String[]{NULL});
                return;
            }
        }

        boolean isValid = true;

        String format = "";
        java.util.Date d = null;

        // 规则部分，以类似email=true的方式
        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim();
            if (value != null && !value.equals("")) {
                if (cond.startsWith("format")) {
                    format = getCondValue(cond);
                    SimpleDateFormat sdf = new SimpleDateFormat(
                            format, SkinUtil.getLocale(request));
                    try {
                        d = sdf.parse(value);
                    } catch (Exception e) {
                        isValid = false;
                        addMsg("err_format", new String[]{fieldDesc + "=" + value});
                    }
                }
            }
        }

        // 存储field值
        Field f = new Field(fieldName, fieldDesc, d, type);
        fields.put(fieldName, f);

        // 验证
        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim();
            if (cond.startsWith("min")) {
                if (d != null) {
                    // 取出符号
                    char token = cond.charAt(3);
                    if (token == '>') {
                        if (cond.charAt(4) == '=') {
                            String strMin = cond.substring(5, cond.length()).trim();
                            try {
                                Date min = null;
                                try {
                                    // curDate是来自于表单字段的属性：大小，其没有等于的情况，只有>=、>或<=、<
                                    if ("curDate".equals(strMin)) {
                                        if ("yyyy-MM-dd HH:mm:ss".equals(format)) {
                                            min = new Date();
                                        }
                                        else {
                                            min = DateUtil.parse(DateUtil.format(new Date(), format), format);
                                        }
                                    }
                                    else {
                                        min = DateUtil.parse(strMin, format, SkinUtil.getLocale(request));
                                    }
                                } catch (Exception e) {
                                    LogUtil.getLog(getClass()).info(ParamChecker.class + " checkFieldDate ParseException: " + e.getMessage() + " ruleStr=" + ruleStr + " strMax=" + strMin);
                                    addMsg("err_format", new String[]{ruleStr + ", min value=" + strMin});
                                }
                                if (min != null) {
                                    if (DateUtil.compare(d, min) == 1 ||
                                            DateUtil.compare(d, min) == 0) {
                                        ;
                                    } else {
                                        isValid = false;
                                        addMsg("err_need_more_equal", new String[]{fieldDesc, DateUtil.format(min, format)});
                                    }
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strMin = cond.substring(4, cond.length()).trim();
                            try {
                                Date min = null;
                                try {
                                    // curDate是来自于表单字段的属性：大小，其没有等于的情况，只有>=、>或<=、<
                                    if ("curDate".equals(strMin)) {
                                        if ("yyyy-MM-dd HH:mm:ss".equals(format)) {
                                            min = new Date();
                                        }
                                        else {
                                            min = DateUtil.parse(DateUtil.format(new Date(), format), format);
                                        }
                                    }
                                    else {
                                        min = DateUtil.parse(strMin, format, SkinUtil.getLocale(request));
                                    }
                                } catch (Exception e) {
                                    LogUtil.getLog(getClass()).error(e);
                                    addMsg("err_format", new String[]{ruleStr + ", min value=" + strMin});
                                }
                                if (min != null) {
                                    if (DateUtil.compare(d, min) == 1) {
                                        ;
                                    } else {
                                        isValid = false;
                                        addMsg("err_need_more", new String[]{fieldDesc, DateUtil.format(min, format)});
                                    }
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            } else if (cond.startsWith("max")) {
                if (d != null) {
                    // 取出符号
                    char token = cond.charAt(3);
                    if (token == '<') {
                        if (cond.charAt(4) == '=') {
                            String strMax = cond.substring(5, cond.length()).trim();
                            try {
                                Date max = null;
                                try {
                                    if ("curDate".equals(strMax)) {
                                        if ("yyyy-MM-dd HH:mm:ss".equals(format)) {
                                            max = new Date();
                                        }
                                        else {
                                            max = DateUtil.parse(DateUtil.format(new Date(), format), format);
                                        }
                                    }
                                    else {
                                        max = DateUtil.parse(strMax, format, SkinUtil.getLocale(request));
                                    }
                                } catch (java.text.ParseException e) {
                                    LogUtil.getLog(getClass()).error(e);
                                    addMsg("err_format", new String[]{ruleStr + ", max value=" + strMax});
                                }
                                if (max != null) {
                                    if (DateUtil.compare(d, max) == 2 ||
                                            DateUtil.compare(d, max) == 0) {
                                        ;
                                    } else {
                                        isValid = false;
                                        addMsg("err_need_less_equal", new String[]{fieldDesc, DateUtil.format(max, format)});
                                    }
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strMax = cond.substring(4, cond.length()).trim();
                            try {
                                Date max = null;
                                try {
                                    if ("curDate".equals(strMax)) {
                                        if ("yyyy-MM-dd HH:mm:ss".equals(format)) {
                                            max = new Date();
                                        }
                                        else {
                                            max = DateUtil.parse(DateUtil.format(new Date(), format), format);
                                        }
                                    }
                                    else {
                                        max = DateUtil.parse(strMax, format, SkinUtil.getLocale(request));
                                    }
                                } catch (Exception e) {
                                    LogUtil.getLog(getClass()).error(e);
                                    addMsg("err_format", new String[]{ruleStr + ", max value=" + strMax});
                                }
                                if (max != null) {
                                    if (DateUtil.compare(d, max) == 2) {
                                        ;
                                    } else {
                                        isValid = false;
                                        addMsg("err_need_less", new String[]{fieldDesc, DateUtil.format(max, format)});
                                    }
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            }
        }
        if (!isValid) {
            if (onErrorExit) {
                throw new CheckErrException(msgs);
            }
        }
    }

    public void checkFieldLong(String ruleStr) throws CheckErrException {
        String fieldName = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        fieldName = rule[1];

        String value = getFieldValue(fieldName);
        checkFieldLong(ruleStr, value);
    }

    public void checkFieldLong(String ruleStr, String value) throws CheckErrException {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        String NULL = rule[3];

        // String value = getFieldValue(fieldName);

        Long longValue = null;

        if (value == null || value.equals("")) {
            if (NULL.equalsIgnoreCase("not")) {
                addMsg("err_want", new String[]{fieldDesc});
                return;
            } else if (NULL.equals("allow"))
                ;
            else if (NULL.startsWith("auto_inc")) {
                // 自动增长
                String s = NULL.substring(9, NULL.length());
                String[] ary = s.split("\\$");
                try {
                    ISequence is = (ISequence) Class.forName(ary[0]).newInstance();
                    int typeId = Integer.parseInt(ary[1]);
                    longValue = new Long(is.getNextId(typeId));
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error("checkFieldLong1:" + StrUtil.trace(e));
                    addMsg("err_format", new String[]{fieldDesc + "=" + e.getMessage()});
                    return;
                }
            } else {
                try {
                    long v = Long.parseLong(NULL);
                    longValue = new Long(v);
                } catch (Exception e) {
                    addMsg("err_format", new String[]{NULL});
                    return;
                }
            }
        } else {
            try {
                long v = Long.parseLong(value);
                longValue = new Long(v);
            } catch (Exception e) {
                addMsg("err_format", new String[]{fieldDesc + "=" + value});
                return;
            }
        }

        // 存储field值
        Field f = new Field(fieldName, fieldDesc, longValue, type);
        fields.put(fieldName, f);

        boolean isValid = true;

        // 规则部分，以类似email=true的方式
        if (len >= 5) {
            // 如果长度大于5，表示有规则，而如果值为允许空且获得的值为空，则不再验证这些规则
            if (longValue == null) {
                return;
                // addMsg("The value of " + fieldName + " is null but has rule, please check the rule is correct!");
            }
        }
        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim();
            if (cond.startsWith("min")) {
                if (value != null) {
                    long fieldValue = longValue.longValue();
                    // 取出符号
                    char token = cond.charAt(3);
                    if (token == '>') {
                        if (cond.charAt(4) == '=') {
                            String strMin = cond.substring(5, cond.length()).
                                    trim();
                            try {
                                long min = Long.parseLong(strMin);
                                if (fieldValue >= min)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_more_equal", new String[]{fieldDesc, "" + min});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strMin = cond.substring(4, cond.length()).
                                    trim();
                            try {
                                long min = Long.parseLong(strMin);
                                if (fieldValue > min)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_more", new String[]{fieldDesc, "" + min});
                                }
                            } catch (Exception e) {
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            } else if (cond.startsWith("max")) {
                if (value != null) {
                    long fieldValue = longValue.longValue();
                    char token = cond.charAt(3);
                    // 取出符号
                    if (token == '<') {
                        if (cond.charAt(4) == '=') {
                            String strMax = cond.substring(5, cond.length()).
                                    trim();
                            try {
                                long max = Long.parseLong(strMax);
                                if (fieldValue <= max)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_less_equal", new String[]{fieldDesc, "" + max});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strMax = cond.substring(4, cond.length()).
                                    trim();
                            try {
                                long max = Long.parseLong(strMax);
                                if (fieldValue < max)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_less", new String[]{fieldDesc, "" + max});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            }
        }
        if (!isValid) {
            if (onErrorExit) {
                throw new CheckErrException(msgs);
            }
        }
    }

    public void checkFieldDouble(String ruleStr) throws CheckErrException {
        String fieldName = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        fieldName = rule[1];

        String value = getFieldValue(fieldName);
        checkFieldDouble(ruleStr, value);
    }

    public void checkFieldPrice(String ruleStr) throws CheckErrException {
        String fieldName = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        fieldName = rule[1];

        String value = getFieldValue(fieldName);
        // 去掉千分位逗号
        value = value.replaceAll(",", "");

        checkFieldDouble(ruleStr, value);
    }

    public void checkFieldDouble(String ruleStr, String value) throws CheckErrException {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        String NULL = rule[3];

        // String value = getFieldValue(fieldName);

        Double doubleValue = null;

        if (value == null || value.equals("")) {
            if (NULL.equalsIgnoreCase("not")) {
                addMsg("err_want", new String[]{fieldDesc});
                return;
            } else if (NULL.equals("allow"))
                ;
            else {
                try {
                    double v = Double.parseDouble(NULL);
                    doubleValue = new Double(v);
                } catch (Exception e) {
                    addMsg("err_format", new String[]{NULL});
                    return;
                }
            }
        } else {
            try {
                double v = Double.parseDouble(value);
                doubleValue = new Double(v);
            } catch (Exception e) {
                addMsg("err_format", new String[]{fieldDesc + "=" + value});
                return;
            }
        }

        // 存储field值
        Field f = new Field(fieldName, fieldDesc, doubleValue, type);
        fields.put(fieldName, f);

        boolean isValid = true;

        // 规则部分，以类似email=true的方式
        if (len >= 5) {
            // 如果长度大于5，表示有规则，而如果值为允许空且获得的值为空，则不再验证这些规则
            if (doubleValue == null) {
                return;
                // addMsg("The value of " + fieldName + " is null but has rule, please check the rule is correct!");
            }
        }
        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim();
            if (cond.startsWith("min")) {
                if (value != null) {
                    double fieldValue = doubleValue.doubleValue();
                    // 取出符号
                    char token = cond.charAt(3);
                    if (token == '>') {
                        if (cond.charAt(4) == '=') {
                            String strMin = cond.substring(5, cond.length()).
                                    trim();
                            try {
                                double min = Double.parseDouble(strMin);
                                if (fieldValue >= min)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_more_equal", new String[]{fieldDesc, "" + min});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strMin = cond.substring(4, cond.length()).
                                    trim();
                            try {
                                double min = Double.parseDouble(strMin);
                                if (fieldValue > min)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_more", new String[]{fieldDesc, "" + min});
                                }
                            } catch (Exception e) {
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            } else if (cond.startsWith("max")) {
                if (value != null) {
                    double fieldValue = doubleValue.doubleValue();
                    char token = cond.charAt(3);
                    // 取出符号
                    if (token == '<') {
                        if (cond.charAt(4) == '=') {
                            String strMax = cond.substring(5, cond.length()).
                                    trim();
                            try {
                                double max = Double.parseDouble(strMax);
                                if (fieldValue <= max)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_less_equal", new String[]{fieldDesc, "" + max});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strMax = cond.substring(4, cond.length()).
                                    trim();
                            try {
                                double max = Double.parseDouble(strMax);
                                if (fieldValue < max)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_less", new String[]{fieldDesc, "" + max});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            }
        }
        if (!isValid) {
            if (onErrorExit) {
                throw new CheckErrException(msgs);
            }
        }
    }

    public void checkFieldFloat(String ruleStr) throws CheckErrException {
        String fieldName = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        fieldName = rule[1];

        String value = getFieldValue(fieldName);
        checkFieldFloat(ruleStr, value);
    }

    public void checkFieldFloat(String ruleStr, String value) throws CheckErrException {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        String NULL = rule[3];

        // String value = getFieldValue(fieldName);

        Float floatValue = null;

        if (value == null || value.equals("")) {
            if (NULL.equalsIgnoreCase("not")) {
                addMsg("err_want", new String[]{fieldDesc});
                return;
            } else if (NULL.equals("allow"))
                ;
            else {
                try {
                    float v = Float.parseFloat(NULL);
                    floatValue = new Float(v);
                } catch (Exception e) {
                    addMsg("err_format", new String[]{NULL});
                    return;
                }
            }
        } else {
            try {
                float v = Float.parseFloat(value);
                floatValue = new Float(v);
            } catch (Exception e) {
                addMsg("err_format", new String[]{fieldDesc + "=" + value});
                return;
            }
        }

        // 存储field值
        Field f = new Field(fieldName, fieldDesc, floatValue, type);
        fields.put(fieldName, f);

        boolean isValid = true;

        // 规则部分，以类似email=true的方式
        if (len >= 5) {
            // 如果长度大于5，表示有规则，而如果值为允许空且获得的值为空，则不再验证这些规则
            if (floatValue == null) {
                return;
                // addMsg("The value of " + fieldName + " is null but has rule, please check the rule is correct!");
            }
        }
        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim();
            if (cond.startsWith("min")) {
                if (value != null) {
                    float fieldValue = floatValue.floatValue();
                    // 取出符号
                    char token = cond.charAt(3);
                    if (token == '>') {
                        if (cond.charAt(4) == '=') {
                            String strMin = cond.substring(5, cond.length()).
                                    trim();
                            try {
                                float min = Float.parseFloat(strMin);
                                if (fieldValue >= min)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_more_equal", new String[]{fieldDesc, "" + min});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strMin = cond.substring(4, cond.length()).
                                    trim();
                            try {
                                float min = Float.parseFloat(strMin);
                                if (fieldValue > min)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_more", new String[]{fieldDesc, "" + min});
                                }
                            } catch (Exception e) {
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            } else if (cond.startsWith("max")) {
                if (value != null) {
                    float fieldValue = floatValue.floatValue();
                    char token = cond.charAt(3);
                    // 取出符号
                    if (token == '<') {
                        if (cond.charAt(4) == '=') {
                            String strMax = cond.substring(5, cond.length()).
                                    trim();
                            try {
                                float max = Float.parseFloat(strMax);
                                if (fieldValue <= max)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_less_equal", new String[]{fieldDesc, "" + max});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strMax = cond.substring(4, cond.length()).
                                    trim();
                            try {
                                float max = Float.parseFloat(strMax);
                                if (fieldValue < max)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_less", new String[]{fieldDesc, "" + max});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            }
        }
        if (!isValid) {
            if (onErrorExit) {
                throw new CheckErrException(msgs);
            }
        }
    }

    public void checkFieldInt(String ruleStr) throws CheckErrException {
        String fieldName = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }
        fieldName = rule[1];

        String value = getFieldValue(fieldName);
        checkFieldInt(ruleStr, value);
    }

    public void checkFieldInt(String ruleStr, String value) throws CheckErrException {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }
        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        String NULL = rule[3];

        Integer intValue = null;

        if (value == null || value.equals("")) {
            if (NULL.equalsIgnoreCase("not")) {
                addMsg("err_want", new String[]{fieldDesc});
                return;
            } else if (NULL.equals("allow"))
                ;
            else if (NULL.startsWith("auto_inc")) {
                // 自动增长
                String s = NULL.substring(9, NULL.length());
                String[] ary = s.split("\\$");
                try {
                    ISequence is = (ISequence) Class.forName(ary[0]).newInstance();
                    int typeId = Integer.parseInt(ary[1]);
                    intValue = new Integer((int) is.getNextId(typeId));
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error(e);
                    addMsg("err_format", new String[]{fieldDesc + "=" + e.getMessage()});
                    return;
                }
            } else {
                try {
                    int v = Integer.parseInt(NULL);
                    intValue = new Integer(v);
                } catch (Exception e) {
                    addMsg("err_format", new String[]{fieldDesc + "=" + NULL});
                    return;
                }
            }
        } else {
            try {
                int v = Integer.parseInt(value);
                intValue = new Integer(v);
            } catch (Exception e) {
                addMsg("err_format", new String[]{fieldDesc + "=" + value});
                return;
            }
        }

        // 存储field值
        Field f = new Field(fieldName, fieldDesc, intValue, type);
        fields.put(fieldName, f);

        boolean isValid = true;

        // 验证规则部分，以类似email=true的方式
        if (len >= 5) {
            // 如果长度大于5，表示有规则，而如果值为允许空且获得的值为空，则不再验证这些规则
            if (intValue == null) {
                return;
                // addMsg("The value of " + fieldName + " is null but has rule, please check the rule is correct!");
            }
        }
        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim();
            if (cond.startsWith("min")) {
                if (value != null) {
                    int fieldValue = intValue.intValue();
                    // 取出符号
                    char token = cond.charAt(3);
                    if (token == '>') {
                        if (cond.charAt(4) == '=') {
                            String strMin = cond.substring(5, cond.length()).
                                    trim();
                            try {
                                int min = Integer.parseInt(strMin);
                                if (fieldValue >= min)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_more_equal", new String[]{fieldDesc, "" + min});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strMin = cond.substring(4, cond.length()).
                                    trim();
                            try {
                                int min = Integer.parseInt(strMin);
                                if (fieldValue > min)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_more", new String[]{fieldDesc, "" + min});
                                }
                            } catch (Exception e) {
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            } else if (cond.startsWith("max")) {
                if (value != null) {
                    int fieldValue = intValue.intValue();
                    char token = cond.charAt(3);
                    // 取出符号
                    if (token == '<') {
                        if (cond.charAt(4) == '=') {
                            String strMax = cond.substring(5, cond.length()).
                                    trim();
                            try {
                                int max = Integer.parseInt(strMax);
                                if (fieldValue <= max)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_less_equal", new String[]{fieldDesc, "" + max});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strMax = cond.substring(4, cond.length()).
                                    trim();
                            try {
                                int max = Integer.parseInt(strMax);
                                if (fieldValue < max)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_need_less", new String[]{fieldDesc, "" + max});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            }
        }
        if (!isValid) {
            if (onErrorExit) {
                throw new CheckErrException(msgs);
            }
        }
    }

    public void checkFieldString(String ruleStr) throws CheckErrException {
        String fieldName = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }
        fieldName = rule[1];

        String value = getFieldValue(fieldName);
        
/*      
 * 		Privilege privilege = new Privilege();
		try {
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, fieldName, value, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, fieldName, value, getClass().getName());
		} catch(Exception e) {
			addMsg("err_format", new String[] {rule[2]} );
			throw new CheckErrException(msgs);
		}*/

        checkFieldString(ruleStr, value);
    }

    public void checkFieldString(String ruleStr, String value) throws CheckErrException {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return;
        }
        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        String NULL = rule[3];

        boolean isValid = true;

        boolean isReturn = false;

        if (value == null) {
            isReturn = true;
            if (NULL.equalsIgnoreCase("not")) {
                addMsg("err_want", new String[]{fieldDesc});
            } else if (NULL.equalsIgnoreCase("empty")) {
                value = "";
            } else if (NULL.equalsIgnoreCase("allow")) {
                ;
            } else if (NULL.equalsIgnoreCase("ip")) {
                value = request.getHeader("HTTP_X_FORWARDED_FOR"); // 如果有代理
                if (value == null) {
                    value = IPUtil.getRemoteAddr(request);
                }
                // 存储field值
                Field f = new Field(fieldName, fieldDesc, value, type);
                fields.put(fieldName, f);
            } else
                value = NULL;
        } else {
            value = value.trim();
            if (value.equals("")) {
                isReturn = true;
                if (NULL.equalsIgnoreCase("not")) {
                    addMsg("err_blank", new String[]{fieldDesc});
                    isValid = false;
                }
                if (NULL.equalsIgnoreCase("empty")) {
                    Field f = new Field(fieldName, fieldDesc, value, type);
                    fields.put(fieldName, f);
                    return;
                }
            }
        }

        // 存储field值
        // LogUtil.getLog(getClass()).info("checkFieldString:" + fieldName + " " + fieldDesc + " value=" + value + " type=" + type);
        Field f = new Field(fieldName, fieldDesc, value, type);
        fields.put(fieldName, f);

        if (isReturn)
            return;

        // 规则部分，以类似email=true,xss=true,isnotcn=true的方式
        if (len >= 5) {
            // 如果长度大于5，表示有规则，而如果值为允许空且获得的值为空，则不再验证这些规则
            if (value == null) {
                return;
                // addMsg("The value of " + fieldName + " is null but has rule, please check the rule is correct!");
            }
        }
        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim().toLowerCase();
            if (cond.startsWith("email")) {
                if (value != null) {
                    String v = getCondValue(cond);
                    if (v.equals("true")) {
                        if (!StrUtil.IsValidEmail(value)) {
                            isValid = false;
                            addMsg("err_email", new String[]{fieldDesc});
                        }
                    }
                }
            } else if (cond.startsWith("isnotcn")) {
                if (value != null) {
                    String v = getCondValue(cond);
                    if (v.equals("true")) {
                        if (!StrUtil.isNotCN(value)) {
                            isValid = false;
                            addMsg("err_cn", new String[]{fieldDesc});
                        }
                    }
                }
            } else if (cond.startsWith("exclude")) {
                if (value != null) {
                    String v = getCondValue(cond);
                    String[] chars = StrUtil.split(v, "\\|");
                    int chlen = 0;
                    if (chars != null) {
                        chlen = chars.length;
                    }
                    for (int k = 0; k < chlen; k++) {
                        if (value.indexOf(chars[k]) != -1) {
                            isValid = false;
                            addMsg("err_except", new String[]{fieldDesc, chars[k]});
                        }
                    }
                }
            } else if (cond.startsWith("sql")) {
                if (value != null) {
                    String v = getCondValue(cond);
                    if (v.equals("sqlserver")) {
                        if (!SecurityUtil.isValidSqlParam(value)) {
                            isValid = false;
                            addMsg("err_sql", new String[]{fieldDesc});
                        }
                    }
                }
            } else if (cond.startsWith("isnum")) {
                if (value != null) {
                    String v = getCondValue(cond);
                    if (v.equals("true")) {
                        if (!StrUtil.isNumeric(value)) {
                            isValid = false;
                            addMsg("err_not_num", new String[]{fieldDesc});
                        }
                    }
                }
            } else if (cond.startsWith("xss")) { // 过滤跨站攻击
                if (value != null) {
                    String v = getCondValue(cond);
                    if (v.equals("true")) {
                        value = cn.js.fan.security.AntiXSS.antiXSS(value);
                    }
                }
            } else if (cond.startsWith("min")) {
                if (value != null) {
                    int valueLen = value.length();
                    // 取出符号
                    char token = cond.charAt(3);
                    if (token == '>') {
                        if (cond.charAt(4) == '=') {
                            String strLen = cond.substring(5, cond.length()).
                                    trim();
                            try {
                                int minLen = Integer.parseInt(strLen);
                                if (valueLen >= minLen)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_len_more_equal", new String[]{fieldDesc, "" + minLen});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strLen = cond.substring(4, cond.length()).
                                    trim();
                            try {
                                int minLen = Integer.parseInt(strLen);
                                if (valueLen > minLen)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_len_more", new String[]{fieldDesc, "" + minLen});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else if (token == '<') {
                        // 最小长度，不应出现<符号
                        addMsg("err_format", new String[]{cond});
                    } else if (token == '=') {
                        String strLen = cond.substring(4, cond.length()).trim();
                        try {
                            int slen = Integer.parseInt(strLen);
                            if (valueLen == slen)
                                ;
                            else {
                                isValid = false;
                                addMsg("err_len_equal", new String[]{fieldDesc, "" + slen});
                            }
                        } catch (Exception e) {
                            isValid = false;
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            } else if (cond.startsWith("max")) {
                if (value != null) {
                    int valueLen = value.length();
                    char token = cond.charAt(3);
                    // 取出符号
                    if (token == '<') {
                        if (cond.charAt(4) == '=') {
                            String strLen = cond.substring(5, cond.length()).
                                    trim();
                            try {
                                int maxLen = Integer.parseInt(strLen);
                                if (valueLen <= maxLen)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_len_less_equal", new String[]{fieldDesc, "" + maxLen});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        } else {
                            String strLen = cond.substring(4, cond.length()).
                                    trim();
                            try {
                                int maxLen = Integer.parseInt(strLen);
                                if (valueLen < maxLen)
                                    ;
                                else {
                                    isValid = false;
                                    addMsg("err_len_less", new String[]{fieldDesc, "" + maxLen});
                                }
                            } catch (Exception e) {
                                isValid = false;
                                addMsg("err_format", new String[]{cond});
                            }
                        }
                    } else if (token == '>') {
                        // 最小长度，不应出现<符号
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    } else if (token == '=') {
                        String strLen = cond.substring(4, cond.length()).trim();
                        try {
                            int slen = Integer.parseInt(strLen);
                            if (valueLen == slen)
                                ;
                            else {
                                isValid = false;
                                addMsg("err_len_less_equal", new String[]{fieldDesc, "" + slen});
                            }
                        } catch (Exception e) {
                            isValid = false;
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        isValid = false;
                        addMsg("err_format", new String[]{cond});
                    }
                }
            }
        }
        if (!isValid) {
            if (onErrorExit) {
                throw new CheckErrException(msgs);
            }
        }
    }

    public String doGetCheckJS(FormRule fr, boolean isForm) throws ErrMsgException {
        if (fr == null)
            throw new ErrMsgException(LoadString("err_formrule_none", new String[]{}));
        formResource = fr.getRes();
        return getCheckJS(fr.getRules(), isForm);
    }

    public String doGetCheckJS(FormRule fr) throws ErrMsgException {
        return doGetCheckJS(fr, true);
    }

    /**
     * @param rules  Vector
     * @param isForm boolean 是否需带有form提交事件检查
     * @return String
     */
    public String getCheckJS(Vector rules, boolean isForm) throws ErrMsgException {
        StringBuffer sb = new StringBuffer();
        Iterator ir = rules.iterator();
        String someNotNullFieldName = ""; // 非空元素
        String fieldNameFirst = ""; // 第一个元素
        while (ir.hasNext()) {
            String ruleStr = (String) ir.next();
            if (fieldNameFirst.equals("")) {
                String[] rule = split(ruleStr);
                if (rule != null) {
                    int len = rule.length;
                    if (len >= 4) {
                        if (!rule[1].equalsIgnoreCase("id")) {
                            fieldNameFirst = rule[1];
                        }
                    }
                }
            }
            if (someNotNullFieldName.equals("")) {
                String[] rule = split(ruleStr);
                if (rule != null) {
                    int len = rule.length;
                    if (len >= 4) {
                        if (!fieldNameFirst.equals(rule[1])) {
                            String NULL = rule[3];
                            if (NULL.equalsIgnoreCase("not")) {
                                // 跳过ID（可能为序列）
                                if (!rule[1].equalsIgnoreCase("id")) {
                                    someNotNullFieldName = rule[1];
                                }
                            }
                        }
                    }
                }
            }

            sb.append(getCheckJS(ruleStr));
        }

        if (isForm) {
            // 可能会出现someNotNullFieldName为空的情况，如：没有必填项，所以增加fieldNameFirst
            if (!someNotNullFieldName.equals("")) {
                sb.append("var automaticOn" + someNotNullFieldName + "Submit = " +
                        "f_" + someNotNullFieldName + ".form.onsubmit;\n");
                sb.append("f_" + someNotNullFieldName +
                        ".form.onsubmit = function() {\n");
                sb.append("var valid = automaticOn" + someNotNullFieldName +
                        "Submit();\n");
                sb.append("if(valid)\n");
                sb.append("        return true;\n");
                sb.append("else\n");
                sb.append("        return false;\n");
                sb.append("}\n");
            } else if (!fieldNameFirst.equals("")) {
                sb.append("var automaticOnFirst" + fieldNameFirst + "Submit = " +
                        "f_" + fieldNameFirst + ".form.onsubmit;\n");
                sb.append("f_" + fieldNameFirst + ".form.onsubmit = function() {\n");
                sb.append("var valid = automaticOnFirst" + fieldNameFirst +
                        "Submit();\n");
                sb.append("if(valid)\n");
                sb.append("        return true;\n");
                sb.append("else\n");
                sb.append("        return false;\n");
                sb.append("}\n");
            }
        }
        return sb.toString();
    }

    public String getCheckJS(Vector rules) throws ErrMsgException {
        return getCheckJS(rules, true);
    }

    public String getCheckJS(String rule) throws ErrMsgException {
        rule = rule.trim();
        if (rule.startsWith(TYPE_STRING)) {
            return getCheckJSOfFieldString(rule);
        } else if (rule.startsWith(TYPE_INT)) {
            return getCheckJSOfFieldInt(rule);
        } else if (rule.startsWith(TYPE_DATE)) {
            return getCheckJSOfFieldDate(rule);
        } else if (rule.startsWith(TYPE_LONG)) {
            return getCheckJSOfFieldLong(rule);
        } else if (rule.startsWith(TYPE_BOOLEAN)) {
            return getCheckJSOfFieldBoolean(rule);
        } else if (rule.startsWith(TYPE_DOUBLE)) {
            return getCheckJSOfFieldDouble(rule);
        } else if (rule.startsWith(TYPE_FLOAT)) {
            return getCheckJSOfFieldFloat(rule);
        } else {
            return "";
        }
    }

    public String getCheckJSOfFieldString(String ruleStr) {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        String NULL = rule[3];

        StringBuffer js = new StringBuffer();
        js.append("var f_" + fieldName + " = new LiveValidation('" + fieldName + "');\n");

        if ("not".equalsIgnoreCase(NULL)) {
			// 嵌套表格2字段可能在渲染后不存在，所以需判断		
            js.append("if (findObj('" + fieldName + "')) {\n");
			js.append("if ($(findObj('" + fieldName + "')).is(':hidden') || findObj('" + fieldName + "').type=='hidden') {\n");
            js.append("     if (findObj('" + fieldName + "_realshow')) {\n"); // 表单域选择宏控件
            // js.append("console.log(findObj('" + fieldName + "_realshow'));\n");
            js.append("         new LiveValidation('" + fieldName + "_realshow').add(Validate.Presence, {failureMessage:'" + LoadString("err_blank", new String[]{""}) + "'});\n");
            js.append("     }\n");
            js.append("     else if (findObj('" + fieldName + "_realname')) {\n"); // 表单域选择宏控件
            // js.append("console.log(o('" + fieldName + "_realname'));\n");
            js.append("         new LiveValidation('" + fieldName + "_realname').add(Validate.Presence, {failureMessage:'" + LoadString("err_blank", new String[]{""}) + "'});\n");
            js.append("     }\n");
            js.append("} else {\n");
            js.append("f_" + fieldName + ".add(Validate.Presence, {failureMessage:'" + LoadString("err_blank", new String[]{""}) + "'});\n");
            js.append("}\n");
            js.append("} else { console.warn('getCheckJSOfFieldString: " + fieldName + " is not exist') }\n");
        }

        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim().toLowerCase();
            if (cond.startsWith("email")) {
                String v = getCondValue(cond);
                if (v.equals("true")) {
                    js.append("f_" + fieldName + ".add(Validate.Email, {failureMessage:'" + LoadString("err_email", new String[]{fieldDesc}) + "'});\n");
                }
            } else if (cond.startsWith("isnotcn")) {
                String v = getCondValue(cond);
                if (v.equals("true")) {
                    js.append("f_" + fieldName + ".add(Validate.isNotCN, {failureMessage:'" + LoadString("err_cn", new String[]{fieldDesc}) + "'});\n");
                }
            } else if (cond.startsWith("exclude")) {
                String v = getCondValue(cond);
                String[] chars = StrUtil.split(v, "\\|");
                int chlen = 0;
                if (chars != null) {
                    chlen = chars.length;
                }
                String chs = "";
                for (int k = 0; k < chlen; k++) {
                    String c = chars[k];
                    if (c.equals("\"")) {
                        c = "\\" + c;
                    }
                    if (chs == "")
                        chs = "\"" + c + "\"";
                    else
                        chs += ",\"" + c + "\"";
                }
                js.append("f_" + fieldName + ".add(Validate.Exclusion, { within: [ " + chs + " ], partialMatch: true, failureMessage:'" + LoadString("err_except", new String[]{fieldDesc}) + "'});\n");

            } else if (cond.startsWith("sql")) {
                String v = getCondValue(cond);
                if (v.equals("sqlserver")) {
                    js.append("f_" + fieldName + ".add(Validate.isSQLInjection, { failureMessage:'" + LoadString("err_sql", new String[]{fieldDesc}) + "'});\n");
                }
            } else if (cond.startsWith("isnum")) {
                String v = getCondValue(cond);
                if (v.equals("true")) {
                    js.append("f_" + fieldName + ".add(Validate.Numericality, { failureMessage:'" + LoadString("err_not_num", new String[]{fieldDesc}) + "'});\n");
                }
            } else if (cond.startsWith("ismobile")) {
                String v = getCondValue(cond);
                if (v.equals("true")) {
                    js.append("f_" + fieldName + ".add(Validate.Mobile, { failureMessage:'" + LoadString("err_not_mobile", new String[]{fieldDesc}) + "'});\n");
                }
            }	            
            /*
            else if (cond.startsWith("xss")) { // 过滤跨站攻击，客户端不需要过滤
                String v = getCondValue(cond);
                if (v.equals("true")) {
                    value = cn.js.fan.security.AntiXSS.antiXSS(value);
                }
            }
            */
            else if (cond.startsWith("min")) {
                // 取出符号
                char token = cond.charAt(3);
                if (token == '>') {
                    if (cond.charAt(4) == '=') {
                        String strLen = cond.substring(5, cond.length()).
                                trim();
                        try {
                            int minLen = Integer.parseInt(strLen);
                            js.append("f_" + fieldName +
                                    ".add(Validate.Length, { minimum:" +
                                    minLen + ",tooShortMessage:\"" +
                                    LoadString("err_len_more_equal",
                                            new String[]{fieldDesc, "" + minLen}) +
                                    "\"});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        String strLen = cond.substring(4, cond.length()).
                                trim();
                        try {
                            // @task:暂不支持大于
                            int minLen = Integer.parseInt(strLen) + 1;
                            js.append("f_" + fieldName +
                                    ".add(Validate.Length, { minimum:" +
                                    minLen + ",tooShortMessage:\"" +
                                    LoadString("err_len_more",
                                            new String[]{fieldDesc, "" + minLen}) +
                                    "\"});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    }
                } else if (token == '<') {
                    // 最小长度，不应出现<符号
                    addMsg("err_format", new String[]{cond});
                } else if (token == '=') {
                    String strLen = cond.substring(4, cond.length()).trim();
                    try {
                        int slen = Integer.parseInt(strLen);
                        js.append("f_" + fieldName +
                                ".add(Validate.Length, { is:" +
                                slen + ",wrongLengthMessage:'" +
                                LoadString("err_len_equal",
                                        new String[]{fieldDesc, "" + slen}) +
                                "'});\n");
                    } catch (Exception e) {
                        addMsg("err_format", new String[]{cond});
                    }
                } else {
                    addMsg("err_format", new String[]{cond});
                }

            } else if (cond.startsWith("max")) {
                char token = cond.charAt(3);
                // 取出符号
                if (token == '<') {
                    if (cond.charAt(4) == '=') {
                        String strLen = cond.substring(5, cond.length()).
                                trim();
                        try {
                            int maxLen = Integer.parseInt(strLen);

                            js.append("f_" + fieldName +
                                    ".add(Validate.Length, { maximum:" +
                                    maxLen + ",tooLongMessage:'" +
                                    LoadString("err_len_less_equal",
                                            new String[]{fieldDesc, "" + maxLen}) +
                                    "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        String strLen = cond.substring(4, cond.length()).
                                trim();
                        try {
                            int maxLen = Integer.parseInt(strLen) - 1;

                            js.append("f_" + fieldName +
                                    ".add(Validate.Length, { maximum:" +
                                    maxLen + ",tooLongMessage:'" +
                                    LoadString("err_len_less",
                                            new String[]{fieldDesc, "" + maxLen}) +
                                    "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    }
                } else if (token == '>') {
                    // 最小长度，不应出现<符号
                    addMsg("err_format", new String[]{cond});
                } else if (token == '=') {
                    String strLen = cond.substring(4, cond.length()).trim();
                    try {
                        int slen = Integer.parseInt(strLen);
                        js.append("f_" + fieldName +
                                ".add(Validate.Length, { maximum:" +
                                slen + ",wrongLengthMessage:'" +
                                LoadString("err_len_less",
                                        new String[]{fieldDesc, "" + slen}) +
                                "'});\n");
                    } catch (Exception e) {
                        addMsg("err_format", new String[]{cond});
                    }
                } else {
                    addMsg("err_format", new String[]{cond});
                }
            }

        }

        return js.toString();
    }

    public String getCheckJSOfFieldInt(String ruleStr) {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }
        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        String NULL = rule[3];


        StringBuffer js = new StringBuffer();
        js.append("var f_" + fieldName + " = new LiveValidation('" + fieldName + "');\n");

        if (NULL.equalsIgnoreCase("not")) {
            js.append("f_" + fieldName + ".add(Validate.Presence, {failureMessage:'" + LoadString("err_blank", new String[]{""}) + "'});\n");
        }

        boolean isFound = false;
        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim();
            if (cond.startsWith("min")) {
                // 取出符号
                char token = cond.charAt(3);
                if (token == '>') {
                    if (cond.charAt(4) == '=') {
                        String strMin = cond.substring(5, cond.length()).trim();
                        try {
                            int min = Integer.parseInt(strMin);
                            js.append("f_" + fieldName + ".add(Validate.Numericality, { minimum:" + min + ", eq:true, tooLowMessage:'" + LoadString("err_need_more_equal", new String[]{fieldDesc, "" + min}) + "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        String strMin = cond.substring(4, cond.length()).
                                trim();
                        try {
                            int min = Integer.parseInt(strMin);
                            js.append("f_" + fieldName + ".add(Validate.Numericality, { minimum:" + min + ", eq:false, tooLowMessage:'" + LoadString("err_need_more", new String[]{fieldDesc, "" + min}) + "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    }
                    isFound = true;
                } else {
                    addMsg("err_format", new String[]{cond});
                }

            } else if (cond.startsWith("max")) {
                char token = cond.charAt(3);
                // 取出符号
                if (token == '<') {
                    if (cond.charAt(4) == '=') {
                        String strMax = cond.substring(5, cond.length()).trim();
                        try {
                            int max = Integer.parseInt(strMax);
                            js.append("f_" + fieldName + ".add(Validate.Numericality, { maximum:" + max + ", eq:true, tooHighMessage:'" + LoadString("err_need_less_equal", new String[]{fieldDesc, "" + max}) + "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        String strMax = cond.substring(4, cond.length()).trim();
                        try {
                            int max = Integer.parseInt(strMax);
                            js.append("f_" + fieldName + ".add(Validate.Numericality, { maximum:" + max + ", eq:false, tooHighMessage:'" + LoadString("err_need_less", new String[]{fieldDesc, "" + max}) + "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    }
                    isFound = true;
                } else {
                    addMsg("err_format", new String[]{cond});
                }
            }
        }
        if (!isFound) {
            js.append("f_" + fieldName + ".add(Validate.Numericality, { onlyInteger: true });\n");
        }
        return js.toString();
    }

    public String getCheckJSOfFieldDate(String ruleStr) {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }
        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        StringBuffer js = new StringBuffer();
        js.append("var f_" + fieldName + " = new LiveValidation('" + fieldName + "');\n");
        js.append("f_" + fieldName + ".add(Validate.Date);\n");

        String NULL = rule[3];
        if (NULL.equalsIgnoreCase("not")) {
            js.append("f_" + fieldName + ".add(Validate.Presence, {failureMessage:'" + LoadString("err_blank", new String[]{""}) + "'});\n");
        }
        return js.toString();
    }

    public String getCheckJSOfFieldLong(String ruleStr) {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        StringBuffer js = new StringBuffer();
        js.append("var f_" + fieldName + " = new LiveValidation('" + fieldName + "');\n");

        String NULL = rule[3];
        if (NULL.equalsIgnoreCase("not")) {
            js.append("f_" + fieldName + ".add(Validate.Presence, {failureMessage:'" + LoadString("err_blank", new String[]{""}) + "'});\n");
        }

        boolean isFound = false;
        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim();
            if (cond.startsWith("min")) {
                // 取出符号
                char token = cond.charAt(3);
                if (token == '>') {
                    if (cond.charAt(4) == '=') {
                        String strMin = cond.substring(5, cond.length()).trim();
                        try {
                            long min = Long.parseLong(strMin);
                            js.append("f_" + fieldName + ".add(Validate.Numericality, { minimum:" + min + ", eq:true, tooLowMessage:'" + LoadString("err_need_more_equal", new String[]{fieldDesc, "" + min}) + "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        String strMin = cond.substring(4, cond.length()).trim();
                        try {
                            long min = Long.parseLong(strMin);
                            js.append("f_" + fieldName + ".add(Validate.Numericality, { minimum:" + min + ", eq:false, tooLowMessage:'" + LoadString("err_need_more", new String[]{fieldDesc, "" + min}) + "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    }
                    isFound = true;
                } else {
                    addMsg("err_format", new String[]{cond});
                }
            } else if (cond.startsWith("max")) {
                char token = cond.charAt(3);
                // 取出符号
                if (token == '<') {
                    if (cond.charAt(4) == '=') {
                        String strMax = cond.substring(5, cond.length()).trim();
                        try {
                            long max = Long.parseLong(strMax);
                            js.append("f_" + fieldName +
                                    ".add(Validate.Numericality, { maximum:" +
                                    max + ", eq:true, tooHighMessage:'" +
                                    LoadString("err_need_less_equal",
                                            new String[]{fieldDesc, "" + max}) +
                                    "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        String strMax = cond.substring(4, cond.length()).trim();
                        try {
                            long max = Long.parseLong(strMax);
                            js.append("f_" + fieldName +
                                    ".add(Validate.Numericality, { maximum:" +
                                    max + ", eq:false, tooHighMessage:'" +
                                    LoadString("err_need_less",
                                            new String[]{fieldDesc, "" + max}) +
                                    "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    }
                    isFound = true;
                }

            }
        }

        if (!isFound) {
            js.append("f_" + fieldName + ".add(Validate.Numericality, { onlyInteger: true });\n");
        }
        return js.toString();
    }

    public String getCheckJSOfFieldBoolean(String ruleStr) {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        StringBuffer js = new StringBuffer();
        js.append("var f_" + fieldName + " = new LiveValidation('" + fieldName + "');\n");

        String NULL = rule[3];
        if (NULL.equalsIgnoreCase("not")) {
            js.append("f_" + fieldName + ".add(Validate.Presence, {failureMessage:'" + LoadString("err_blank", new String[]{""}) + "'});\n");
        }
        return js.toString();
    }

    public String getCheckJSOfFieldDouble(String ruleStr) {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        StringBuffer js = new StringBuffer();
        js.append("var f_" + fieldName + " = new LiveValidation('" + fieldName + "');\n");

        String NULL = rule[3];
        if (NULL.equalsIgnoreCase("not")) {
            js.append("f_" + fieldName + ".add(Validate.Presence, {failureMessage:'" + LoadString("err_blank", new String[]{""}) + "'});\n");
        }

        boolean isFound = false;
        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim();
            if (cond.startsWith("min")) {
                // 取出符号
                char token = cond.charAt(3);
                if (token == '>') {
                    if (cond.charAt(4) == '=') {
                        String strMin = cond.substring(5, cond.length()).trim();
                        try {
                            double min = Double.parseDouble(strMin);
                            js.append("f_" + fieldName + ".add(Validate.Numericality, { minimum:" + min + ", eq:true, tooLowMessage:'" + LoadString("err_need_more_equal", new String[]{fieldDesc, "" + min}) + "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        String strMin = cond.substring(4, cond.length()).trim();
                        try {
                            double min = Double.parseDouble(strMin);
                            js.append("f_" + fieldName + ".add(Validate.Numericality, { minimum:" + min + ", eq:false, tooLowMessage:'" + LoadString("err_need_more", new String[]{fieldDesc, "" + min}) + "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    }
                    isFound = true;
                } else {
                    addMsg("err_format", new String[]{cond});
                }
            } else if (cond.startsWith("max")) {
                char token = cond.charAt(3);
                // 取出符号
                if (token == '<') {
                    if (cond.charAt(4) == '=') {
                        String strMax = cond.substring(5, cond.length()).trim();
                        try {
                            double max = Double.parseDouble(strMax);
                            js.append("f_" + fieldName +
                                    ".add(Validate.Numericality, { maximum:" +
                                    max + ", eq:true, tooHighMessage:'" +
                                    LoadString("err_need_less_equal",
                                            new String[]{fieldDesc, "" + max}) +
                                    "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        String strMax = cond.substring(4, cond.length()).trim();
                        try {
                            double max = Double.parseDouble(strMax);
                            js.append("f_" + fieldName +
                                    ".add(Validate.Numericality, { maximum:" +
                                    max + ", eq:false, tooHighMessage:'" +
                                    LoadString("err_need_less",
                                            new String[]{fieldDesc, "" + max}) +
                                    "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    }
                    isFound = true;
                }
            }
        }
        if (!isFound) {
            js.append("f_" + fieldName + ".add(Validate.Numericality);\n");
        }
        return js.toString();
    }

    public String getCheckJSOfFieldFloat(String ruleStr) {
        String fieldName = "";
        String type = "";
        String fieldDesc = "";

        String[] rule = split(ruleStr);
        if (rule == null) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        int len = rule.length;
        if (len < 4) {
            addMsg("err_format", new String[]{ruleStr});
            return "";
        }

        type = rule[0];
        fieldName = rule[1];
        fieldDesc = parseFieldDesc(rule[2]);

        StringBuffer js = new StringBuffer();
        js.append("var f_" + fieldName + " = new LiveValidation('" + fieldName + "');\n");

        String NULL = rule[3];
        if (NULL.equalsIgnoreCase("not")) {
            js.append("f_" + fieldName + ".add(Validate.Presence, {failureMessage:'" + LoadString("err_blank", new String[]{""}) + "'});\n");
        }

        boolean isFound = false;
        for (int i = 4; i < len; i++) {
            String cond = rule[i].trim();
            if (cond.startsWith("min")) {
                // 取出符号
                char token = cond.charAt(3);
                if (token == '>') {
                    if (cond.charAt(4) == '=') {
                        String strMin = cond.substring(5, cond.length()).trim();
                        try {
                            float min = Float.parseFloat(strMin);
                            js.append("f_" + fieldName + ".add(Validate.Numericality, { minimum:" + min + ", eq:true, tooLowMessage:'" + LoadString("err_need_more_equal", new String[]{fieldDesc, "" + min}) + "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        String strMin = cond.substring(4, cond.length()).trim();
                        try {
                            float min = Float.parseFloat(strMin);
                            js.append("f_" + fieldName + ".add(Validate.Numericality, { minimum:" + min + ", eq:false, tooLowMessage:'" + LoadString("err_need_more", new String[]{fieldDesc, "" + min}) + "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    }
                    isFound = true;
                }
            } else if (cond.startsWith("max")) {
                char token = cond.charAt(3);
                // 取出符号
                if (token == '<') {
                    if (cond.charAt(4) == '=') {
                        String strMax = cond.substring(5, cond.length()).trim();
                        try {
                            float max = Float.parseFloat(strMax);
                            js.append("f_" + fieldName +
                                    ".add(Validate.Numericality, { maximum:" +
                                    max + ", eq:true, tooHighMessage:'" +
                                    LoadString("err_need_less_equal",
                                            new String[]{fieldDesc, "" + max}) +
                                    "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    } else {
                        String strMax = cond.substring(4, cond.length()).trim();
                        try {
                            float max = Float.parseFloat(strMax);
                            js.append("f_" + fieldName +
                                    ".add(Validate.Numericality, { maximum:" +
                                    max + ", eq:false, tooHighMessage:'" +
                                    LoadString("err_need_less",
                                            new String[]{fieldDesc, "" + max}) +
                                    "'});\n");
                        } catch (Exception e) {
                            addMsg("err_format", new String[]{cond});
                        }
                    }
                    isFound = true;
                }
            }
        }
        if (!isFound) {
            js.append("f_" + fieldName + ".add(Validate.Numericality);\n");
        }
        return js.toString();
    }

    /**
     * 取得表达式中＝号后面的值
     *
     * @param cond String
     * @return String
     */
    public String getCondValue(String cond) {
        int p = cond.indexOf("=");
        if (cond.length() > p + 1) {
            return cond.substring(p + 1, cond.length()).trim();
        } else {
            return "";
        }
    }

    public void addMsg(String str) {
        msgs.addElement(str);
    }

    public String getMessage(boolean isHtml) {
        String str = "";
        Iterator ir = msgs.iterator();
        while (ir.hasNext()) {
            if ("".equals(str)) {
                str = (String) ir.next();
            } else {
                str += "\r\n" + ir.next();
            }
        }
        if (isHtml) {
            str = StrUtil.toHtml(str);
        }
        return str;
    }

    public Vector getMsgs() {
        return this.msgs;
    }

    /**
     * 设置当检查出来错误时，是否继续检查其它域
     *
     * @param onErrorExit boolean
     */
    public void setOnErrorExit(boolean onErrorExit) {
        this.onErrorExit = onErrorExit;
    }

    public boolean getOnError() {
        return this.onErrorExit;
    }

    public void addMsg(String key, String[] ary) {
        addMsg(LoadString(key, ary));
    }

    public String LoadString(String key, String[] ary) {
        String str = SkinUtil.LoadString(request, res, key);
        return format(str, ary);
    }

    public String format(String str, String[] ary) {
        for (String s : ary) {
            str = str.replaceFirst("%s", s);
        }
        return str;
    }

    class Field {
        public String name;
        public String desc;
        public Object value;
        public String type;

        public Field(String name, String desc, Object value) {
            this.name = name;
            this.desc = desc;
            this.value = value;

            if (value instanceof Date) {
                type = TYPE_DATE;
            } else if (value instanceof Integer) {
                type = TYPE_INT;
            } else if (value instanceof String) {
                type = TYPE_STRING;
            } else if (value instanceof String[]) {
                type = TYPE_STRING_ARRAY;
            } else if (value instanceof int[]) {
                type = TYPE_INT_ARRAY;
            } else if (value instanceof Boolean) {
                type = TYPE_BOOLEAN;
            } else if (value instanceof Long) {
                type = TYPE_LONG;
            }
        }

        public Field(String name, String desc, Object value, String type) {
            this.name = name;
            this.desc = desc;
            this.value = value;
            this.type = type;
        }
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public String getFieldDesc(String field) {
        Field f = (Field) fields.get(field);
        if (f == null) {
            return "";
        } else {
            return f.desc;
        }
    }
}
