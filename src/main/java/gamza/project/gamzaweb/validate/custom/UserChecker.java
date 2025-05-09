package gamza.project.gamzaweb.validate.custom;

public class UserChecker extends RuntimeException {
    public UserChecker(String message) {
        super(message);
    }
}
