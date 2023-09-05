package com.cloudwebsoft.framework.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import cn.js.fan.util.XMLProperties;
import org.jdom.Document;
import java.io.FileOutputStream;

import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import java.util.List;
import java.util.Iterator;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.KeyUnit;
import java.util.HashMap;
import org.jdom.output.Format;
import java.net.URLDecoder;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 * <p>Title: 从configDB.xml中读取表、主键、SQL语句等的信息</p>
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
public class QDBConfig {
    final String cacheGroup = "QDBConfig";

    public String fileName = "config_db.xml";
    public static final String FORM_VALIDATOR_FILE = "form_checker.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public QDBConfig() {
    }

    public QDBConfig(String fileName) {
        this.fileName = fileName;
    }

    public void init() {
        confURL = getClass().getResource("/" + fileName);

        if (!isInited) {
            xmlPath = confURL.getFile();
            xmlPath = URLDecoder.decode(xmlPath);

            InputStream inputStream = null;
            SAXBuilder sb = new SAXBuilder();
            try {
                Resource resource = new ClassPathResource(fileName);
                inputStream = resource.getInputStream();
                doc = sb.build(inputStream);
                root = doc.getRootElement();

                /*FileInputStream fin = new FileInputStream(xmlPath);
                doc = sb.build(fin);
                root = doc.getRootElement();
                fin.close();*/

                isInited = true;
            } catch (JDOMException | IOException e) {
                LogUtil.getLog(this.getClass().getName()).error(e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }
            }
        }
    }

    public Element getRootElement() {
        return root;
    }

    public void reload() {
        isInited = false;
        try  {
            QCache.getInstance().invalidateGroup(cacheGroup);
        }
        catch (Exception e) {
            LogUtil.getLog(this.getClass().getName()).error(e.getMessage());
        }
    }

    /**
     * 取得表的配置信息
     * @param objectName String
     * @return DBTable
     */
    public QDBTable getQDBTable(String objectName) {
        QDBTable dt = null;
        try {
            dt = (QDBTable)QCache.getInstance().getFromGroup(objectName, cacheGroup);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getQDBTable1:" + e.getMessage());
        }
        if (dt==null) {
            init();
            Element tables = root.getChild("tables");
            List list = tables.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String objName = child.getAttributeValue("objName");
                    // LogUtil.getLog(getClass()).info("getQDBTable: objName=" + objName + " objectName=" + objectName);
                    if (objName.equals(objectName)) {
                        String name = child.getAttributeValue("name");
                        String create = child.getChildText("create");
                        String load = child.getChildText("load");
                        String queryList = child.getChildText("list");
                        String save = child.getChildText("save");
                        String del = child.getChildText("del");
                        
                		// 去掉换行回车制表
                        create = create.replaceAll("[\\t\\n\\r]", " ");	                        
                        load = load.replaceAll("[\\t\\n\\r]", " ");	                        
                        queryList = queryList.replaceAll("[\\t\\n\\r]", " ");	                        
                        save = save.replaceAll("[\\t\\n\\r]", " ");	                        
                        del = del.replaceAll("[\\t\\n\\r]", " ");	                        
                        
                        String connName = StrUtil.getNullStr(child.getChildText("connName"));
                        if ("".equals(connName)) {
                            connName = Global.getDefaultDB();
                        }

                        String sBlockSize = StrUtil.getNullStr(child.getChildText("blockSize"));
                        int blockSize;
                        try {
                            blockSize = Integer.parseInt(sBlockSize);
                        }
                        catch (Exception e) {
                            LogUtil.getLog(getClass()).error(e);
                            blockSize = 100;
                        }

                        String formValidatorFile = StrUtil.getNullStr(child.getChildText("formValidatorFile"));
                        if ("".equals(formValidatorFile)) {
                            formValidatorFile = FORM_VALIDATOR_FILE;
                        }

                        boolean objCachable = !StrUtil.getNullStr(child.getChildText("objCachable")).equals("false");
                        boolean listCachable = !StrUtil.getNullStr(child.getChildText("listCachable")).equals("false");

                        dt = new QDBTable(name, objName);
                        dt.setQueryCreate(create);
                        dt.setQueryLoad(load);
                        dt.setQueryList(queryList);
                        dt.setQuerySave(save);
                        dt.setQueryDel(del);
                        dt.setConnName(connName);
                        dt.setBlockSize(blockSize);
                        dt.setFormValidatorFile(formValidatorFile);
                        dt.setObjCachable(objCachable);
                        dt.setListCachable(listCachable);

                        Element sqls = child.getChild("sqls");
                        if (sqls!=null) {
                            List sqlList = sqls.getChildren("sql");
                            if (sqlList!=null) {
                                Iterator sqlIr = sqlList.iterator();
                                while (sqlIr.hasNext()) {
                                    Element sql = (Element)sqlIr.next();
                                    String sqlName = sql.getAttributeValue("name");
                                    
                                    String sqlText = sql.getText();
                                    sqlText = sqlText.replaceAll("[\\t\\n\\r]", " ");	                                                            
                                    
                                    dt.sqls.put(sqlName, sqlText);
                                }
                            }
                        }

                        Element pk = child.getChild("primaryKey");
                        String pkType = pk.getAttributeValue("type");
                        if (pkType.equalsIgnoreCase("String")) {
                            Element unit = pk.getChild("unit");
                            String pkName = unit.getChildText("name");
                            dt.setPrimaryKey(new PrimaryKey(pkName, PrimaryKey.TYPE_STRING));
                        } else if (pkType.equals("int")) {
                            Element unit = pk.getChild("unit");
                            String pkName = unit.getChildText("name");
                            dt.setPrimaryKey(new PrimaryKey(pkName, PrimaryKey.TYPE_INT));
                        } else if (pkType.equals("long")) {
                            Element unit = pk.getChild("unit");
                            String pkName = unit.getChildText("name");
                            dt.setPrimaryKey(new PrimaryKey(pkName, PrimaryKey.TYPE_LONG));
                        }
                        else if (pkType.equals("Date")) {
                            Element unit = pk.getChild("unit");
                            String pkName = unit.getChildText("name");
                            dt.setPrimaryKey(new PrimaryKey(pkName, PrimaryKey.TYPE_DATE));
                        }
                        else if (pkType.equals("compound")) {
                            List listpmk = pk.getChildren("unit");
                            Iterator irunit = listpmk.iterator();
                            HashMap key = new HashMap();
                            int orders = 0;
                            while (irunit.hasNext()) {
                                Element e = (Element) irunit.next();
                                String keyName = e.getChildTextTrim("name");
                                String keyType = e.getChildTextTrim("type");
                                if ("String".equalsIgnoreCase(keyType)) {
                                    key.put(keyName, new KeyUnit(PrimaryKey.TYPE_STRING, orders));
                                } else if ("int".equals(keyType)) {
                                    key.put(keyName, new KeyUnit(PrimaryKey.TYPE_INT, orders));
                                } else if ("long".equals(keyType)) {
                                    key.put(keyName, new KeyUnit(PrimaryKey.TYPE_LONG, orders));
                                } else if ("Date".equals(keyType)) {
                                    key.put(keyName, new KeyUnit(PrimaryKey.TYPE_DATE, orders));
                                } else {
                                    LogUtil.getLog(getClass()).info("getDBTable: 解析表" + name + "的主键时，type=" + keyType + " 未知!");
                                }
                                orders++;
                            }
                            dt.primaryKey = new PrimaryKey(key);
                        } else {
                            LogUtil.getLog(getClass()).error("getDBTable: 解析表" + name + "的主键时，type=" + pkType + " 未知!");
                        }
                        try {
                            QCache.getInstance().putInGroup(objName, cacheGroup, dt);
                        } catch (Exception e) {
                            LogUtil.getLog(getClass()).error("getDBTable:" + e.getMessage());
                        }

                        break;
                    }
                }
            }
        }

        return dt;
    }

    public void writemodify() {
        String indent = "    ";
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");

        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(xmlPath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
    }

}


