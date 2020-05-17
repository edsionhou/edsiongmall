package gmall.annotation;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    拦截器相关的自定义注解
    自定义注解内容在 反射模块学习过
 */
@Target({ElementType.TYPE, ElementType.METHOD}) //只在方法中生效
@Retention(RetentionPolicy.RUNTIME) //运行时也保留此注解
public @interface LoginRequired {
    //注解方法，使用时直接调用 LoginSuccess = ？
    boolean LoginNecessary() default  true;  // 是否 必须要登录成功

}
