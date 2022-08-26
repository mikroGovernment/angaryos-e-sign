/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omersavas.angaryos.eimza.helpers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 *
 * @author omers
 */
public class Encryption {    
    Cipher ecipher, dcipher;
    KeySpec keySpec;
    SecretKey key;
    AlgorithmParameterSpec paramSpec;
    String charSet = "UTF-8";
    
    public Encryption(String pin) {
        try {
            byte[] salt = {
                (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
                (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
            };

            int iterationCount = 19;
            
            keySpec = new PBEKeySpec(pin.toCharArray(), salt, iterationCount);
            key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            // Prepare the parameter to the ciphers
            paramSpec = new PBEParameterSpec(salt, iterationCount);
            
            dcipher = Cipher.getInstance(key.getAlgorithm());
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
            
            ecipher = Cipher.getInstance(key.getAlgorithm());
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);            
            
        } catch (Exception e) {
            Log.send(e);
        }        
    }
        
    public String encode(String s) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        try{
            byte[] in = s.getBytes(charSet);
            byte[] out = ecipher.doFinal(in);

            String encStr = new String(Base64.getEncoder().encode(out));
            return encStr;
        } catch (Exception e) {
            return "";
        }
    }

    public String decode(String s) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, IOException {
        try{
            byte[] enc = Base64.getDecoder().decode(s);
            byte[] utf8 = dcipher.doFinal(enc);

            String plainStr = new String(utf8, charSet);
            return plainStr;
        } catch (Exception e) {
            return "";
        }
    }  
}
