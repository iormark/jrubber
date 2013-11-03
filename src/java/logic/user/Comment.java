/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.user;

import core.Util;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author mark
 */
public class Comment {

    private LinkedHashMap<String, HashMap> Comment = new LinkedHashMap();
    private static Util util = new Util();

    public Comment(Statement stmt, String id) throws SQLException {
        queryComment(stmt, id);
    }

    public Comment(Statement stmt, String id, String post) throws SQLException {
        queryComment(stmt, id, post);
    }

    private void queryComment(Statement stmt, String id) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT u.id AS user_id, u.login, c.* FROM users u, comment c WHERE u.id=c.user AND c.post='" + id + "' AND c.status='on'");

        for (int i = 0; rs.next(); i++) {
            HashMap data = new HashMap();
            data.put("user_id", rs.getString("user_id"));
            data.put("login", rs.getString("login"));
            data.put("comment", util.lineFeed(StringEscapeUtils.escapeHtml4(rs.getString("comment"))));
            data.put("created", util.dateFormat(rs.getTimestamp("created")));
            data.put("time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+03:00").format(rs.getTimestamp("created")));
            data.put("status", rs.getString("status"));
            data.put("vote", rs.getInt("vote") > 0 ? "+" + rs.getString("vote") : rs.getString("vote"));

            Comment.put(rs.getString("id"), data);

        }
    }

    private void queryComment(Statement stmt, String id, String post) throws SQLException {
        
    }

    public LinkedHashMap getComment() {
        return Comment;
    }
}
