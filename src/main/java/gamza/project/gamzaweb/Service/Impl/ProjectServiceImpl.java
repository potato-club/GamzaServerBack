package gamza.project.gamzaweb.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Image;
import gamza.project.gamzaweb.Dto.application.ApplicationRequestDto;
import gamza.project.gamzaweb.Dto.docker.ImageBuildEventDto;
import gamza.project.gamzaweb.Dto.project.*;
import gamza.project.gamzaweb.Entity.ApplicationEntity;
import gamza.project.gamzaweb.Entity.ImageEntity;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.BadRequestException;
import gamza.project.gamzaweb.Error.requestError.DockerRequestException;
import gamza.project.gamzaweb.Error.requestError.ForbiddenException;
import gamza.project.gamzaweb.Error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.Repository.ApplicationRepository;
import gamza.project.gamzaweb.Repository.ImageRepository;
import gamza.project.gamzaweb.Repository.ProjectRepository;
import gamza.project.gamzaweb.Repository.UserRepository;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import gamza.project.gamzaweb.Service.Jwt.JwtTokenProvider;
import gamza.project.gamzaweb.Validate.UserValidate;
import gamza.project.gamzaweb.dctutil.DockerDataStore;
import gamza.project.gamzaweb.dctutil.DockerProvider;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final DockerClient dockerClient = DockerDataStore.getInstance().getDockerClient();;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final ImageRepository imageRepository;
    private final UserValidate userValidate;
    private final DockerProvider dockerProvider;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void createProject(HttpServletRequest request, ProjectRequestDto dto, MultipartFile file) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity user = userRepository.findById(userId).orElseThrow();

        // 도커 파일 경로는 unzipSaveDockerfile을 통해서 zip 파일 압축 해제 후 jpa.save 됨
        try {

            ApplicationEntity application = ApplicationEntity.builder()
                    .name(dto.getApplicationName())
                    .tag(dto.getTag())
                    .internalPort(80)
                    .outerPort(dto.getOuterPort())
                    .variableKey(dto.getVariableKey())
                    .type(dto.getApplicationType())
                    .build();

            applicationRepository.save(application);

            ProjectEntity project = ProjectEntity.builder()
                    .application(application)
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .state(dto.getState())
                    .leader(user)
                    .startedDate(dto.getStartedDate())
                    .endedDate(dto.getEndedDate())
                    .build();


            projectRepository.save(project);

            Path tempDir = Files.createTempDirectory("dockerfile_project_" + project.getName()); // 지금 이부분이 오류네
            unzipAndSaveDockerfile(file, tempDir, project);

        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("Fail Created Project (DockerFile Error)", ErrorCode.FAILED_PROJECT_ERROR);
        }
        // zip not null Error
    }

    private void unzipAndSaveDockerfile(MultipartFile file, Path destDir, ProjectEntity project) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                Path newPath = zipSlipProtect(zipEntry, destDir);
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                if (zipEntry.getName().endsWith("Dockerfile")) {
                    System.out.println("Dockerfile path: " + newPath);
                    project.getApplication().updateDockerfilePath(newPath.toString());
                    projectRepository.save(project);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    private Path zipSlipProtect(ZipEntry zipEntry, Path destDir) throws IOException {
        Path targetDirResolved = destDir.resolve(zipEntry.getName());
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(destDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }
        return normalizePath;
    }


    public ProjectListResponseDto getAllProject(Pageable pageable) {
        Page<ProjectEntity> projectPage = projectRepository.findByOrderByUpdatedDateDesc(pageable);

        List<ProjectResponseDto> collect = projectPage.getContent().stream()
                .map(project -> new ProjectResponseDto(
                        project.getName(),
                        project.getDescription(),
                        project.getState(),
                        project.getStartedDate(),
                        project.getEndedDate()
                ))
                .collect(Collectors.toList());

        // ProjectListResponseDto 반환
        return ProjectListResponseDto.builder()
                .size(collect.size())
                .contents(collect)
                .build();
    }

    @Override
    public ProjectListPerResponseDto personalProject(Pageable pageable, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);

        UserEntity user = userRepository.findById(userId).orElseThrow(() ->
                new ForbiddenException("유저를 찾을 수 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION));


        Page<ProjectEntity> projects = projectRepository.findByLeaderOrderByUpdatedDateDesc(user, pageable);

        // ProjectEntity 리스트를 ProjectPerResponseDto 리스트로 변환
        List<ProjectPerResponseDto> projectDtos = projects.stream()
                .map(project -> new ProjectPerResponseDto(project.getName(), project.isApproveState()))
                .collect(Collectors.toList());

        // ProjectListPerResponseDto 생성 및 반환
        return ProjectListPerResponseDto.builder()
                .size(projectDtos.size())
                .contents(projectDtos)
                .build();
    }

    @Override
    public void updateProject(HttpServletRequest request, ProjectUpdateRequestDto dto, Long id) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);

        UserEntity user = userRepository.findById(userId).orElseThrow(() ->
                new ForbiddenException("유저를 찾을 수 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION));

        ProjectEntity project = projectRepository.findById(id).orElseThrow(() ->
                new ForbiddenException("프로젝트를 찾을 수 없습니다.", ErrorCode.FAILED_PROJECT_ERROR));

        if (!project.getLeader().getId().equals(userId)) {
            throw new ForbiddenException("이 프로젝트를 수정할 권한이 없습니다.", ErrorCode.FORBIDDEN_EXCEPTION);
        }

        project.updateProject(dto.getName(), dto.getDescription(), dto.getState(), dto.getStartedDate(), dto.getEndedDate());
        projectRepository.save(project);

    }

    @Override
    public Page<ProjectListNotApproveResponse> notApproveProjectList(HttpServletRequest request, Pageable pageable) {
//        userValidate.validateUserRole(request);

        Page<ProjectEntity> projectEntities = projectRepository.findByApproveState(false, pageable);

        return projectEntities.map(ProjectListNotApproveResponse::new);

        // 이거 리스트 만들기 전에 프로젝트 승인해주는 api 먼저 만들기
    }

    @Override
    public void approveExecutionApplication(HttpServletRequest request, Long id) {
//        userValidate.validateUserRole(request);
        ProjectEntity project = getProjectById(id);
        checkProjectApprovalState(project);

        // Docker 이미지 빌드
        buildDockerImageFromApplicationZip(request, project);
    }

    private void buildDockerImageFromApplicationZip(HttpServletRequest request, ProjectEntity project) {
        if (project.getApplication().getImageId() == null) {
            throw new BadRequestException("PROJECT ZIP PATH IS NULL", ErrorCode.FAILED_PROJECT_ERROR);
        }

        try {
            Path dockerfilePath = extractDockerfileFromZip(project.getApplication().getImageId());
            buildDockerImage(request, dockerfilePath.toFile(), project.getApplication().getName(), project.getApplication().getTag(), project.getApplication().getVariableKey() , userPk -> {
                // 이미지 빌드 성공 후 콜백
                System.out.println("Docker image built successfully: " + userPk);
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new BadRequestException("Failed to extract Dockerfile from ZIP", ErrorCode.FAILED_PROJECT_ERROR);
        }
    }

    private void buildDockerImage(HttpServletRequest request, File dockerfile, String name, @Nullable String tag, @Nullable String key, DockerProvider.DockerProviderBuildCallback callback) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);

        ImageEntity imageEntity = ImageEntity.builder()
                .user(userPk)
                .name(name)
                .variableKey(key)
                .build();
        imageRepository.save(imageEntity);

        if (name != null && tag != null && isImageExists(name, tag)) {
            throw new DockerRequestException("3001 FAILED IMAGE BUILD", ErrorCode.FAILED_IMAGE_BUILD);
        }

        executeDockerBuild(dockerfile, name, tag, key, callback, userPk);
    }

    private boolean isImageExists(String name, String tag) {
        List<Image> existingImages = dockerClient.listImagesCmd().exec();
        return existingImages.stream()
                .anyMatch(image -> image.getRepoTags() != null &&
                        Arrays.asList(image.getRepoTags()).contains(name + ":" + tag));
    }

    private void executeDockerBuild(File dockerfile, String name, @Nullable String tag, @Nullable String key, DockerProvider.DockerProviderBuildCallback callback, UserEntity userPk) {
        BuildImageCmd buildImageCmd = dockerClient.buildImageCmd(dockerfile);

        if (key != null && !key.isEmpty()) {
            buildImageCmd.withBuildArg("key", key);
        }

        try {
            buildImageCmd.exec(new BuildImageResultCallback() {
                @Override
                public void onNext(BuildResponseItem item) {
                    super.onNext(item);
                    if (item.getImageId() != null) {
                        dockerProvider.taggingImage(item.getImageId(), name, tag);
                        callback.getImageId(item.getImageId());

                        ImageBuildEventDto event = new ImageBuildEventDto(
                                userPk, item.getImageId(), name, key
                        );
                        applicationEventPublisher.publishEvent(event);
                    }
                }
            });
        } catch (Exception e) {
            throw new DockerRequestException("3001 FAILED IMAGE BUILD", ErrorCode.FAILED_IMAGE_BUILD);
        }
    }


    private Path extractDockerfileFromZip(String zipPath) throws IOException {
        Path tempDir = Files.createTempDirectory("dockerfile-extract");
        unzipDockerFile(new File(zipPath), tempDir);

        File dockerfile = new File(tempDir.toFile(), "Dockerfile");
        System.out.println(dockerfile);
        if (!dockerfile.exists()) {
            throw new BadRequestException("Dockerfile not found in the zip archive", ErrorCode.FAILED_PROJECT_ERROR);
        }
        return dockerfile.toPath();
    }

    private void unzipDockerFile(File zipFile, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                Path newPath = destDir.resolve(zipEntry.getName()).normalize();
                if (!newPath.startsWith(destDir)) {
                    throw new IOException("Bad zip entry: " + zipEntry.getName());
                }
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    private ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("4001 NOT FOUND PROJECT", ErrorCode.FAILED_PROJECT_ERROR));
    }

    private void checkProjectApprovalState(ProjectEntity project) {
        project.approveCreateProject();
        projectRepository.save(project);
    }

    private void updateProjectApprovalState(ProjectEntity project) {
        project.approveCreateProject();
        projectRepository.save(project);
    }


//    @Override
//    public void approveCreateProject(HttpServletRequest request, Long id) {
//        String token = jwtTokenProvider.resolveAccessToken(request);
//        String userRole = jwtTokenProvider.extractRole(token);
//
//        ProjectEntity project = projectRepository.findById(id)
//                .orElseThrow(()-> new BadRequestException("4001 NOT FOUND PROJECT", ErrorCode.FAILED_PROJECT_ERROR));
//
//        if(!userRole.equals("0")) {
//            throw new UnAuthorizedException("401 NOT ADMIN", ErrorCode.UNAUTHORIZED_EXCEPTION);
//        }
//
//        if(project.isApproveState()) {
//            throw new BadRequestException("4001 PROJECT ALREADY APPROVE", ErrorCode.FAILED_PROJECT_ERROR);
//        }
//
//        project.approveCreateProject();
//        projectRepository.save(project);
//
//        if(project.getZipPath() == null) {
//            throw new BadRequestException("PROJECT ZIP PATH IS NULL", ErrorCode.FAILED_PROJECT_ERROR);
//        }
//    }

}

