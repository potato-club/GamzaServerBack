package gamza.project.gamzaweb.Entity.Enums;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum AllowPath {

    LOGIN("/login"),
    LOGOUT("/logout"),
//    REISSUE("/reissue"),
    SWAGGER("/swagger-ui");
//    TEMP_ALL("/**");

    //우리 다 들어갈 수 있는 api 정리해서 여기다가 추가할 것

    private final String allowPath;

    AllowPath(String allowPath) {
        this.allowPath = allowPath;
    }

    private static final Set<String> ALLOWED_PATH = EnumSet.allOf(AllowPath.class)
            .stream()
            .map(AllowPath::getAllowPath)
            .collect(Collectors.toSet());

    public static boolean isAllowed(String path) {
        return ALLOWED_PATH.contains(path);
    }


}
