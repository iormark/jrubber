/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author mark
 */
public class Util {

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

        return (sizename <= 1 ? Math.round(size)+"" : round(size, 1)+"") + (sizenames[sizename]);
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
        return str.replaceAll("[\n]", "<br>");
    }

    /**
     * bbCode
     *
     * @param str
     * @return
     */
    public String bbCode(String str) {
        return str.replaceAll("\\[br\\]+", "<br>");
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
}
