/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omersavas.angaryos.eimza.models;

import com.google.gson.internal.LinkedTreeMap;
import com.omersavas.angaryos.eimza.helpers.GeneralHelper;
import com.omersavas.angaryos.eimza.helpers.Security;
import com.omersavas.angaryos.eimza.helpers.Log;
import com.omersavas.angaryos.eimza.helpers.SigningSmartCardManager;
import com.omersavas.angaryos.eimza.helpers.SigningTestConstants;
import com.omersavas.angaryos.eimza.helpers.Encryption;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import tr.gov.tubitak.uekae.esya.api.cmssignature.CMSSignatureException;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.SmartOp;

/**
 *
 * @author omers
 */
public class Session {

    public ServerSocket server;
    public Socket clientSocket;
    public InputStream inputStream;
    public OutputStream outputStream;
    
    public String tc = "";
    
    private String lastResponse = "";
    
    public Session() throws IOException, ESYAException {
        tc = GeneralHelper.getSigning().getTCNumber();
    }
    
    public void startSockerServer(int port)
    {
        GeneralHelper.runAsync(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    server = new ServerSocket(port);        
                    clientSocket = server.accept();
                    
                    Log.info("Server başladı");
                    
                    GeneralHelper.getSession().waitForSocket();
                }
                catch(java.net.BindException b){
                    GeneralHelper.showMessageBox("Şuan açık olan başka bir uygulama var!");
                    System.exit(0);
                }catch (Exception e) {
                    Log.send(e);
                    GeneralHelper.showMessageBox("E-imza sunucusu başlatılamadı!");
                }

                return null;
            }
        });
    }
    
    public void waitForSocket()
    {
        GeneralHelper.runAsync(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    Log.info("Mesaj bekleniyor");
                    
                    inputStream  = clientSocket.getInputStream();
                    outputStream = clientSocket.getOutputStream();
                    
                    String msg = readFromSocket();
                    socketMessageReaded(msg); 
                    
                    Log.info("Mesaj ok yeniden dinlemeye başlanacak: " + msg);
                } catch (Exception e) {
                    clientSocket = server.accept();
                }

                Thread.sleep(50);                    
                waitForSocket();
                
                Log.info("fnc ok");
                return null;
            }
        });
    }
    
    public String getLastResponse()
    {
        return this.lastResponse;
    }
    
    private void socketMessageReaded(String msg) throws IOException, ESYAException, CMSSignatureException
    {        
        Log.info("Mesaj okundu: " + msg);
        
        if(msg.equals("")) return;
        
        LinkedTreeMap data = GeneralHelper.jsonDecode(msg);
        
        String type = "";
        try {
            type = data.get("type").toString();
        } catch (Exception e) {
        }
                
        switch(type)
        {
            case "connectionTest":
                Log.info("Test mesajı: " + msg);
                writeToSocket("{\"type\": \"connectionSuccess\"}");
                break;
            case "getUserTc":
                Log.info("Tc talebi: " + msg);                
                writeToSocket("{\"type\": \"returnTc\", \"tc\": "+GeneralHelper.getSession().tc+"}");
                break;
            case "doESign":
                try 
                {
                    Log.info("İmzalama talebi: " + msg);
                    String name = GeneralHelper.getSigning().getNewFileName();
                    
                    boolean control = GeneralHelper.getSigning().doESign(data, name);                    
                    if(!control) 
                    {
                        data.put("type", "doESignError");
                        data.put("log", Log.getLastLogMessage());
                        data.put("response", this.getLastResponse());
                        writeToSocket(GeneralHelper.jsonEncode(data));
                        break;
                    }
                        
                    String filePath = SigningTestConstants.getDirectory() + "/" + name + ".p7s";
                    control = uploadSign(filePath, data); 
                    
                    if(!control){
                        data.put("type", "doESignError");
                        data.put("log", Log.getLastLogMessage());
                        data.put("response", this.getLastResponse());
                    }
                    else data.put("type", "doESignSuccess");
                    
                    writeToSocket(GeneralHelper.jsonEncode(data));
                }
                catch (Exception e)
                {
                    Log.send(e);
                }
                break;
            default:
                Log.info("Geçersiz komut: " + msg);
                writeToSocket("{\"type\": \"uncorrectRequest\"}");
                break;
        }
        
        Log.info("Mesaj için gereği yapıldı: " + msg);
    }
    
    private void writeToSocket(String msg) throws IOException
    {
        Log.info("Sokete yazılacak: " + msg);
        
        outputStream.write(encodeForSocket(msg));
        outputStream.flush();
        
        Log.info("Sokete yazıldı: " + msg);
    }
    
    private boolean socketHandShakeControl(byte[] b) throws UnsupportedEncodingException
    {
        Log.info("El sıkışma kontrol");
        
        String data = new String(b);
        
        Log.info("El sıkışma kontrol: " + data.substring(0, 3));
        
        if(!data.startsWith("GET")) return false;
        
        Log.info("El sıkışma başlıyor");
        
        Matcher get = Pattern.compile("^GET").matcher(data);

        if (get.find()) {
            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            match.find();

            byte[] response = null;
            try {
                response = ("HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: "
                        + DatatypeConverter.printBase64Binary(
                        MessageDigest
                                .getInstance("SHA-1")
                                .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                        .getBytes("UTF-8")))
                        + "\r\n\r\n")
                        .getBytes("UTF-8");
            } catch (NoSuchAlgorithmException e) {
                Log.send(e);
                e.printStackTrace();
            }

            try {
                outputStream.write(response, 0, response.length);
            } catch (IOException e) {
                Log.send(e);
                e.printStackTrace();
            }
        }
        
        Log.info("El sıkışma başarılı");
        
        return true;        
    }
    
    private String readFromSocket() throws IOException {
        Log.info("Soket okunmak için beklenecek");
        
        int len = 0;
        byte[] b = new byte[1024];
        
        len = inputStream.read(b);
        Log.info("Soket data uzunluğu" + len);
        if(len == -1)
        {
            Log.info("-1 uzunluklu mesaj geldi dinleme yeniden başlatılıyor.");
            clientSocket = server.accept();
            return "";
        }
        
        if(this.socketHandShakeControl(b)) return "";//test et tekrar read bekleme koyulabilir belki. bi üstteki gibi
        
        Log.info("Mesaj el sıkışma talebi değil");
        
        byte rLength = 0;
        int rMaskIndex = 2;
        int rDataStart = 0;
        //b[0] is always text in my case so no need to check;
        byte data = b[1];
        byte op = (byte) 127;
        rLength = (byte) (data & op);

        if(rLength==(byte)126) rMaskIndex=4;
        if(rLength==(byte)127) rMaskIndex=10;

        byte[] masks = new byte[4];

        int j=0;
        int i=0;
        for(i=rMaskIndex;i<(rMaskIndex+4);i++){
            masks[j] = b[i];
            j++;
        }

        rDataStart = rMaskIndex + 4;

        int messLen = len - rDataStart;

        byte[] message = new byte[messLen];

        for(i=rDataStart, j=0; i<len; i++, j++){
            message[j] = (byte) (b[i] ^ masks[j % 4]);
        }

        String temp = new String(message); 
        Log.info("Mesaj çözüldü: " + temp);
        return temp;
    }
    
    public static byte[] encodeForSocket(String mess) throws IOException{
        byte[] rawData = mess.getBytes(UTF_8);

        int frameCount  = 0;
        byte[] frame = new byte[10];

        frame[0] = (byte) 129;

        if(rawData.length <= 125){
            frame[1] = (byte) rawData.length;
            frameCount = 2;
        }else if(rawData.length >= 126 && rawData.length <= 65535){
            frame[1] = (byte) 126;
            int len = rawData.length;
            frame[2] = (byte)((len >> 8 ) & (byte)255);
            frame[3] = (byte)(len & (byte)255);
            frameCount = 4;
        }else{
            frame[1] = (byte) 127;
            int len = rawData.length;
            frame[2] = (byte)((len >> 56 ) & (byte)255);
            frame[3] = (byte)((len >> 48 ) & (byte)255);
            frame[4] = (byte)((len >> 40 ) & (byte)255);
            frame[5] = (byte)((len >> 32 ) & (byte)255);
            frame[6] = (byte)((len >> 24 ) & (byte)255);
            frame[7] = (byte)((len >> 16 ) & (byte)255);
            frame[8] = (byte)((len >> 8 ) & (byte)255);
            frame[9] = (byte)(len & (byte)255);
            frameCount = 10;
        }

        int bLength = frameCount + rawData.length;

        byte[] reply = new byte[bLength];

        int bLim = 0;
        for(int i=0; i<frameCount;i++){
            reply[bLim] = frame[i];
            bLim++;
        }
        for(int i=0; i<rawData.length;i++){
            reply[bLim] = rawData[i];
            bLim++;
        }

        return reply;
    }
    
    public String httpGetBasic(String u) throws MalformedURLException, IOException
    {
        HttpURLConnection conn = null;
        BufferedReader rd = null;
        StringBuilder result = null;
        
        try {
            if(GeneralHelper.sslDisable)
            {
                Log.info("httpGetBasic sslDisable for " + u);
                
                SSLUtilities.trustAllHostnames();
                SSLUtilities.trustAllHttpsCertificates();
            }
    
            URL url = new URL(u);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
               result.append(line);
            }
            rd.close();

            this.lastResponse = result.toString();
            
            return this.lastResponse;
            
        } catch (Exception e) {
            
            try {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            
                result = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                   result.append(line);
                }
                rd.close();

                this.lastResponse = result.toString();
                
                return this.lastResponse;
            } catch (Exception ee) {
                
                GeneralHelper.showMessageBox("Sunucuya erişilemedi! Lütfen sonra tekrar deneyin.");
                return null;
            }
        }
    }
    
    public LinkedTreeMap httpGet(String u) throws MalformedURLException, IOException
    {
        try {
            String json = this.httpGetBasic(u);
            if(json == null) return null;
            
            LinkedTreeMap r = GeneralHelper.jsonDecode(json);
            
            String status = r.get("status").toString();
            LinkedTreeMap d = (LinkedTreeMap)r.get("data");

            if(status.equals("success")) return d;
            else this.writeServerMessage(d.get("message").toString());
            
            return null;
            
        } catch (Exception e) {
            GeneralHelper.showMessageBox("Sunucuya erişilemedi! Lütfen sonra tekrar deneyin.");
            return null;
        }
    }
    
    public LinkedTreeMap httpPost(String u, List<NameValuePair> data) {
        
        try {
            
            HttpClient httpclient;
            
            if(GeneralHelper.sslDisable)
            {
                Log.info("httpPost sslDisable for " + u);
                httpclient = HttpClients
                                        .custom()
                                        .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                        .build();
            }
            else 
                httpclient = HttpClients.createDefault();
            
            HttpPost httppost = new HttpPost(u);

            httppost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                this.lastResponse = EntityUtils.toString(entity);
                LinkedTreeMap r = GeneralHelper.jsonDecode(this.lastResponse);
            
                String status = r.get("status").toString();
                LinkedTreeMap d = (LinkedTreeMap)r.get("data");
                
                if(status.equals("success")) return d;
                else this.writeServerMessage(d.get("message").toString());
            }
            
            return null;
            
        } catch (Exception e) {
            GeneralHelper.showMessageBox("Sunucuya erişilemedi! Lütfen sonra tekrar deneyin.");
            return null;
        }
    }
    
    private void writeServerMessage(String m)
    {
        switch(m)
        {
            case "mail.or.password.incorrect":
                m = "Mail yada şifre yanlış!";
                break;
            case "fail.token":
                m = "Oturum zaman aşımına uğramış tekrar giriş yapınız";
                break;
        }
        
        GeneralHelper.showMessageBox(m);
    }
    
    public boolean uploadSign(String filePath, LinkedTreeMap data) {
        
        try {
            String url = data.get("url").toString();
            String columnSetId = data.get("columnSetId").toString();
            String recordId = data.get("recordId").toString();
            
            HttpClient httpclient;
            
            if(GeneralHelper.sslDisable)
            {
                Log.info("uploadSign sslDisable");
                httpclient = HttpClients
                                        .custom()
                                        .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                        .build();
            }
            else 
                httpclient = HttpClients.createDefault();

            HttpPost httpPost = new HttpPost(url);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("state", "1", ContentType.TEXT_PLAIN);
            builder.addTextBody("signed_at", GeneralHelper.getTimeStamp("yyyy-MM-dd HH:mm:ss"), ContentType.TEXT_PLAIN);
            builder.addTextBody("sign_file_old", "", ContentType.TEXT_PLAIN);
            builder.addTextBody("column_set_id", columnSetId+"", ContentType.TEXT_PLAIN);
            builder.addTextBody("id", recordId+"", ContentType.TEXT_PLAIN);

            File f = new File(filePath);
            builder.addBinaryBody( "sign_file[]", new FileInputStream(f), ContentType.create("application/pkcs7-signature"), f.getName() );

            HttpEntity multipart = builder.build();
            
            httpPost.setEntity(multipart);
            
            
            
            HttpResponse response = httpclient.execute(httpPost);            
            HttpEntity entity = response.getEntity();

            if (entity == null) return false;
            
            this.lastResponse = EntityUtils.toString(entity);
            LinkedTreeMap r = GeneralHelper.jsonDecode(this.lastResponse);

            String status = r.get("status").toString();

            return status.equals("success");
            
        } catch (Exception e) {
            Log.send(e);
            return false;
        }
    }
}
