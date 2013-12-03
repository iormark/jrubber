package core;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author mark
 */
public class Templating {
    
    public Template getTemplating(String RealPath) throws Exception {
        

        Configuration tcfg = new Configuration();
        tcfg.setNumberFormat("0.#");
        tcfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);

        tcfg.setDirectoryForTemplateLoading(
                new File(RealPath + "/templates/dmoz/html"));

        tcfg.setObjectWrapper(new DefaultObjectWrapper());


        Template temp = tcfg.getTemplate("index.html");

        return temp;
    }
    
    public Template getTemplating(String RealPath, String layout) throws Exception {
        

        Configuration tcfg = new Configuration();
        tcfg.setNumberFormat("0.#");
        tcfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);

        tcfg.setDirectoryForTemplateLoading(
                new File(RealPath + "/templates/dmoz/html"));

        tcfg.setObjectWrapper(new DefaultObjectWrapper());


        Template temp = tcfg.getTemplate(layout!=null ? layout : "index.html");

        return temp;
    }
    
    
    public String getTemplating(String template, HashMap root, String RealPath) throws Exception {

        Configuration tcfg = new Configuration();
        tcfg.setNumberFormat("0.#");
        tcfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);

        tcfg.setDirectoryForTemplateLoading(
                new File(RealPath + "/templates/dmoz/html"));

        tcfg.setObjectWrapper(new DefaultObjectWrapper());


        Template temp = tcfg.getTemplate(template!=null ? template : "index.html");

        Writer out = new StringWriter();
        temp.process(root, out);
        
        return out.toString();
    }
}
