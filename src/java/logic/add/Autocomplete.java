/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.add;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author mark
 */
public class Autocomplete {

    private PrintWriter out;
    private String tags = "";
    private List listTags = new LinkedList();

    public Autocomplete(HttpServletRequest request, HttpServletResponse response, Connection conn, Statement stmt, PrintWriter out) {
        this.out = out;
        tags = request.getParameter("tags") != null ? request.getParameter("tags") : "";
        if ("".equals(tags)) {
            return;
        }
        String[] tagArray = tags.split(",");
        tags = (tagArray[tagArray.length - 1]).trim();
        try {
            query(stmt);
        } catch (Exception ex) {
        }
    }

    private List query(Statement stmt) throws IOException, Exception {
        ResultSet rs = stmt.executeQuery("SELECT t.tags, COUNT(*) AS count FROM tags t, tags_link tl WHERE t.tags LIKE '" + tags + "%' AND t.id=tl.tags GROUP BY tl.tags LIMIT 30");
        while (rs.next()) {
            listTags.add("<span class='tag'>" + rs.getString("tags") + "</span> × <span class='count'>" + rs.getString("count") + "</span>");
        }
        return listTags;
    }

    /**
     * Json
     *
     * @return
     */
    public String getJson() {
        String json = "[]";
        if (!listTags.isEmpty()) {
            json = "[\"" + tags + "\", [";
            for (Object i : listTags) {
                json += "\"" + i + "\",";
            }
            json = json.replaceAll("[,]$", "") + "]]";
        }
        return json;
    }
}
