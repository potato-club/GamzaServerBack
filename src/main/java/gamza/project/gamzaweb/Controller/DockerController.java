package gamza.project.gamzaweb.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DockerController {

    @GetMapping("/test")
    public ResponseEntity<String> testAPI() {
        String result = "API 통신 성공";
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
