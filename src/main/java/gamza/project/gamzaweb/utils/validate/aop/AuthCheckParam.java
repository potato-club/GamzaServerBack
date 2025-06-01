package gamza.project.gamzaweb.utils.validate.aop;


import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthCheckParam {
}
