package logic;

import core.CategoriesTree;
import core.Util;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author mark
 */
public class NavBlock extends CreatorBlock {

    private HttpServletRequest request = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    private Properties cfg = new Properties();
    private Util util = new Util();
    private String TypeTitle = null;

    public NavBlock(HttpServletRequest request, Statement stmt) {
        this.stmt = stmt;
        this.request = request;
    }

    public String getTimeAddition() {
        String datetime = null;

        try {
            rs = stmt.executeQuery("SELECT date FROM `post` WHERE status='on' ORDER BY `date` DESC LIMIT 1");
            if (rs.next()) {
                Calendar c = new GregorianCalendar();
                c.setTimeInMillis(rs.getTimestamp("date").getTime());
                c.add(Calendar.MINUTE,61);

                //datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
                
                long time = c.getTimeInMillis();

                time = (time - System.currentTimeMillis());

                long hour = (time / (1000 * 60 * 60) );
                long minute = (((time) / (1000* 60)) % 60 );

                if (hour >= 0 && minute >= 0) {
                    datetime = "<span>" + (hour < 10 ? "0" + hour : hour) + "</span> ч. "
                            + "<span>" + (minute < 10 ? "0" + minute : minute) + "</span> мин.";
                }

            }
        } catch (SQLException ex) {
        }
        return datetime;
    }

    
    public StringBuilder getCategoriesLi() throws SQLException {
        CategoriesTree ct = new CategoriesTree(stmt, 0, true);
        return ct.getCategoriesLi();
        //return null;
    }

    public LinkedHashMap getWidget() throws SQLException {
        LinkedHashMap<String, HashMap> item = new LinkedHashMap<String, HashMap>();


        int rand = 0, max = 0, min = 0;
        ResultSet rs = null;
        CategoriesTree ct = null;
        Random random = new Random();

        int[] cat = new int[]{2, 8, 10};
        int n = (int) Math.floor(Math.random() * cat.length);

        ct = new CategoriesTree(stmt, cat[n]);
        String type = " AND p.type IN(" + ct.getCategoriesAllId() + ")";

        rs = stmt.executeQuery("SELECT MAX(id) AS max, MIN(id) AS min FROM post p WHERE p.status ='on'" + type);
        if (rs.next()) {
            max = rs.getInt("max");
            min = rs.getInt("min");
        }

        rand = min + random.nextInt((max - min) + 1);

        rs = stmt.executeQuery("SELECT t.name AS typeName, t.hurl, i.text, i.image, i.alt,COUNT(i.id) as CountPosts, p.* FROM type t, `post_item` i, `post` p WHERE t.id=p.type AND i.post = p.id AND p.status ='on' "
                + "AND (p.id <=" + max + " AND p.id>=" + min + ") AND p.id>=" + rand + type + " GROUP BY i.post LIMIT 1");
        if (rs.next()) {
            HashMap<String, String> content = new HashMap<String, String>();


            content.put("id", rs.getString("id"));
            content.put("text", util.Shortening(util.bbCode(rs.getString("text")), 400, "<br><a href=\"/anekdot?id=" + rs.getString("id") + "\" target=\"_blank\">Читать дальше</a>"));

            if (rs.getString("image") != null) {
                content.put("alt", util.specialCharactersTags(rs.getString("alt")));
                content.put("image", rs.getString("image"));
            }


            content.put("typeHurl", rs.getString("hurl"));
            content.put("typeName", rs.getString("typeName"));
            content.put("date", new SimpleDateFormat("d MMM yyyy, HH:mm:ss").format(rs.getTimestamp("date")));

            item.put("all", content);
        }


        return item;
    }

    @Override
    public String getTypeTitle() {
        return TypeTitle;
    }

    @Override
    public HashMap getBlock() {
        throw null;
    }
}
