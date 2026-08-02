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
    static int N = 7;

    public static void main(String a[]){

        int total = B - A + 1;
        int tamSegmento = total / N;

        Thread[] hilos = new Thread[N];

        for (int i = 0; i < N; i++){
            int inicio = A + i * tamSegmento;
            int fin = (i == N-1) ? B + 1 : inicio + tamSegmento;

            hilos[i] = new Thread(new CountThread(inicio, fin));
        }

        for (Thread hilo : hilos){
            hilo.start();
        }

    }

}
