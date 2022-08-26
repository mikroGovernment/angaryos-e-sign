/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omersavas.angaryos.eimza.helpers;

import tr.gov.tubitak.uekae.esya.api.certificate.validation.policy.PolicyReader;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.policy.ValidationPolicy;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.crypto.alg.DigestAlg;
import tr.gov.tubitak.uekae.esya.api.infra.tsclient.TSSettings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 *
 * @author omers
 */
public class SigningTestConstants 
{
    private static final String DIRECTORY = "./signs";
	
    //private static final String PIN = "1234";

    private static ValidationPolicy POLICY;

    private static final String POLICY_FILE = "./config/certval-policy-test.xml";

    public static String getDirectory()												
    {
        return DIRECTORY;
    }

    public synchronized static ValidationPolicy getPolicy() throws ESYAException
    {
        if(POLICY == null)
        {
            try 
            {
                POLICY = PolicyReader.readValidationPolicy(new FileInputStream(POLICY_FILE));
                addTestEnvironment(POLICY);
            } 
            catch (FileNotFoundException e) 
            {
                throw new RuntimeException("Policy file could not be found", e);
            }
        }
        return POLICY;
    }


    private static void addTestEnvironment(ValidationPolicy policy)
    {
            //For KSM Test Environment, we add test roots.
            //This is needed for test time stamp. You don't need for production.
//		HashMap<String, Object> parameters = new HashMap<String, Object>();
//		parameters.put("dizin", "sertifika deposu/test kok sertifika");
//		policy.bulmaPolitikasiAl().addTrustedCertificateFinder("tr.gov.tubitak.uekae.esya.api.certificate.validation.find.certificate.trusted.TrustedCertificateFinderFromFileSystem",
//				parameters);

    }

    public static TSSettings getTSSettings()
    {
            //for getting test TimeStamp or qualified TimeStamp account, mail to bilgi@kamusm.gov.tr.
            //This configuration, user ID (2) and password (PASSWORD), is invalid. 
            return new TSSettings("http://tzd.kamusm.gov.tr", 2, "PASSWORD",DigestAlg.SHA256);
    }

    //To-Do Get PIN from user
    /*public static String getPIN()
    {
            return PIN;
    }*/
    
    public static boolean getCheckQCStatement()
    {
            //return false;   //Unqualified 
            return true;   //Qualified 
    }

    public static boolean validateCertificate()
    {
            return false;
    }
}
