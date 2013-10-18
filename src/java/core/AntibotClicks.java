package core;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 *
 * @author mark
 * Хранение ipck 127.0.0.1=stop/play:time:count
 */
public class AntibotClicks {
    public static LinkedHashMap<String, String> ipck = new LinkedHashMap<String, String>();
    private String addr;
    private boolean access = true; // доступ открыт/закрыт
    private int limit = 3;

    public AntibotClicks() {
    }

    /**
     * Проверка доступа.
     * @param addr
     * @return
     */
    public void access(String addr) {
        this.addr = addr;
        if (ipck.containsKey(addr)) {
            String[] m = ipck.get(addr).split(":");
            if (m[0].equals("s")) {
                long timeDiff = Time(Long.parseLong(m[1]));
                if (timeDiff < 5000) {
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
        if (!ipck.containsKey(addr)) {
            ipck.put(addr, "p:"+time + ":1");
        } else {
            if (access != true) {
            } else {
                String[] m = ipck.get(addr).split(":");
                long timeDiff = Time(Long.parseLong(m[1]));
                int count = Integer.parseInt(m[2]);

                if (timeDiff < 500) {
                    ipck.put(addr, "p:" + time + ":" + (count + 1));
                } else {
                    ipck.put(addr, "p:"+time + ":" + ((count * 50) / 100));
                }

                if ((count + 1) >= limit) {
                    ipck.put(addr, "s:" + time + ":" + ((count * 50) / 100));
                }

                //get = ipck + " -> " + timeDiff + ":" + count;
            }
        }

        if (ipck.size() > 5000) {
            ipck.clear();
        }
    }

    /**
     * Проверяем отведённое время
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

    public String get() {
        return ipck.toString();
    }
}