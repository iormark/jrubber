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

    public Comment(Statement stmt, int id) throws SQLException {
        queryComment(stmt, id);
    }

    public Comment(Statement stmt, int id, int post) throws SQLException {
        queryComment(stmt, id, post);
    }

    private void queryComment(Statement stmt, int id) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT c.id, u.id AS user, u.login, c.parent, c.comment, c.created, c.vote, c.status, r.user_reply, r.login_reply "
                + "FROM users u, comment c LEFT JOIN "
                + "(SELECT c.id, u.id AS user_reply, u.login AS login_reply FROM users u, comment c WHERE u.id=c.user) AS r "
                + "ON r.id=c.parent WHERE u.id=c.user AND c.status IN('on','reply','first') AND c.post=" + id);

        for (int i = 0; rs.next(); i++) {
            HashMap data = new HashMap();
            data.put("user_id", rs.getString("user"));
            data.put("login", rs.getString("login"));
            data.put("comment", util.lineFeed(StringEscapeUtils.escapeHtml4(rs.getString("comment"))));
            data.put("created", util.dateFormat(rs.getTimestamp("created")));
            data.put("time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+03:00").format(rs.getTimestamp("created")));
            data.put("status", rs.getString("status"));
            data.put("vote", rs.getInt("vote") > 0 ? "+" + rs.getString("vote") : rs.getString("vote"));
            data.put("id_reply", rs.getString("parent"));
            data.put("user_reply", rs.getString("user_reply"));
            data.put("login_reply", rs.getString("login_reply"));
            Comment.put(rs.getString("id"), data);

        }
    }

    private void queryComment(Statement stmt, int id, int post) throws SQLException {
    }

    public LinkedHashMap getComment() {
        return !Comment.isEmpty() ? Comment : null;
    }
}
