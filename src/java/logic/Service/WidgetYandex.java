package logic.Service;

import core.CategoriesTree;
import core.Util;
import core.ViewMethod;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import logic.Creator;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
public class WidgetYandex extends Creator {

    private LinkedHashMap<String, HashMap> item = new LinkedHashMap();
    private Util util = new Util();
    private static final Logger logger = Logger.getLogger(WidgetYandex.class);

    public WidgetYandex(HttpServletRequest request, Statement stmt) throws Exception {
        String act = request.getParameter("act") != null ? request.getParameter("act") : "";
        String type = request.getParameter("type") != null ? request.getParameter("type") : "";

        int rand = 0, max = 0, min = 0;
        ResultSet rs = null;
        CategoriesTree ct = null;
        Random random = new Random();

        rs = stmt.executeQuery("SELECT MAX(p.id) AS max, MIN(p.id) AS min FROM `post_item` i, post p WHERE i.post = p.id AND p.status ='on' AND p.age='0' AND p.profanity='off' AND i.type is not null");
        if (rs.next()) {
            max = rs.getInt("max");
            min = rs.getInt("min");
        }

        rand = min + random.nextInt((max - min) + 1);

        rs = stmt.executeQuery("SELECT i.content, i.type, p.* FROM `post_item` i, `post` p WHERE i.post = p.id AND p.status ='on' AND p.age='0' AND p.profanity='off' "
                + "AND (p.id <=" + max + " AND p.id>=" + min + ") AND p.id>=" + rand + " AND i.type is not null GROUP BY i.post LIMIT 1");
       
        Properties props = new Properties();
        props.setProperty("title", "Подробнее...");
        props.setProperty("textSize", "200");
        ViewMethod view = new ViewMethod(rs, props);
        item = view.getMinItem(rs);

        
        logger.info(request.getRemoteAddr()+" - " + request.getHeader("Referer"));
    }

    public LinkedHashMap getItem() {
        return item;
    }

    @Override
    public String getMetaTitle() {
        return "";
    }

    @Override
    public String getMetaHead() {
        return "";
    }

    @Override
    public Date getLastModified() {
        return null;
    }

    @Override
    public int getServerStatus() {
        return 200;
    }
}
