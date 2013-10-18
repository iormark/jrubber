package core;

import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 *
 * @author mark
 */
public class UrlOption {

    //private HttpServletRequest request;
    private URL url;
    private LinkedHashMap<String, String> getParam = new LinkedHashMap<String, String>();
    private static final Logger logger = Logger.getLogger(UrlOption.class);

    public UrlOption() {
    }

    public UrlOption(HttpServletRequest request) {
        try {
            url = new URL(request.getRequestURL() + (request.getQueryString() == null ? "" : "?" + request.getQueryString()));
        } catch (Exception e) {
            logger.error(e);
        }

        HashMap set = setParam();
        if (set != null) {
            getParam.putAll(set);
        }

    }

    /**
     * Добавляет новый параметр к URL newParam=hello Конструкция param=[delete]
     * удаляет необходимый параметр.
     *
     * @return
     */
    public String addParam(String add) {

        StringBuilder result = new StringBuilder();

        try {
            LinkedHashMap<String, String> p = new LinkedHashMap<>();

            LinkedHashMap<String, String> AddPar = new LinkedHashMap<>();

            String[] mk = add.split("&");

            for (int i = 0; i < mk.length; i++) {
                String[] mv = mk[i].split("=");

                AddPar.put(mv[0], mv[1]);
            }


            if (url.getQuery() != null) {
                String[] param = url.getQuery().split("&");

                for (int i = 0; i < param.length; i++) {
                    String[] m = param[i].split("=");

                    if (m.length == 2) {
                        p.put(m[0], m[1]);
                    }
                }
            }

            if (p.isEmpty()) {
                p = AddPar;
            } else {
                p.putAll(AddPar);
            }


            Set<Map.Entry<String, String>> set = p.entrySet();
            for (Map.Entry<String, String> me : set) {
                if (!me.getValue().equals("[delete]")) {
                    result.append(me.getKey()).append("=").append(me.getValue()).append("&");
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }
        
        return result.toString().replaceAll("&$", "");
    }

    /**
     * Добавляет новый параметр к URL переданному в параметре newParam=hello
     * Конструкция param=[delete] удаляет необходимый параметр.
     *
     * @param add
     * @param url
     * @return
     */
    public String addParam(String add, URL url) {
        //System.out.println(add+" - "+url);

        if (url.getQuery() == null) {
            return add;
        }

        if (add == null) {
            return url.getQuery();
        }

        StringBuilder result = new StringBuilder();

        try {
            LinkedHashMap<String, String> p = new LinkedHashMap();
            String[] add_arr = add.split("=");
            String[] param = url.getQuery().split("&");


            for (int i = 0; i < param.length; i++) {
                String[] m = param[i].split("=");
                if (m.length == 2) {
                    if (add_arr[0].equals(m[0]) && add_arr[1].equals("[delete]")) {
                    } else {
                        p.put(m[0], m[1]);
                    }
                }
            }

            if (!add_arr[1].equals("[delete]")) {
                p.put(add_arr[0], add_arr[1]);
            }

            Set<Map.Entry<String, String>> set = p.entrySet();
            for (Map.Entry<String, String> me : set) {
                result.append(me.getKey()).append("=").append(me.getValue()).append("&");
            }

        } catch (Exception e) {
            logger.error(e);
        }
        System.out.println(result);
        return result.toString().replaceAll("[&]$", "");
    }

    /**
     * Добавляет в hash таблицу список параметров из URL запроса.
     *
     * @return
     */
    private HashMap setParam() {

        if (url.getQuery() == null) {
            return null;
        }

        LinkedHashMap<String, String> p = new LinkedHashMap<String, String>();

        try {
            String[] param = URLDecoder.decode(url.getQuery()).split("&");

            for (int i = 0; i < param.length; i++) {
                String[] m = param[i].split("=");
                if (m.length == 2) {
                    p.put(m[0], m[1]);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return p;
    }
    
    public String addParam1_1(String add) {
        if (url.getQuery() == null) {
            return add;
        }

        if (add == null) {
            return url.getQuery();
        }
        
        StringBuilder result = new StringBuilder();

        try {
            LinkedHashMap<String, String> just = new LinkedHashMap();
            HashMap<String, String> a = new HashMap();
            HashMap<String, String> b = new HashMap();

            String[] AddParam = add.split("&");
            String[] Param = url.getQuery().split("&");


            for (int i = 0; i < Param.length; i++) {
                
                String[] m = Param[i].split("=");
                a.put(m[0], m.length == 2 ? m[1] : "");
            }

            for (int i = 0; i < AddParam.length; i++) {
                String[] m = AddParam[i].split("=");
                b.put(m[0], m.length == 2 ? m[1] : "");
            }

            a.putAll(b);
            b.clear();


            Set<Map.Entry<String, String>> set = a.entrySet();
            for (Map.Entry<String, String> me : set) {

                if (!me.getValue().equals("[delete]")) {
                    result.append(me.getKey()).append("=").append(me.getValue()).append("&");
                }

            }

        } catch (Exception e) {
            //System.out.println(e);
            logger.error(e);
        }

        
        return result.toString().replaceAll("(&)$", "");
    }

    /**
     * Возвращает таблицу параметров из URL запроса.
     *
     * @return
     */
    public HashMap getParam() {
        return getParam;
    }

    /**
     * Придаём числовым данным из GET запроса правильный вид. Запросы с
     * параметрами типа show=1/0
     *
     * @param str
     * @return
     */
    public int Format(String str) {
        if (str == null) {
            str = "0";
        }

        str = str.replaceAll("[^0-1]", "0").trim();
        if (str.length() < 1) {
            str = "0";
        }
        return Integer.parseInt(str);
    }

    /**
     * Принимает строку, парсит в число. Если невозможно перевести в число,
     * возвращает число переданное в параметре.
     *
     * @param numberStr
     * @param def
     * @return
     */
    public int NumberReplacementInt(String numberStr, int def) {
        int number = def;

        try {
            number = Integer.parseInt(numberStr);
        } catch (Exception e) {
            return def;
        }

        return number;
    }

    /**
     * Принимает строку, парсит в число. Если невозможно перевести в число,
     * возвращает число переданное в параметре.
     *
     * @param numberStr
     * @param def
     * @return
     */
    public long NumberReplacementLong(String numberStr, long def) {
        long number = def;

        try {
            number = Long.parseLong(numberStr);
        } catch (NumberFormatException e) {
            return def;
        }

        return number;
    }

    /**
     * Придаём числовым данным из GET запроса правильный вид. Запросы с
     * параметрами типа id=3454
     *
     * @param str
     * @return int
     */
    public String Format1Str(String str, String def) {
        if (str == null) {
            str = def;
        }

        str = str.replaceAll("[^0-9]", "").replaceAll("[0-9]{9,}", "").trim();
        if (str.length() < 1) {
            str = def;
        }
        return str;
    }

    /**
     * Если число больше заданной величины, возвращает ноль.
     *
     * @param str
     * @param max
     * @return
     */
    public int Format2(String str, int max) {
        if (str == null) {
            return 0;
        }

        str = str.replaceAll("[^0-9]", "").replaceAll("[0-9]{3,}", "").trim();
        if (str.length() == 0) {
            return 0;
        }
        if (Integer.parseInt(str) > max) {
            str = "0";
        }
        return Integer.parseInt(str);
    }

    /**
     * Данные в параметрах типа shop=1,2,3,4
     *
     * @param str
     * @param max
     * @return
     */
    public String Format3(String str) {
        if (str == null || str.length() == 0) {
            return "0";
        }
        return str.replaceAll("[^0-9,]", "").trim();
    }

    public String addHref(Object str, String reg, String url, String extra) {
        if (str == null) {
            return "";
        }
        String s = str.toString();

        if (s.length() < 2) {
            return "";
        }

        if (reg.length() == 0) {
            s = "<a href='" + url + s.replaceAll("<[^>]*>", "") + (extra.length() > 0 ? "&" + extra : "") + "'>" + s + "</a>";
        } else {
            String m[] = s.split(reg);
            s = "";
            for (int i = 0; i < m.length; i++) {
                s += "<a href='" + url + m[i].replaceAll("<[^>]*>", "") + (extra.length() > 0 ? "&" + extra : "") + "'>" + m[i].trim() + "</a>, ";
            }
        }
        return s.replaceAll("(\\s*" + reg + "\\s*)$", "").trim();
    }
}