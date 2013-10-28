/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import core.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import logic.Service.FileUpload;
import logic.user.Check;
import logic.user.Login;
import logic.user.Register;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author mark
 */
@WebServlet(name = "Service", urlPatterns = {"/Service"})
public class Service extends HttpServlet {

    private Util util = new Util();
    private static final Logger logger = Logger.getLogger(Service.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        EditCookie editcookie = new EditCookie(request, response);

        StringBuilder message = new StringBuilder();
        JNDIConnection jndi = new JNDIConnection();
        Connection conn = jndi.init();
        Statement stmt = null;
        ResultSet rs = null;

        
        try {
            try {
                stmt = conn.createStatement();
            } catch (SQLException e) {
                out.println(e + "Creates a Statement object for sending SQL statements to the database.");
                return;
            }

            String q = request.getParameter("q") != null ? request.getParameter("q") : "";

            if ("FileUpload".equals(q)) {
                FileUpload fp = new FileUpload(request, response);
            } else if ("login".equals(q)) {
                Login login = new Login(request, response, stmt);
                out.println(login.getMessage());
            } else if ("register".equals(q)) {
                Register register = new Register(request, response, conn, stmt);
                out.println(register.getMessage());
            } else if ("Quiz".equals(q)) {

                out.println(request.getParameter("id") + " ; " + request.getParameter("myPochta"));

                stmt.executeUpdate("UPDATE `quiz` SET `value` = `value`+1 WHERE `id` = '" + request.getParameter("id") + "' AND `alias` = '" + request.getParameter("myPochta") + "' LIMIT 1;");
            } else if ("rating".equals(q)) {
                Check check = new Check(request, response, stmt);

                String outJson = "";

                String vote = request.getParameter("vote"),
                        process = request.getParameter("action"),
                        id = request.getParameter("id"),
                        lastVote = request.getParameter("lastVote");
                String voteCount = "0";

                int intVote = 0;

                HttpSession session = request.getSession(true);

                JSONObject resultJson = new JSONObject();

                JSONParser parser = new JSONParser();

                JSONArray list = new JSONArray();

                switch (vote) {
                    case "top":
                        intVote = 1;
                        vote = "+1";
                        break;
                    case "down":
                        intVote = -1;
                        vote = "-1";
                        break;
                }

                ContainerFactory containerFactory = new ContainerFactory() {

                    @Override
                    public List creatArrayContainer() {
                        return new LinkedList();
                    }

                    @Override
                    public Map createObjectContainer() {
                        return new LinkedHashMap();
                    }
                };

                Blockage b = new Blockage(request.getRemoteAddr(), process, id, intVote, "blockage", stmt, out);

                if (b.getResult() != false || check.getCheck()) {

                    try {
                        stmt.executeUpdate("update `" + process + "` set `vote`=`vote`" + vote + " where `id`=" + id + " limit 1");
                    } catch (SQLException e) {
                        out.print("{\"status\":\"error\",\"message\":\"Упс!\"}");
                        logger.error(e);
                        return;
                    }

                }

                try {
                    rs = stmt.executeQuery("select `vote` from `" + process + "` where `id`=" + id + " limit 1");
                    while (rs.next()) {
                        voteCount = rs.getString("vote");

                        if (rs.getInt("vote") > 0 && "comment".equals(process)) {
                            voteCount = "+" + voteCount;
                        }

                    }
                } catch (SQLException e) {
                    out.print("{\"status\":\"good\",\"message\":\"Упс!\"}");
                    logger.error(e);
                    return;
                }

                if (b.getResult() != false) {
                    outJson = ("{\"status\":\"good\",\"message\":\"" + voteCount + "\"}");
                } else {
                    outJson = ("{\"status\":\"error\",\"message\":\"" + voteCount + "\"}");
                }

                out.print(outJson);

            } else if ("AddComment".equals(q)) {

                if (request.getParameter("id") != null) {

                    //String cname = editcookie.getCookie("name");
                    //String cemail = editcookie.getCookie("email");
                    //String name = cname != null ? cname : request.getParameter("name");
                    //String email = cemail != null ? cemail : request.getParameter("email");
                    /*
                     * try { CheckSpam cs = new CheckSpam(
                     * request.getRemoteAddr(), name, "", "Hello"); } catch
                     * (Exception ex) { System.out.println(ex); }
                     */
                    //if ((cname != null && request.getParameter("name") != null) || (cemail != null && request.getParameter("email") != null)) {
                    //    message.append("<li>Иди на хуй, бот ебаный!!!.</li>");
                    //}
                    String text = request.getParameter("text");

                    /*/ имя
                     if (name != null) {
                     if (name.equals("")) {
                     message.append("<li>Поле Имя обязательно для заполнения.</li>");
                     } else if (name.length() > 55) {
                     message.append("<li>Поле Имя не должно быть таким длинным.</li>");
                     } else if (name.length() == 1) {
                     message.append("<li>Ваше Имя состоит из одной буквы? ну на хер!</li>");
                     }
                     } else {
                     message.append("<li>Поле Имя обязательно для заполнения.</li>");
                     }


                     // e-mail

                     if (email != null) {
                     if (email.equals("")) {
                     message.append("<li>Поле E-mail обязательно для заполнения.</li>");
                     } else if (!util.checkEmail(email)) {
                     message.append("<li>Адрес электронной почты ").append(email).append(" не корректен.</li>");
                     }
                     } else {
                     message.append("<li>Поле E-mail обязательно для заполнения.</li>");
                     }*/
                    // text
                    if (text != null) {
                        text = text.trim();
                        if (text.equals("")) {
                            message.append("<li>Поле Комментарий обязательно для заполнения.</li>");
                        } else if (text.length() > 500) {
                            message.append("<li>Вы Комментарий пишете или книгу?</li>");
                        }
                    } else {
                        message.append("<li>Поле Комментарий обязательно для заполнения.</li>");
                    }

                    String idComment = "0";
                    String status = "show";

                    if (message.length() != 0) {
                        out.println("{\"status\":\"error\",\"message\":\"" + message + "\"}");
                    } else {

                        Pattern p = Pattern.compile("(((http|https):\\/\\/)?(www[.])?[a-zа-я0-9-]+\\.[a-zа-я0-9-]{2,6})");
                        Matcher m = p.matcher(text);
                        if (m.find()) {
                            status = "black";
                        }

                        text = util.lineFeed(util.specialCharactersTags(text));

                        try {
                            PreparedStatement ps = conn.prepareStatement("INSERT INTO `comment` VALUES (NULL, ?, '', '', ?, NOW(), 0, ?)", Statement.RETURN_GENERATED_KEYS);
                            ps.setInt(1, Integer.parseInt(request.getParameter("id")));
                            //ps.setString(2, name);
                            //ps.setString(3, email);
                            ps.setString(2, text);
                            ps.setString(3, status);
                            ps.executeUpdate();
                            rs = ps.getGeneratedKeys();
                            if (rs != null && rs.next()) {
                                idComment = rs.getLong(1) + "";
                            }

                        } catch (SQLException | NumberFormatException e) {
                            message.append("<li>" + e + "</li>");
                            out.println("{\"status\":\"error\",\"message\":\"" + message + "\"}");
                        }

                    }

                    if (message.length() == 0) {

                        message.append("Комментарий добавлен");

                        out.println("{\"status\":\"good\",\"message\":\"" + StringEscapeUtils.escapeEcmaScript(message.toString()).replaceAll("'", "\"") + "\"}");

                        HashMap h = new HashMap();
                        h.put("subject", "Новый отзыв в YourMood.ru");
                        h.put("to", "iormark@ya.ru");
                        h.put("url", "http://yourmood.ru/anekdot?id=" + request.getParameter("id"));
                        h.put("name", request.getRemoteAddr());
                        //h.put("email", email);

                        SendMail send = new SendMail("mail/new-comment.html", h, getServletContext().getRealPath("/"));

                        //editcookie.setCookie("name", name, null, 36);
                        //editcookie.setCookie("email", email, null, 36);
                        //editcookie.setCookie("name", name, null, 3600 * 24 * 365 * 100);
                        //editcookie.setCookie("email", email, null, 3600 * 24 * 365 * 100);
                    }

                }

            }
        } catch (Exception e) {
            out.println("{\"status\":\"good\",\"message\":\"Упс! Уже чиним. " + e + "\"}");
            logger.error("", e);

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
