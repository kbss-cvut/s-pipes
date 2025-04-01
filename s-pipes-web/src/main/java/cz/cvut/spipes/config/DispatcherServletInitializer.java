package cz.cvut.spipes.config;


import cz.cvut.spipes.security.SecurityConstants;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class DispatcherServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{WebAppConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/*"};
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        System.out.println("****** Application Context Initialization ******");

//        initSecurityFilter(servletContext);
        servletContext.addListener(new RequestContextListener());
        servletContext.getSessionCookieConfig().setName(SecurityConstants.SESSION_COOKIE_NAME);
        super.onStartup(servletContext);
    }

//    private void initSecurityFilter(ServletContext servletContext) {
//        FilterRegistration.Dynamic securityFilter = servletContext.addFilter("springSecurityFilterChain",
//                DelegatingFilterProxy.class);
//        final EnumSet<DispatcherType> es = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD);
//        securityFilter.addMappingForUrlPatterns(es, true, "/*");
//    }
}
