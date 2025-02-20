package gamza.project.gamzaweb.Service.Impl;

import gamza.project.gamzaweb.Error.requestError.DockerRequestException;
import gamza.project.gamzaweb.Service.Interface.NginxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class NginxServiceImpl implements NginxService {

    static String BASIC_PATH = "/etc/nginx/conf.d/";

    @Override
    public void generateNginxConf(String applicationName, int port) {

        String applicationConfName = applicationName + ".conf"; // project.conf
        String filePath = BASIC_PATH + applicationConfName;
        // gamzaweb.store -> 호빈이 domain 으로 추후 수정
        //
        String confContent = """
                server {
                    listen 80;
                    listen [::]:80;
                    server_name %s.gamzaweb.store;
                    return 301 https://$host$request_uri;
                }
                server {
                    listen 443 ssl http2;
                    listen [::]:443 ssl http2;
                    server_name %s.gamzaweb.store;
                    ssl_certificate /etc/letsencrypt/live/gamzaweb.store/fullchain.pem;
                    ssl_certificate_key /etc/letsencrypt/live/gamzaweb.store/privkey.pem;
                
                    location / {
                            proxy_pass http://localhost:%d;
                    proxy_http_version 1.1;
                    proxy_set_header Upgrade $http_upgrade;
                    proxy_set_header Connection 'upgrade';
                    proxy_set_header Host $host;
                    proxy_cache_bypass $http_upgrade;
                    }
                }
                """.formatted(applicationName, applicationName, port);

        try {
            File file = new File(filePath);
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(confContent);

                System.out.println("nginx conf filePath :" + filePath);

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Fail: generate nginx.conf");
            }
        } catch (DockerRequestException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void restartNginx() {
        try {
            ProcessBuilder testConfig = new ProcessBuilder("bash", "-c", "nginx -t");
            Process testProcess = testConfig.start();
            int testExitCode = testProcess.waitFor();

            if (testExitCode != 0) {
                throw new RuntimeException("Nginx config test failed.");
            }

            ProcessBuilder reloadProcess = new ProcessBuilder("bash", "-c", "nginx -s reload");
            Process process = reloadProcess.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Nginx reloaded successfully.");
            } else {
                System.err.println("Failed to reload Nginx. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to reload Nginx", e);
        }
    }
}
