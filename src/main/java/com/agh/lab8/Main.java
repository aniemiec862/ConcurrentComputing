package com.agh.lab8;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Main {
    public static void main(String[] args) {
        int MAX_ITER = 500;

        try (Writer output = new BufferedWriter(new FileWriter("results.txt", true))) {
            for (int no_threads = 1; no_threads < 20; no_threads++) {
                List<ExecutorTest> executorTests = new ArrayList<>();
                executorTests.add(new NewSingleThreadExecutorTest());
                executorTests.add(new ThreadPoolTest(Executors.newFixedThreadPool(no_threads)));
                executorTests.add(new ThreadPoolTest(Executors.newCachedThreadPool()));
                executorTests.add(new ThreadPoolTest(Executors.newWorkStealingPool(no_threads)));

                List<Double> results = new ArrayList<>();
                for (ExecutorTest executorTest : executorTests) {
                    results.add(executorTest.runExecutorTest(MAX_ITER, no_threads));
                }

                for (Double result : results) {
                    output.append(String.valueOf(result)).append(" ");
                }
                output.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

interface ExecutorTest {
    double runExecutorTest(int MAX_ITER, int no_threads);
}

class NewSingleThreadExecutorTest implements ExecutorTest {

    @Override
    public double runExecutorTest(int MAX_ITER, int no_threads) {
        Instant start = Instant.now();
        List<ExecutorService> executors = new ArrayList<>();
        for (int i = 0; i < no_threads; i++) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executors.add(executor);
            executor.submit(new ExecutorThread(MAX_ITER));
            executor.shutdown();
        }
        for (ExecutorService executor : executors) {
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Instant finish = Instant.now();
        return (double) Duration.between(start, finish).toMillis() / 1000;
    }
}

class ThreadPoolTest implements ExecutorTest {
    private final ExecutorService executor;

    ThreadPoolTest(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public double runExecutorTest(int MAX_ITER, int no_threads) {
        Instant start = Instant.now();
        for (int i = 0; i < no_threads; i++) {
            executor.submit(new ExecutorThread(MAX_ITER));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Instant finish = Instant.now();
        return (double) Duration.between(start, finish).toMillis() / 1000;
    }
}

class Mandelbrot extends JFrame {
    private final double ZOOM = 150;
    private final BufferedImage I;
    private double zx, zy, cX, cY, tmp;

    Mandelbrot(int MAX_ITER) {
        super("Mandelbrot Set");
        setBounds(100, 100, 800, 600);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                zx = zy = 0;
                cX = (x - 400) / ZOOM;
                cY = (y - 300) / ZOOM;
                int iter = MAX_ITER;
                while (zx * zx + zy * zy < 4 && iter > 0) {
                    tmp = zx * zx - zy * zy + cX;
                    zy = 2.0 * zx * zy + cY;
                    zx = tmp;
                    iter--;
                }
                I.setRGB(x, y, iter | (iter << 8));
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(I, 0, 0, this);
    }
}

class ExecutorThread extends Thread {
    private final int MAX_ITER;

    ExecutorThread(int MAX_ITER) {
        this.MAX_ITER = MAX_ITER;
    }

    @Override
    public void run() {
        new Mandelbrot(MAX_ITER).setVisible(false);
    }
}