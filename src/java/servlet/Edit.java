/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import core.EditCookie;
import core.JNDIConnection;
import core.Templating;
import core.Util;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.user.Check;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
@WebServlet(name = "ServletEdit", urlPatterns = {"/svc/edit"})
public class Edit extends HttpServlet {

    private Util util = new Util();
    private static final Logger logger = Logger.getLogger(Edit.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            String realPath = getServletContext().getRealPath("/");
            JNDIConnection jndi = new JNDIConnection();
            Connection conn = jndi.init();
            Statement stmt = null;
            ResultSet rs = null;
            String statusResponse = "good", message = "";
            String dataType = request.getParameter("dataType") != null ? request.getParameter("dataType") : "json";

            try {
                try {
                    stmt = conn.createStatement();
                } catch (SQLException e) {
                    logger.error(e);
                    return;
                }

                Check check = new Check(request, response, stmt);

                String q = request.getParameter("q") != null ? request.getParameter("q") : "";

                if ("editsssssssssssssssss".equals(q) && check.getCheck()) {

                    String email = request.getParameter("email"),
                            sex = request.getParameter("sex"),
                            password = request.getParameter("password"),
                            password_new = request.getParameter("password_new"),
                            hash = request.getParameter("hash");

                    if (email != null) {
                        email = email.trim();
                        if (email.equals("")) {
                            message = ("Необходимо указать e-mail адрес.");
                        } else if (!util.checkEmail(email)) {
                            message = ("Адрес электронной почты не корректен.");
                        } else {
                            if (!email.equals(check.getUserEmail())) {
                                rs = stmt.executeQuery("SELECT * FROM `users` WHERE `email` = '" + email + "'");
                                if (rs.next()) {
                                    message = ("E-mail адрес уже занят.");
                                }
                            }
                        }
                    } else {
                        message = ("Необходимо указать e-mail адрес.");
                    }

                    if (message.length() != 0) {
                        return;
                    }

                    if (sex != null) {
                        sex = sex.trim();
                        if (!sex.equals("1") && !sex.equals("2")) {
                            message = ("Укажите пожалуйста какого вы пола.");
                        }
                    } else {
                        message = ("Укажите пожалуйста какого вы пола.");
                    }

                    if (message.length() != 0) {
                        return;
                    }

                    String password_sql = "";
                    MessageDigest md = MessageDigest.getInstance("MD5");

                    if (hash != null && password_new != null) {
                        password_new = password_new.trim();

                        if (!password_new.equals("")) {
                            if (util.checkPassword(password_new)) {
                                rs = stmt.executeQuery("SELECT hash FROM `users` WHERE `id`='" + check.getUserID() + "' AND hash='" + hash + "'");
                                if (rs.next()) {

                                    String md5Hash = Long.toString(new Date().getTime());

                                    md.reset();
                                    md.update(md5Hash.getBytes(), 0, md5Hash.length());
                                    md5Hash = new BigInteger(1, md.digest()).toString(16);

                                    stmt.executeUpdate("UPDATE `users` SET `password` = MD5('" + password_new + "'), hash='" + md5Hash + "' WHERE `id` = " + check.getUserID() + ";");

                                    EditCookie editcookie = new EditCookie(request, response);
                                    editcookie.setCookie("user_id", check.getUserID(), null, 3600 * 24 * 90);
                                    editcookie.setCookie("user_hash", md5Hash, null, 3600 * 24 * 90);

                                } else {
                                    message = ("Простите, произошла непредвиденная ошибка.");
                                }
                            } else {
                                message = ("Пароль должен содержать от 5 до 20 символов. Можно использовать латинские буквы, цифры и символы из списка: <b>! @ # $ % ^ &amp; * ( ) _ - + : ; , .</b>");
                            }
                        } else {
                            message = ("Пожалуйста, введите новый пароль.");
                        }

                    } else {

                        if (password_new != null) {
                            password_new = password_new.trim();
                            if (!password_new.equals("")) {
                                if (util.checkPassword(password_new)) {
                                    String md5Hash = password;

                                    md.reset();
                                    md.update(md5Hash.getBytes(), 0, md5Hash.length());
                                    md5Hash = new BigInteger(1, md.digest()).toString(16);

                                    rs = stmt.executeQuery("SELECT password FROM `users` WHERE `id`='" + check.getUserID() + "' AND password='" + md5Hash + "'");
                                    if (rs.next()) {
                                        password_sql = ", password=MD5('" + password_new + "')";

                                    } else {
                                        message = ("Текущий пароль не совпадает с тем что вы ввели.");
                                    }
                                } else {
                                    message = ("Пароль должен содержать от 5 до 20 символов. Можно использовать латинские буквы, цифры и символы из списка: <b>! @ # $ % ^ &amp; * ( ) _ - + : ; , .</b>");
                                }
                            }
                        } else {
                            message = ("Текущий пароль не совпадает с тем что вы ввели.");
                        }
                    }

                    if (message.length() == 0) {

                        stmt.executeUpdate("UPDATE `users` SET `email`='" + email + "', sex='" + sex + "' " + password_sql + ""
                                + " WHERE `id`='" + check.getUserID() + "';");
                    }

                    
                    
                    
                    
                    
                } else if ("comment".equals(q) && check.getCheck()) {

                    // Comments Edit
                    
                    
                    if (request.getParameter("post") != null) {
                        String text = request.getParameter("text");

                        if (text != null) {
                            text = text.trim();
                            if (text.equals("")) {
                                return;
                                //message = ("Поле Комментарий обязательно для заполнения.");
                            } else if (text.length() > 2000) {
                                return;
                                //message = ("Макс. длина комментария 500 символов.");
                            }
                        } else {
                            return;
                            //message = ("Поле Комментарий обязательно для заполнения.");
                        }

                        String status = "on";

                        if (message.length() == 0) {
                            String post = request.getParameter("post");
                            int parent = request.getParameter("parent") != null ? Integer.parseInt(request.getParameter("parent")) : 0;
                            String id = request.getParameter("id");

                            // check url 
                            Pattern p = Pattern.compile("(((http|https):\\/\\/)?(www[.])?[a-zа-я0-9-]+\\.[a-zа-я0-9-]{2,6})");
                            Matcher m = p.matcher(text);
                            if (m.find()) {
                                status = "check";
                            } else if(parent > 0) {
                                status = "reply";
                            } else {
                                //rs = stmt.executeQuery("SELECT id FROM `comment` WHERE `post` = " + post + " LIMIT 1");
                                //if (!rs.next()) {
                                //    status = "first";
                                //}
                            }
                            
                            
                            

                            try {
                                if (id == null) {
                                    PreparedStatement ps
                                            = conn.prepareStatement("INSERT INTO `comment` (id, parent, post, user, comment, created, status) VALUES "
                                            + "(" + id + ", ?, ?, ?, ?, NOW(), ?)", Statement.RETURN_GENERATED_KEYS);

                                    ps.setInt(1, parent);
                                    ps.setInt(2, Integer.parseInt(post));
                                    ps.setInt(3, Integer.parseInt(check.getUserID()));
                                    ps.setString(4, text);
                                    ps.setString(5, status);
                                    ps.executeUpdate();

                                    rs = ps.getGeneratedKeys();
                                    if (rs.next()) {
                                        id = rs.getString(1);
                                    } else {

                                        message = "<div class=\"message\">Простите пожалуйста, произошел сбой :(</div>";
                                        return;
                                    }

                                    // Если запись созданна выводим 
                                    //комментарий, через шаблон.
                                    LinkedHashMap<String, HashMap> com = new LinkedHashMap();

                                    rs = stmt.executeQuery("SELECT u.id AS user_id, u.login, c.* FROM "
                                            + "users u, comment c WHERE u.id=c.user AND c.id=" + id + " AND "
                                            + "c.post='" + post + "'");

                                    for (int i = 0; rs.next(); i++) {
                                        HashMap data = new HashMap();
                                        data.put("id", rs.getString("id"));
                                        data.put("post", post);
                                        data.put("user_auth", check.getUserID());
                                        data.put("user_id", rs.getString("user_id"));
                                        data.put("login", rs.getString("login"));
                                        data.put("comment", util.lineFeed(StringEscapeUtils.escapeHtml4(rs.getString("comment"))));
                                        data.put("created", util.dateFormat(rs.getTimestamp("created")));
                                        data.put("time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+03:00").format(rs.getTimestamp("created")));
                                        data.put("status", rs.getString("status"));
                                        data.put("vote", rs.getInt("vote") > 0 ? "+" + rs.getString("vote") : rs.getString("vote"));

                                        com.put(rs.getString("id"), data);

                                    }

                                    HashMap root = new HashMap();
                                    root.put("content", com);

                                    message = new Templating().getTemplating("user/comment-item.html", root, realPath);

                                } else {

                                    PreparedStatement ps
                                            = conn.prepareStatement("UPDATE comment SET comment=? WHERE "
                                            + " id=" + id + " AND user=" + Integer.parseInt(check.getUserID()) + "");
                                    ps.setString(1, text);
                                    ps.executeUpdate();

                                    int row = ps.executeUpdate();

                                    if (row <= 0) {
                                        message = "Ошибка доступа.";
                                    }
                                }

                            } catch (SQLException | NumberFormatException e) {
                                logger.error(e);
                                message = ("" + e + "");
                                out.println("{\"status\":\"error\",\"message\":\"" + message + "\"}");

                            }

                        }
                    }

                } else if ("comment_delete".equals(q) && check.getCheck()) {
                    String id = request.getParameter("id");
                    int rows = stmt.executeUpdate("DELETE FROM `comment` WHERE `id` = " + id + " AND user=" + Integer.parseInt(check.getUserID()) + "");
                    if (rows != 0) {
                        statusResponse = "replace";
                        message = "Сообщение удалено.";
                    } else {
                        statusResponse = "error";
                        message = "Ошибка доступа.";
                    }

                } else {
                    message = "<div class=\"message\">Пожалуйста, войдите в аккаунт или зарегистрируйтесь.</div>";
                }

            } catch (Exception e) {
                message = e.getMessage();
                out.println("{\"status\":\"error\",\"message\":\"Упс! Уже чиним. " + message + "\"}");
                logger.error("", e);

            } finally {

                jndi.close(stmt, null);

                if (dataType.equals("json")) {

                    if (message.length() != 0) {
                        out.println(("{\"status\":\"" + statusResponse + "\",\"message\":\"" + message + "\"}"));
                    } else {
                        out.println(("{\"status\":\"good\",\"message\":\"Изменения сохранены.\"}"));
                    }

                } else if (dataType.equals("html")) {
                    out.println(message);
                }

                out.close();
            }
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
