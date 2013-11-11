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
import java.util.Properties;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author mark
 */
public class ViewMethod {

    private Properties props = new Properties();
    {
        props.setProperty("title", "");
        props.setProperty("textSize", "1000");
        props.setProperty("comment", "false");
    }
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
        this.props = props;
    }
    
    public ViewMethod(ResultSet rs, Statement stmt, Properties props) {
        this.rs = rs;
        this.stmt = stmt;
        this.props = props;
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

                    } else if ("text".equals(colNames[j])) {
                        
                        int textSize = Integer.parseInt(props.getProperty("textSize"));
                        if (textSize <= 0) {
                            f = util.lineFeed(util.bbCode(StringEscapeUtils.escapeHtml4(rs.getString("text")) + ""));
                        } else {
                            f = util.Shortening(util.lineFeed(util.bbCode(StringEscapeUtils.escapeHtml4(rs.getString("text")) + "")), textSize, "<br><a href=\"http://yourmood.ru/post?id=" + rs.getString("id") + "\" target=\"_blank\" title=\"Откроется в новом окне\">Читать дальше</a>");
                        }

                    } else if ("itemCount".equals(colNames[j])) {
                        if (rs.getInt("itemCount") > 1) {
                            f = " (" + rs.getString("itemCount") + " фото)";
                            content.put("readMore", "<a href=\"/post?id=" + rs.getString("id") + "\" target=\"_blank\" title=\"Откроется в новом окне\">Читать дальше</a>");
                        } else {
                            f = "";
                        }

                    } else if ("vote".equals(colNames[j])) {
                        f = rs.getInt("vote") > 0 ? (Boolean.parseBoolean(props.getProperty("comment")) ? "+" : "") + rs.getString("vote") : rs.getString("vote");

                    } else if ("image".equals(colNames[j])) {
                        if (rs.getString("image") != null) {
                            if (!rs.getString("image").equals("")) {
                                content.put("alt", util.Shortening(util.specialCharactersTags(rs.getString("alt")), 255, ""));
                                f = rs.getString("image");
                            }
                        }

                    } else if ("img".equals(colNames[j])) {
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

                    } else if ("video".equals(colNames[j])) {
                        if (rs.getString("video") != null) {
                            if (!rs.getString("video").equals("")) {
                                f = ("<iframe width=\"600\" height=\"400\" src=\"//" + rs.getString("video") + "\" frameborder=\"0\" allowfullscreen></iframe>");
                            }
                        } else {
                            f = null;
                        }
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

        return LastModified;
    }
}
