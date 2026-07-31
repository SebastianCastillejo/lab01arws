/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.threads;

/**
 *
 * @author hcadavid
 */


public class CountThreadsMain {
    
    static int A = 0;
    static int B = 299;    

    public static void main(String a[]){
        Thread hilo1 = new Thread(new CountThread( A, B/3));
        Thread hilo2 = new Thread(new CountThread(B/3, B/2));
        Thread hilo3 = new Thread(new CountThread(B/2, B));

        hilo1.start();
        hilo2.start();
        hilo3.start();
    }
    
}
