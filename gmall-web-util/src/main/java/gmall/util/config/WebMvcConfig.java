package gmall.util.config;

import gmall.util.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *  拦截器              WebMvcConfigurerAdapter 类似于 springmvc.xml
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Autowired
    AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(authInterceptor).addPathPatterns("/**").excludePathPatterns("/error");
        /*
            由于静态资源找不到，springboot默认会转到/error请求，输出错误页面给前端，
            拦截器开始拦截工作--> /index
            拦截器开始检查工作/error
            所以我们希望 排除掉 拦截器去拦截/error

         */
    }
}
