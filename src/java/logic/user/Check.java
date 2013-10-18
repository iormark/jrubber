/*
 * Проверка на авторизацию.
 */
package logic.user;

import core.EditCookie;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author mark
 */
public class Check {

    private boolean check = false;
    String UserID = "", UserHash = "", UserName = "", UserEmail = "";
    private ResultSet rs = null;

    public Check(HttpServletRequest request, HttpServletResponse response, Statement stmt) throws Exception {
        EditCookie editcookie = new EditCookie(request, response);

        UserID = editcookie.getCookie("user_id");
        UserHash = editcookie.getCookie("user_hash");

        if (UserID != null && UserHash != null) {

            rs = stmt.executeQuery("SELECT * FROM `users` WHERE `id` = '" + UserID + "'");
            if (rs.next()) {
                if (UserHash.equals(rs.getString("hash"))) {
                    check = true;
                    UserName = rs.getString("name");
                    UserEmail = rs.getString("email");
                }
            }

        } else {
            check = false;
        }
        
        
        if (check) {
            stmt.executeUpdate("UPDATE `users` SET `last_login`=NOW() WHERE `id` = '" + UserID + "';");
        }
    }

    public boolean getCheck() {
        return check;
    }

    public String getUserID() {
        return UserID != null ? UserID : "0";
    }

    public String getUserHash() {
        return UserHash != null ? UserHash : "0";
    }
    
    public String getUserName() {
        return UserName != null ? UserName : "Волонтер";
    }
    
    public String getUserEmail() {
        return UserEmail != null ? UserEmail : "@";
    }
}
