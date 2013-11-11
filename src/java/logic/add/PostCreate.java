/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.add;

import core.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.user.Check;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author mark
 */
public class PostCreate {

    private Connection conn = null;
    private Statement stmt = null;

    private String message = "";
    private String userID = "0";
    private String title = null;
    private String text = null;
    private String video = null;
    private long key = 0;
    private Util util = new Util();

    private int insertPostId = 0;

    public PostCreate(Connection conn, Statement stmt, Check check) {
        this.conn = conn;
        this.stmt = stmt;
    }

    public PostCreate(HttpServletRequest request, HttpServletResponse response, Connection conn, Statement stmt, Check check) throws SQLException {

        this.conn = conn;
        this.stmt = stmt;

        userID = check.getUserID();
        title = request.getParameter("title");
        title = (String.valueOf(title.charAt(0)).toUpperCase()).concat(title.substring(1));
        text = request.getParameter("text");
        video = request.getParameter("video");

        try {
            key = Long.parseLong(request.getParameter("key"));
        } catch (NumberFormatException ex) {
            message = ("<li>Ошибочка вышла, простите...</li>");
            return;
        }

        //title = checkRequest.heckTitle(title);
    }

    public void createPost(boolean isVideo) throws SQLException {

        System.out.println("------start-------");
        System.out.println("Query post_article");
        PreparedStatement ps = conn.prepareStatement("INSERT INTO `post` "
                + "(`user`, `name`, `email`, `title`, `date`, `svc_date`, `type`, status) "
                + "VALUES (?, '', '', ?, NOW(), NOW(), 0, 'new')", Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, userID);
        ps.setString(2, title);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            insertPostId = rs.getInt(1);
        } else {
            //message = ("<li>Ошибочка вышла, простите...</li>");
        }

        if (isVideo) {
            createPost_item(video, 99);
        }

        System.out.println("------end-------\n");
    }

    public void createPost_item(String video, int sort) throws SQLException {

        System.out.println("------start-------");
        System.out.println("Query post_item");
        PreparedStatement ps = conn.prepareStatement("INSERT INTO `post_item` "
                + "(`post`, `sort`, `text`, `image`, `img`, `video`, `alt`, `date`, `key`) "
                + "VALUES (?, ?, ?, null, null, ?, '', NOW(), null);");

        ps.setInt(1, insertPostId);
        ps.setInt(2, sort);
        ps.setString(3, text);
        ps.setString(4, video);
        ps.executeUpdate();
        System.out.println("------end-------\n");
    }

    public void createPost_items(LinkedHashMap<Integer, HashMap> ListContent, HashSet tags) throws SQLException {

        System.out.println("------start-------");
        System.out.println("Query post_items");
        for (int i = 0; i < ListContent.size(); i++) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO `post_item` "
                    + "(`post`, `sort`, `text`, `image`, `img`, `alt`, `date`, `key`) "
                    + "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?);");

            ps.setInt(1, 0);
            ps.setInt(2, Integer.parseInt((String) ListContent.get(i).get("sort")));
            ps.setString(3, (String) ListContent.get(i).get("text"));

            String img = null;
            if (ListContent.get(i).containsKey("original")) {
                img = (String) ListContent.get(i).get("original");
            }
            ps.setString(4, img);

            img = null;
            if (ListContent.get(i).containsKey("imgXml")) {
                img = (String) ListContent.get(i).get("imgXml");
            }
            ps.setString(5, img);

            ps.setString(6, tags.toString().replaceAll("[\\[\\]]", ""));

            ps.setString(7, (String) ListContent.get(i).get("key"));
            //System.out.println(ps);
            ps.executeUpdate();
        }
        System.out.println("------end-------\n");
    }

    public void updateItem_post() throws SQLException {
        stmt.executeUpdate("UPDATE `post_item` SET post=" + insertPostId + ", `key`=null WHERE `key`='" + key + "'");
    }

    public void createTags(HashSet tagsMap) throws SQLException {

        System.out.println("------start-------");
        System.out.println("Query tags");
        for (Object element : tagsMap) {

            String teg = (String) element;
            PreparedStatement ps = conn.prepareStatement("INSERT IGNORE `tags` "
                    + "(`tags`) VALUES (?);", Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, teg);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int tagsId = 0;

            if (rs.next()) {
                tagsId = rs.getInt(1);
            } else {

                rs = stmt.executeQuery("SELECT id FROM `tags` WHERE `tags` = '" + teg + "' LIMIT 1");
                if (rs.next()) {
                    tagsId = rs.getInt("id");
                }
            }

            stmt.executeUpdate("INSERT IGNORE `tags_link`"
                    + " (tags, post) VALUES"
                    + " (" + tagsId + ", " + insertPostId + ")");
        }
        System.out.println("------end-------");
    }
}
