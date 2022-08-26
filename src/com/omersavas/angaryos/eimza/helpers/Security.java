/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.omersavas.angaryos.eimza.helpers;

/**
 *
 * @author omers
 */
public class Security 
{
    public static byte tryLoginCount = 0;
    
    public static boolean tryLogin()
    {
        return (++tryLoginCount < 5); 
    }
}
