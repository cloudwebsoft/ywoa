package com.cloudweb.oa.api;

import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObjectInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface IObsService {

    void upload(String visualPath, String diskName, File localFile) throws CosClientException;

    InputStream getInputStream(String key);

    void delete(String key) throws CosClientException;

    void copy(String srcKey, String desKey, boolean isReserveLocalSrcFile) throws IOException;
}
