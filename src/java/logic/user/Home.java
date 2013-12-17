/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.user;

import core.CategoriesTree;
import core.PagingNavigation;
import core.UrlOption;
import core.Util;
import core.ViewMethod;
import core.XmlOptionReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;
import logic.Creator;

/**
 *
 * @author mark
 */
public class Home extends Creator {

    private int serverStatus = 200;
    private long found = 0; // общее колличество найденных записей.
    private int page = 1, // текущая страница
            pageSpecular = 0,
            lt = 25,
            begin = 0;
    private String byLink = "";
    private String pagnav = "";
    private UrlOption urloption;
    private Util util = new Util();
    private Connection conn;
    private Statement stmt = null;
    private ResultSet rs = null;
    private Date LastModified = null;
    private LinkedHashMap<String, HashMap> item = new LinkedHashMap<>();
    private HashMap<String, HashSet> tags = new HashMap();
    private HashMap user = new HashMap();
    private HttpServletRequest request = null;
    private ArrayList args;
    private CategoriesTree ct = null;
    private HashMap selected = null;
    private String status = "on";
    private ViewMethod view = null;
    private XmlOptionReader xor = new XmlOptionReader();

    public Home(HttpServletRequest request, ArrayList args, Connection conn) throws SQLException, Exception {
        this.conn = conn;
        this.args = args;
        this.request = request;
        stmt = conn.createStatement();

        urloption = new UrlOption(request);
        page = urloption.NumberReplacementInt(request.getParameter("page"), 1);

        //page = page <= 0 ? 1 : page;
        begin = page > 0 ? (page * lt) : 0;
        begin = begin > 1000 ? 1000 : begin;

        String login = request.getParameter("q") != null ? request.getParameter("q") : "";
        selected = new HashMap();
        String userID = "";
        int userId = 0;
        status = (String) args.get(args.size() - 1);

        // по login определяем id пользователя.
        rs = stmt.executeQuery("SELECT * FROM users WHERE login='" + login + "'");
        if (rs.next()) {
            userId = rs.getInt("id");
            selected.put("login", rs.getString("login"));
        }

        if (userId > 0) {
            userID = " AND u.id=" + userId;
        } else if (!tags.equals("home.html") && !tags.equals("new")) {
            serverStatus = 404;
            return;
        }

        rs = stmt.executeQuery("SELECT u.*, COUNT(p.id) AS postCount, (SELECT COUNT(*) FROM `comment` c WHERE u.id = c.user) AS commentCount FROM users u LEFT JOIN post p ON u.id=p.user AND p.status!='del' WHERE u.status='on' AND u.id=" + userId + " GROUP BY u.id");

        while (rs.next()) {
            user.put("login", rs.getString("login"));

            int sex = rs.getInt("sex");
            if (sex == 1) {
                user.put("sex_string", "женский");
                user.put("sex_end", "а");
            } else if (sex == 2) {
                user.put("sex_string", "мужской");
                user.put("sex_end", "");
            }

            if (rs.getString("avatar") != null) {
                xor.setField(new String[]{"o"});
                HashMap<String, HashMap> avatar = xor.setDocument(rs.getString("avatar"));
                HashMap cnt = new HashMap();
                cnt.put("name", avatar.get("o").get("name"));
                cnt.put("path", avatar.get("o").get("p"));
                user.put("avatar", cnt);
            }

            user.put("rating", rs.getString("rating"));
            user.put("created", new SimpleDateFormat("d MMM yyyy").format(rs.getTimestamp("created")));
            user.put("postCount", rs.getString("postCount"));
            user.put("commentCount", rs.getString("commentCount"));
            user.put("lastVisit", util.dateFormat(rs.getTimestamp("last_login")));
        }

        setHome(request, userID, status);

    }

    private void setHome(HttpServletRequest request, String userID, String status) throws SQLException {

        rs = stmt.executeQuery("SELECT SQL_CALC_FOUND_ROWS u.login, p.*, "
                + "(SELECT COUNT(*) FROM `comment` c WHERE p.id = c.post) AS commentCount,"
                + "(SELECT COUNT(*) FROM `post_item` i WHERE p.id = i.post) AS itemCount,"
                + "MAX(if(i.sort=0, i.content, NULL)) AS content, MAX(if(i.sort=0, i.type, NULL)) AS type,"
                + "MAX(if(i.sort=1, i.content, NULL)) AS content2, MAX(if(i.sort=1, i.type, NULL)) AS type2 "
                + "FROM users u, tags_link tl, post p LEFT JOIN post_item i on i.post=p.id "
                + "WHERE u.id=p.user AND p.status IN('on','new','abyss') AND tl.post=p.id " + userID + " "
                + "GROUP BY p.id ORDER BY p.date DESC  LIMIT " + (page == 1 ? 0 : begin - lt) + "," + lt);

        view = new ViewMethod(rs, stmt);
        item = view.getPostItem(rs);

        rs = stmt.executeQuery("SELECT FOUND_ROWS() as rows;");
        if (rs.next()) {
            found = rs.getInt("rows");
        }

        tags = view.getPostTags();
        LastModified = view.getLastModified();

        //pagnav = new PagingNavigationSpecular(found, request.getParameter("page"), lt, urloption);
        PagingNavigation pnav = new PagingNavigation(found, request.getParameter("page"), lt, urloption);
        pagnav = pnav.PagingPreviousNext();

    }

    public String getAlt(String id) {
        return view.getPostTags(id);
    }

    public HashMap getSelected() {
        return selected;
    }

    public HashMap getUser() {
        return !user.isEmpty() ? user : null;
    }

    public LinkedHashMap getItem() {
        return !item.isEmpty() ? item : null;
    }

    public HashMap getTagsItem() {
        return tags;
    }

    public String PageNavig() {
        return pagnav;
    }

    public String getByLink() {
        return byLink;
    }

    public String getMenu() {
        String tag = selected.containsKey("login") ? "<a href=\"/\">×</a> <h1>" + selected.get("login") + "</h1>" : "";

        String menu = "<ul class=\"menu\">";

        menu += "<li class=\"tag\" title=\"Удалить\">" + tag + "</li>";

        menu += "</ul>";
        return menu;
    }

    @Override
    public String getMetaTitle() {
        if (!selected.isEmpty()) {

            return selected.get("login").toString();
        } else {
            return "";
        }
    }

    @Override
    public int getServerStatus() {

        if (item.isEmpty()) {
            //return 404;
        } else {
        }
        return serverStatus;
    }

    @Override
    public Date getLastModified() {
        return LastModified;
    }

    @Override
    public String getMetaHead() {
        return "";
    }
}
