/*
 * Первоначальная обработка поискового запроса.
 * Удаление опасных для поиска символов.
 */
package core;

import java.util.*;

/**
 *
 * @author mark
 */
public class AdvancedAnalyzerQuest {

    private LinkedHashMap<String, String> field = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> part = new LinkedHashMap<String, String>();
    private LinkedHashSet<String> FieldName = new LinkedHashSet<String>();
    Set<Map.Entry<String, String>> set;

    public AdvancedAnalyzerQuest(HashMap<String, String> field) {

        set = field.entrySet();
        

        for (Map.Entry<String, String> list : set) {
            if (list.getValue() == null) {
                continue;
            } else if (list.getValue().equals("")) {
                continue;
            }

            this.field.put(list.getKey(), list.getValue());
        }

        FieldName.addAll(this.field.keySet());


        getSpel();
        getNet();
        getLucene();

        part.putAll(field);
        //part.put("keywords", part.get("net"));


    }

    private void getSpel() {

        for (Map.Entry<String, String> list : set) {
            String w = "";
            String[] word = list.getValue().split("\\s");
            for (String val : word) {
                if (!val.endsWith("*")) {
                    w += val+" ";
                }
            }
            part.put("spel", (part.get("spel") != null ? part.get("spel") : "") + " " + w);
        }
        
        part.put("spel", part.get("spel").replaceAll("\\b(.*)[*]\\b", "").replaceAll("[{},]", "").
                    replaceAll("[\":+!\\-@#/$%^&(=)|\\.,;'`~{?}\\[\\]]+", " ").trim().
                    replaceAll("[\\s]{2,}", " "));
    }

    /**
     * Отчищаем запрос от ненужных символов.
     *
     * @return
     */
    private void getNet() {

        for (Map.Entry<String, String> list : set) {
            part.put("net", (part.get("net") != null ? part.get("net") : "") + " " + list.getValue());
        }
        part.put("net", part.get("net").replaceAll("[{},]", "").
                replaceAll("[\":+!\\-@#/$%^&(=)|\\.,;'`~{?}\\[\\]]+", " ").trim().
                replaceAll("[\\s]{2,}", " "));

    }

    /**
     * Формируем запрос, для Lucene
     *
     * @return
     */
    private void getLucene() {
        set = field.entrySet();
        for (Map.Entry<String, String> list : set) {
            field.put(list.getKey(), "(" + list.getValue().replaceAll("[:!/]+", "") + ") AND ");
        }

        part.put("lucene", field.toString().replaceAll("[{},]", " ").
                replaceAll("=", ":").trim().
                replaceAll("[\\s]{2,}", " ").
                replaceAll("keywords:", "").
                replaceAll("(AND)\\s*$", "").
                trim());
    }

    public HashMap get() {
        return part;
    }

    public LinkedHashSet getFieldName() {
        return FieldName;
    }
}
