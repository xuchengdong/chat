package com.df.test;

public class ThreadTest {
    public static void main(String[] args) {
        new Thread(new Test()).start();
    }
}

class Test implements Runnable {

    int i = 1;

    public void run() {
        while (true) {
            System.out.println(i++);

            try {

                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }

    }

}
