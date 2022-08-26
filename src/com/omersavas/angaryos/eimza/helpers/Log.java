/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omersavas.angaryos.eimza.helpers;

import com.omersavas.angaryos.eimza.models.Session;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 *
 * @author omers
 */
public class Log {
    public static boolean showMessage = true;
    public static Exception lastLog;
    
    
    private static void fail(String level, String m)
    {
        if(!showMessage) return;

        GeneralHelper.showMessageBox("Log gönderilemedi ve kaydedilemedi! ("+level+": " + m+")");        
    }
    
    public static boolean send(Exception e)
    {
        lastLog = e;
        
        try {
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            
            String m = e.getLocalizedMessage();
            if(m.indexOf("GUVENLIK_KOSULU_SAGLANAMADI") > 0)
            {
                GeneralHelper.showMessageBox("Şuan e-imza kullanılamıyor. Kullanabilecek uygulamaları kapatıp yeniden açmayı deneyin.");
                System.exit(0);
            }
            
            if(GeneralHelper.debug) GeneralHelper.showMessageBox("getLocalizedMessage: " + e.getLocalizedMessage());
            if(send("Error", "{\"Hata\":"+GeneralHelper.jsonEncode(e)+"},\"Stack\":"+GeneralHelper.jsonEncode(st))) 
            {
                return true;
            }
        }
        catch (Exception ee) { }
        
        fail("Error", e.getMessage());
        return false;
    }
    
    public static String getLastLogMessage()
    {
        try {
            return lastLog.getMessage();
        } catch (Exception e) {
            return "";
        }
    }
    
    public static boolean send(String level, String j)
    {
        try {
            if(GeneralHelper.debug) GeneralHelper.showMessageBox(j);
            
            if(sendToServer(level, j))
                return true;
            else{                
                if(saveToLocal(level, j)) 
                   return true;
                else
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean sendToServer(String level, String j)
    {
        return false;
        /*try {
            Session session = GeneralHelper.getSession();
            String uri = "https://cbs.kutahyaozid.gov.tr/tr/api/hata?n=JavaMasaustu&t="+session.token+"&j="+session.parametreTemizle(j);
            String r = session.httpGet(uri);
            
            if(r.equals("OK")){
                if(showMessage) GeneralHelper.showMessageBox("Bir hata oluştu ve sunucuya gönderildi!");
                return true;
            }
            else return false;
                
        }
        catch (Exception e) {
            return false;
        }*/
    }
    
    private static void fillXml(Document document,  Element rootElement, String level, String j)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
            
        Element element = document.createElement("Hata");

        Element levelCol = document.createElement("Level");
        Text text = document.createTextNode(level);
        levelCol.appendChild(text);
        element.appendChild(levelCol);
        
        Element jsonCol = document.createElement("Json");
        Text text1 = document.createTextNode(j);
        jsonCol.appendChild(text1);
        element.appendChild(jsonCol);

        Element tarihCol = document.createElement("Tarih");
        Text text2 = document.createTextNode(dateFormat.format(date));
        tarihCol.appendChild(text2);
        element.appendChild(tarihCol);
        
        rootElement.appendChild(element);
    }
    
    private static boolean saveToLocal(String level, String j)
    {
        try { 
            String fn = "files/Log.xml";
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.newDocument();
 
            Element rootElement = document.createElement("Logs");
            document.appendChild(rootElement);
 
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            
            File f = new File(fn);
            
            StreamResult sr;
            
            if(!f.exists()){            
                fillXml(document, rootElement, level, j);
                sr = new StreamResult(f);
            }
            else{
                document = db.parse(fn);
                rootElement = document.getDocumentElement();
                fillXml(document, rootElement, level, j);

                File ff = new File(fn);
                sr = new StreamResult(ff);
            }
            
            Node node = document.getDocumentElement();
            DOMSource src = new DOMSource(node);
            t.transform(src, sr);
            
            if(showMessage) GeneralHelper.showMessageBox("Bir hata oluştu ve dosyaya yazıldı!");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static void info(String msg)
    {
        if(!GeneralHelper.debug) return;
        System.out.println(msg);
    }
}
