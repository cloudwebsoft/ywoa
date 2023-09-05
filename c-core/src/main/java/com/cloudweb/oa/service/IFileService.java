package com.cloudweb.oa.service;

import com.redmoon.kit.util.FileInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public interface IFileService {

    void write(FileInfo fileInfo, String visualPath, boolean isRand);

    File write(FileInfo fileInfo, String visualPath, boolean isRand, boolean isDelLocalFile);

    void write(MultipartFile file, String visualPath, String diskName) throws IOException;

    void upload(String filePath, String visualPath, String diskName);

    boolean del(String visualPath, String diskName);

    boolean del(String path);

    void download(HttpServletResponse response, String fileName, String filePath) throws IOException;

    void download(HttpServletResponse response, String fileName, String visualPath, String diskName) throws IOException;

    void write(FileInfo fileInfo, String visualPath);

    void write(String localFilePath, String visualPath, String diskName);

    void write(String localFilePath, String visualPath, String diskName, boolean isLocalFileDelByConfig);

    void preview(HttpServletResponse response, String filePath) throws IOException;

    void copy(String srcVisualPath, String srcDiskName, String visualPath, String diskName) throws IOException;

    void copyToLocalFile(String fileName, String visualPath, String diskName, String localDirPath) throws IOException;

}
