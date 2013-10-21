/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mark
 */
public class TagsTree {

    private Map<String, HashMap> Tree = new LinkedHashMap();

    public TagsTree(Statement stmt, int id, boolean isCount) throws SQLException {
        getTagsTree(stmt, id, isCount);
    }

    private class Node {

        int id;
        int count;
        String tags;
        List<TagsTree.Node> children = new LinkedList();
    }

    private void getTagsTree(Statement stmt, int id, boolean isCount) throws SQLException {
        ResultSet rs;

        if (isCount) {
            rs = stmt.executeQuery("SELECT `id`, `tags`, (SELECT count(*) FROM `tags_link` WHERE tags = t.id) AS count FROM `tags` t WHERE 1 ORDER BY id;");
        } else {
            rs = stmt.executeQuery("SELECT `id`, `parent`, `name`, `hurl`, 0 AS count FROM `type` WHERE " + (id > 0 ? "parent=" + id + " AND " : "") + "`edit`='on';");
        }

        while (rs.next()) {

            HashMap node = new HashMap();
            node.put("id", rs.getString("id"));
            node.put("count", rs.getString("count"));
            node.put("tags", rs.getString("tags"));
            Tree.put(rs.getString("id"), node);
        }

        System.out.println(Tree.get(1));
    }

    public Map<String, HashMap> getTags() {
        return Tree;
    }
}
