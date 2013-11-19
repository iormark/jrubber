/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Properties;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author mark
 */
public class ViewMethod {

    Properties defaultProps = new Properties();

    {
        defaultProps.setProperty("title", "");
        defaultProps.setProperty("textSize", "1000");
        defaultProps.setProperty("textLineFeed", "true");
        defaultProps.setProperty("comment", "false");
        defaultProps.setProperty("videoIframe", "true");
    }
    private Properties props = new Properties(defaultProps);

    private Date LastModified = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    private LinkedHashMap<String, HashMap> items = new LinkedHashMap();
    private HashMap<String, HashSet> tags = new HashMap();
    private Util util = new Util();
    private XmlOptionReader xor = new XmlOptionReader();
    boolean view = false, comment = false; //true-в крточке

    public ViewMethod(ResultSet rs, Statement stmt) {
        this.rs = rs;
        this.stmt = stmt;
    }

    public ViewMethod(ResultSet rs, Statement stmt, boolean view, boolean comment) {
        this.rs = rs;
        this.stmt = stmt;
        this.view = view;
        this.comment = comment;
    }

    public ViewMethod(ResultSet rs) {
        this.rs = rs;
    }

    public ViewMethod(ResultSet rs, boolean view, boolean comment) {
        this.rs = rs;
        this.view = view;
        this.comment = comment;
    }

    public ViewMethod(ResultSet rs, Properties props) {
        this.rs = rs;
        this.props.putAll(props);
    }

    public ViewMethod(ResultSet rs, Statement stmt, Properties props) {
        this.rs = rs;
        this.stmt = stmt;
        this.props.putAll(props);
    }

    public LinkedHashMap<String, HashMap> getPostItem(ResultSet rs) throws SQLException {

        while (rs.next()) {

            HashMap map = new HashMap();
            String id = rs.getString("id");
            String login = rs.getString("login");
            String title = rs.getString("title");
            int vote = rs.getInt("vote");
            Date created = rs.getTimestamp("date");
            int commentCount = rs.getInt("commentCount");

            String type = rs.getString("type");
            String content = rs.getString("content");
            String type2 = rs.getString("type2");
            String content2 = rs.getString("content2");

            map.put("login", login);
            if (title == null) {
                title = "№ " + title;
            } else {
                title = util.Shortening(StringEscapeUtils.escapeHtml4(title), 85, "");
            }
            

            if (title.equals("")) {
                title = "№ " + rs.getString("id");
            } else {
                title = util.Shortening(StringEscapeUtils.escapeHtml4(rs.getString("title") + ""), 85, "");
            }
            map.put("title", title);

            map.put("vote", vote > 0 ? (Boolean.parseBoolean(props.getProperty("comment")) ? "+" : "") + vote : vote);
            map.put("created", util.dateFormat(created));
            map.put("commentCount", commentCount);

            if (content != null || type != null) {
                map.putAll(getType("", content, type, id));
            }
            if (content2 != null || type2 != null) {
                map.putAll(getType("2", content2, type2, id));
            }

            items.put(id, map);

            if (LastModified == null) {
                LastModified = rs.getTimestamp("last_modified");
            }
        }
        //System.out.println(items);
        return items;
    }

    public LinkedHashMap<String, HashMap> getItem(ResultSet rs) throws SQLException {

        while (rs.next()) {

            HashMap map = new HashMap();
            String item = rs.getString("id");
            String post = rs.getString("post");
            String type = rs.getString("type");
            String content = rs.getString("content");

            if (content == null || type == null) {
                continue;
            }

            map.putAll(getType("", content, type, post));
            map.put("type", type);
            map.put("sort", rs.getString("sort"));
            items.put(item, map);
        }
        //System.out.println(items);
        return items;
    }

    public LinkedHashMap<String, HashMap> getMinItem(ResultSet rs) throws SQLException {

        while (rs.next()) {

            HashMap map = new HashMap();
            String item = rs.getString("id");
            String title = rs.getString("title");
            String type = rs.getString("type");
            String content = rs.getString("content");

            if (content == null || type == null) {
                continue;
            }

            map.putAll(getType("", content, type, item));
            map.put("title", title);
            map.put("type", type);
            items.put(item, map);
        }
        //System.out.println(items);
        return items;
    }

    public HashMap getType(String suff, String content, String type, String post) {
        HashMap map = new HashMap();
        switch (type) {
            case "text":
                content = StringEscapeUtils.escapeHtml4(content);

                if (Boolean.parseBoolean(props.getProperty("textLineFeed"))) {
                    content = util.lineFeed(util.bbCode(content));
                }

                int textSize = Integer.parseInt(props.getProperty("textSize"));
                if (textSize > 0) {
                    content = util.Shortening(content, textSize, "<br><a href=\"http://yourmood.ru/post?id=" + post + "\" target=\"_blank\" title=\"Откроется в новом окне\">Читать дальше</a>");
                }
                map.put("content" + suff, content);

                break;
            case "image":
                if (!content.equals("")) {
                try {
                    xor.setField(new String[]{"original", "middle"});
                    HashMap<String, HashMap> h = xor.setDocument(content);

                    map.put("content" + suff, h.get("original").get("name"));
                    map.put("imagePath" + suff, h.get("original").containsKey("path") ? h.get("original").get("path") : "/photo_anekdot");

                    map.put("width" + suff, h.get("middle").get("width"));
                    map.put("height" + suff, h.get("middle").get("height"));

                    int gif = Integer.parseInt(h.get("original").get("animated").toString());
                    map.put("animated" + suff, gif > 0 ? "img__animated" : "");

                    long size = Long.parseLong(h.get("original").get("size").toString());
                    map.put("size" + suff, util.sizeFormat(size));
                } catch (Exception ex) {
                    System.out.println(ex);
                }

            }
                break;
            case "video":
                System.out.println(":::" + props.getProperty("videoIframe"));
                if (Boolean.parseBoolean(props.getProperty("videoIframe"))) {
                    content = ("<iframe width=\"600\" height=\"400\" src=\"//" + content + "\" frameborder=\"0\" allowfullscreen></iframe>");
                }

                map.put("content" + suff, content);
                break;
        }

        map.put("type" + suff, type);
        return map;
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

                    if ("last_modified".equals(colNames[j])) {
                        if (LastModified == null) {
                            LastModified = rs.getTimestamp("last_modified");
                        }

                    } else if ("date".equals(colNames[j])) {
                        f = util.dateFormat(rs.getTimestamp("date"));
                        content.put("dateMore", new SimpleDateFormat("d MMM yy, HH:mm:ss").format(rs.getTimestamp("date")));

                    } else if ("title".equals(colNames[j])) {

                        if (rs.getString("title").equals("")) {
                            //if(!props.getProperty("title").equals("")) {
                            //    f = props.getProperty("title");
                            //} else {
                            f = "№ " + rs.getString("id");
                            //}
                        } else {
                            f = util.Shortening(StringEscapeUtils.escapeHtml4(rs.getString("title") + ""), 85, "");
                        }

                    } else if ("vote".equals(colNames[j])) {
                        f = rs.getInt("vote") > 0 ? (Boolean.parseBoolean(props.getProperty("comment")) ? "+" : "") + rs.getString("vote") : rs.getString("vote");

                    } else if ("status".equals(colNames[j])) {

                        String state = rs.getString("status");
                        if (state != null) {
                            if ("on".equals(state)) {
                                f = "";
                            } else if ("off".equals(state)) {
                                f = "выключен";
                            } else if ("black".equals(state)) {
                                f = "спам";
                            } else if ("check".equals(state)) {
                                f = "ожидает проверки";
                            }
                        } else {
                            f = null;
                        }
                    } else if ("content".equals(colNames[j])) {
                    }

                    //System.out.println(colNames[j]+"="+f);
                    content.put(colNames[j], f);
                    content.remove("img");
                }
            }

            items.put(rs.getString("id"), content);
        }
        return items;
    }

    public HashMap<String, HashSet> getPostTags(int id) throws SQLException {

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

        return tags;
    }

    public HashMap<String, HashSet> getPostTags() throws SQLException {
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

        return LastModified;
    }
}
