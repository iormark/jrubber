/*
 * Регистрация на сайте.
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
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author mark
 */
public class Register extends Creator {

    private String name = "", email = "", password = "", passwordConfirm = "", button = "";
    private StringBuilder message = new StringBuilder();
    private Util util = new Util();
    private ResultSet rs = null;

    public Register(HttpServletRequest request, HttpServletResponse response, Statement stmt) throws Exception {

        button = request.getParameter("button");

        if (button != null) {
            if (button.equals("Регистрация")) {

                name = request.getParameter("name");
                email = request.getParameter("email");
                password = request.getParameter("password");
                passwordConfirm = request.getParameter("passwordConfirm");


                // имя
                
                if (name != null) {
                    if (name.equals("")) {
                        message.append("<li>Поле Имя, Фамилия обязательно для заполнения.</li>");
                    } else if (name.length() > 100) {
                        message.append("<li>Поле Имя, Фамилия не должно быть таким длинным.</li>");
                    } else if (name.length() == 1) {
                        message.append("<li>Ваше Имя состоит из одной буквы? ну на хер!</li>");
                    }
                } else {
                    message.append("<li>Поле Имя, Фамилия обязательно для заполнения.</li>");
                }

                // e-mail

                if (email != null) {
                    if (email.equals("")) {
                        message.append("<li>Поле E-mail обязательно для заполнения.</li>");
                    } else if (!util.checkEmail(email)) {
                        message.append("<li>Адрес электронной почты ").append(email).append(" не корректен.</li>");
                    } else {
                        rs = stmt.executeQuery("SELECT * FROM `users` WHERE `email` = '" + email + "'");
                        if (rs.next()) {
                            message.append("<li>Пользователь с таким E-mail адресом уже существует.</li>");
                        }
                    }
                } else {
                    message.append("<li>Поле E-mail обязательно для заполнения.</li>");
                }

                // пароль

                if (password != null) {
                    if (password.equals("")) {
                        message.append("<li>Поле Пароль обязательно для заполнения.</li>");
                    } else if (!util.checkPassword(password)) {
                        message.append("<li>Пароль должен содержать от 5 до 20 символов. Можно использовать латинские буквы, цифры и символы из списка:<strong>! @ # $ % ^ &amp; * ( ) _ - + : ; , .</strong></li>");
                    } else {
                        if (passwordConfirm != null) {
                            if (passwordConfirm.equals("")) {
                                message.append("<li>Поле Подтвердите пароль обязательно для заполнения.</li>");
                            } else if (!passwordConfirm.equals(password)) {
                                message.append("<li>Пароли не совпадают!</li>");
                            }
                        }
                    }
                } else {
                    message.append("<li>Поле Пароль обязательно для заполнения.</li>");
                }

                
                


                if (message.length() == 0) {
                    
                    String md5Hash = Long.toString(new Date().getTime());
                    
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.reset();
                    md.update(md5Hash.getBytes(), 0, md5Hash.length());
                    md5Hash = new BigInteger(1, md.digest()).toString(16);


                    message.append("<li>" + md5Hash + "</li>");
                    
                    // добовляем нового пользователя
                    
                    stmt.executeUpdate("INSERT INTO `users` (`id`, `email`, `name`, `password`, `created`) VALUES (NULL, '" + email + "', '" + StringEscapeUtils.escapeHtml4(name) + "', MD5('" + password + "'), NOW())");
                    
                    
                    // входим в учётку
                    
                    rs = stmt.executeQuery("SELECT LAST_INSERT_ID() as last");
                    
                    int LastInsertID = 0;
                    if (rs.next()) {
                        LastInsertID = rs.getInt("last");
                    }
                    
                    
                    stmt.executeUpdate("UPDATE `users` SET `hash` = '" + md5Hash + "' WHERE `id` = " + LastInsertID + ";");

                    message.append("<li>" + Integer.toString(LastInsertID) + "</li>");
                    EditCookie editcookie = new EditCookie(request, response);
                    editcookie.setCookie("user_id", Integer.toString(LastInsertID), null, 3600 * 24 * 30);
                    editcookie.setCookie("user_hash", md5Hash, null, 3600 * 24 * 30);
                    
                    
                    response.sendRedirect("/user/home");
                    
                }

            } else {
            }
        }
    }

    public String getName() {
        return name;
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
        return "Регистрация нового пользователя на сайте";
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
