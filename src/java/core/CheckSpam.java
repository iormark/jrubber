/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CheckSpam {
    //private static String q = "пасобие для начинаюих java. спавочник. пасобие";

    private String ip = "";
    private String name = "";
    private String subject = "";
    private String body = "";
    private String quest_copy = "";
    private final String SPACE = "  ";

    public CheckSpam(String ip, String name, String subject, String body) {

        this.ip = ip;
        this.name = name;
        this.subject = subject;
        this.body = body;



        createGUI();


    }

    private void createGUI() {
        try {
            Document doc = getDocument();
            showDocument(doc);
        } catch (Exception ex) {
        }
    }

    /**
     * Возвращает объект Document, который является объектным представлением XML
     * документа.
     */
    private Document getDocument() throws Exception {

        InputStream stream = null;
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setValidating(false);
            DocumentBuilder builder = f.newDocumentBuilder();
        try {
            /*
            URL dataURL = new URL(
                    "http://cleanweb-api.yandex.ru/1.0/check-spam?key=cw.1.1.20131004T114524Z.0ee7f13f806ff779.4dbbfaf94fe17276133d8e469b04c8137ff72c9d"
                    + "&ip=" + ip
                    + "&name=" + URLEncoder.encode(name, "UTF-8")
                    + "&subject-plain=" + URLEncoder.encode(subject, "UTF-8")
                    + "&body-plain=" + URLEncoder.encode(body, "UTF-8"));
            URLConnection connection = dataURL.openConnection();
            connection.setConnectTimeout(1500);
             stream = connection.getInputStream();*/

 
            

            

            
            //return builder.parse(stream);
            return builder.parse(new File("file.xml"));
        } catch (Exception ex) {
            throw new Exception(ex);
        } finally {
            if(stream!=null) {
                stream.close();
            }
        }
    }

    private void showDocument(Document doc) {

        StringBuilder content = new StringBuilder();
        System.out.print(doc);
        Node node = doc.getChildNodes().item(0);
        ApplicationNode appNode = new ApplicationNode(node);

        List<ClassNode> classes = appNode.getClasses();

        for (int i = 0; i < classes.size(); i++) {
            ClassNode classNode = classes.get(i);
            System.out.print(classNode.getName());
            if (!classNode.getName().equals("1")) {
                continue;
            }
            //content.append(SPACE + "error: " + classNode.getName() + " \n");

            List<MethodNode> methods = classNode.getMethods();

            String str = "";
            for (int j = 0; j < methods.size(); j++) {
                MethodNode methodNode = methods.get(j);
                //content.append(SPACE + SPACE + methodNode.getName() + ": "
                //+ methodNode.getText() + " \n");

                str += methodNode.getText().trim() + ":";
            }

            String[] p = str.replaceAll(":$", "").split(":");
            //quest_copy = quest_copy.replaceAll("\\b" + p[0] + "\\b", "<b>" + p[1] + "</b>");

            quest_copy += p[1] + ", ";
        }

        quest_copy = quest_copy.replaceAll(",\\s*$", "");

        //System.out.println(quest);
        //System.out.println(content.toString());
    }

    public String getSpeller() {
        if (quest_copy.equals("")) {
            quest_copy = "";
        }
        return quest_copy;
    }

    public class ApplicationNode {

        Node node;

        public ApplicationNode(Node node) {
            this.node = node;
        }

        public List<ClassNode> getClasses() {
            ArrayList<ClassNode> classes = new ArrayList<ClassNode>();

            NodeList classNodes = node.getChildNodes();

            for (int i = 0; i < classNodes.getLength(); i++) {
                Node node = classNodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    ClassNode classNode = new ClassNode(node);
                    classes.add(classNode);
                }
            }

            return classes;
        }
    }

    public class ClassNode {

        Node node;

        public ClassNode(Node node) {
            this.node = node;
        }

        public List<MethodNode> getMethods() {
            ArrayList<MethodNode> methods = new ArrayList<>();

            NodeList methodNodes = node.getChildNodes();

            for (int i = 0; i < methodNodes.getLength(); i++) {
                node = methodNodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    MethodNode methodNode = new MethodNode(node);
                    methods.add(methodNode);
                }
            }
            return methods;
        }

        public String getName() {
            NamedNodeMap attributes = node.getAttributes();
            Node nameAttrib = attributes.getNamedItem("code");
            return nameAttrib.getNodeValue();
        }
    }

    public class MethodNode {

        Node node;

        public MethodNode(Node node) {
            this.node = node;
        }

        public String getText() {
            return node.getTextContent();
        }

        public String getName() {
            return node.getNodeName();
        }
    }
}
