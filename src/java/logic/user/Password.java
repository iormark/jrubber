/*
 * Вспомнить пароль.
 */
package logic.user;

import core.SendMail;
import core.Util;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.Creator;

/**
 *
 * @author mark
 */
public class Password {

    private String email = "", password = "", button = "";
    private String message = "";
    private Util util = new Util();

    public Password(HttpServletRequest request, HttpServletResponse response, Statement stmt) throws Exception {

        String email = request.getParameter("email");
        String UserID = "", UserHash = "";

        //e-mail
        if (email != null) {
            if (email.equals("")) {
                message = ("{\"status\":\"error\",\"message\":\"Необходимо указать e-mail адрес.\"}");
            } else if (!util.checkEmail(email)) {
                message = ("{\"status\":\"error\",\"message\":\"Адрес электронной почты не корректен.\"}");
            } else {
                ResultSet rs = stmt.executeQuery("SELECT * FROM `users` WHERE `email`='" + email + "'");
                if (!rs.next()) {
                    message = ("{\"status\":\"error\",\"message\":\"Такого E-mail адреса у нас нет.\"}");
                } else {
                    UserID = rs.getString("id");
                    UserHash = rs.getString("hash");
                }
            }
        } else {
            message = ("{\"status\":\"error\",\"message\":\"Необходимо указать e-mail адрес.\"}");
        }

        if (message.length() == 0) {

            HashMap content = new HashMap();
            content.put("subject", "Изменение пароля для " + email + " на YourMood.Ru");
            //content.put("name", email);
            content.put("to", email);
            content.put("url", "http://yourmood.ru/svc/user?q=redirect&id=" + UserID + "&hash=" + UserHash);

            System.out.println(request.getServletContext().getRealPath("/"));
            SendMail send = new SendMail("mail/password.html", content, request.getServletContext().getRealPath("/"));

            message = ("{\"status\":\"good\",\"message\":\"Дальнейшие инструкции отправлены на адрес вашей электронной почты.\"}");
        }

    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message.toString();
    }
}
