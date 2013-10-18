/*
 * Разбор XML из потока.
 */
package core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author mark
 */
public class XmlOptionReader {

    private DocumentBuilderFactory f;
    private DocumentBuilder builder;
    private StringEscapeUtils seu = new StringEscapeUtils();
    private ArrayList fieldlist = new ArrayList();

    public XmlOptionReader() {

        f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);

        try {
            builder = f.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.err.println(this.getClass() + ": " + e);
        }

    }

    public void setField(String[] field) {
        if (field != null) {
            fieldlist.addAll(Arrays.asList(field));
        } else {
            fieldlist.clear();
        }
    }

    /**
     * Чтение настроек из файла.
     *
     * @param file
     * @return
     * @throws Exception
     */
    public HashMap setFileDocument(String file) throws Exception {
        try {
            return showDocument(builder.parse(new InputSource(file)));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * Чтение настроек в переданной строке.
     *
     * @param xml
     * @return
     * @throws Exception
     */
    public HashMap setDocument(String xml) throws Exception {
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>" + xml + "</root>";
        try {
            InputStream io = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            return showDocument(builder.parse(io));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private HashMap showDocument(Document doc) {
        //Element root = doc.getDocumentElement();
        Node node = doc.getDocumentElement();
        NodeList methodNodes = node.getChildNodes();
        HashMap<String, HashMap> map = new HashMap();

        for (int i = 0; i < methodNodes.getLength(); i++) {
            node = methodNodes.item(i);
            HashMap attr = new HashMap();

            for (int j = 0; j < node.getAttributes().getLength(); j++) {
                attr.put(node.getAttributes().item(j).getNodeName(),
                        node.getAttributes().item(j).getNodeValue());
            }


            if ((node.getNodeType() == Node.ELEMENT_NODE) && (fieldlist.contains(node.getNodeName()) || fieldlist.isEmpty())) {

                Node nodeChild;
                NodeList methodNodesChild = node.getChildNodes();
                if (methodNodesChild.getLength() == 0) {
                    map.put(node.getNodeName(), attr);
                } else {
                    for (int j = 0; j < methodNodesChild.getLength(); j++) {
                        nodeChild = methodNodesChild.item(j);

                        if (nodeChild.getNodeName().equals("#comment")
                                || nodeChild.getTextContent().replaceAll("[\n\t\\s]+", "").equals("")) {
                            continue;
                        }

                        if (!nodeChild.getTextContent().equals("\n")) {
                            attr.put("name", nodeChild.getTextContent());
                            map.put(node.getNodeName() + (nodeChild.getNodeName().equals("#text") ? "" : " " + nodeChild.getNodeName()), attr);
                        }
                    }
                }
            }
        }
        return map;
    }

    public String getDocumentString(Properties cfg) throws Exception {

        StringBuilder message = new StringBuilder();
        HashMap xml = new HashMap();
        Enumeration en = cfg.propertyNames();


        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();

            String[] tag = key.split("\\s");
            //System.out.println(tag[0]);

            try {

                xml.put(tag[0],
                        (xml.get(tag[0]) != null ? xml.get(tag[0]) : "")
                        + "<" + tag[1] + ">" + StringEscapeUtils.escapeXml(cfg.getProperty(key)) + "</" + tag[1] + ">");

            } catch (ArrayIndexOutOfBoundsException e) {
            }

        }

        Set set = xml.entrySet();
        Iterator i = set.iterator();

        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            message.append("<" + me.getKey() + ">" + me.getValue() + "</" + me.getKey() + ">");
        }

        //System.out.println(message);
        return message.toString();
    }
}
