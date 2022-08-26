/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omersavas.angaryos.eimza.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import static com.objsys.asn1j.runtime.i.o;
import com.omersavas.angaryos.eimza.models.Session;
import com.omersavas.angaryos.eimza.views.MainWindow;
import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Void;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JOptionPane;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.SmartCardException;
import java.lang.Object;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;

/**
 *
 * @author omers
 */
public class GeneralHelper {
    private static MainWindow mainWindow;
    private static Encryption encryption;
    private static Session session;
    private static Component currentWindow;
    private static Gson gson;
    private static Signing signing;
    public static LinkedTreeMap<String, Object> pipe = new LinkedTreeMap<>();
    
    public static String rememberedESignPassword = "";
    
    public static String mainInfoPath = "./files/control1.ang"; 
    public static String debugPath = "files/debug.ang";
    public static String textPath = "files/text.ang";
    public static String portPath = "files/port.ang";
    public static String sslDisablePath = "files/sslDisable.ang";
    
    public static boolean showMainInfoWindowOnLoad = false;
    
    public static boolean debug = false;
    public static boolean sslDisable = false;    
    public static String homePath = System.getProperty("user.home");
    public static String runningPath = (new File(".")).getAbsolutePath().replace("/.", "/").replace("\\.", "\\");
    public static String osName = System.getProperty("os.name");
    private static String appPath = "";    
    
    
    public static void buzzer() {
        try {
            String bip = "./files/message.wav";
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(bip).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch(Exception ex) {
            //Log.send(e);
        }
    }
    
    public static boolean isWindows()
    {
        return GeneralHelper.osName.toLowerCase().indexOf("windows") > -1;
    }
    
    public static String appPath()
    {
        if(appPath.length() == 0)
        {
            /*if(runningPath.indexOf("C:\\Data\\angaryosESign") > -1)
                appPath = "C:\\angaryosESign\\";
            else*/
                appPath = runningPath;
        }
        
        return appPath;
    }
    
    public static String userMainPath()
    {
        return System.getProperty("user.home");
    }
    
    public static void runAsync(Callable<Void> f) {
        Thread asyn = new Thread(() ->{
            try {
                f.call();
            } catch (Exception ex) {
                Log.send(ex);
            }
        });
        asyn.start();
    }
    
    private static Map<String, Object> model = new HashMap<String, Object>();
    
    public static Object factory(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        if(model.get(name) == null)
        {
            String c = "com.omersavas.angaryos.eimza.models." + name;
            Class<?> cls = Class.forName(c);
            Object o = cls.newInstance();
            model.put(name, o);
        }
        
        return model.get(name);
    }
    
    public static String jsonEncode(Object o)
    {
        if(gson == null)
            gson = new Gson();
        
        return gson.toJson(o);
    }
    
    public static LinkedTreeMap jsonDecode(String j)
    { 
        if(gson == null)
            gson = new Gson();
        
        return (LinkedTreeMap)gson.fromJson(j, Object.class);
    }
    
    public static ArrayList<LinkedTreeMap> jsonDecodeAsArray(String j)
    { 
        if(gson == null)
            gson = new Gson();
        
        return (ArrayList<LinkedTreeMap>)gson.fromJson(j, Object.class);
    }
    
    public static Session getSession() throws IOException, ESYAException
    {
        if(session == null) session = new Session();        
        return session;
    }
    
    public static Signing getSigning() throws SmartCardException, ESYAException
    {
        if(signing == null)
            signing = new Signing();
        
        return signing;
    }
    
    public static MainWindow getMainWindow() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        if(mainWindow == null)
            mainWindow = new MainWindow();
        
        return mainWindow;
    }
    
    public static void createEncryptionObject(String pin) throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        encryption = new Encryption(pin);
    }
    
    public static Encryption getEncryption()
    {
        return encryption;
    }
    
    public static void setCurrentWindow(Component ap)
    {
        currentWindow = ap;
    }
    
    public static void showMessageBox(String m)
    {
        JOptionPane.showMessageDialog(currentWindow, m);
    }

    public static void loading(boolean b) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        getMainWindow().loading(b);
    }

    public static Image Logo() {
        return Toolkit.getDefaultToolkit().getImage("img/icon.png");
    }

    public static String getTimeStamp(String s) {
        return new SimpleDateFormat(s).format(new Date());
    }
    
    public static String readFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    /*public static void BildirimYap(String title, String desc) {
        BildirimPencere bp = new BildirimPencere(title, desc);
        BasicInternalFrameUI bi = (BasicInternalFrameUI)bp.getUI();
        bi.setNorthPane(null);
        mainWindow.pencereAc(bp);
        bp.show();
    }*/

    /*public static int DizidenIdGetir(String[] Dizi, String eleman) {
        int r = 0;
        for(String d: Dizi){
            if(d.equals(eleman)) return r;
            else r++;
        }
        
        return -1;
    }*/
}
