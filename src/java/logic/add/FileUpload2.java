/*
 * Загрузка картинки или анекдота.
 */
package logic.add;

import core.EditCookie;
import core.JNDIConnection;
import core.Util;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.user.Check;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
@WebServlet(name = "FileUpload2", urlPatterns = {"/svc/FileUpload2"})
public class FileUpload2 extends HttpServlet {

    private static final Logger logger = Logger.getLogger(FileUpload2.class);
    private Util util = new Util();
    private String message = "";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            response.setContentType("text/html;charset=UTF-8");

            EditCookie editcookie = new EditCookie(request, response);
            PrintWriter out = response.getWriter();
            String q = request.getParameter("q") != null ? request.getParameter("q") : "file";

            // Соединение с DB
            JNDIConnection jndi = new JNDIConnection();
            Connection conn = jndi.init();
            Statement stmt = conn.createStatement();
            Check check = new Check(request, response, stmt);

            String realPath = getServletContext().getRealPath("/").replaceAll("/ROOT", "");

            if (!check.getCheck()) {
                out.print("{\"status\":\"error\",\"message\":\"Отказанно в доступе!\"}");
                return;
            }

            try {
                request.setCharacterEncoding("UTF-8");

                FieldCheck fc = null;
                HashSet tags = null;
                System.out.println(q);
                switch (q) {
                    case "header":

                        fc = new FieldCheck(request);
                        fc.checkTags(request.getParameter("tags"));
                        fc.checkTitle(request.getParameter("title"));
                        message = fc.getMessage();

                        break;
                    case "article":

                        fc = new FieldCheck(request, response);
                        tags = fc.checkTags(request.getParameter("tags"));
                        fc.checkTitle(request.getParameter("title"));
                        message = fc.getMessage();

                        if (message.equals("")) {
                            PostCreate pc = new PostCreate(request, response, conn, stmt, check);
                            pc.createPost(true);
                            pc.createTags(tags);
                        }

                        break;
                    case "file":

                        FileUploader fu = new FileUploader(realPath, request, response, conn, stmt, out, check);
                        message = fu.getMessage();

                        break;
                    case "create":
                        PostCreate pc = new PostCreate(request, response, conn, stmt, check);
                        fc = new FieldCheck(request, response);
                        //message = fc.getMessage();

                        //if (!message.equals("")) {
                        tags = fc.checkTags(request.getParameter("tags"));
                        pc.createPost(false);
                        pc.updateItem_post();
                        pc.createTags(tags);

                        if (request.getParameter("video") != null) {
                            if (!request.getParameter("video").equals("")) {
                                pc.createPost_item(request.getParameter("video"), 99);
                            }
                        }

                        //}
                        break;

                }

            } catch (Exception ex) {
                message = ("<li>" + ex.getMessage().trim() + "</li>");
                logger.error("", ex);
            } finally {
                jndi.close(stmt, null);

                if (message.length() == 0) {
                    out.print("{\"status\":\"ok\",\"message\":\"Сохранено\"}");
                } else {
                    out.print("{\"status\":\"error\",\"action\":\"" + q + "\",\"message\":\"" + message.toString().trim() + "\"}");
                }

                System.out.println("message: " + message);

                message = "";
            }

        } catch (SQLException ex) {
            logger.error("", ex);
        } catch (Exception ex) {
            logger.error("", ex);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        JNDIConnection jndi = new JNDIConnection();
        Connection conn = jndi.init();
        Statement stmt;
        try {
            stmt = conn.createStatement();

            try {
                Autocomplete auto = new Autocomplete(request, response, conn, stmt, out);
                out.println(auto.getJson());
            } catch (Exception ex) {
                out.println(ex);
            } finally {
                jndi.close(stmt, null);
            }
        } catch (SQLException ex) {
            out.println(ex);
        }

    }
}
