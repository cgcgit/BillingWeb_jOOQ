/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.filter;

import es.billingweb.controller.LoginPageController;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Filter checks if LoginBean has loginIn property set to true. If it is not set
 * then request is being redirected to the login page.
 *
 * @author catuxa source:
 * http://www.itcuties.com/j2ee/jsf-2-login-filter-example/
 */
//@WebFilter("/protected/*")
public class LoginFilter implements Filter {

//    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(LoginFilter.class);
    private static final ResourceBundle PAGE_PROPERTIES = ResourceBundle.getBundle("properties.pages");
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.debug("LoginFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // Get the currentUser for session attribute
        // managed bean name is exactly the session attribute name
        LoginPageController currentUser = (LoginPageController) httpServletRequest
                .getSession().getAttribute("loginPageController");

        if (currentUser == null || !currentUser.isLoggedIn()) {
            LOGGER.warn("User is not logged in");
            //redirect to login page
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + PAGE_PROPERTIES.getString("R_WELCOME_PAGE"));

        } 
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // close resources
    }

}
