/*
 * Работа с категориями.
 */
package core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author mark
 */
public final class CategoriesTree {

    private Map<Integer, Node> Tree = new LinkedHashMap();
    private HashSet<Integer> TreeId = new HashSet();

    public CategoriesTree(Statement stmt, int id) throws SQLException {
        getCategoriesTree(stmt, id, false);
    }

    public CategoriesTree(Statement stmt, int id, boolean isCount) throws SQLException {
        getCategoriesTree(stmt, id, isCount);
    }

    private class Node {

        int id;
        int parentId;
        int count;
        String name;
        String hurl;
        List<Node> children = new LinkedList();
    }

    private void getCategoriesTree(Statement stmt, int id, boolean isCount) throws SQLException {
        ResultSet rs;

        if (isCount) {
            rs = stmt.executeQuery("SELECT `id`, `parent`, `name`, `hurl`, (SELECT count(*) FROM `post` WHERE type = t.id) AS count FROM `type` t WHERE " + (id > 0 ? "t.parent=" + id + " AND " : "") + "t.edit='on' ORDER BY t.weight;");
        } else {
            rs = stmt.executeQuery("SELECT `id`, `parent`, `name`, `hurl`, 0 AS count FROM `type` WHERE " + (id > 0 ? "parent=" + id + " AND " : "") + "`edit`='on';");
        }


        while (rs.next()) {

            Node node = new Node();
            node.id = rs.getInt("id");
            node.parentId = rs.getInt("parent");
            node.count = rs.getInt("count");
            node.name = rs.getString("name");
            node.hurl = rs.getString("hurl");
            Tree.put(rs.getInt("id"), node);
            TreeId.add(rs.getInt("id"));

        }

        if (id > 0) {
            TreeId.add(id);
        }

        HashSet<Integer> delete = new HashSet();
        for (Map.Entry<Integer, Node> entry : Tree.entrySet()) {
            if (Tree.containsKey(entry.getValue().parentId)) {
                Tree.get(entry.getValue().parentId).children.add(entry.getValue());
                delete.add(entry.getKey());
            }
        }

        for (Integer element : delete) {
            Tree.remove(element);
        }

    }

    public String getCategoriesAllId() {
        //return TreeId;
        return TreeId.toString().replaceAll("[\\[\\]]", "");
    }

    /**
     * Вывод в виде select
     *
     * @return
     */
    public StringBuilder getCategoriesSelect() {

        StringBuilder str = new StringBuilder();
        str.append("<option value=\"\">").append(" - Категория - ").append("</option>\n");
        for (Map.Entry<Integer, Node> entry : Tree.entrySet()) {
            //System.out.println(tree.get(entry.getKey()).name);
            str.append("<option value=\"" + Tree.get(entry.getKey()).hurl + "\">").append(Tree.get(entry.getKey()).name).append("</option>\n");
            ListIterator<Node> itr = Tree.get(entry.getKey()).children.listIterator();

            while (itr.hasNext()) {
                Node n = itr.next();
                //System.out.println("-" + n.name);
                str.append("<option value=\"" + n.hurl + "\">").append("-").append(n.name).append("</option>\n");

                ListIterator<Node> itr2 = n.children.listIterator();
                while (itr2.hasNext()) {
                    Node n2 = itr2.next();
                    //System.out.println("--" + itr2.next().name);
                    str.append("<option value=\"" + n2.hurl + "\">").append("--").append(n2.name).append("</option>\n");
                }

            }
        }

        return str;
    }

    /**
     * Вывод в виде select
     *
     * @return
     */
    public StringBuilder getCategoriesLi() {

        StringBuilder str = new StringBuilder();
        //str.append("<ul>\n");
        for (Map.Entry<Integer, Node> entry : Tree.entrySet()) {

            str.append("<li><a href=\"/" + Tree.get(entry.getKey()).hurl + "\">").
                    append(Tree.get(entry.getKey()).name).
                    append(Tree.get(entry.getKey()).count > 0 ? " (" + Tree.get(entry.getKey()).count + ")" : "").append("</a>");
            ListIterator<Node> itr = Tree.get(entry.getKey()).children.listIterator();

            str.append("<ul>\n");

            while (itr.hasNext()) {
                Node n = itr.next();
                //System.out.println("-" + n.name);
                str.append("<li><a href=\"/" + Tree.get(entry.getKey()).hurl + "/" + n.id + "\">").append(n.name).append(" (" + n.count + ")").append("</a>\n");

                ListIterator<Node> itr2 = n.children.listIterator();

                str.append("<ul>\n");
                while (itr2.hasNext()) {
                    Node n2 = itr2.next();
                    //System.out.println("--" + itr2.next().name);
                    str.append("<li><a href=\"/" + Tree.get(entry.getKey()).hurl + "/" + n2.id + "\">").append(n2.name).append(" (" + n2.count + ")").append("</a></li>\n");
                }
                str.append("</ul>");
                str.append("</li>\n");
            }

            str.append("</ul>");
            str.append("</li>\n");
        }
        //str.append("</ul>\n");

        return str;
    }
}
