/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import logic.user.Check;

/**
 *
 * @author mark
 */
public class Edit extends Creator {

    private String hash = null;

    public Edit(HttpServletRequest request, ArrayList args, Statement stmt, Check check) throws SQLException {

        if (request.getParameter("hash") != null) {
            ResultSet rs = stmt.executeQuery("SELECT hash FROM `users` WHERE `id`='" + check.getUserID() + "' AND hash='" + request.getParameter("hash") + "'");
            if (rs.next()) {
                hash = rs.getString("hash");
            }
        }
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String getMetaTitle() {
        return "Мои настройки";
    }

    @Override
    public String getMetaHead() {
        return "";
    }

    @Override
    public Date getLastModified() {
        return null;
    }

    @Override
    public int getServerStatus() {
        return 200;
    }

}
