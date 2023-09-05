package com.cloudweb.oa.utils.file.check.annotation;

import com.cloudweb.oa.utils.file.check.enums.FileType;

import java.lang.annotation.*;

import static com.cloudweb.oa.utils.file.check.constant.Constant.DEFAULT_FILE_CHECK_ERROR_MESSAGE;

/**
 * @author cube.li
 * @date 2021/6/25 20:58
 * @description 文件校验切面
 * https://www.jianshu.com/p/be3f4c26c39a
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface FileCheck {

    /**
     * 校验不通过提示信息
     *
     * @return
     */
    String message() default DEFAULT_FILE_CHECK_ERROR_MESSAGE;

    /*
    校验方式
     */
    CheckType type() default CheckType.SUFFIX;

    /**
     * 支持的文件后缀
     *
     * @return
     */
    String[] supportedSuffixes() default {};

    /**
     * 支持的文件类型
     *
     * @return
     */
    FileType[] supportedFileTypes() default {};

    enum CheckType {
        /**
         * 仅校验后缀
         */
        SUFFIX,
        /**
         * 校验文件头(魔数)
         */
        MAGIC_NUMBER
    }
}