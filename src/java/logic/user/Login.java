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
public class Login extends Creator {
    
    private String email = "", password = "", button = "";
    private StringBuilder message = new StringBuilder();
    private Util util = new Util();
    private ResultSet rs = null;

    public Login(HttpServletRequest request, HttpServletResponse response, Statement stmt) throws Exception {
        
        button = request.getParameter("button");
        
        if (button != null) {
            if (button.equals("Войти")) {

                email = request.getParameter("email");
                password = request.getParameter("password");
            }
            
            
            //e-mail
            
            if (email != null) {
                if (email.equals("")) {
                    message.append("<li>Поле E-mail обязательно для заполнения.</li>");
                } else if (!util.checkEmail(email)) {
                    message.append("<li>Адрес электронной почты ").append(email).append(" не корректен.</li>");
                }
            } else {
                message.append("<li>Поле E-mail обязательно для заполнения.</li>");
            }
            

            //пароль
            
            if (password != null) {
                if (password.equals("")) {
                    message.append("<li>Поле Пароль обязательно для заполнения.</li>");
                }
            } else {
                message.append("<li>Поле Пароль обязательно для заполнения.</li>");
            }

            
            
            if (message.length() == 0) {

                rs = stmt.executeQuery("SELECT * FROM `users` WHERE `email` = '" + email + "'");
                if (rs.next()) {

                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.reset();

                    md.update(password.getBytes(), 0, password.length());
                    password = new BigInteger(1, md.digest()).toString(16);

                    if (!password.equals(rs.getString("password"))) {
                        message.append("<li>Извините, E-mail или пароль неверны.</li>");
                    } else {
                        EditCookie editcookie = new EditCookie(request, response);
                        editcookie.setCookie("user_id", rs.getString("id"), null, 3600 * 24 * 30);
                        editcookie.setCookie("user_hash", rs.getString("hash"), null, 3600 * 24 * 30);

                        response.sendRedirect("/user/home");
                    }

                    
                } else {
                    message.append("<li>Извините, E-mail или пароль неверны.</li>");
                }
                
            }

        }
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
   

    @Override
    public String getMetaTitle() {
        return "Войти";
    }

    @Override
    public String getMetaHead() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Date getLastModified() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getServerStatus() {
        return 200;
    }
    
}
