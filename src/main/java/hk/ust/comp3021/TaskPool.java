package hk.ust.comp3021;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public class TaskPool {
  public class TaskQueue {
    private ArrayDeque<Runnable> queue = new ArrayDeque<>();
    private boolean terminated = false;
    private int working;
    private Semaphore idle;

    public TaskQueue(int numThreads, Semaphore idle) {
      working = numThreads;
      this.idle = idle;
    }

    public synchronized Optional<Runnable> getTask() {
      // part 3: task pool
        while (queue.isEmpty() && !terminated) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        if(queue.isEmpty())
            return Optional.empty();
        else
            return Optional.ofNullable(queue.poll());
      //throw new UnsupportedOperationException();
    }

    public synchronized void addTask(Runnable task) {
      // part 3: task pool
        if (!terminated) {
            queue.add(task);
            working++;
            notify();
        }
      //throw new UnsupportedOperationException();
    }

    public synchronized void terminate() {
      // part 3: task pool
      terminated = true;
      notifyAll();
      //throw new UnsupportedOperationException();
    }
  }

  private TaskQueue queue;
  private Thread workers[];
  private Semaphore idle = new Semaphore(0);

  public TaskPool(int numThreads) {
    // part 3: task pool
    queue = new TaskQueue(numThreads, idle);
    workers = new Thread[numThreads];

    for (int i = 0; i < numThreads; i++) {
        workers[i] = new Thread(() -> {
            while (true) {
                Optional<Runnable> task = queue.getTask();
                if (task.isEmpty() || queue.terminated)
                    break;
                task.get().run();
                idle.release();
            }
        });
        workers[i].start();
    }
    //throw new UnsupportedOperationException();
  }

  public void addTask(Runnable task) { queue.addTask(task); }

    public void addTasks(List<Runnable> tasks) {
        // part 3: task pool
        tasks.forEach(this::addTask);

        synchronized (this) {
            while (Arrays.stream(workers).allMatch(thread -> thread.getState() == Thread.State.TERMINATED) && !queue.terminated) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (!queue.terminated) {
            try {
                idle.acquire(tasks.size());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    //throw new UnsupportedOperationException();
  }

  public void terminate() {
    queue.terminate();
    for (Thread thread : workers) {
      try {
        // this will send an InterruptedException to the thread to wake it up
        // from blocking operations such as Thread.sleep.
        if (thread.isAlive())
          thread.interrupt();
        thread.join();
      } catch (InterruptedException ex) {
      }
    }
  }
}
