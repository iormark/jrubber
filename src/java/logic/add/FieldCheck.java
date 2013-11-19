package logic.add;

import core.Util;
import java.util.HashMap;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author mark
 */
public class FieldCheck {

    private String text = null;
    private String video = null;
    private String message = "";
    private Util util = new Util();

    private static HashMap censure = new HashMap();

    {
        censure.put("пизда", "пи*да");
        censure.put("пизду", "пи*ду");
        censure.put("пиздой", "пи*дой");
        censure.put("пезда", "пе*да");
        censure.put("пездой", "пе*дой");
        censure.put("пездень", "пе*день");
        censure.put("пизданутый", "пи*данутый");
        censure.put("пизданутые", "пи*данутые");
        censure.put("пизданутая", "пи*данутая");
        censure.put("хуй", "х*й");
        censure.put("хуевый", "х*евый");
        censure.put("хуевая", "х*евая");
        censure.put("хуевое", "х*евое");
        censure.put("хуйня", "х*йня");
        censure.put("членосос", "чле*осос");
        censure.put("залупа", "за*упа");
        censure.put("залупой", "за*упой");
        censure.put("член", "ч*ен");
    }

    private static HashSet blackTag = new HashSet();

    {
        blackTag.add("all");
        blackTag.add("моё");
        blackTag.add("мое");
        blackTag.add("маё");
        blackTag.add("мае");
    }

    public FieldCheck(HttpServletRequest request) {
    }

    public FieldCheck(HttpServletRequest request, HttpServletResponse response) {
        text = request.getParameter("text") != null ? request.getParameter("text").trim() : "";
        video = request.getParameter("video") != null ? request.getParameter("video").trim() : "";

        if (!text.equals("") || !video.equals("")) {
            String text2 = checkText();
            String video2 = checkVideo();

            if (!text2.equals("") && !video2.equals("")) {
                if (!text2.equals("")) {
                    message = text2;
                }
                if (!video2.equals("")) {
                    message = video2;
                }
            }

            if (!text2.equals("") && video2.equals("")) {
                message = text2;
            }

            if (text2.equals("") && !video2.equals("")) {
                message = video2;
            }

        } else {
            message = ("Постить пустоту запрещено!");
        }
    }

    public FieldCheck(String text, String video, boolean file) {
        this.text = text != null ? text.trim() : "";
        this.video = video != null ? video.trim() : "";

        if (!file) {
            if (!this.text.equals("") || !this.video.equals("")) {
                String text2 = checkText();
                String video2 = checkVideo();

                if (!text2.equals("") || !video2.equals("")) {
                    if (!text2.equals("")) {
                        message = text2;
                    }
                    if (!video2.equals("")) {
                        message = video2;
                    }
                }

                if (!text2.equals("") && video2.equals("")) {
                    message = text2;
                }

                if (text2.equals("") && !video2.equals("")) {
                    message = video2;
                }
            } else {
                message = ("Постить пустоту запрещено!");
            }
        }
    }

    public String checkTitle(String title) {
        if (title != null) {
            title = title.trim();
            if (title.length() > 255) {
                message = ("Простите, очень длинное название");
            } else if (title.length() >= 6) {
                title = (String.valueOf(title.charAt(0)).toUpperCase()).concat(title.substring(1));
            } else if ((title.length() < 6)) {
                message = ("Простите, очень короткое название");
            }
        } else {
            message = ("Простите, очень короткое название");
        }

        return title;
    }

    private String checkText() {
        String msg = "";
        if (!text.equals("")) {
            if (text.length() < 7) {
                msg = ("Простите, очень короткое описание");
            } else if (text.length() > 10000) {
                msg = ("У нас тут, не Википедия!");
            }
        } else {
            // msg = ("Постить пустоту запрещено!");
        }

        System.out.println("checkText " + message);
        //message = ("=" + text + "");
        return msg;
    }

    private String checkVideo() {
        String msg = "";
        if (!video.equals("")) {
            if (video.length() > 255) {
                msg = ("URL должен быть не более 255 символов.");
            } else if (!util.checkURLconnect(video)) {
                msg = ("Сервер по указанному URL не отвечает!");
            }
        } else {
            //msg = ("Постить пустоту запрещено!");
        }

        System.out.println("checkVideo " + message);
        return msg;
    }

    public HashSet checkTags(String tags) {
        if (tags == null) {
            message = ("Добавьте несколько тегов");
            return null;
        }

        tags = tags.trim();
        HashSet tagsMap = new HashSet();

        if (tags.length() > 120) {
            message = ("Общая длина тегов не может превышать 240 символов");
            return null;
        }

        String[] tagArray = tags.split(",");

        if (tagArray.length < 2) {
            message = ("Добавьте несколько тегов");
            return null;
        }

        if (tagArray.length > 6) {
            message = ("Всего разрешено 6 тегов");
            return null;
        }

        for (String key : tagArray) {
            String tag = key.trim();

            if (tag.matches("((?iu)[a-zçа-яё0-9-*@(=:;'’)!\\s]+)")) {
                String[] wordArray = tag.split("[\\s]+");

                if (wordArray.length > 4) {
                    message = ("Один тег не может содержать более 4-х слов!");
                    break;
                }

                if (tag.length() > 30) {
                    message = ("Один тег не может превышать 30 символов!");
                    break;
                }

                if (tag.length() <= 1) {
                    message = ("Тег не может состоять из 1-го символа!");
                    break;
                }

                if (blackTag.contains(tag)) {
                    message = ("Тег &laquo;" + tag + "&raquo; недоступен.");
                    break;
                }

                String words = "";
                for (String word : wordArray) {
                    words += !censure.containsKey(word) ? word : (String) censure.get(word);
                    words += " ";
                }

                tagsMap.add(words.trim());
            } else {
                message = ("Недопустимые символы в тегах!");
                break;
            }

        }
        return tagsMap;
    }

    public String getMessage() {
        return message;
    }
}
