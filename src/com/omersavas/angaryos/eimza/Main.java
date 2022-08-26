/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omersavas.angaryos.eimza;

import com.google.gson.internal.LinkedTreeMap;
import com.omersavas.angaryos.eimza.helpers.GeneralHelper;
import com.omersavas.angaryos.eimza.helpers.Log;
import com.omersavas.angaryos.eimza.helpers.Encryption;
import com.omersavas.angaryos.eimza.models.Session;
import com.omersavas.angaryos.eimza.views.InfoWindow;
import com.omersavas.angaryos.eimza.views.MainWindow;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.plaf.basic.BasicInternalFrameUI;
/**
 *
 * @author omers
 */
public class Main {
    
    private static void debugStateUpdate()
    {
        File f = new File(GeneralHelper.debugPath);
        if(f.exists()) GeneralHelper.debug = true;
        else  GeneralHelper.debug = false;
        
        f = new File(GeneralHelper.sslDisablePath);
        if(f.exists()) GeneralHelper.sslDisable = true;
        else  GeneralHelper.sslDisable = false;
    }
    
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException 
    {
        try {
            
            Main.ESignControl();
            Main.GeneralOperations();           
            
            MainWindow mainWindow = GeneralHelper.getMainWindow();
            mainWindow.show();
            
        } catch (Exception e) {
            Log.send(e);
        }        
    }    
    
    public static void GeneralOperations()
    {
        try{
            debugStateUpdate();
        }
        catch(Exception eee){ }     

        try{
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch(Exception eee){ }
    }
    
    public static void ESignControl()
    {
        try  {
            GeneralHelper.getSigning().getTCNumber();
        } 
        catch (Exception e)  {
            GeneralHelper.showMessageBox("Şuan e-imza kullanılamıyor. Cihazın takılı olduğundan emin olup kullanabilecek uygulamaları kapatıp yeniden açmayı deneyin.");
            System.exit(0);
        }
    }
}
