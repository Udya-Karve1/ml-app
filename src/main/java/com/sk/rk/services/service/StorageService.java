package com.sk.rk.services.service;

import com.sk.rk.services.utils.CommonUtils;
import com.sk.rk.services.utils.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;


/**
 * An interface, have two implementations.
 * one for local storage which works on local and default profile.
 * The other works on azure blob storage. Azure storage implementation
 * works with DEV, QA and PROD profile.
 *
 * @author uday.karve
 *
 * CHPBDP-9     Add/edit user User image upload/download
 *
 */
public interface StorageService {
    String uploadFile(MultipartFile file, String category) throws URISyntaxException, IOException;

    String uploadFile(MultipartFile file, String category, String fileName) throws URISyntaxException, IOException;

    String uploadUserImage(MultipartFile file, String fileName) throws IOException;

    Resource downloadFile(String category, String fileName) throws MalformedURLException;

    default String getFileNameToUpload(MultipartFile file) {
        String multipartName = "";

        if(null != file && null != file.getOriginalFilename()) {
            multipartName = CommonUtils.replace(file.getOriginalFilename(), Constants.FILE_NAME_ESC_CHARS, "_");
        }

        String extension = FilenameUtils.getExtension(multipartName);
        multipartName = FilenameUtils.getBaseName(multipartName);
        multipartName = multipartName + "_" +  "" + "." + extension;
        return multipartName;
    }

    String getContentType(HttpServletRequest request, Resource resource) throws IOException;
}
