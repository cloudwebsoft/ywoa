package cn.js.fan.util;
/**
 * $RCSfile: XMLProperties.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2002/09/09 13:51:08 $
 *
 * New Jive  from Jdon.com.
 *
 * This software is the proprietary information of CoolServlets, Inc.
 * Use is subject to license terms.
 */

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.utils.CommonConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

/**
 * Provides the the ability to use simple XML property files. Each property is
 * in the form X.Y.Z, which would map to an XML snippet of:
 * <pre>
 * &lt;X&gt;
 *     &lt;Y&gt;
 *         &lt;Z&gt;someValue&lt;/Z&gt;
 *     &lt;/Y&gt;
 * &lt;/X&gt;
 * </pre>
 *
 * The XML file is passed in to the constructor and must be readable and
 * writtable. Setting property values will automatically persist those value
 * to disk.
 */
public class XMLProperties {

    private File file;
    private Document doc;
    private String fileName;
    private boolean isXml = false;

    /**
     * Parsing the XML file every time we need a property is slow. Therefore,
     * we use a Map to cache property values that are accessed more than once.
     */
    private Map propertyCache = new HashMap();

    /**
     * Creates a new XMLProperties object.
     *
     * @parm file the full path the file that properties should be read from
     *      and written to.
     */
    public void XMLPropertiesXXX(String file) {
        this.file = new File(file);
        try {
            SAXBuilder builder = new SAXBuilder();
            // Strip formatting
            DataUnformatFilter format = new DataUnformatFilter();
            builder.setXMLFilter(format);
            doc = builder.build(new File(file));
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    /*public XMLProperties(String fileName, String xml) {
        this.fileName = fileName;
        this.isXml = true;
        try {
            SAXBuilder builder = new SAXBuilder();
            DataUnformatFilter format = new DataUnformatFilter();
            builder.setXMLFilter(format);
            doc = builder.build(new InputSource(new StringReader(xml)));
        }
        catch (Exception e) {
            System.err.println("Error creating XML parser in "
                    + "PropertyManager.java");
        }
    }*/

    public XMLProperties(String fileName, Document doc, boolean isXml) {
        this.doc = doc;
        this.fileName = fileName;
        this.isXml = true;
    }

    public XMLProperties(String fileName, Document doc) {
        this.doc = doc;
        this.fileName = fileName;
    }

    public XMLProperties(File file, Document doc) {
        this.file = file;
        this.doc = doc;
    }

    /**
     * Returns the value of the specified property.
     *
     * @param name the name of the property to get.
     * @return the value of the specified property.
     */
    public String getProperty(String name) {
        if (propertyCache.containsKey(name)) {
            return (String)propertyCache.get(name);
        }

        String[] propName = parsePropertyName(name);
        // Search for this property by traversing down the XML heirarchy.
        Element element = doc.getRootElement();
        for (int i = 0; i < propName.length; i++) {
            element = element.getChild(propName[i]);
            if (element == null) {
                // This node doesn't match this part of the property name which
                // indicates this property doesn't exist so return null.
                return null;
            }
        }
        // At this point, we found a matching property, so return its value.
        // Empty strings are returned as null.
        String value = element.getText();
        if ("".equals(value)) {
            return null;
        }
        else {
            // Add to cache so that getting property next time is fast.
            if (value!=null) {
                value = value.trim();
            }
            propertyCache.put(name, value);
            return value;
        }
    }

    /**
     * 从name节点中取出属性名称为attributeName，属性值为attributeValue的text
     * @param name String
     * @param childAttributeName String
     * @param childAttributeValue String
     * @return String
     */
    public String getProperty(String name, String childAttributeName, String childAttributeValue) {
        String visualName = name + "_-_" + childAttributeName + "_-_" + childAttributeValue;

        if (propertyCache.containsKey(visualName)) {
            return (String)propertyCache.get(visualName);
        }

        String[] propName = parsePropertyName(name);
        // Search for this property by traversing down the XML heirarchy.
        Element element = doc.getRootElement();
        for (int i = 0; i < propName.length; i++) {
            element = element.getChild(propName[i]);
            if (element == null) {
                // This node doesn't match this part of the property name which
                // indicates this property doesn't exist so return null.
                return null;
            }
        }
        // At this point, we found a matching property, so return its value.
        // Empty strings are returned as null.
        String value = null;
        List list = element.getChildren();
        if (list==null) {
            return null;
        }
        Iterator ir = list.iterator();
        while (ir.hasNext()) {
            Element child = (Element)ir.next();
            String attrValue = child.getAttributeValue(childAttributeName);
            if (attrValue!=null) {
                if (attrValue.equals(childAttributeValue)) {
                    value = child.getText();
                    break;
                }
            }
        }
        if ("".equals(value)) {
            return null;
        }
        else {
            // Add to cache so that getting property next time is fast.
            if (value!=null) {
                value = value.trim();
            }
            propertyCache.put(visualName, value);
            return value;
        }
    }

    /**
     * 从name节点中取出属性名称为attributeName，属性值为attributeValue的孩子节点childName的text
     * @param name String
     * @return String
     */
    public String getProperty(String name, String childAttributeName, String childAttributeValue, String subChildName) {
        String visualName = name + "_-_" + childAttributeName + "_-_" + childAttributeValue + "_-_" + subChildName;
        if (propertyCache.containsKey(visualName)) {
            return (String)propertyCache.get(visualName);
        }

        String[] propName = parsePropertyName(name);
        // Search for this property by traversing down the XML heirarchy.
        Element element = doc.getRootElement();
        for (int i = 0; i < propName.length; i++) {
            element = element.getChild(propName[i]);
            if (element == null) {
                // This node doesn't match this part of the property name which
                // indicates this property doesn't exist so return null.
                return null;
            }
        }
        // At this point, we found a matching property, so return its value.
        // Empty strings are returned as null.
        String value = "";
        List list = element.getChildren();
        if (list==null) {
            return null;
        }
        Iterator ir = list.iterator();
        while (ir.hasNext()) {
            Element child = (Element)ir.next();
            String attrValue = child.getAttributeValue(childAttributeName);
            if (attrValue!=null) {
                if (attrValue.equals(childAttributeValue)) {
                    value = child.getChildText(subChildName);
                    break;
                }
            }
        }

        if ("".equals(value)) {
            return null;
        }
        else {
            // Add to cache so that getting property next time is fast.
            value = value.trim();
            propertyCache.put(visualName, value);
            return value;
        }
    }

    /**
     * Return all children property names of a parent property as a String array,
     * or an empty array if the if there are no children. For example, given
     * the properties <tt>X.Y.A</tt>, <tt>X.Y.B</tt>, and <tt>X.Y.C</tt>, then
     * the child properties of <tt>X.Y</tt> are <tt>A</tt>, <tt>B</tt>, and
     * <tt>C</tt>.
     *
     * @param parent the name of the parent property.
     * @return all child property values for the given parent.
     */
    public String [] getChildrenProperties(String parent) {
        String[] propName = parsePropertyName(parent);
        // Search for this property by traversing down the XML heirarchy.
        Element element = doc.getRootElement();
        for (int i = 0; i < propName.length; i++) {
            element = element.getChild(propName[i]);
            if (element == null) {
                // This node doesn't match this part of the property name which
                // indicates this property doesn't exist so return empty array.
                return new String [] { };
            }
        }
        // We found matching property, return names of children.
        List children = element.getChildren();
        int childCount = children.size();
        String [] childrenNames = new String[childCount];
        for (int i=0; i<childCount; i++) {
            childrenNames[i] = ((Element)children.get(i)).getName();
        }
        return childrenNames;
    }

    /**
     * Sets the value of the specified property. If the property doesn't
     * currently exist, it will be automatically created.
     *
     * @param name the name of the property to set.
     * @param value the new value for the property.
     */
    public void setProperty(String name, String value) {
        // Set cache correctly with prop name and value.
        propertyCache.put(name, value);

        String[] propName = parsePropertyName(name);
        // Search for this property by traversing down the XML heirarchy.
        Element element = doc.getRootElement();
        for (int i=0; i<propName.length; i++) {
            // If we don't find this part of the property in the XML heirarchy
            // we add it as a new node
            if (element.getChild(propName[i]) == null) {
                element.addContent(new Element(propName[i]));
            }
            element = element.getChild(propName[i]);
        }
        // Set the value of the property in this node.
        element.setText(value);
        // write the XML properties to disk
        saveProperties();
    }

    /**
     * 置name节点的子节点的值，属性名称为attributeName，属性值为attributeValue
     * @param name String
     * @param value String
     */
    public void setProperty(String name, String childAttributeName, String childAttributeValue, String value) {
         // Set cache correctly with prop name and value.
         String visualName = name + "_-_" + childAttributeName + "_-_" + childAttributeValue;

         propertyCache.put(visualName, value);

         String[] propName = parsePropertyName(name);
         // Search for this property by traversing down the XML heirarchy.
         Element element = doc.getRootElement();
         for (int i=0; i<propName.length; i++) {
             element = element.getChild(propName[i]);
             if (element==null) {
                 return;
             }
         }

         List list = element.getChildren();
         if (list==null) {
             return;
         }
         Iterator ir = list.iterator();
         while (ir.hasNext()) {
             Element child = (Element)ir.next();
             String attrValue = child.getAttributeValue(childAttributeName);
             if (attrValue!=null) {
                 if (attrValue.equals(childAttributeValue)) {
                     child.setText(value);
                     break;
                 }
             }
        }
         // Set the value of the property in this node.
         // write the XML properties to disk
         saveProperties();
    }

    /**
     * 置name节点的子节点的孩子childName的值，子节点属性名称为attributeName，属性值为attributeValue
     * @param name String
     * @param value String
     */
    public void setProperty(String name, String childAttributeName, String childAttributeValue, String subChildName, String value) {
         // Set cache correctly with prop name and value.
         String visualName = name + "_-_" + childAttributeName + "_-_" + childAttributeValue + "_-_" + subChildName;

         propertyCache.put(visualName, value);

         String[] propName = parsePropertyName(name);
         // Search for this property by traversing down the XML heirarchy.
         Element element = doc.getRootElement();
         for (int i=0; i<propName.length; i++) {
             element = element.getChild(propName[i]);
             if (element==null) {
                 return;
             }
         }

         List list = element.getChildren();
         if (list==null) {
             return;
         }
         Iterator ir = list.iterator();
         while (ir.hasNext()) {
             Element child = (Element)ir.next();
             String attrValue = child.getAttributeValue(childAttributeName);
             if (attrValue!=null) {
                 if (attrValue.equals(childAttributeValue)) {
                     child.getChild(subChildName).setText(value);
                     break;
                 }
             }
        }
         // Set the value of the property in this node.
         // write the XML properties to disk
         saveProperties();
    }

    /**
     * Deletes the specified property.
     *
     * @param name the property to delete.
     */
    public void deleteProperty(String name) {
        String[] propName = parsePropertyName(name);
        // Search for this property by traversing down the XML heirarchy.
        Element element = doc.getRootElement();
        for (int i=0; i<propName.length-1; i++) {
            element = element.getChild(propName[i]);
            // Can't find the property so return.
            if (element == null) {
                return;
            }
        }
        // Found the correct element to remove, so remove it...
        element.removeChild(propName[propName.length-1]);
        // .. then write to disk.
        saveProperties();
    }

    /**
     * Saves the properties to disk as an XML document. A temporary file is
     * used during the writing process for maximum safety.
     */
    private synchronized void saveProperties() {
        OutputStream out = null;
        boolean error = false;
        // Write data out to a temporary file first.
        File tempFile = null;
        try {
            String indent = "    ";
            org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
            format.setIndent(indent);
            format.setEncoding("utf-8");
            XMLOutputter outp = new XMLOutputter(format);

            if (file != null) {
                tempFile = new File(file.getParentFile(), file.getName() + ".tmp");
                // Use JDOM's XMLOutputter to do the writing and formatting. The
                // file should always come out pretty-printed.
                // XMLOutputter outputter = new XMLOutputter("    ", true);
                out = new BufferedOutputStream(new FileOutputStream(tempFile));
                outp.output(doc, out);
            }
            else {
                if (isXml) {
                    IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
                    configUtil.putXml(fileName, doc);
                }
                else {
                    // 判断是否在jar文件中运行
                    URL url = getClass().getResource("");
                    String protocol = url.getProtocol();
                    if (!CommonConstUtil.RUN_MODE_JAR.equals(protocol)) {
                        URL cfgUrl = getClass().getResource("/" + fileName);
                        String cfgpath = URLDecoder.decode(cfgUrl.getFile());

                        File f = new File(cfgpath);
                        out = new BufferedOutputStream(new FileOutputStream(f));
                        outp.output(doc, out);
                    }/* else if ("file".equals(protocol)) {
                    test = "本地运行启动";
                    }*/
                }
            }
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
            // There were errors so abort replacing the old property file.
            error = true;
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error(e);
                error = true;
            }
        }
        // No errors occured, so we should be safe in replacing the old
        if (!error && file!=null) {
            // Delete the old file so we can replace it.
            file.delete();
            // Rename the temp file. The delete and rename won't be an
            // automic operation, but we should be pretty safe in general.
            // At the very least, the temp file should remain in some form.
            tempFile.renameTo(file);
        }
    }

    /**
     * Returns an array representation of the given Jive property. Jive
     * properties are always in the format "prop.name.is.this" which would be
     * represented as an array of four Strings.
     *
     * @param name the name of the Jive property.
     * @return an array representation of the given Jive property.
     */
    private String[] parsePropertyName(String name) {
        // Figure out the number of parts of the name (this becomes the size
        // of the resulting array).
        int size = 1;
        for (int i=0; i<name.length(); i++) {
            if (name.charAt(i) == '.') {
                size++;
            }
        }
        String[] propName = new String[size];
        // Use a StringTokenizer to tokenize the property name.
        StringTokenizer tokenizer = new StringTokenizer(name, ".");
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            propName[i] = tokenizer.nextToken();
            i++;
        }
        return propName;
    }

    public void refresh() {
        propertyCache.clear();
    }
}
