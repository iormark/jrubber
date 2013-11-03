/*
 * Вход в учётку.
 */
package logic.user;

import core.EditCookie;
import core.Util;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.Creator;

/**
 *
 * @author mark
 */
public class Login {

    private String email = "", password = "", button = "";
    private String message = "";
    private Util util = new Util();
    private ResultSet rs = null;

    public Login(HttpServletRequest request, HttpServletResponse response, Statement stmt) throws Exception {

        email = request.getParameter("email");
        password = request.getParameter("password");

        //e-mail
        if (email != null) {
            if (email.equals("")) {
                message = ("{\"status\":\"error\",\"message\":\"Необходимо заполнить все поля.\"}");
                return;
            } else if (!util.checkEmail(email)) {
                message = ("{\"status\":\"error\",\"message\":\"E-mail адрес некорректен.\"}");
                return;
            }
        } else {
            message = ("{\"status\":\"error\",\"message\":\"Необходимо заполнить все поля.\"}");
            return;
        }

        //пароль
        if (password != null) {
            if (password.equals("")) {
                message = ("{\"status\":\"error\",\"message\":\"Необходимо заполнить все поля.\"}");
                return;
            }
        } else {
            message = ("{\"status\":\"error\",\"message\":\"Необходимо заполнить все поля.\"}");
            return;
        }

        rs = stmt.executeQuery("SELECT * FROM `users` WHERE `email` = '" + email + "'");
        if (rs.next()) {

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();

            md.update(password.getBytes(), 0, password.length());
            password = new BigInteger(1, md.digest()).toString(16);

            if (!password.equals(rs.getString("password"))) {
                message = ("{\"status\":\"error\",\"message\":\"Простите, E-mail или пароль неверны.\"}");
            } else {
                EditCookie editcookie = new EditCookie(request, response);
                editcookie.setCookie("user_id", rs.getString("id"), null, 3600 * 24 * 30);
                editcookie.setCookie("user_hash", rs.getString("hash"), null, 3600 * 24 * 30);
                message = ("{\"status\":\"redirect\",\"message\":\"/\"}");
            }

        } else {
            message = ("{\"status\":\"error\",\"message\":\"Простите, E-mail или пароль неверны.\"}");
        }

    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getMessage() {
        return message;
    }

}
