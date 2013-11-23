/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.add;

import core.Util;
import core.XmlOptionReader;
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
    private Check check = null;

    private String message = "";
    private int post = 0;
    private String userId = "0";
    private String title = null;
    private String text = null;
    private String video = null;
    private long key = 0;
    private Util util = new Util();
    private XmlOptionReader xor = new XmlOptionReader();

    private int insertPostId = 0;

    public PostCreate(Connection conn, Statement stmt, Check check) {
        this.conn = conn;
        this.stmt = stmt;
        this.check = check;
    }

    public PostCreate(HttpServletRequest request, HttpServletResponse response, Connection conn, Statement stmt, Check check) throws SQLException {

        this.conn = conn;
        this.stmt = stmt;

        post = Integer.parseInt(request.getParameter("post"));
        userId = check.getUserID();
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

    public int createPost() throws SQLException {

        System.out.println("Query post");
        PreparedStatement ps = conn.prepareStatement("INSERT INTO `post` "
                + "(`id`, `user`, `title`, `date`, `svc_date`, status) "
                + "VALUES (?, ?, ?, NOW(), NOW(), 'new') "
                + "ON DUPLICATE KEY UPDATE "
                + "title='" + title + "'", Statement.RETURN_GENERATED_KEYS);

        ps.setInt(1, post);
        ps.setString(2, userId);
        ps.setString(3, title);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            insertPostId = rs.getInt(1);
        } else {
            //message = ("Ошибочка вышла, простите... insertPostId: " + insertPostId);
        }

        System.out.println("------end-------\n");

        return insertPostId > 0 ? insertPostId : post;
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

    public int createPost_items(LinkedHashMap<Integer, HashMap> ListContent, HashSet tags, String realPath) throws Exception {

        System.out.println("Query post_items");
        System.out.println(ListContent);
        int insertItem = 0;
        int item = 0;
        ResultSet rs = null;

        for (int i = 0; i < ListContent.size(); i++) {

            item = Integer.parseInt((String) ListContent.get(i).get("item"));
            int post = Integer.parseInt((String) ListContent.get(i).get("post"));
            int sort = Integer.parseInt((String) ListContent.get(i).get("sort"));

            String type = null;
            String content = null;

            if (!ListContent.get(i).get("text").equals("")) {
                content = (String) ListContent.get(i).get("text");
                type = "text";
            } else if (ListContent.get(i).containsKey("imgXml")) {
                content = (String) ListContent.get(i).get("imgXml");
                type = "image";
            } else if (!ListContent.get(i).get("video").equals("")) {

                content = (String) ListContent.get(i).get("video");
                content = content.trim();
                type = "video";
                /*
                 rs = stmt.executeQuery("SELECT id, post FROM post_item WHERE content='" + content + "' AND type='" + type + "' LIMIT 1");
                 if (rs.next()) {
                 message = ("<a href='/post?id=" + rs.getInt("post") + "' target='_blank'>Это видео</a> уже есть на сайте");
                 return rs.getInt("id");
                 }*/

            }

            if (content != null) {
                content = content.replace("'", "\\'");
            }

            HashSet files = new HashSet();

            if (item > 0 && "image".equals(type)) {
                rs = stmt.executeQuery("SELECT content FROM post_item WHERE user='" + check.getUserID() + "' AND id='" + item + "' AND type='image' LIMIT 1");
                if (rs.next()) {
                    String loadPath = null;
                    xor.setField(new String[]{"original"});
                    HashMap<String, HashMap> image = xor.setDocument(rs.getString("content"));
                    loadPath = (String) (image.get("original").containsKey("path") ? image.get("original").get("path") : "/photo_anekdot");
                    files.add(realPath + loadPath + "/" + image.get("original").get("name"));
                    files.add(realPath + loadPath + "/middle_" + image.get("original").get("name"));
                    files.add(realPath + loadPath + "/small_" + image.get("original").get("name"));
                }
            }

            PreparedStatement ps = conn.prepareStatement("INSERT INTO `post_item` "
                    + "(`id`, `user`, `post`, `sort`, `content`, `type`, `key`) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE "
                    + "sort=" + sort
                    + (content != null ? ",content='" + content + "',type=" + (type != null ? "'" + type + "'" : "NULL") : ""),
                    Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, item);
            ps.setInt(2, Integer.parseInt(check.getUserID()));
            ps.setInt(3, post);
            ps.setInt(4, sort);
            ps.setString(5, content);
            ps.setString(6, type);
            ps.setString(7, (String) ListContent.get(i).get("key"));
            System.out.println(ps);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                insertItem = rs.getInt(1);
            }

            System.out.println(insertItem);

            if (!files.isEmpty()) {
                util.deleteFile(files);
            }
        }

        System.out.println("------end-------\n");

        return insertItem != 0 ? insertItem : item;
    }

    public void updateItem_post() throws SQLException {
        stmt.executeUpdate("UPDATE `post_item` SET post=if(post=0," + insertPostId + ",post), `key`=null WHERE `key`='" + key + "'");
    }

    public void createTags(HashSet tagsMap) throws SQLException {

        System.out.println("Query tags");

        ResultSet rs = null;

        for (Object element : tagsMap) {

            String teg = (String) element;
            PreparedStatement ps = conn.prepareStatement("INSERT IGNORE `tags` "
                    + "(`tags`) VALUES (?);", Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, teg);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            int tagsid = 0;

            if (rs.next()) {
                tagsid = rs.getInt(1);
            } else {

                rs = stmt.executeQuery("SELECT id FROM `tags` WHERE `tags` = '" + teg + "' LIMIT 1");
                if (rs.next()) {
                    tagsid = rs.getInt("id");
                }
            }

            stmt.executeUpdate("INSERT IGNORE `tags_link`"
                    + " (tags, post) VALUES"
                    + " (" + tagsid + ", " + (post == 0 ? insertPostId : post) + ")");
        }

        HashMap tagsId = new HashMap();
        HashSet tagsName = new HashSet();

        rs = stmt.executeQuery("SELECT t.id, t.tags FROM post p, tags_link tl, tags t WHERE p.id=tl.post AND tl.tags=t.id AND p.id=" + post);
        while (rs.next()) {
            tagsId.put(rs.getString("tags"), rs.getInt("id"));
            tagsName.add(rs.getString("tags"));
        }

        tagsName.removeAll(tagsMap);

        for (Object element : tagsName) {
            stmt.executeUpdate("DELETE FROM tags_link WHERE tags = " + tagsId.get(element) + " AND post = " + post);
        }

        System.out.println("------end-------");
    }

    public void deleteItem(String realPath, int item) throws SQLException, Exception {
        HashSet files = new HashSet();
        ResultSet rs = stmt.executeQuery("SELECT content, type FROM post_item WHERE user='" + check.getUserID() + "' AND id='" + item + "' LIMIT 1");
        if (rs.next()) {

            if ("image".equals(rs.getString("type"))) {
                String loadPath = null;
                xor.setField(new String[]{"original"});
                HashMap<String, HashMap> image = xor.setDocument(rs.getString("content"));
                loadPath = (String) (image.get("original").containsKey("path") ? image.get("original").get("path") : "/photo_anekdot");
                files.add(realPath + loadPath + "/" + image.get("original").get("name"));
                files.add(realPath + loadPath + "/middle_" + image.get("original").get("name"));
                files.add(realPath + loadPath + "/small_" + image.get("original").get("name"));
            }
        } else {
            message = "Простите, ошибка доступа.";
            return;
        }

        if (!files.isEmpty()) {
            util.deleteFile(files);
        }
        
        stmt.executeUpdate("DELETE FROM post_item WHERE id = " + item);
    }

    public String getMessage() {
        return message;
    }
}
