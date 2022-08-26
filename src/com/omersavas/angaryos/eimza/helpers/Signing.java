/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omersavas.angaryos.eimza.helpers;

import com.google.gson.internal.LinkedTreeMap;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import javax.swing.JOptionPane;
import sun.security.pkcs11.wrapper.PKCS11Exception;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.asn.x509.EName;
import tr.gov.tubitak.uekae.esya.api.cmssignature.CMSSignatureException;
import tr.gov.tubitak.uekae.esya.api.cmssignature.ISignable;
import tr.gov.tubitak.uekae.esya.api.cmssignature.SignableByteArray;
import tr.gov.tubitak.uekae.esya.api.cmssignature.attribute.EParameters;
import tr.gov.tubitak.uekae.esya.api.cmssignature.signature.BaseSignedData;
import tr.gov.tubitak.uekae.esya.api.cmssignature.signature.ESignatureType;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.common.crypto.BaseSigner;
import tr.gov.tubitak.uekae.esya.api.common.util.StringUtil;
import tr.gov.tubitak.uekae.esya.api.common.util.bag.Pair;
import tr.gov.tubitak.uekae.esya.api.smartcard.apdu.APDUSmartCard;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.BaseSmartCard;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.CardType;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.LoginException;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.P11SmartCard;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.SmartCardException;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.SmartOp;
import tr.gov.tubitak.uekae.esya.asn.util.AsnIO;

/**
 *
 * @author omers
 */
public class Signing 
{
    public String tc = "";
    public String lastSignedText = "";
    
    public BigInteger getSerialNumber() throws ESYAException{
        SigningSmartCardManager.getInstance();
        return SigningSmartCardManager.serial;
    }
    
    public String getTCNumber() throws ESYAException
    {
        boolean checkQCStatement = SigningTestConstants.getCheckQCStatement();
        ECertificate cert = SigningSmartCardManager.getInstance().getSignatureCertificate(checkQCStatement, !checkQCStatement);
        EName temp = cert.getSubject();
        return temp.getSerialNumberAttribute();
    }
    
    public boolean doESign(LinkedTreeMap data, String name) throws CMSSignatureException, SmartCardException, ESYAException, IOException {
        try {
            Log.info("test:A");
                               
            String str = data.get("text").toString();
            String pin = data.get("pin").toString();
            
            Log.info("B");
            try {                
                Log.info("C");
                if(SmartOp.getCardTerminals().length == 0){
                    GeneralHelper.showMessageBox("Takılı e-imza cihazı yok!");
                    return false;
                }
                
                Log.info("D");
                if(!SigningSmartCardManager.serial.equals(BigInteger.ZERO) && !SigningSmartCardManager.serial.equals(this.getSerialNumber())) {
                    GeneralHelper.showMessageBox("Farklı bir e imza cihazı takılmış! Tekrar giriş yapın.");
                    return false;
                }
            } 
            catch (Exception e) {
                GeneralHelper.showMessageBox("E-İmza cihazına erişilemedi! ("+ e.getMessage() +")");
                return false;
            }
                        
            Log.info("G");
            BaseSignedData bs = new BaseSignedData();
        
            String t = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            str += " (" + t + ")";
            ISignable content = new SignableByteArray(str.getBytes());
            bs.addContent(content);

            HashMap<String, Object> params = new HashMap<String, Object>();
            Log.info("H");
            //if the user does not want certificate validation at generating signature,he can add 
            //P_VALIDATE_CERTIFICATE_BEFORE_SIGNING parameter with its value set to false
            //params.put(EParameters.P_VALIDATE_CERTIFICATE_BEFORE_SIGNING, false);

            //necessary for certificate validation.By default,certificate validation is done 
            params.put(EParameters.P_CERT_VALIDATION_POLICY, SigningTestConstants.getPolicy());
            Log.info("I");
            
            //By default, QC statement is checked,and signature wont be created if it is not a 
            //qualified certificate. 
            boolean checkQCStatement = SigningTestConstants.getCheckQCStatement();
            Log.info("J");
            
            //Get qualified or non-qualified certificate.
            ECertificate cert = SigningSmartCardManager.getInstance().getSignatureCertificate(checkQCStatement, !checkQCStatement);
            
            EName temp = cert.getSubject();
            tc = temp.getSerialNumberAttribute();
            Log.info("K");
            
            BaseSigner signer = SigningSmartCardManager.getInstance().getSigner(pin, cert);
            Log.info("L");
            
            //add signer
            //Since the specified attributes are mandatory for bes,null is given as parameter 
            //for optional attributes
            bs.addSigner(ESignatureType.TYPE_BES, cert , signer, null, params);
            Log.info("M");
            
            SigningSmartCardManager.getInstance().logout();
            Log.info("N");
            
            byte [] signedDocument = bs.getEncoded();
            //return new String(signedDocument);
            //return signedDocument.toString();
            //return bs.getEncoded().toString();
            //
            Log.info("O");
            //Genel.showMessageBox("Burada stringi return et hata olursa boş return et");
            //write the contentinfo to file
            
            File tempDir = new File(SigningTestConstants.getDirectory());
            tempDir.mkdirs();

            AsnIO.dosyayaz(signedDocument, SigningTestConstants.getDirectory() + "/" + name + ".p7s");

            lastSignedText = str;
            Log.info("P");
            return true;
                
        } catch (LoginException e) {
            GeneralHelper.showMessageBox("İmzalama yapılamadı! ("+ e.getMessage() +")");
            Log.send(e);
        }catch (Exception e) {
            Log.send(e);
        }
        
        lastSignedText = "";
        return false;
    }

    public String getNewFileName() {
        return GeneralHelper.getTimeStamp("yyyy-MM-dd_HHmmss");
    }
}
