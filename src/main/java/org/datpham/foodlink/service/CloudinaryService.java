package org.datpham.foodlink.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface CloudinaryService {
    String uploadImage(MultipartFile file, String folderName) throws IOException;
    void deleteImage(String publicId) throws IOException;
}
