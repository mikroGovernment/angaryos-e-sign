/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omersavas.angaryos.eimza.views;

import com.google.gson.internal.LinkedTreeMap;
import com.omersavas.angaryos.eimza.helpers.GeneralHelper;
import com.omersavas.angaryos.eimza.helpers.Log;
import com.omersavas.angaryos.eimza.helpers.Signing;
import com.omersavas.angaryos.eimza.helpers.SigningTestConstants;
import com.omersavas.angaryos.eimza.models.Session;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import tr.gov.tubitak.uekae.esya.api.cmssignature.CMSSignatureException;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.SmartCardException;

/**
 *
 * @author omers
 */
public class MainWindow extends javax.swing.JFrame {

    private static boolean loadingState = false;
    
    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        try 
        {
            GeneralHelper.createEncryptionObject("angaryos");
            
            this.selfUpdateWindow();
            this.infoWindow();
            
            this.addInSystemTry();
            this.frameOperations();
            this.startSockerServer();
            
        }
        catch (Exception e) 
        {
            Log.send(e);
        }        
    }
    
    private void frameOperations() throws MalformedURLException, IOException
    {
        setSize(new Dimension(640, 480));
        setResizable(false);
        
        setIconImage(GeneralHelper.Logo());
        
        JLabel label = new JLabel(new ImageIcon("img/bg.jpg"));
        add(label);
               
        setTitle("E-imza Uygulaması");
    }
    
    private void addInSystemTry()
    {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        
        Image icon = Toolkit.getDefaultToolkit().getImage("img/icon16x16.png");
        final TrayIcon trayIcon = new TrayIcon(icon);
        
        final SystemTray tray = SystemTray.getSystemTray();
       
       
        final PopupMenu popup = new PopupMenu();
                
        MenuItem showItem = new MenuItem("Göster");
        showItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    GeneralHelper.getMainWindow().setVisible(true);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchPaddingException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidKeyException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalBlockSizeException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BadPaddingException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        popup.add(showItem);
        
        popup.addSeparator();
        
        MenuItem exitItem = new MenuItem("Çıkış");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        popup.add(exitItem);
       
        trayIcon.setPopupMenu(popup);
       
        try {
            tray.add(trayIcon);
        } catch (AWTException e) { }
    }
    
    private void startSockerServer() throws IOException, ESYAException
    {
        int port = 4326;
        
        try 
        {
            File f = new File(GeneralHelper.portPath);
            if(f.exists())
            {
                String temp = GeneralHelper.readFromFile(GeneralHelper.portPath);
                port = Integer.parseInt(temp);
            }
            
        } catch (Exception e) { }
        
        GeneralHelper.getSession().startSockerServer(port);
    }
    
    private void selfUpdateWindow() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException
    {
        SelfUpdateWindows suw = new SelfUpdateWindows(this, true);            
        suw.setLocationRelativeTo(null);
        GeneralHelper.setCurrentWindow(suw);
        suw.show();

        if(suw.updated){
            GeneralHelper.showMessageBox("Uygulama güncellendi! Lütfen yeniden açınız.");
            System.exit(0);
        }
    }
    
    private void infoWindow() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        if(!GeneralHelper.showMainInfoWindowOnLoad) return;
        
        InfoWindow iw = new InfoWindow(this, true);
        iw.setLocationRelativeTo(null);
        GeneralHelper.setCurrentWindow(iw);
        iw.show();        
    }
      
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setFont(new java.awt.Font("Trebuchet MS", 0, 10)); // NOI18N
        setPreferredSize(new java.awt.Dimension(640, 480));
        setSize(new java.awt.Dimension(640, 480));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 480, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    
    /*public String createESignFile(LinkedTreeMap eSign) throws ESYAException, CMSSignatureException, SmartCardException, IOException
    {
        Signing signing = GeneralHelper.getSigning();
        
        String name = signing.getNewFileName();
        String pass = signing.getPasswordFromUser();

        String signedText = eSign.get("signed_text").toString();
        if(!signing.sing(signedText, pass, name)) return "";
        
        return name;
    }
    
    public boolean sign(LinkedTreeMap eSign) throws ESYAException, CMSSignatureException, SmartCardException, IOException
    {
        String path = this.createESignFile(eSign);
        if(path == "") return false;
        
        path = SigningTestConstants.getDirectory() + "/" + path + ".p7s";
        
        Session session = GeneralHelper.getSession();
        return session.uploadSign(eSign, path);
    }*/
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    public void loading(boolean b) {
        this.loadingState = b;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
