package ngsanalyser.ncbiservice;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Timer {
    private int interval;
    private boolean ready = true;

    public Timer(int interval) {
        this.interval = interval;
    }
    
    public synchronized void getPermission() {
        while (!ready) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ready = false;
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    timingFinished();
                }
            }
        })).start();
    }

    synchronized private void timingFinished() {
        ready = true;
        notify();
    }
    
   
    synchronized public void start() {
        while (!ready) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        startTiming();
    }

    synchronized private void startTiming() {
        new Thread(new Timing(interval)).start();
        ready = false;
    }
    
    private class Timing implements Runnable {
        private final int interval;

        public Timing(int interval) {
            this.interval = interval;
        }
        
        @Override
        public void run() {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ex) {
                Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
            }
            timingFinished();
        }
    }
}
