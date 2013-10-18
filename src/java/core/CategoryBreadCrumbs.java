/*
 * Работа с категориями.
 */
package core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author mark
 */
public class CategoryBreadCrumbs {

    private Connection conn;
    private Statement stmt;
    private LinkedHashMap<Integer, HashMap> CategoriesTree = new LinkedHashMap();
    private LinkedHashMap<Integer, HashMap> CategoriesTemp = new LinkedHashMap();
    private LinkedHashMap<String, String> BreadCrumbs = new LinkedHashMap();
    private StringBuilder BreadCrumbsText = new StringBuilder();

    public CategoryBreadCrumbs(Connection conn, int CategoryId) {
        this.conn = conn;

        categoryBreadCrumbs(CategoryId);
    }

    /**
     * Хлебные крошки.
     *
     * @param stmt
     * @param id
     */
    private void categoryBreadCrumbs(int CategoryId) {
        if (CategoryId == 0) {
            return;
        }

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM `type` WHERE `id`=" + CategoryId + " LIMIT 1;");

            while (rs.next()) {
                categoryBreadCrumbs(rs.getInt("parent"));
                String parent = rs.getString("name");
                String title = rs.getString("title");
                String hurl = rs.getString("hurl");
                BreadCrumbs.put(hurl, parent);
                BreadCrumbsText.append(parent).append(" / ");
            }

            rs.close();
        } catch (SQLException e) {
        }
    }

    public String getCategoryBreadCrumbsText() {
        return BreadCrumbsText.toString().replaceAll("\\s*[/]\\s*$", "");
    }

    public String getCategoryBreadCrumbs(boolean lastLink) {
        StringBuilder temp = new StringBuilder();
        int i = 0;

        Map<String, String> map = new LinkedHashMap();

        if (!BreadCrumbs.isEmpty()) {
            temp.append("<a href=\"/\">Юмор</a> <font style=\"color:#ccc;\">&gt;</font> ");
        }

        for (Map.Entry<String, String> entry : BreadCrumbs.entrySet()) {
            i++;

            //if ((BreadCrumbs.size() == i && !lastLink) && BreadCrumbs.size() > 1) {
            //    temp.append(entry.getValue());
            //} else {
            temp.append("<a href=\"/").append(entry.getKey()).
                    append("\">").append(entry.getValue()).
                    append("</a> " + (BreadCrumbs.size() != i ? "<font style=\"color:#ccc;\">&gt;</font> " : ""));
            //}


        }

        return temp.toString();
    }
}
