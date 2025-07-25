package gamza.project.gamzaweb.service.impl;

import gamza.project.gamzaweb.utils.error.requestError.DockerRequestException;
import gamza.project.gamzaweb.service.Interface.NginxService;
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

        String applicationConfName = applicationName + ".conf";
        String filePath = BASIC_PATH + applicationConfName;
        String confContent = """
                server {
                    listen 80;
                    listen [::]:80;
                    server_name %s.gamza.online;
                    return 301 https://$host$request_uri;
                }
                server {
                    listen 443 ssl http2;
                    listen [::]:443 ssl http2;
                    server_name %s.gamza.online;
                    ssl_certificate /etc/letsencrypt/live/gamza.online/fullchain.pem;
                    ssl_certificate_key /etc/letsencrypt/live/gamza.online/privkey.pem;
                
                    location / {
                    proxy_pass http://gamza.online:%d;
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
}
