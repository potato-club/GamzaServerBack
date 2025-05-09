package gamza.project.gamzaweb.service.Interface;

import java.io.IOException;

public interface NginxService {

    void generateNginxConf(String applicationName, int port);
}

