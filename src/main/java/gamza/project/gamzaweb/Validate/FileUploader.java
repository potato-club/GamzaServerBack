package gamza.project.gamzaweb.Validate;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import gamza.project.gamzaweb.Entity.FileEntity;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.BadRequestException;
import gamza.project.gamzaweb.Repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class FileUploader {

    private final AmazonS3Client amazonS3Client;
    private final FileRepository fileRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void upload(MultipartFile file, String dirName, ProjectEntity project) {

        try {
            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = dirName + "/" + UUID.randomUUID() + "_" + originalFileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3Client.putObject(bucket, uniqueFileName, file.getInputStream(), metadata);
            String fileUrl = amazonS3Client.getUrl(bucket, uniqueFileName).toString();

            FileEntity fileEntity =  FileEntity.builder()
                    .fileUrl(fileUrl)
                    .fileName(originalFileName)
                    .project(project)
                    .build();

            fileRepository.save(fileEntity);

        } catch (IOException e) {
            throw new BadRequestException("파일 업로드가 실패되었습니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }
    }


    public String getFileUrl(ProjectEntity project) {

        FileEntity fileEntity = fileRepository.findByProject(project);
        if(fileEntity == null) {
            return null;
        }
        return fileEntity.getFileUrl();
    }

}