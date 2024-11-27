package hk.ust.comp3021;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParEvaluator<T> implements Evaluator<T> {
  private HashMap<FunNode<T>, List<Consumer<T>>> listeners = new HashMap<>();
  private TaskPool pool;

  public ParEvaluator(int numThreads) { pool = new TaskPool(numThreads); }

  public void addDependency(FunNode<T> a, FunNode<T> b, int i) {
    // part 4: parallel function evaluator
    //throw new UnsupportedOperationException();
      listeners.computeIfAbsent(a, k -> new ArrayList<>()).add(nodeValue -> {
          synchronized (b) {
              Optional<FunNode<T>> isFull = b.setInput(i, nodeValue);
              if (isFull.isEmpty()) {
                  long waitTime = System.currentTimeMillis();
                  long timeout = 100;
                  while (isFull.isEmpty()) {
                      long elapsed = System.currentTimeMillis() - waitTime;
                      if (elapsed > timeout) {
                          break;
                      }
                      try {
                          b.wait(timeout - elapsed);
                      } catch (InterruptedException e) {
                          Thread.currentThread().interrupt();
                          throw new RuntimeException(e);
                      }
                      isFull = b.setInput(i, nodeValue);
                  }
              }
              pool.addTask(() -> {
                  b.eval();
                  Optional.ofNullable(listeners.get(b))
                          .ifPresent(dependencyList -> dependencyList
                                  .forEach(consumer -> consumer.accept(b.getResult())));
              });
              b.notifyAll();
          }
      });
  }

  public void terminate() { pool.terminate(); }

  public void start(List<FunNode<T>> nodes) {
    // part 4: parallel function evaluator
      pool.addTasks(nodes.stream()
              .map(node -> (Runnable) () -> {
                  node.eval(); // Evaluate the node
                  Optional.ofNullable(listeners.get(node))
                          .ifPresent(dependencyList -> dependencyList
                                  .forEach(consumer -> consumer.accept(node.getResult())));
              })
              .toList());

    //throw new UnsupportedOperationException();
  }
}
