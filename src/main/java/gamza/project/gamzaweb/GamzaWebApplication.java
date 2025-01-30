package gamza.project.gamzaweb;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@OpenAPIDefinition(servers = {
        @Server(url = "/", description = "Default Server URL")
})
public class GamzaWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(GamzaWebApplication.class, args);
    }
}
