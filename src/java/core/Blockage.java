/*
 * Блокировка действий пользователей по ip
 */
package core;

import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;
import org.apache.log4j.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author mark
 */
public class Blockage {

    private int timeLimit = 300;
    private boolean status = false; // true - пропуск есть, false - пропуска нет
    private String ip = "", process = "", id = "";
    private int vote = 0;
    private Statement stmt = null;
    private ResultSet rs = null;
    private PrintWriter out = null;
    private JSONObject resultJson = new JSONObject();
    private JSONParser parserJson = new JSONParser();
    private static final Logger logger = Logger.getLogger(Blockage.class);

    public Blockage(String ip, String process, String id, int vote, String action, Statement stmt, PrintWriter out) throws Exception {
        if (ip.length() < 1 || process.length() < 1
                || id.length() < 1 || action.length() < 1) {
            return;
        }

        this.ip = ip;
        this.process = process;
        this.id = id;
        this.vote = vote;
        this.stmt = stmt;
        this.out = out;

        if (action.equals("blockage")) {
            Blockage();
        }
    }

    private void Blockage() throws Exception {
        HashMap textJson = new HashMap();
        LinkedList ll = new LinkedList();

        ContainerFactory containerFactory = new ContainerFactory() {

            @Override
            public List creatArrayContainer() {
                return new LinkedList();
            }

            @Override
            public Map createObjectContainer() {
                return new LinkedHashMap();
            }
        };

        long time = new Date().getTime();

        rs = stmt.executeQuery("select * from `blockage2` where `ip`='" + ip + "' limit 1;");

        String part = "", extra = "";
        if (rs.next()) {
            Map json = (Map) parserJson.parse(rs.getString("process"), containerFactory);

            if (json.containsKey(process)) {

                Map next = (Map) json.get(process);

                if (next.containsKey(id)) {

                    if (next.size() > 100) {
                        Iterator it = next.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry) it.next();
                            if (!id.equals(pairs.getKey().toString())) {
                                it.remove();
                                break;
                            }
                        }
                    }

                    LinkedList ll2 = (LinkedList) next.get(id);
                    int dbVote = Integer.parseInt(ll2.get(0).toString());

                    if ((dbVote == 1 && vote == -1) || (dbVote == -1 && vote == 1)) {
                        ll.add(0, 0);
                        ll.add(1, time);
                        next.put(id, ll);
                        status = true;
                    } else if (dbVote == 0) {
                        ll.add(0, vote);
                        ll.add(1, time);
                        next.put(id, ll);
                        status = true;
                    } else {
                        vote = 0;
                    }

                    long procID = Time(Long.parseLong(ll2.get(1).toString()));

                    if (procID >= timeLimit) {
                        status = true;
                        ll.add(0, vote);
                        ll.add(1, time);
                        next.put(id, ll);
                    }

                    //out.println(procID);
                    json.put(process, next);
                } else {
                    ll.add(0, vote);
                    ll.add(1, time);
                    next.put(id, ll);
                    json.put(process, next);
                    status = true;
                }



            } else {
                ll.add(0, vote);
                ll.add(1, time);
                textJson.put(id, ll);
                json.put(process, textJson);
                status = true;
            }

            //out.println("<br>");
            //out.println("Действие: " + status);

            resultJson.putAll(json);
            //out.println("<br>");
            //out.println(resultJson);

            stmt.executeUpdate("update `blockage2` SET `process`='" + resultJson + "',`last_modified`=NOW() "
                    + "where `ip`='" + ip + "' limit 1");


        } else {
            ll.add(0, vote);
            ll.add(1, time);
            textJson.put(id, ll);
            resultJson.put(process, textJson);
            stmt.executeUpdate("insert into `blockage2` values ('" + ip + "','" + resultJson + "',NOW());");
            stmt.executeUpdate("delete from `blockage2` where `last_modified`<adddate(NOW(),INTERVAL -96 HOUR);");
        }


    }

    /**
     * Проверяем отведённое время
     *
     * @param expTime
     * @return
     */
    private long Time(long expTime) {
        Date currentDate = new Date();
        long currTime = currentDate.getTime();
        long diff = currTime - expTime;
        return diff / (1000);
    }

    public boolean getResult() {
        return status;
    }
}
