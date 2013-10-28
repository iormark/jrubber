/*
 * Регистрация на сайте.
 */
package logic.user;

import core.EditCookie;
import core.Util;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.Creator;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author mark
 */
public class Register {

    private String login = "", email = "", password = "", passwordConfirm = "", button = "";
    private String message = "";
    private Util util = new Util();
    private ResultSet rs = null;

    public Register(HttpServletRequest request, HttpServletResponse response, Connection conn, Statement stmt) throws Exception {

        login = request.getParameter("login");
        email = request.getParameter("email");
        password = request.getParameter("password");
        //passwordConfirm = request.getParameter("passwordConfirm");

        // имя
        if (login != null) {
            if (login.equals("")) {
                message = ("{\"status\":\"error\",\"message\":\"Придумайте пожалуйста себе логин.\"}");
                return;
            } else if (login.length() > 36) {
                message = ("{\"status\":\"error\",\"message\":\"Придумайте себе логин покороче.\"}");
                return;
            } else if (login.length() < 2) {
                message = ("{\"status\":\"error\",\"message\":\"Придумайте себе логин подлинее.\"}");
                return;
            } else {
                rs = stmt.executeQuery("SELECT * FROM `users` WHERE `login` = '" + login + "'");
                if (rs.next()) {
                    message = ("{\"status\":\"error\",\"message\":\"Этот логин занят.\"}");
                    return;
                }
            }
        } else {
            message = ("{\"status\":\"error\",\"message\":\"Придумайте пожалуйста себе логин.\"}");
            return;
        }

        if (email != null) {
            if (email.equals("")) {
                message = ("{\"status\":\"error\",\"message\":\"Необходимо указать e-mail адрес.\"}");
                return;
            } else if (!util.checkEmail(email)) {
                message = ("{\"status\":\"error\",\"message\":\"Адрес электронной почты не корректен.\"}");
                return;
            } else {
                rs = stmt.executeQuery("SELECT * FROM `users` WHERE `email` = '" + email + "'");
                if (rs.next()) {
                    message = ("{\"status\":\"error\",\"message\":\"E-mail адрес уже существует.\"}");
                    return;
                }
            }
        } else {
            message = ("{\"status\":\"error\",\"message\":\"Необходимо указать e-mail адрес.\"}");
            return;
        }

        // пароль
        if (password != null) {
            if (password.equals("")) {
                message = ("{\"status\":\"error\",\"message\":\"Придумайте пожалуйста себе пароль.\"}");
                return;
            } else if (!util.checkPassword(password)) {
                message = ("{\"status\":\"error\",\"message\":\"Пароль должен содержать от 5 до 20 символов. Можно использовать латинские буквы, цифры и символы из списка:<strong>! @ # $ % ^ &amp; * ( ) _ - + : ; , .</strong>\"}");
                return;
            }
        } else {
            message = ("{\"status\":\"error\",\"message\":\"Придумайте пожалуйста себе пароль.\"}");
            return;
        }

        if (message.length() == 0) {

            String md5Hash = Long.toString(new Date().getTime());
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(md5Hash.getBytes(), 0, md5Hash.length());
            md5Hash = new BigInteger(1, md.digest()).toString(16);
/**/
            // добовляем нового пользователя
            PreparedStatement ps = conn.prepareStatement("INSERT INTO `users` (`id`, `email`, `login`, `password`, `hash`, `created`) "
                    + "VALUES (NULL, ?, ?, MD5('" + password + "'), '" + md5Hash + "', NOW())", Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, login);
            ps.setString(2, email);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int LastInsertID = 0;
            if (rs.next()) {
                LastInsertID = rs.getInt(1);
            } else {
                message = ("{\"status\":\"error\",\"message\":\"Ошибочка вышла, простите...\"}");
                return;
            }

            //stmt.executeUpdate("UPDATE `users` SET `hash` = '" + md5Hash + "' WHERE `id` = " + LastInsertID + ";");

            EditCookie editcookie = new EditCookie(request, response);
            editcookie.setCookie("user_id", Integer.toString(LastInsertID), null, 3600 * 24 * 90);
            editcookie.setCookie("user_hash", md5Hash, null, 3600 * 24 * 90);
            

            message = ("{\"status\":\"redirect\",\"message\":\"" + login + "\"}");
        }
    }

    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getMessage() {
        return message.toString();
    }
}
