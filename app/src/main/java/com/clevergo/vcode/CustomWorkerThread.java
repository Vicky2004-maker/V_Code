package com.clevergo.vcode;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CustomWorkerThread {

    private final Runnable KILLER_THREAD = () -> {
    };
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private Thread workerThread;

    public CustomWorkerThread() {
        initWorkerThread();
    }

    private void initWorkerThread() {
        workerThread = new Thread(() -> {
            while (true) {
                Runnable runnable;
                try {
                    runnable = queue.take();
                } catch (InterruptedException e) {
                    return;
                }
                if (runnable == KILLER_THREAD) {
                    return;
                }
                runnable.run();
            }
        });

        workerThread.setName("CustomWorkerThread");

        workerThread.start();
    }

    public Thread.State currentThreadState() {
        return workerThread.getState();
    }

    public void stop() {
        queue.clear();
        queue.add(KILLER_THREAD);
    }

    void addWork(Runnable runnable) {
        queue.add(runnable);
    }

    public Thread getThread() {
        return workerThread;
    }
}
