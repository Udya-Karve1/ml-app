package com.sk.rk.services.service;

import com.sk.rk.services.exception.BaseRunTimeException;
import com.sk.rk.services.utils.Constants;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Service
public class LocalFileService implements StorageService {

    @Value("${file.upload.user.root.path}")
    private String rootPath;

    private static Map<String, Path> fileStorageLocationMap = new HashMap<>();

    @PostConstruct
    public void setup() {
        fileStorageLocationMap.put(Constants.FILE_TYPE_CSV, Paths.get(rootPath
                + Constants.FILE_PATH_SEPERATOR + Constants.FILE_TYPE_CSV).toAbsolutePath().normalize());
    }

    @Override
    public String uploadFile (MultipartFile file, String category, String fileName) throws URISyntaxException, IOException {
        Path fileStorageLocation = fileStorageLocationMap.get(category);
        Path targetLocation = fileStorageLocation.resolve(fileName);

        if (!targetLocation.toFile().exists()) {
            Files.createDirectories(targetLocation);
        }

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return rootPath + "/"+category+"/" + fileName;
    }

    /**
     * Upload file to folder (category), refId is the ID of source record
     * in database. This refId can be used as postfix in file name.
     *
     * @param file
     * @param refId
     * @param category
     * @return
     * @throws IOException
     */
    @Override
    public String uploadFile(MultipartFile file, String category) throws IOException {
        String fileName = getFileNameToUpload(file);
        Path fileStorageLocation = fileStorageLocationMap.get(category);
        Path targetLocation = fileStorageLocation.resolve(fileName);

        if (!targetLocation.toFile().exists()) {
            Files.createDirectories(targetLocation);
        }

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return rootPath + "/"+category+"/" + fileName;
    }

    @Override
    public String uploadUserImage (MultipartFile file, String fileName) throws IOException {
        Path fileStorageLocation = fileStorageLocationMap.get("user");
        Path targetLocation = fileStorageLocation.resolve(fileName);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    /**
     * File can be donwloaded from local storage by filename and
     * @param category
     * @param fileName
     * @return
     * @throws MalformedURLException
     */
    public Resource downloadFile(String category, String fileName) throws MalformedURLException {

        Path fileStorageLocation = fileStorageLocationMap.get(category);
        Path filePath = fileStorageLocation.resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if(resource.exists()) {
            return resource;
        } else {
            throw new BaseRunTimeException(400, "File not found " + fileName);
        }
    }

    @Override
    public String getContentType (HttpServletRequest request, Resource resource) throws IOException {
        return request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
    }
}
