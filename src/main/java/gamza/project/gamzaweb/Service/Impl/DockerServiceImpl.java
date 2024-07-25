//package gamza.project.gamzaweb.Service.Impl;
//
//import gamza.project.gamzaweb.Service.Interface.DockerService;
//import lombok.RequiredArgsConstructor;
//import org.apache.coyote.BadRequestException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Service
//@RequiredArgsConstructor
//public class DockerServiceImpl implements DockerService {
//
//    private static final int MIN_PORT = 9000;
//    private static final int MAX_PORT = 9999;
//    private AtomicInteger currentPort = new AtomicInteger(MIN_PORT);
//
//    @Override
//    @Transactional
//    public String generateDockerfile(String name) {
//        int port = allocatePort();
//        if(port == -1) {
//            throw new NullPointerException(); // 추후 에러 커스텀으로 오류 추가
//        }
//
//        String dockerFile
//    }
//
//    private int allocatePort() {
//        int port = currentPort.getAndIncrement();
//        if(port > MAX_PORT) {
//            return -1;
//        }
//        return port;
//    }
//
//    private String generateDockerFileContent(String name, int port) {
//        return
//    }
//}
