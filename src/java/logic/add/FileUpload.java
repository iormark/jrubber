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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.annotation.MultipartConfig;
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
@WebServlet(name = "FileUpload", urlPatterns = {"/svc/FileUpload"})
public class FileUpload extends HttpServlet {

    private static final Logger logger = Logger.getLogger(FileUpload.class);
    private Util util = new Util();
    private String message = "", additionalMessage = "";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=UTF-8");

            PrintWriter out = response.getWriter();
            EditCookie editcookie = new EditCookie(request, response);

            System.out.println("q:" + request.getAttribute("q"));
            
            String q = request.getParameter("q") != null ? request.getParameter("q") : "item";
            String id = (String) request.getParameter("id");

            // Connect with DB
            JNDIConnection jndi = new JNDIConnection();
            Connection conn = jndi.init();
            Statement stmt = conn.createStatement();

            // Information about the user
            Check check = new Check(request, response, stmt);

            // Path to the servlet
            String realPath = getServletContext().getRealPath("/").replaceAll("/ROOT", "").replaceAll("[/]$", "");

            if (!check.getCheck()) {
                out.print("{\"status\":\"error\",\"message\":\"Отказанно в доступе!\"}");
                return;
            }

            try {

                FieldCheck fc = null;
                HashSet tags = null;

                switch (q) {
                    case "header":

                        break;
                    case "article":

                        break;
                    case "item":

                        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                        calendar.setTime(new Date());
                        String loadPath = "/img/" + calendar.get(Calendar.YEAR) + "/" + calendar.get(Calendar.MONTH);
                        
                        FileUploader fu = new FileUploader(realPath, loadPath, request, response, conn, stmt, out, check);
                        additionalMessage = ",\"item\":" + fu.getInsertItem();
                        message = fu.getMessage();

                        break;
                    case "create":
                        
                        PostCreate pc = new PostCreate(request, response, conn, stmt, check);
                        fc = new FieldCheck(request, response);
                        tags = fc.checkTags(request.getParameter("tags"));
                        int insertPostId = pc.createPost();
                        pc.updateItem_post();
                        pc.createTags(tags);
                        message = pc.getMessage();
                        
                        
                        additionalMessage = ",\"post\":\""+insertPostId+"\"";
                        
                        break;
                    default:


                        break;

                }

            } catch (Exception ex) {
                message = ("" + ex.getMessage() + "");
                logger.error("", ex);
            } finally {
                jndi.close(stmt, null);

                if (message.length() == 0) {
                    out.print("{\"status\":\"ok\",\"message\":\"Сохранено\"" + additionalMessage + "}");
                } else {
                    out.print("{\"status\":\"error\",\"action\":\"" + q + "\",\"message\":\"" + message.toString().trim() + "\"" + additionalMessage + "}");
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
