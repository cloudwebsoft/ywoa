package com.cloudweb.oa.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Component
public class ZipUtil {

    /**
     * 打包文件或目录
     *
     * @param out
     * @param path
     * @throws IOException
     * @Description:
     */
    public void zipDir(ZipOutputStream out, String path) throws IOException {
        File file = new File(path);
        if (!file.isDirectory()) {
            out.setEncoding("gbk");
            try (FileInputStream in = new FileInputStream(path)) {
                String fpath = file.getPath();
                int point = fpath.lastIndexOf(File.separator);
                fpath = fpath.substring(point + 1);
                ZipEntry entry = new ZipEntry(fpath);
                out.putNextEntry(entry);
                int nNumber;
                while ((nNumber = in.read()) != -1) {
                    out.write(nNumber);
                }
            }
        } else {
            String[] entries = file.list();
            String pathTemp = file.getPath();
            for (String entry : entries) {
                doZipDir(out, pathTemp + "/" + entry);
            }
        }
    }

    /**
     * 打包目录
     *
     * @param out
     * @param path
     * @throws IOException
     */
    public void doZipDir(ZipOutputStream out, String path)
            throws IOException {
        File file = new File(path);
        if (!file.isDirectory()) {
            String parent = file.getParent();
            FileInputStream in = new FileInputStream(path);
            String ent = parent.substring(parent.lastIndexOf(File.separator) + 1) + File.separator + file.getName();
            ZipEntry entry = new ZipEntry(ent);
            out.putNextEntry(entry);
            int nNumber;
            while ((nNumber = in.read()) != -1) {
                out.write(nNumber);
            }
            in.close();
        } else {
            String[] entries = file.list();
            String pathTemp = file.getPath();
            for (String entry : entries) {
                doZipDir(out, pathTemp + "/" + entry);
            }
        }
    }
}
