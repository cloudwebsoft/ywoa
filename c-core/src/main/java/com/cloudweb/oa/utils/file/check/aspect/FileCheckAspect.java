package com.cloudweb.oa.utils.file.check.aspect;

import cn.hutool.core.io.FileUtil;
import com.cloudweb.oa.exception.FileCheckException;
import com.cloudweb.oa.utils.file.check.annotation.FileCheck;
import com.cloudweb.oa.utils.file.check.enums.FileType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cube.li
 * @date 2021/6/25 20:58
 * @description 文件校验切面
 * https://www.jianshu.com/p/be3f4c26c39a
 */
@Component
@Slf4j
@Aspect
// @ConditionalOnProperty(prefix = "file-check", name = "enabled", havingValue = "true")
public class FileCheckAspect {

    @Before("@annotation(annotation)")
    public void before(JoinPoint joinPoint, FileCheck annotation) {

        final String[] suffixes = annotation.supportedSuffixes();
        final FileCheck.CheckType type = annotation.type();
        final FileType[] fileTypes = annotation.supportedFileTypes();
        final String message = annotation.message();

        if (ArrayUtils.isEmpty(suffixes) && ArrayUtils.isEmpty(fileTypes)) {
            return;
        }

        Object[] args = joinPoint.getArgs();

        Set<String> suffixSet = new HashSet<>(Arrays.asList(suffixes));
        for (FileType fileType : fileTypes) {
            suffixSet.add(fileType.getSuffix());
        }
        Set<FileType> fileTypeSet = new HashSet<>(Arrays.asList(fileTypes));
        for (String suffix : suffixes) {
            fileTypeSet.add(FileType.getBySuffix(suffix));
        }

        for (Object arg : args) {
            if (arg instanceof MultipartFile) {
                doCheck((MultipartFile) arg, type, suffixSet, fileTypeSet, message);
            } else if (arg instanceof MultipartFile[]) {
                for (MultipartFile file : (MultipartFile[]) arg) {
                    doCheck(file, type, suffixSet, fileTypeSet, message);
                }
            }
        }
    }

    private void doCheck(MultipartFile file, FileCheck.CheckType type, Set<String> suffixSet, Set<FileType> fileTypeSet, String message) {
        if (type == FileCheck.CheckType.SUFFIX) {
            doCheckSuffix(file, suffixSet, message);
        } else {
            doCheckMagicNumber(file, fileTypeSet, message);
        }
    }

    private void doCheckMagicNumber(MultipartFile file, Set<FileType> fileTypeSet, String message) {
        String magicNumber = readMagicNumber(file);
        for (FileType fileType : fileTypeSet) {
            if (fileType.getMagicNumber().startsWith(magicNumber)) {
                return;
            }
        }
        throw new FileCheckException(message);
    }

    private void doCheckSuffix(MultipartFile file, Set<String> suffixSet, String message) {
        String fileName = file.getOriginalFilename();
        String fileSuffix = FileUtil.extName(fileName);
        for (String suffix : suffixSet) {
            if (suffix.toUpperCase().equalsIgnoreCase(fileSuffix)) {
                return;
            }
        }
        throw new FileCheckException(message);
    }

    private String readMagicNumber(MultipartFile file) {
        InputStream is = null;
        try {
            is = file.getInputStream();
            byte[] fileHeader = new byte[4];
            is.read(fileHeader);
            return byteArray2Hex(fileHeader);
        } catch (IOException e) {
            throw new FileCheckException("读取文件失败!");
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private String byteArray2Hex(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        if (ArrayUtils.isEmpty(data)) {
            return null;
        }
        for (byte datum : data) {
            int v = datum & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        String result = stringBuilder.toString();
        log.debug("文件头: {}", result);
        return result;
    }
}