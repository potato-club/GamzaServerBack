package gamza.project.gamzaweb.Controller;


import gamza.project.gamzaweb.dctutil.DockerProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/doc")
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequiredArgsConstructor
public class DockerController {

    private final DockerProvider provider;

    @GetMapping("/logs/{containerId}") // 컨테이너 로그 반환 있어야겠다
    @Operation(description = "컨테이너 로그")
    public List<String> getContainerLogs(@PathVariable("containerId") String containerId,
                                         @RequestParam(value = "lines", defaultValue = "100") int lines) {
        return provider.getContainerLogs(containerId, lines);
    }

//    @GetMapping("/update/nginx")
//    public String updateNginxConfig(@RequestParam("containerId") String containerId,
//                                    @RequestParam("port") String port,
//                                    @RequestParam("cname") String cname) {
//        return provider.updateNginxConfig(containerId, port, cname);
//    }


}