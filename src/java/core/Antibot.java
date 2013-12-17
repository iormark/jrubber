package core;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author mark Хранение ip 127.0.0.1=stop/play:time:count
 */
public class Antibot {

    private HashMap<String, String> ip = new HashMap<String, String>();
    private HashSet<String> bots = new HashSet<String>();
    private String addr;
    private boolean access = true; // доступ открыт/закрыт
    private int LimitQuery = 15;
    private int BlockingTime = 5000;
    private int TimeBetweenRequests = 300;

    /**
     *
     * @param LimitQuery лимит запросов
     * @param BlockingTime время блокировки
     * @param TimeBetweenRequests время между запросами
     * @param ip
     */
    public Antibot(int LimitQuery, int BlockingTime, int TimeBetweenRequests, HashMap<String, String> ip) {
        this.LimitQuery = LimitQuery;
        this.BlockingTime = BlockingTime;
        this.TimeBetweenRequests = TimeBetweenRequests;
        this.ip = ip;

        bots.add("searchmetricsbot");
        bots.add("ahrefsbot");
        bots.add("mail.ru_bot");
        bots.add("wbsearchbot");
        bots.add("solomonobot");
        bots.add("wbsearchbot");
        bots.add("nikolaydovydov@km.ru");
        bots.add("googlebot");
        bots.add("yandexbot");
        bots.add("dcpbot");
        bots.add("q312461");
        bots.add("screenerbot");
        
    }

    public void filterUserAgent(String addr, String UserAgent) {

        if (UserAgent == null) {
            access = false;
            long time = new Date().getTime();
            ip.put(addr, "s:" + time + ":50");
        } else {

            String[] m = UserAgent.split("[(;/:)]");

            for (String i : m) {

                if (bots.contains(i.trim().toLowerCase())) {
                    access = false;
                    long time = new Date().getTime();
                    ip.put(addr, "s:" + time + ":50");
                }
            }
        }
    }

    /**
     * Проверка доступа.
     *
     * @param addr
     */
    public void access(String addr) {
        this.addr = addr;
        if (ip.containsKey(addr)) {
            String[] m = ip.get(addr).split(":");
            if (m[0].equals("s")) {
                long timeDiff = Time(Long.parseLong(m[1]));
                if (timeDiff < BlockingTime) {
                    access = false;
                } else {
                    access = true;
                }
            } else {
                access = true;
            }
        } else {
            access = true;
        }

        service();
    }

    private synchronized void service() {
        long time = new Date().getTime();
        if (!ip.containsKey(addr)) {
            ip.put(addr, "p:" + time + ":1");
        } else {
            if (access != true) {
            } else {
                String[] m = ip.get(addr).split(":");
                long timeDiff = Time(Long.parseLong(m[1]));
                int count = Integer.parseInt(m[2]);

                if (timeDiff < TimeBetweenRequests) {
                    ip.put(addr, "p:" + time + ":" + (count + 1));
                } else {
                    ip.put(addr, "p:" + time + ":" + ((count * 70) / 100));
                }

                if ((count + 1) >= LimitQuery) {
                    ip.put(addr, "s:" + time + ":" + ((count * 70) / 100));
                }

                //get = ip + " -> " + timeDiff + ":" + count;
            }
        }

        if (ip.size() > 5000) {
            ip.clear();
        }
    }

    /**
     * Проверяем отведённое время
     *
     * @param expTime
     * @return
     */
    private long Time(long expTime) {
        long currTime = new Date().getTime();
        long diff = currTime - expTime;
        return diff;
    }

    public boolean getAccess() {
        return access;
    }

    public HashMap get() {
        return ip;
    }
}