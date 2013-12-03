/*
 * Проверка на авторизацию.
 */
package logic.user;

import core.EditCookie;
import core.XmlOptionReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author mark
 */
public class Check {

    private HashMap userMap = new HashMap();
    private boolean check = false;
    private String UserID = "", UserHash = "", UserLogin = "", UserEmail = "", password = "";
    private int rating = 0;
    private ResultSet rs = null;
    private XmlOptionReader xor = new XmlOptionReader();

    public Check(HttpServletRequest request, HttpServletResponse response, Statement stmt) throws Exception {
        EditCookie editcookie = new EditCookie(request, response);

        UserID = editcookie.getCookie("user_id");
        UserHash = editcookie.getCookie("user_hash");

        if (UserID != null && UserHash != null) {

            rs = stmt.executeQuery("SELECT * FROM `users` WHERE `id` = '" + UserID + "' AND status='on' LIMIT 1");
            if (rs.next()) {
                if (UserHash.equals(rs.getString("hash"))) {
                    check = true;
                    UserLogin = rs.getString("login");
                    UserEmail = rs.getString("email");
                    rating = rs.getInt("rating");

                    userMap.put("login", rs.getString("login"));
                    userMap.put("email", rs.getString("email"));
                    userMap.put("sex", rs.getString("sex"));

                    if (rs.getString("avatar") != null) {
                        xor.setField(new String[]{"o"});
                        HashMap<String, HashMap> avatar = xor.setDocument(rs.getString("avatar"));
                        HashMap cnt = new HashMap();
                        cnt.put("name", avatar.get("o").get("name"));
                        cnt.put("path", avatar.get("o").get("p"));
                        userMap.put("avatar", cnt);
                    }

                    userMap.put("created", rs.getDate("created"));
                    userMap.put("rating", rs.getString("rating"));
                    userMap.put("hash", rs.getString("hash"));

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

    public HashMap getUserMap() {
        return userMap;
    }

    public String getUserID() {
        return UserID != null ? UserID : "0";
    }

    public String getUserHash() {
        return UserHash != null ? UserHash : "0";
    }

    public String getUserLogin() {
        return UserLogin != null ? UserLogin : "Волонтер";
    }

    public String getUserEmail() {
        return UserEmail != null ? UserEmail : "@";
    }

    public int getRating() {
        return rating;
    }

    public String getPassword() {
        return password != null ? password : "";
    }
}
