/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omersavas.angaryos.eimza.views;

import com.google.gson.internal.LinkedTreeMap;
import com.omersavas.angaryos.eimza.helpers.GeneralHelper;
import com.omersavas.angaryos.eimza.helpers.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JOptionPane;
import static com.omersavas.angaryos.eimza.helpers.GeneralHelper.loading;
import com.omersavas.angaryos.eimza.models.Session;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;

/**
 *
 * @author omers
 */
public class SelfUpdateWindows extends javax.swing.JDialog {
    
    private String updateUrl = "https://192.168.10.185/uploads/2020/01/01/eSign/";
    private String updateUrlPath = "files/updateUrl.ang";
    public boolean updated = false;
    
    private boolean versionControl()
    {
        try {
            String vf = GeneralHelper.appPath() + "version.txt";
        
            File f = new File(vf);
            if(!f.exists()) return false;

            String localVersion = new String(Files.readAllBytes(Paths.get(vf)));

            Session session = GeneralHelper.getSession();
            String onlineVersion = session.httpGetBasic(updateUrl+"version.txt");
            
            return localVersion.equals(onlineVersion);
        } catch (Exception e) {
            Log.send(e);
            return false;
        }
    }
    
    public void Update() throws IOException, ESYAException
    {
        jProgressBar1.setIndeterminate(true);

        if(this.versionControl())
        {
            dispose();
            return;
        }
        
        jProgressBar1.setIndeterminate(false);
        
        Session session = GeneralHelper.getSession();
        
        String a = GeneralHelper.appPath();
        
        File f = new File(GeneralHelper.appPath());
        if(!f.exists()) new File(GeneralHelper.appPath()).mkdirs();

        String fl = session.httpGetBasic(updateUrl+"filelist.txt");
        String[] files = fl.split(",");

        jProgressBar1.setMaximum(files.length);
        jProgressBar1.setValue(0);

        for(String file: files){
            jProgressBar1.setValue(jProgressBar1.getValue()+1);
            
            jLabel1.setText("Dosyalar indiriliyor ("+files.length+"/"+jProgressBar1.getValue()+")");
            
            if(file.length() == 0) continue;

            file = file.replaceAll("\"", "");

            String[] tempP = file.split("/");
            
            if(tempP.length > 1){
                f = new File(GeneralHelper.appPath()+tempP[0]);
                if(!f.exists()){
                    new File(GeneralHelper.appPath()+tempP[0]).mkdirs();
                }
            }

            BufferedInputStream in = new BufferedInputStream(new URL(updateUrl+file).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(GeneralHelper.appPath()+file);
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
              fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }

        this.DownloadTrustedCertificate();

        updated = true;
    }
    
    public void DownloadTrustedCertificate() throws MalformedURLException, IOException
    {
        jProgressBar1.setValue(jProgressBar1.getValue()+1);
        jLabel1.setText("Sertifikalar indiriliyor...");

        File sertifikadeposu = new File(GeneralHelper.userMainPath()+"\\.sertifikadeposu\\");
        
        if(!sertifikadeposu.exists()) { 
            sertifikadeposu.mkdir();
        }
        
        BufferedInputStream in = new BufferedInputStream(new URL(updateUrl+"SertifikaDeposu.svt").openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(GeneralHelper.userMainPath()+"\\.sertifikadeposu\\SertifikaDeposu.svt");
        byte dataBuffer[] = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
          fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
        
        BufferedInputStream in2 = new BufferedInputStream(new URL(updateUrl+"SertifikaDeposu.xml").openStream());
        FileOutputStream fileOutputStream2 = new FileOutputStream(GeneralHelper.userMainPath()+"\\.sertifikadeposu\\SertifikaDeposu.xml");
        byte dataBuffer2[] = new byte[1024];
        int bytesRead2;
        while ((bytesRead2 = in2.read(dataBuffer2, 0, 1024)) != -1) {
          fileOutputStream2.write(dataBuffer2, 0, bytesRead2);
        }
    }
    
    public void CopyFile(File in, File out) throws FileNotFoundException, IOException
    {
        try (FileInputStream fis = new FileInputStream(in);
             FileOutputStream fos = new FileOutputStream(out)) {

            byte[] buffer = new byte[1024];
            int length;

            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }
    
    public void ShortcutOperations() throws FileNotFoundException, IOException
    {
        String startupPath = System.getProperty("user.home")+"\\Start Menu\\Programs\\Startup\\e-imza.lnk"; 
        File startupFile = new File(startupPath);
        
        String eSignShortcutPath = "C:\\eSign\\files\\e-imza.lnk";
        File eSignShortcutFile = new File(eSignShortcutPath);
        
        String eSignDesktopPath = System.getProperty("user.home")+"\\Desktop\\e-imza.lnk"; 
        File eSignDesktopFile = new File(eSignDesktopPath);
        
        CopyFile(eSignShortcutFile, startupFile);
        CopyFile(eSignShortcutFile, eSignDesktopFile);
        
        
        
        File autoLoginShortcutFile = new File("C:\\eSign\\files\\KbsOtoGiris.url");
        File closeConnectionShortcutFile = new File("C:\\eSign\\files\\InternetiKapat.url");
         
        File autoLoginDesktopFile = new File(System.getProperty("user.home")+"\\Desktop\\KBS Oto Giriş.url");
        File closeConnectionDescktopFile = new File(System.getProperty("user.home")+"\\Desktop\\İnterneti Kapat.url");
        
        CopyFile(autoLoginShortcutFile, autoLoginDesktopFile);
        CopyFile(closeConnectionShortcutFile, closeConnectionDescktopFile);
    }
    
    public void UpdateAsync() throws IOException
    {
        Thread asyn = new Thread(() ->{
            try {
                Update();                
                if(updated) ShortcutOperations();                
                dispose();
            } catch (Exception ex) {
                Log.send(ex);
            }
        });
        asyn.start();       
    }
    
    /**
     * Creates new form PinPencere
     */
    public SelfUpdateWindows(java.awt.Frame parent, boolean modal) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException 
    {
        super(parent, modal);
        initComponents();
        
        File f = new File(updateUrlPath);
        if(f.exists()) this.updateUrl = (new String(Files.readAllBytes(Paths.get(this.updateUrlPath))));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Güncelleme Kontrolü");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jLabel1.setText("Versiyon Kontrolü Yapılıyor...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        try {
            UpdateAsync();
        } catch (Exception ex) {
            Log.send(ex);
        }
    }//GEN-LAST:event_formWindowOpened

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
            java.util.logging.Logger.getLogger(SelfUpdateWindows.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SelfUpdateWindows.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SelfUpdateWindows.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SelfUpdateWindows.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SelfUpdateWindows dialog = null;
                try {
                    dialog = new SelfUpdateWindows(new javax.swing.JFrame(), true);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(SelfUpdateWindows.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchPaddingException ex) {
                    Logger.getLogger(SelfUpdateWindows.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidKeyException ex) {
                    Logger.getLogger(SelfUpdateWindows.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalBlockSizeException ex) {
                    Logger.getLogger(SelfUpdateWindows.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BadPaddingException ex) {
                    Logger.getLogger(SelfUpdateWindows.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(SelfUpdateWindows.class.getName()).log(Level.SEVERE, null, ex);
                }
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
}
