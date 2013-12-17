/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Part;

/**
 *
 * @author mark
 */
public class Util {

    private static HashSet notLogin = new HashSet();

    {
        notLogin.add("search");
        notLogin.add("searching");
        notLogin.add("edits");
        notLogin.add("links");
        notLogin.add("catalog");
        notLogin.add("category");
        notLogin.add("catalogs");
        notLogin.add("password");
        notLogin.add("users");
        notLogin.add("yourmood");
        notLogin.add("terms_of_use");
        notLogin.add("tagged");
        notLogin.add("register");
        notLogin.add("login");
        notLogin.add("logins");
        notLogin.add("signout");
        notLogin.add("anekdot");
        notLogin.add("anekdoty");
        notLogin.add("aforizm");
        notLogin.add("kartinka");
        notLogin.add("comment");
        notLogin.add("index");
        notLogin.add("reset");
        notLogin.add("about");
        notLogin.add("widget");
        notLogin.add("default");
        notLogin.add("share");
        notLogin.add("posts");
        notLogin.add("robot");
        notLogin.add("robots");
        notLogin.add("sitemap");
        notLogin.add("upload");
        notLogin.add("servlet");
        notLogin.add("porka");
    }

    /**
     * Если строка длинее заданной длины этот метод обрезает с учотом новых
     * слов.
     *
     * @param str
     * @param limit
     * @return
     */
    public String Shortening(String str, int limit, String def) {
        if (str == null) {
            return "";
        }

        str = str.replaceAll("\\s{2,}", " ")/*
                 * .replaceAll("({3,}[.]{3,})$", " ")
                 */.trim();

        if (str.length() > limit) {
            String extra[] = str.split("[\\s]");
            str = "";
            int len = 0;
            for (int i = 0; i < extra.length; i++) {
                len += extra[i].trim().length() + 1;
                if (len > limit) {
                    break;
                }
                str += extra[i].trim() + " ";
            }

            if (str.length() > 10) {
                str = str.trim().replaceAll("[.]$", "");
            }

            str = str.replaceAll("(<[^>]*>\\s*)$", "");
            str += "<b>...</b>" + def;
        }

        return str;
    }

    public String dateFormat(Date create) {
        if (create == null) {
            return "";
        }

        long time = System.currentTimeMillis() - create.getTime();
        long day = (time / (1000 * 60 * 60 * 24));
        long hour = (time / (1000 * 60 * 60));
        long minute = (((time) / (1000 * 60)) % 60);
        String string = "";

        if (day == 0) {

            if (hour > 0) {
                if (hour == 1) {
                    string = "Час ";
                } else if (hour == 21) {
                    string = hour + " час ";
                } else if ((hour >= 2 && hour <= 4) || (hour >= 22 && hour <= 24)) {
                    string = hour + " часa ";
                } else if (hour >= 5 && hour <= 20) {
                    string = hour + " часов ";
                }
                string = string + " назад";
            } else {
                if (minute > 0) {
                    string = minute + " мин назад";
                } else {
                    string = "Только что";
                }
            }
        } else if (day <= 7) {
            if (day == 1) {
                string = day + " день ";
            } else if ((day >= 2 && day <= 4)) {
                string = day + " дня ";
            } else if (day >= 5) {
                string = day + " дней ";
            }
            string = string + " назад";
        } else {
            string = new SimpleDateFormat("d MMM yy в HH:mm").format(create);
        }

        return string;
    }

    public String dateFormat2(Date create) {
        if (create == null) {
            return "";
        }

        long time = System.currentTimeMillis() - create.getTime();
        long day = (time / (1000 * 60 * 60 * 24));
        long hour = (time / (1000 * 60 * 60));
        long minute = (((time) / (1000 * 60)) % 60);
        String string = "";

        if (day == 0) {

            if (hour > 0) {
                if (hour == 1) {
                    string = "Час ";
                } else if (hour == 21) {
                    string = hour + " час ";
                } else if ((hour >= 2 && hour <= 4) || (hour >= 22 && hour <= 24)) {
                    string = hour + " часa ";
                } else if (hour >= 5 && hour <= 20) {
                    string = hour + " часов ";
                }
                string = string + "";
            } else {
                if (minute > 0) {
                    string = minute + " мин";
                } else {
                    string = "Только что";
                }
            }
        } else if (day <= 7) {
            if (day == 1) {
                string = day + " день ";
            } else if ((day >= 2 && day <= 4)) {
                string = day + " дня ";
            } else if (day >= 5) {
                string = day + " дней ";
            }
        } else {
            string = new SimpleDateFormat("d MMM yy в HH:mm").format(create);
        }

        return string;
    }

    /**
     *
     * @param currency
     * @return
     */
    public String PriceFormat(double currency) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        return currencyFormatter.format(currency);
    }

    /**
     *
     * @param currency
     * @return
     */
    public String NumberFormat(double currency) {
        NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
        return currencyFormatter.format(currency);
    }

    /**
     * Байты в килобайты, мегабайты, гигабайты
     *
     * @param size
     * @return
     */
    public String sizeFormat(float size) {
        String[] sizenames = new String[]{" bytes", " кб", " мб", " гб", " тб", " пб"};

        int sizename = 0;
        while (Math.floor(size / 1024) > 0) {
            sizename++;
            size /= 1024;
        }

        return (sizename <= 1 ? Math.round(size) + "" : round(size, 1) + "") + (sizenames[sizename]);
    }

    private float round(float number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++) {
            pow *= 10;
        }
        float tmp = number * pow;
        return (float) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
    }

    /**
     * Проверка домтупности логина.
     *
     * @param login
     * @return
     */
    public boolean reachLogin(String login) {
        boolean result = false;

        if (login == null) {
            return result;
        }

        if (!notLogin.contains(login)) {
            result = true;
        }

        return result;
    }

    /**
     * Проверка коректности логина.
     *
     * @param password
     * @return
     */
    public boolean checkLogin(String login) {
        boolean result = false;

        if (login == null) {
            return result;
        }

        if (login.matches("(?iu)[a-z_0-9]{5,36}")) {
            result = true;
        }

        return result;
    }

    /**
     * Replace IFRAME, TEXT to URL
     *
     * @param string
     * @return
     */
    public String replaceURLconnect(String string) {
        Pattern p = Pattern.compile("src=[\"]*[^\\s\"]*[\\s\"]");
        Matcher m = p.matcher(string);
        if (m.find()) {
            String src = m.group();
            string = src.replaceAll("src=|[\"]", "");
        }
        return string.replaceAll("^(http://|https://|//)", "").trim();
    }

    public boolean checkURLconnect(String string) {
        string = replaceURLconnect(string);
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL("http://" + string);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != 200) {
                url = new URL("https://" + string);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != 200) {
                    return false;
                }
            }
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    /**
     * Проверка коректности email адреса.
     *
     * @param email
     * @return
     */
    public boolean checkEmail(String email) {
        boolean result = false;

        if (email == null) {
            return result;
        }

        if (email.trim().matches("(?iu)(.+)@(.+)")) {
            result = true;
        }

        return result;
    }

    /**
     * Проверка коректности email адреса.
     *
     * @param password
     * @return
     */
    public boolean checkPassword(String password) {
        boolean result = false;

        if (password == null) {
            return result;
        }

        if (password.matches("(?iu)[a-z0-9!@#$%^&*()_\\-+:;,.]{5,20}")) {
            result = true;
        }

        return result;
    }

    /**
     * Замена перевода строки <br>
     *
     * @param str
     * @return
     */
    public String lineFeed(String str) {
        if (str != null) {
            return str.replaceAll("(\r\n|\r|\n|\n\r)", "<br>");
        } else {
            return "";
        }
    }

    /**
     * bbCode
     *
     * @param html
     * @return
     */
    public static String bbCode(String html) {
        if (html == null) {
            return "";
        }

        Map<String, String> bbMap = new HashMap();

        //bbMap.put("(\r\n|\r|\n|\n\r)", "<br>");
        bbMap.put("\\[b\\](.+?)\\[/b\\]", "<strong>$1</strong>");
        bbMap.put("\\[i\\](.+?)\\[/i\\]", "<span style=\"font-style:italic;\">$1</span>");
        bbMap.put("\\[u\\](.+?)\\[/u\\]", "<span style=\"text-decoration:underline;\">$1</span>");
        bbMap.put("\\[h3\\](.+?)\\[/h3\\]", "<h3>$1</h3>");
        bbMap.put("\\[h4\\](.+?)\\[/h4\\]", "<h4>$1</h4>");
        bbMap.put("\\[url\\](.+?)\\[/url\\]", "<a href=\"$1\">$1</a>");
        bbMap.put("\\[url=(.+?)\\](.+?)\\[/url\\]", "<a href=\"$1\">$2</a>");

        for (Map.Entry entry : bbMap.entrySet()) {
            html = html.replaceAll(entry.getKey().toString(), entry.getValue().toString());
        }

        return html;
    }

    /**
     * Удаление опаных символов и замена на более крассивые.
     *
     * @param str
     * @return
     */
    public String specialCharacters(String str) {
        if (str == null) {
            return "";
        }

        return str.replace("\\", "").// [\]
                replace("'", "\"").
                replaceAll("<[^>]*>", "").
                replace("`", "\"").
                trim();
    }

    /**
     * Для текста находящегося в тегах.
     *
     * @param str
     * @return
     */
    public String specialCharactersTags(String str) {
        if (str == null) {
            return "";
        }

        return str.replace("\\", " ").// [\]
                replaceAll("<[^>]*>", " ").
                replaceAll("\\[br\\]+", " ").
                replace("`", "'").
                trim();
    }

    public String forJSON(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        int len = input.length();
        // сделаем небольшой запас, чтобы не выделять память потом
        final StringBuilder result = new StringBuilder(len + len / 4);
        final StringCharacterIterator iterator = new StringCharacterIterator(input);
        char ch = iterator.current();
        while (ch != CharacterIterator.DONE) {
            if (ch == '\'') {
                result.append("\\\'");
            } else if (ch == '"') {
                result.append("\\\"");
            } else {
                result.append(ch);
            }
            ch = iterator.next();
        }
        return result.toString();
    }

    /**
     * Delete files
     *
     * @param files
     */
    public void deleteFile(HashSet files) {
        Iterator i = files.iterator();

        while (i.hasNext()) {
            String item = (String) i.next();
            File file = new File(item);
            if (file.exists()) {
                System.out.println(file);
                file.delete();
            }
        }
    }
    
    /**
     * Method used to get uploaded file name.
     * This will parse following string and get filename
     * Content-Disposition: form-data; name="content"; filename="a.txt"
     * @param p
     * @return 
     */
    public String getUploadedFileName(Part p) {
        String file = "", header = "content-disposition";
        String[] strArray = p.getHeader(header).split(";");
        for(String split : strArray) {
            if(split.trim().startsWith("filename")) {
                file = split.substring(split.indexOf('=') + 1);
                file = file.trim().replace("\"", "");
            }
        }
        return file;
    }
}
