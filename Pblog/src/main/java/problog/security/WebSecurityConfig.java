package problog.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import problog.security.service.CustomUserDetailsService;

import javax.sql.DataSource;

/**
 * springSecurity核心配置类
 * @ClassName:WebSecurityConfig
 * @Author:Timelin
 **/
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig  extends WebSecurityConfigurerAdapter{

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private DataSource dataSource;

    @Bean
    public PersistentTokenRepository persistentTokenRepository(){
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

  /*  *//*
    * 返回RememberMeServices实例
    * *//*
    @Bean
    public RememberMeServices rememberMeServices(){
        JdbcTokenRepositoryImpl rememberMeTokenRepository = new JdbcTokenRepositoryImpl();
        // 此时需要设置数据源，否则无法从数据库查询验证信息
        rememberMeTokenRepository.setDataSource(dataSource);

        // 此处的key可以为任意非空值（null或“”），但必须和起前面
        PersistentTokenBasedRememberMeServices rememberMeServices = new PersistentTokenBasedRememberMeServices("INTERNAL_SECRET_KEY", userDetailsService, rememberMeTokenRepository);

        //该参数不是必须的，默认值为“remember-me" ,但如果设置必须和页面复选框的name一致
        rememberMeServices.setParameter("remember-me");
        return rememberMeServices;
    }*/


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(userDetailsService).passwordEncoder(new PasswordEncoder() {
            @Override
            public String encode(CharSequence charSequence) {
                return charSequence.toString();
            }

            @Override
            public boolean matches(CharSequence charSequence, String s) {
                return s.equals(charSequence.toString());
            }
        });
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
              // .antMatchers("/article").hasAnyRole("ROLE_USER")
            //  .antMatchers("/article").permitAll()
                .anyRequest().authenticated()
                .and()
                // 设置登录页
               .formLogin().loginPage("/login")
                // 设置登录成功和失败
               .defaultSuccessUrl("/main").permitAll()
                .failureUrl("/login")
                .and()
                .logout()
                   .logoutUrl("/loginOut")// 注销接口
                    .logoutSuccessUrl("/loginOutSuccess").permitAll()  // 注销成功跳转接口
                   // .invalidateHttpSession(true)// 指定是否在注销时让HttpSession无效

                .and()
               .rememberMe()// 自动登录
                  .tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(60*60*24*7)// 设置7天有效
               /* .rememberMeServices(rememberMeServices())
                .key("INTERNAL_SECRET_KEY")*/
                .userDetailsService(userDetailsService);

        // 关闭CSRF跨域
        http.csrf().disable();

    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        //设置拦截器忽略文件夹，可以对静态资源放行

        web.ignoring().antMatchers("/bootstrap/**","/img/**","/fonts/**","/css/**","/js/**");
    }



}
