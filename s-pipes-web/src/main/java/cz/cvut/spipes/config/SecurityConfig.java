package cz.cvut.spipes.config;


import cz.cvut.spipes.security.SecurityConstants;
//@Configuration
//@EnableWebSecurity(debug = false)
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig  {

    private static final String[] COOKIES_TO_DESTROY = {
            SecurityConstants.SESSION_COOKIE_NAME,
            SecurityConstants.REMEMBER_ME_COOKIE_NAME,
            SecurityConstants.CSRF_COOKIE_NAME
    };

//    @Autowired
//    private AuthenticationEntryPoint authenticationEntryPoint;
//
//    @Autowired
//    private AuthenticationFailureHandler authenticationFailureHandler;
//
//    @Autowired
//    private AuthenticationSuccessHandler authenticationSuccessHandler;
//
//    @Autowired
//    private LogoutSuccessHandler logoutSuccessHandler;

//    @Autowired
//    @Qualifier("ontologyAuthenticationProvider")
//    private AuthenticationProvider ontologyAuthenticationProvider;
//
//    @Autowired
//    @Qualifier("portalAuthenticationProvider")
//    private AuthenticationProvider portalAuthenticationProvider;

//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.authenticationProvider(ontologyAuthenticationProvider)
//            .authenticationProvider(portalAuthenticationProvider);
//    }
//
//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests().anyRequest().permitAll().and()
//            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
//            .and().headers().frameOptions().sameOrigin()
//            .and()
//            .authenticationProvider(portalAuthenticationProvider)
//                .authenticationProvider(ontologyAuthenticationProvider)
////            .addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
//                .csrf().disable()
//                .formLogin().successHandler(authenticationSuccessHandler)
//                .failureHandler(authenticationFailureHandler)
//                .loginProcessingUrl(SecurityConstants.SECURITY_CHECK_URI)
//                .usernameParameter(SecurityConstants.USERNAME_PARAM).passwordParameter(SecurityConstants.PASSWORD_PARAM)
//                .and()
//                .logout().invalidateHttpSession(true).deleteCookies(COOKIES_TO_DESTROY)
//                .logoutUrl(SecurityConstants.LOGOUT_URI).logoutSuccessHandler(logoutSuccessHandler)
//                .and().sessionManagement().maximumSessions(1);
//    }
}
