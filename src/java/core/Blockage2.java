/*
 * Блокировка действий пользователей по ip
 */
package core;

import java.sql.*;
import java.util.Date;
import org.apache.log4j.*;

/**
 *
 * @author mark
 */
public class Blockage2 {

    private int timeLimit = 60*24*3;
    private long timeDiff = 0;
    private boolean onoff = true;
    private String ip = "", process = "", id = "";
    
    private Statement stmt = null;
    private ResultSet rs = null;

    private static final Logger logger = Logger.getLogger(Blockage2.class);

    public Blockage2(String ip, String process, String id, String action, Statement stmt) throws Exception {
        if (ip.length() < 1 || process.length() < 1 || 
                id.length() < 1 || action.length() < 1) {
            return;
        }

        this.ip = ip;
        this.process = process;
        this.id = id;
        this.stmt = stmt;
        
        if(action.equals("blockage")) {
            Blockage();
        }
    }

    private void Blockage() throws Exception {

        long time = new Date().getTime();

            rs = stmt.executeQuery("select * from `blockage2` where `ip`='" + ip + "' limit 1;");

            String part = "", extra = "";
            if (rs.next()) {
                String[] a = rs.getString("process").split(",");
                for (int i = 0; i < a.length; i++) {
                    String[] b = a[i].split("=");
                    if (b[0].equals(process)) {
                        part = b[1];
                        onoff = false;
                        continue;
                    }
                    extra += a[i] + ",";
                }

                extra = extra.replaceAll(",$", "");
                a = null;

                // добавляем новый процесс, если его ещё нет.
                if (onoff == true) {
                    part = process + "=" + id + ":" + time + "," + extra;
                } else {

                    String[] b = part.split(";");
                    part = process + "=";
                    onoff = true;
                    for (int i = 0; i < b.length; i++) {
                        String[] c = b[i].split(":");
                        if (c[0].equals(id)) {
                            onoff = false;
                            timeDiff = Time(Long.parseLong(c[1]));
                            //System.out.println("1=" + timeDiff);
                            if (timeDiff >= timeLimit) {
                                onoff = true;
                                continue;
                            }
                        } else {
                            timeDiff = Time(Long.parseLong(c[1]));
                            //System.out.println("2=" + timeDiff);
                            if (timeDiff >= timeLimit) {
                                continue;
                            }
                        }

                        part += b[i] + ";";
                    }

                    // добавляем новый id в процесс, если его там ещё нет.
                    if (onoff == true) {
                        part += id + ":" + time + ";";
                    }
                    part = (part.replaceAll(";$", "") + "," + extra).replaceAll(",$", "");

                }
                /*
                 if (onoff == false) {
                    System.out.println("Блокирован " + onoff);
                } else {
                    System.out.println("Разрешён " + onoff);
                }
                System.out.println(part);
                */
                stmt.executeUpdate("update `blockage` SET `process`='" + part + "',`datetime`=NOW() "
                        + "where `ip`='" + ip + "' limit 1;");
            } else {
                stmt.executeUpdate("insert into `blockage` values "
                        + "('" + ip + "','" + process + "=" + id + ":" + time + "',NOW());");
                stmt.executeUpdate("delete from `blockage` where `datetime`<adddate(NOW(),INTERVAL -5 HOUR);");
            }

    }

    /**
     * Проверяем отведённое время
     * @param expTime
     * @return
     */
    private long Time(long expTime) {
        Date currentDate = new Date();
        long currTime = currentDate.getTime();
        long diff = currTime - expTime;
        return diff / (1000 * 60);
    }

    public boolean getResult() {
        return onoff;
    }
}
