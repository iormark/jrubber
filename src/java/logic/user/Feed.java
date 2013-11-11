/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.user;

import core.ViewMethod;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;
import logic.Creator;

/**
 *
 * @author mark
 */
public class Feed extends Creator {

    private String metaTitle = "";
    private int serverStatus = 200;
    private LinkedHashMap item = new LinkedHashMap();
    private int count = 0;
    private Statement stmt = null;
    private ResultSet rs = null;
    private Check check = null;

    public Feed(HttpServletRequest request, ArrayList args, Statement stmt, Check check) throws SQLException {
        this.stmt = stmt;
        this.check = check;
        String section = request.getParameter("section");

        if (section != null) {
            if ("comment".equals(section)) {
                queryComment();
            } else {
                serverStatus = 404;
            }
        }

    }

    private void queryComment() throws SQLException {

        metaTitle = "Мои комментарии";
        rs = stmt.executeQuery("SELECT SQL_CALC_FOUND_ROWS u.login, p.title, p.id, c.id AS comment_id, c.vote, c.created AS date, c.comment, c.status FROM post p, comment c, users u WHERE p.id=c.post AND c.user=u.id AND u.id=" + check.getUserID()+" ORDER BY c.id DESC LIMIT 500");
        ViewMethod view = new ViewMethod(rs, false, true);
        item = view.getViewCatalog();
        
        
        rs = stmt.executeQuery("SELECT FOUND_ROWS() as rows;");
        if (rs.next()) {
            count = rs.getInt("rows");
        }
    }

    public LinkedHashMap getItem() {
        return item;
    }
    
    public int getItem_count() {
        return count;
    }

    @Override
    public String getMetaTitle() {
        return metaTitle;
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
        return serverStatus;
    }
}
