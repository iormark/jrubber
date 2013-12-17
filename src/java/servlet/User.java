/*
 * Авторизация пользователей на сайте.
 */
package servlet;

import core.Antibot;
import core.EditCookie;
import core.JNDIConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.user.Check;
import logic.user.Login;
import logic.user.Password;
import logic.user.Register;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
@WebServlet(name = "User", urlPatterns = {"/svc/user"})
public class User extends HttpServlet {

    private static HashMap<String, String> ip = new HashMap<>();
    private Antibot antibot = new Antibot(3, 300000, 5000, ip);
    private static final Logger logger = Logger.getLogger(User.class);
    

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        PrintWriter out = response.getWriter();

        String q = request.getParameter("q") != null ? request.getParameter("q") : "home";
        
        antibot.access(request.getRemoteAddr());
        if (antibot.getAccess() == false) {
            out.println("{\"status\":\"error\",\"message\":\"Пожалуйста, повторите попытку через 5 минут.\"}");
            return;
        }

        JNDIConnection jndi = new JNDIConnection();
        Connection conn = jndi.init();
        Statement stmt = null;

        HashMap root = new HashMap();

        try {
            try {
                stmt = conn.createStatement();
            } catch (Exception e) {
                out.println(e);
                //logger.error(e);
                //response.sendError(500);
                return;
            }

            try {

                Check check = new Check(request, response, stmt);

                //out.println(check.getUser());
                /*
                 * if (!check.getCheck()) { out.println("У вас нет прав для
                 * просмотра!"); } else { out.println("Вы авторизированны)");
                 }
                 */
                if ("login".equals(q)) {

                    Login login = new Login(request, response, stmt);
                    out.println(login.getMessage());

                } else if ("register".equals(q)) {

                    Register register = new Register(request, response, conn, stmt);
                    out.println(register.getMessage());

                } else if ("password".equals(q) && !check.getCheck()) {

                    Password password = new Password(request, response, stmt);
                    out.println(password.getMessage());

                } else if ("redirect".equals(q) && !check.getCheck()) {

                    MessageDigest md = MessageDigest.getInstance("MD5");
                    String UserID = request.getParameter("id");
                    String UserHash = request.getParameter("hash");

                    ResultSet rs = stmt.executeQuery("SELECT * FROM `users` WHERE `id` = '" + UserID + "' AND hash='" + UserHash + "'");
                    if (rs.next()) {

                        UserHash = Long.toString(new Date().getTime());

                        md.reset();
                        md.update(UserHash.getBytes(), 0, UserHash.length());
                        UserHash = new BigInteger(1, md.digest()).toString(16);

                        stmt.executeUpdate("UPDATE `users` SET `hash` = '" + UserHash + "' WHERE `id` = " + UserID + ";");

                        EditCookie editcookie = new EditCookie(request, response);
                        editcookie.setCookie("user_id", UserID, null, 3600 * 24 * 3);
                        editcookie.setCookie("user_hash", UserHash, null, 3600 * 24 * 3);

                        response.sendRedirect("/edit?hash=" + UserHash);

                    } else {
                        response.sendError(404);

                    }

                } else if ("signout".equals(q) && check.getCheck()) {

                    EditCookie editcookie = new EditCookie(request, response);
                    editcookie.setCookie("user_id", "", null, 0);
                    editcookie.setCookie("user_hash", "", null, 0);
                    response.sendRedirect(request.getHeader("referer"));

                } else {
                    response.sendRedirect("/");
                }

            } catch (Exception e) {
                logger.error("", e);
                //response.sendError(500);
            }

        } catch (Exception e) {
            logger.error("", e);
            //response.sendError(500);
        } finally {
            jndi.close(stmt, null);
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
