package point.zzicback.challenge.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileStorageRepository {
    private final ResourceLoader resourceLoader;
    private final Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "zzic-uploads");

    public String store(MultipartFile file) {
        try {
            Files.createDirectories(tempDir.resolve("challenges"));
            String fileName = "challenges/" + UUID.randomUUID() + extractExtension(file.getOriginalFilename());
            Path targetPath = tempDir.resolve(fileName);
            
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return targetPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            Path filePath = tempDir.resolve(filename);
            Resource resource = resourceLoader.getResource("file:" + filePath.toString());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load file: " + filename, e);
        }
    }

    private String extractExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}
