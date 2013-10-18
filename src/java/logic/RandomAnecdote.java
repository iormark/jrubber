
package logic;

import core.Util;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 *
 * @author mark
 */
public class RandomAnecdote extends Creator {
    
    private LinkedHashMap<String, HashMap> item = new LinkedHashMap<String, HashMap>();
    private Util util = new Util();
    
    public RandomAnecdote(Statement stmt) throws SQLException {
/*

        String query = null;
        ResultSet rs = stmt.executeQuery("select max(`id`) as `max` from `зщые_шеуь` WHERE status='on'");
        if (rs.next()) {
            query = "select * from `humor`  WHERE status='on' limit " + (new Random().nextInt(rs.getInt("max")-1)+1) + ", 1;";
        }

        if (query != null) {
            rs = stmt.executeQuery(query);

            while (rs.next()) {

                HashMap<String, String> content = new HashMap<String, String>();

                content.put("text", util.bbCode(rs.getString("text")));

                if (rs.getString("image") != null) {
                    content.put("alt", util.Shortening(util.specialCharactersTags(rs.getString("alt")), 255, "") );
                    content.put("image", rs.getString("image"));
                }

                content.put("date", rs.getString("date"));

                item.put(rs.getString("id"), content);
            }
        }
*/
    }
    
    public LinkedHashMap getItem() {
        return item;
    }

    @Override
    public String getMetaTitle() {
        return "Случайный анекдот.";
    }

    @Override
    public int getServerStatus() {

        if (item.isEmpty()) {
            return 404;
        } else {
            return 200;
        }
    }

    @Override
    public Date getLastModified() {
        /*SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
        
        Date d = null;
        try {
            d = formatter.parse("Thu, 26 Jul 2012 15:00:52 GMT");
        } catch (ParseException e) {  
        }
        
        return d;*/
        return null;
    }
    
    @Override
    public String getMetaHead() {
        return "";
    }
}
