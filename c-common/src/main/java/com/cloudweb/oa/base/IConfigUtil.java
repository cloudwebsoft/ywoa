package com.cloudweb.oa.base;

import org.jdom.Document;

import java.io.InputStream;

public interface IConfigUtil {

    String getXml(String name);

    void putXml(String name, Document doc);

    Document getDocument(String fileName);

    InputStream getFile(String fileName);

    String getFilePath();

    String getAppHome();

    boolean isRunJar();
}
