/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.user;

import core.EditCookie;
import core.JNDIConnection;
import core.Util;
import core.XmlOptionReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
@WebServlet(name = "EditProfile", urlPatterns = {"/svc/EditProfile"})
@MultipartConfig(maxFileSize = 1024 * 1024 * 5)
public class EditProfile extends HttpServlet {

    private Util util = new Util();
    private static final Logger logger = Logger.getLogger(EditProfile.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String message = "";
        String avatar_sql = "";
        String realPath = getServletContext().getRealPath("/").replaceAll("/ROOT", "").replaceAll("[/]$", "");

        JNDIConnection jndi = new JNDIConnection();
        Connection conn = jndi.init();
        Statement stmt = null;
        ResultSet rs = null;

        EditProileImage epi = new EditProileImage();

        try {
            try {
                stmt = conn.createStatement();
            } catch (SQLException e) {
                logger.error(e);
                return;
            }

            Check check = new Check(request, response, stmt);

            if (check.getCheck()) {

                String email = request.getParameter("email"),
                        sex = request.getParameter("sex"),
                        avatar = null,
                        password = request.getParameter("password"),
                        password_new = request.getParameter("password_new"),
                        hash = request.getParameter("hash");

                for (Part p : request.getParts()) {
                    if (p.getContentType() != null) {

                        for (String name : p.getHeaderNames()) {
                            System.out.println(" " + name);
                        }
                       
                        avatar = epi.fileProcessing(
                                realPath,
                                util.getUploadedFileName(p),
                                p.getContentType(),
                                p.getInputStream(),
                                (HashMap) check.getUserMap().get("avatar"));

                        avatar_sql = avatar != null ? ",avatar='" + avatar + "'" : "";

                        message = epi.getMessage();
                        if (!message.equals("")) {
                            break;
                        }
                    }
                }

                if (email != null) {
                    email = email.trim();
                    if (email.equals("")) {
                        message = ("Необходимо указать e-mail адрес.");
                        return;
                    } else if (!util.checkEmail(email)) {
                        message = ("Адрес электронной почты не корректен.");
                        return;
                    } else {
                        if (!email.equals(check.getUserEmail())) {
                            rs = stmt.executeQuery("SELECT * FROM `users` WHERE `email` = '" + email + "'");
                            if (rs.next()) {
                                message = ("E-mail адрес уже занят.");
                                return;
                            }
                        }
                    }
                } else {
                    message = ("Необходимо указать e-mail адрес.");
                    return;
                }

                if (sex != null) {
                    sex = sex.trim();
                    if (!sex.equals("1") && !sex.equals("2")) {
                        message = ("Укажите пожалуйста какого вы пола.");
                        return;
                    }
                } else {
                    message = ("Укажите пожалуйста какого вы пола.");
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
                    stmt.executeUpdate("UPDATE `users` SET `email`='" + email + "', sex='" + sex + "' " + avatar_sql + " " + password_sql + ""
                            + " WHERE `id`='" + check.getUserID() + "';");
                }

            } else {
                message = "Ощибка доступа.";
            }

        } catch (Exception ex) {
            message = ex.getMessage();
            logger.error("", ex);
        } finally {

            jndi.close(stmt, null);

            if (message.length() != 0) {
                out.println(("{\"status\":\"error\",\"message\":\"" + message + "\"}"));
            } else {
                if (!"".equals(avatar_sql)) {
                    out.println(("{\"status\":\"redirect\",\"message\":\"/edit\"}"));
                } else {
                    out.println(("{\"status\":\"ok\",\"message\":\"Изменения сохранены.\"}"));
                }
            }
        }
        out.close();
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
