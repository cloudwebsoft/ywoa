package cn.js.fan.util;

import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;
import java.io.*;
import java.net.URL;
import cn.js.fan.util.XMLProperties;
import cn.js.fan.util.StrUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.net.URLDecoder;

public class ParamConfig {
    private XMLProperties properties;
    private String fileName;
    private String filePath;
    Document doc = null;
    Element root = null;

    String encoding = "utf-8";

    public ParamConfig(String fileName) {
        this.fileName = fileName;
        URL cfgURL = getClass().getResource("/" + fileName);

        filePath = cfgURL.getFile();
        filePath = URLDecoder.decode(filePath);

        // File file = new File(filePath);

        InputStream inputStream = null;
        SAXBuilder sb = new SAXBuilder();
        try {
            Resource resource = new ClassPathResource(fileName);
            inputStream = resource.getInputStream();
            doc = sb.build(inputStream);
            root = doc.getRootElement();
            properties = new XMLProperties(fileName, doc);

            /*doc = sb.build(file);
            root = doc.getRootElement();
            properties = new XMLProperties(file, doc);*/
        } catch (JDOMException | IOException e) {
            LogUtil.getLog(getClass()).error("XMLConfig:" + e.getMessage());
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

    public Element getRootElement() {
        return root;
    }

    public FormRule getFormRule(String code) {
        Iterator ir = root.getChildren().iterator();
        Vector[] v = new Vector[2];
        v[0] = new Vector();
        v[1] = new Vector();
        boolean onErrorExit = false;
        String res = "";

        boolean isFinded = false;

        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String c = e.getAttributeValue("code");
            if (c.equals(code)) {
                res = StrUtil.getNullStr(e.getAttributeValue("res"));
                onErrorExit = StrUtil.getNullStr(e.getAttributeValue("onErrorExit")).equals("true");
                Element e1 = e.getChild("rules");
                Iterator ir1 = e1.getChildren().iterator();
                while (ir1.hasNext()) {
                    String t = ((Element) ir1.next()).getText();
                    v[0].addElement(t);
                }
                Element e2 = e.getChild("unionRules");
                Iterator ir2 = e2.getChildren().iterator();
                while (ir2.hasNext()) {
                    String t = ((Element) ir2.next()).getText();
                    v[1].addElement(t);
                }
                isFinded = true;
            }
        }

        if (!isFinded)
            return null;

        FormRule fr = new FormRule();

        fr.setOnErrorExit(onErrorExit);
        fr.setRules(v[0]);
        fr.setUnionRules(v[1]);
        fr.setRes(res);
        return fr;
    }

    public void writemodify() {
        String indent = "    ";
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding(encoding);
        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(filePath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }
}
