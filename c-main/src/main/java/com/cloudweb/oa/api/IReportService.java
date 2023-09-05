package com.cloudweb.oa.api;

import org.springframework.web.multipart.MultipartFile;

public interface IReportService {

    String create(String description, String privCode, String privDesc, MultipartFile file);

    String update(Integer id, String description, String privCode, String privDesc, MultipartFile file);
}
