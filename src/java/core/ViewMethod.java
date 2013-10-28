/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 *
 * @author mark
 */
public class ViewMethod {

    private Date LastModified = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    private LinkedHashMap<String, HashMap> items = new LinkedHashMap();
    private HashMap<String, HashSet> tags = new HashMap();
    private Util util = new Util();
    private XmlOptionReader xor = new XmlOptionReader();
    boolean view = false; //true-в крточке

    public ViewMethod(ResultSet rs, Statement stmt) {
        this.rs = rs;
        this.stmt = stmt;
    }
    
    public ViewMethod(ResultSet rs, Statement stmt, boolean view) {
        this.rs = rs;
        this.stmt = stmt;
        this.view = view;
    }

    public ViewMethod(ResultSet rs) {
        this.rs = rs;
    }

    public ViewMethod(ResultSet rs, boolean view) {
        this.rs = rs;
        this.view = view;
    }

    public LinkedHashMap getViewCatalog() throws SQLException {
        ResultSetMetaData rsm = rs.getMetaData();
        int numColumns = rsm.getColumnCount();
        String[] colNames = new String[numColumns + 1];

        for (int i = 1; i < (numColumns + 1); i++) {
            //System.out.println(rsm.getColumnLabel(i));
            //if (rsm.getColumnName(i).equals("")) {
            colNames[i] = rsm.getColumnLabel(i);
            //}

            //colNames[i] = rsm.getColumnName(i);
        }

        while (rs.next()) {

            HashMap content = new HashMap();

            for (int j = 1; j < (numColumns + 1); j++) {
                if (colNames[j] != null) {
                    Object f;
                    switch (rsm.getColumnType(j)) {
                        case Types.BIGINT: {
                            f = rs.getLong(j);
                            break;
                        }
                        case Types.INTEGER: {
                            f = rs.getInt(j);
                            break;
                        }
                        case Types.DATE: {
                            f = rs.getTimestamp(j);
                            break;
                        }
                        case Types.FLOAT: {
                            f = rs.getFloat(j);
                            break;
                        }
                        case Types.DOUBLE: {
                            f = rs.getDouble(j);
                            break;
                        }
                        case Types.TIME: {
                            f = rs.getDate(j);
                            break;
                        }
                        case Types.BOOLEAN: {
                            f = rs.getBoolean(j);
                            break;
                        }
                        default: {
                            f = rs.getString(j);
                        }
                    }

                    switch (colNames[j]) {
                        case "last_modified": {
                            if (LastModified == null) {
                                LastModified = rs.getTimestamp("last_modified");
                            }
                            break;
                        }
                        case "date": {
                            f = util.dateFormat(rs.getTimestamp("date"));
                            content.put("dateMore", new SimpleDateFormat("d MMM yy, HH:mm:ss").format(rs.getTimestamp("date")));
                            break;
                        }
                        case "title": {

                            if (rs.getString("title").equals("")) {
                                f = "№ " + rs.getString("id");
                            } else {
                                f = util.specialCharacters(util.Shortening(rs.getString("title") + "", 85, ""));
                            }

                            break;
                        }
                        case "text": {
                            if (view) {
                                f = util.bbCode(rs.getString("text") + "");
                            } else {
                                f = util.Shortening(util.bbCode(rs.getString("text") + ""), 1000, "<br><a href=\"/anekdot?id=" + rs.getString("id") + "\" target=\"_blank\" title=\"Откроется в новом окне\">Читать дальше</a>");
                            }

                            break;
                        }
                        case "itemCount": {
                            if (rs.getInt("itemCount") > 1) {
                                f = " (" + rs.getString("itemCount") + " шт.)";
                                content.put("readMore", "<a href=\"/anekdot?id=" + rs.getString("id") + "\" target=\"_blank\" title=\"Откроется в новом окне\">Читать дальше</a>");
                            } else {
                                f = "";
                            }
                            break;
                        }
                        case "vote": {
                            f = rs.getInt("vote") > 0 ? "" + rs.getString("vote") : rs.getString("vote");
                            break;
                        }
                        case "image": {
                            if (rs.getString("image") != null) {
                                if (!rs.getString("image").equals("")) {
                                    content.put("alt", util.Shortening(util.specialCharactersTags(rs.getString("alt")), 255, ""));
                                    f = rs.getString("image");
                                }
                            }
                            break;
                        }
                        case "img": {
                            if (rs.getString("img") != null) {
                                if (!rs.getString("img").equals("")) {

                                    try {
                                        xor.setField(new String[]{"original", "middle"});
                                        HashMap<String, HashMap> h = xor.setDocument(rs.getString("img"));

                                        content.put("image", h.get("original").get("name"));
                                        content.put("imagePath", h.get("original").containsKey("path") ? h.get("original").get("path") : "/photo_anekdot");

                                        content.put("width", h.get("middle").get("width"));
                                        content.put("height", h.get("middle").get("height"));

                                        int gif = Integer.parseInt(h.get("original").get("animated").toString());
                                        content.put("animated", gif > 0 ? "img__animated" : "");

                                        long size = Long.parseLong(h.get("original").get("size").toString());
                                        content.put("size", util.sizeFormat(size));
                                    } catch (Exception ex) {
                                        System.out.println(ex);
                                    }
                                }
                            }
                            break;
                        }

                    }

                    content.put(colNames[j], f);
                    content.remove("img");
                }
            }

            items.put(rs.getString("id"), content);
        }
        return items;
    }

    public HashMap getPostTags(String id) throws SQLException {

        //System.out.println("SELECT tl.post, t.id, t.tags FROM tags t, tags_link tl WHERE t.id=tl.tags AND tl.post IN (" + id + ")");
        rs = stmt.executeQuery("SELECT tl.post, t.id, t.tags FROM tags t, tags_link tl WHERE t.id=tl.tags AND tl.post IN (" + id + ")");

        while (rs.next()) {
            String post = rs.getString("post");
            HashSet node = new HashSet();

            if (!tags.containsKey(post)) {
                node.add(rs.getString("tags"));
                tags.put(post, node);
            } else {
                node.add(rs.getString("tags"));
                node.addAll(tags.get(post));
                tags.put(post, node);
            }
        }
        
        System.out.println(tags);
        return tags;
    }
    
    public HashMap getPostTags() throws SQLException {
        if (items.keySet().isEmpty()) {
            return null;
        }

        String id_s = items.keySet().toString().replace("[", "").replace("]", "");
        //System.out.println("SELECT tl.post, t.id, t.tags FROM tags t, tags_link tl WHERE t.id=tl.tags AND tl.post IN (" + id_s + ")");
        rs = stmt.executeQuery("SELECT tl.post, t.id, t.tags FROM tags t, tags_link tl WHERE t.id=tl.tags AND tl.post IN (" + id_s + ")");

        while (rs.next()) {
            String post = rs.getString("post");
            HashSet node = new HashSet();

            if (!tags.containsKey(post)) {
                node.add(rs.getString("tags"));
                tags.put(post, node);
            } else {
                node.add(rs.getString("tags"));
                node.addAll(tags.get(post));
                tags.put(post, node);
            }
        }
        
        //System.out.println(tags);
        return tags;
    }

    public Date getLastModified() {
        System.out.println(LastModified);
        return LastModified;
    }
}
