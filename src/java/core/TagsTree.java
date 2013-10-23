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

        //if (isCount) {
        rs = stmt.executeQuery("SELECT t.id, t.tags, p.date, COUNT(*) as count FROM tags_link tl, tags t, post p WHERE tl.post=p.id AND t.id=tl.tags AND p.date > DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) GROUP BY tl.tags");
        //} else {
        // rs = stmt.executeQuery("SELECT `id`, `parent`, `name`, `hurl`, 0 AS count FROM `type` WHERE " + (id > 0 ? "parent=" + id + " AND " : "") + "`edit`='on';");
        //}

        while (rs.next()) {
            if (rs.getInt("count") > 0) {
                HashMap node = new HashMap();
                node.put("id", rs.getString("id"));
                node.put("count", rs.getString("count"));
                node.put("tags", rs.getString("tags"));
                Tree.put(rs.getString("id"), node);
            }
        }

        //System.out.println(Tree.get(1));
    }

    public Map<String, HashMap> getTags() {
        return Tree;
    }
}
