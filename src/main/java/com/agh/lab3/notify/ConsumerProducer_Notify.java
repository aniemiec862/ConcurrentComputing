package com.agh.lab3.notify;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class Producer extends Thread {
    private final Buffer _buf;
    private final int iterations;

    Producer(Buffer buffer, int iterations) {
        this._buf = buffer;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        for (int i = 0; i < iterations; ++i) {
            System.out.println("Producer puts " + i);
            _buf.put(i);
            try {
                sleep((int) (Math.random() * 100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Consumer extends Thread {
    private final Buffer _buf;
    private final int iterations;

    Consumer(Buffer buffer, int iterations) {
        this._buf = buffer;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        for (int i = 0; i < iterations; ++i) {
            System.out.println("Consumer received: " + _buf.get());
            try {
                sleep((int) (Math.random() * 100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Buffer {
    private final List<Integer> values = new ArrayList<>();
    private final int M;

    Buffer(int m) {
        this.M = m;
    }

    synchronized void put(int i) {
        while (values.size() >= M) {
            try {
                System.out.println("Producer is waiting");
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        values.add(i);
        notify();
    }

    synchronized int get() {
        while (values.isEmpty()) {
            try {
                System.out.println("Consumer is waiting");
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int returnVal = values.get(0);
        values.remove(0);
        notify();
        return returnVal;
    }
}

class PKmon {
    public static void main(String[] args) {
        Buffer buffer = new Buffer(100);
        List<Thread> threads = new ArrayList<>();

        int noProducers = 100;
        int noConsumers = 100;

        Instant start = Instant.now();

        for (int i = 0; i < noConsumers + noProducers; i++) {
            Thread thread = i < noProducers ? new Producer(buffer, 100) : new Consumer(buffer, 100);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Instant finish = Instant.now();
        double timeElapsed = Duration.between(start, finish).toMillis();

        System.out.println("Liczba producentów: " + noProducers);
        System.out.println("Liczba konsumentów: " + noConsumers);
        System.out.println("Czas działania: " + timeElapsed / 1000 + "s");
    }
}
