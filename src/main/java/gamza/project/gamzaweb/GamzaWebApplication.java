package gamza.project.gamzaweb;

import gamza.project.gamzaweb.dctutil.DockerProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@SpringBootApplication
@EnableScheduling
public class GamzaWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(GamzaWebApplication.class, args);
    }
}
