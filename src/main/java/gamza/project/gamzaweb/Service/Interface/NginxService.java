package gamza.project.gamzaweb.Service.Interface;

import java.io.IOException;

public interface NginxService {

    void generateNginxConf(String applicationName, int port);

    void restartNginx() throws IOException;

//    void fixedNginxConf();

}

