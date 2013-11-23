package logic;

import core.CategoriesTree;
import core.TagsTree;
import core.Util;
import core.ViewMethod;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import logic.user.Check;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author mark
 */
public class NavBlock extends CreatorBlock {

    private HttpServletRequest request = null;
    private Statement stmt = null;
    private Check check = null;
    private ResultSet rs = null;
    private Properties cfg = new Properties();
    private Util util = new Util();
    private String TypeTitle = null;

    public NavBlock(HttpServletRequest request, Statement stmt, Check check) {
        this.stmt = stmt;
        this.request = request;
        this.check = check;
    }
    
    public HashMap getUserMenu() {
        HashMap content = new HashMap();
        
        
        
        return content;
    }

    public String getTimeAddition() {
        String datetime = null;

        try {
            rs = stmt.executeQuery("SELECT svc_date FROM `post` WHERE status='on' ORDER BY `date` DESC LIMIT 1");
            if (rs.next()) {
                Calendar c = new GregorianCalendar();
                c.setTimeInMillis(rs.getTimestamp("svc_date").getTime());
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
        //CategoriesTree ct = new CategoriesTree(stmt, 0, true);
        //return ct.getCategoriesLi();
        return null;
    }
    
    public Map<String, HashMap> getTagsLi() throws SQLException {
        TagsTree tt = new TagsTree(stmt, 0, true);
        return tt.getTags();
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

        rs = stmt.executeQuery("SELECT MAX(p.id) AS max, MIN(p.id) AS min FROM `post_item` i, post p WHERE i.post = p.id AND p.status ='on' AND i.type is not null");
        if (rs.next()) {
            max = rs.getInt("max");
            min = rs.getInt("min");
        }

        rand = min + random.nextInt((max - min) + 1);

        rs = stmt.executeQuery("SELECT i.content, i.type, p.* FROM `post_item` i, `post` p WHERE i.post = p.id AND p.status ='on' "
                + "AND (p.id <=" + max + " AND p.id>=" + min + ") AND p.id>=" + rand + " AND i.type is not null GROUP BY i.post LIMIT 1");
       
        Properties props = new Properties();
        props.setProperty("title", "Подробнее...");
        props.setProperty("textSize", "200");
        ViewMethod view = new ViewMethod(rs, props);
        item = view.getMinItem(rs);


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
