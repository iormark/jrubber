/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import core.JNDIConnection;
import core.Templating;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.Creator;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
@WebServlet(name = "ServiceTmp", urlPatterns = {"/ServiceTmp"})
public class ServiceTmp extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ServiceTmp.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String q = request.getParameter("q") != null ? request.getParameter("q") : "";

        JNDIConnection jndi = null;
        Connection conn = null;
        Statement stmt = null;

        try {
            try {

                jndi = new JNDIConnection();
                conn = jndi.init();

                stmt = conn.createStatement();

            } catch (Exception e) {
                out.println("Упс! Уже чиним.");
                logger.error("", e);
                return;
            }


            Creator creator = null;

            HashMap root = new HashMap();

            try {

                if ("WidgetYandex".equals(q)) {
                    creator = new logic.Service.WidgetYandex(request, stmt);
                }


            } catch (Exception e) {
                out.println("Упс! Уже чиним.");
                logger.error("", e);
                return;
            }




            if (creator == null) {
                out.println("Четыре Ноль Четыре");
                return;
            }


            if (creator.getServerStatus() != 200) {
                response.sendError(creator.getServerStatus());
                return;
            }


            if (creator != null) {
                root.put("content", creator);
            }


            try {
                new Templating().getTemplating(getServletContext().getRealPath("/"), "/service/" + q + ".html").process(root, out);
            } catch (Exception e) {
                out.println("Упс! Уже чиним.");
                logger.error("", e);
            }
        } catch (Exception e) {
            out.println("Упс! Что то сломалось(" + e);
        } finally {
            jndi.close(stmt, null);
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
