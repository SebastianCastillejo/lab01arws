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
public class CountThread implements Runnable {
    private int A;
    private int B;

    public CountThread(int a, int b) {
        A = a;
        B = b;
    }

    @Override
    public void run() {
        for (int i = A; i < B; i++) {
            System.out.println(i);
        }
        
    }
    
}
