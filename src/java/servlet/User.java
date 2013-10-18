/*
 * Авторизация пользователей на сайте.
 */
package servlet;

import core.EditCookie;
import core.JNDIConnection;
import core.Templating;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.Creator;
import logic.user.Check;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
@WebServlet(name = "User", urlPatterns = {"/user"})
public class User extends HttpServlet {

    //private Memory memory = new Memory();
    private static final Logger logger = Logger.getLogger(Main.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();


        String q = request.getParameter("q") != null ? request.getParameter("q") : "home";



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

            Creator creator = null;


            try {

                Check check = new Check(request, response, stmt);

                //out.println(check.getUser());


                /*
                 * if (!check.getCheck()) { out.println("У вас нет прав для
                 * просмотра!"); } else { out.println("Вы авторизированны)");
                }
                 */


                if ("login".equals(q)) {

                    creator = new logic.user.Login(request, response, stmt);

                    if (check.getCheck()) {
                        response.sendRedirect("/user?q=home");
                    }

                } else if ("register".equals(q) && !check.getCheck()) {

                    creator = new logic.user.Register(request, response, stmt);

                } else if ("password".equals(q) && !check.getCheck()) {
                    //creator = new logic.user.Password(request, response, stmt, memory);
                } else if ("reset".equals(q) && check.getCheck()) {
                    //creator = new logic.user.Reset(check.getUserID(), request, response, stmt, memory.getCfg());
                } else if ("redirect".equals(q) && !check.getCheck()) {

                    MessageDigest md = MessageDigest.getInstance("MD5");
                    String UserID = request.getParameter("id");
                    String UserHash = request.getParameter("hash");

                    ResultSet rs = stmt.executeQuery("SELECT * FROM `users` WHERE `id` = '" + UserID + "'");
                    if (rs.next()) {

                        if (!UserHash.equals(rs.getString("hash"))) {
                            response.sendError(404);
                        } else {

                            UserHash = Long.toString(new Date().getTime());

                            md.reset();
                            md.update(UserHash.getBytes(), 0, UserHash.length());
                            UserHash = new BigInteger(1, md.digest()).toString(16);


                            stmt.executeUpdate("UPDATE `users` SET `hash` = '" + UserHash + "' WHERE `id` = " + UserID + ";");


                            EditCookie editcookie = new EditCookie(request, response);
                            editcookie.setCookie("user_id", UserID, null, 3600 * 24 * 3);
                            editcookie.setCookie("user_hash", UserHash, null, 3600 * 24 * 3);


                            response.sendRedirect("/user?q=reset");

                        }


                    } else {
                        response.sendError(404);

                    }

                    return;
                } else if ("signout".equals(q) && check.getCheck()) {

                    EditCookie editcookie = new EditCookie(request, response);
                    editcookie.setCookie("user_id", "", null, 0);
                    editcookie.setCookie("user_hash", "", null, 0);

                    response.sendRedirect("/");

                    return;
                } else if (q.startsWith("home") && check.getCheck()) {
                    creator = new logic.user.Home();
                }
                /*
                 * else if (q.startsWith("profile/reset/") && !check.getCheck())
                 * {
                 *
                 * creator = new logic.user.Profile(request, response, stmt,
                 * memory); q = "profile";
                 *
                 * }
                 */
            } catch (Exception e) {

                logger.error("", e);
                response.sendError(500);
                return;
            }


            if (creator == null) {
                response.sendRedirect("/user?q=login");
                return;
            }

            if (creator.getServerStatus() != 200) {
                response.sendError(creator.getServerStatus());
                return;
            }


            //root.put("memory", memory);

            root.put("content_tpl", "" + q + ".html");
            root.put("title", creator.getMetaTitle() + " | ");
            root.put("content", creator);

            root.put("footer", new SimpleDateFormat("yyyy").format(new Date()));


            try {

                new Templating().getTemplating(getServletContext().getRealPath("/"), "user/index.html").process(root, out);

            } catch (Exception e) {
                out.println(e);
            }

        } catch (Exception e) {
            logger.error("", e);
            response.sendError(500);
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
