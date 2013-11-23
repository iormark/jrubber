/*
 * Фильтр обрабатывающий запросы перед каждой страницей, на сервере
 */
package core;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author mark
 */
public class HelloWorld implements Filter {

    private FilterConfig config = null;

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
    }

    public void destroy() {
        config = null;
    }

    public void doFilter(final ServletRequest request, final ServletResponse response,
            FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        response.setContentType("text/html;charset=UTF-8");



        /*
         * Из за изменения url в каталоге /dir?id=123 на /dir/123/
         * req.getServletPath() будет возвращать /dir/123/ и по этому
         * <url-pattern>*.jsp</url-pattern> не помогает в этом случае т.е фильтр
         * не вызывается при вызове /dir/123/. Из за этого пришлось сделать эту
         * проверку!
         */

        //if (req.getServletPath().matches(".+[.]jsp")
        //|| req.getServletPath().matches("^/detail/(.+)?$")
        //|| req.getServletPath().matches("^/project/(.+)?$")) {



        //System.out.println(req.getHeader("host"));
        if (!"yourmood.ru".matches(req.getHeader("Host")) && !"localhost:8094".matches(req.getHeader("Host"))) {
            resp.setStatus(301);
            resp.setHeader("Location",
                    (req.getRequestURL().toString()).replaceAll("^http://(www.)?" + req.getHeader("host"), "http://yourmood.ru")
                    + (req.getQueryString() == null ? "" : "?" + req.getQueryString()));
            resp.setHeader("Connection", "close");
        }


        HashMap redirect = new HashMap();

        redirect.put("q=kartinki.html", "/kartinki");
        //redirect.put("/best/", "/?q=best");
        //redirect.put("/new/", "/?q=new");

        //PrintWriter out = response.getWriter();


        if (req.getServletPath() != null) {
            if (req.getServletPath().matches("(.*)(kartinki.html)(.*)")) {
                resp.setStatus(301);
                resp.setHeader("Location", "/kartinki");
                resp.setHeader("Connection", "close");
            } else if (req.getServletPath().matches("(.*)(nadpisi.html)(.*)")) {
                resp.setStatus(301);
                resp.setHeader("Location", "/kartinki/8");
                resp.setHeader("Connection", "close");
            }
        }

        request.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);



    }
}