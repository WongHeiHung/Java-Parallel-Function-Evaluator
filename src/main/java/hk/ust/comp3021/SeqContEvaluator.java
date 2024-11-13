package hk.ust.comp3021;

import java.util.*;
import java.util.function.Consumer;

public class SeqContEvaluator<T> implements Evaluator<T> {
  private ArrayDeque<FunNode<T>> toEval = new ArrayDeque<>();
  private HashMap<FunNode<T>, List<Consumer<T>>> listeners = new HashMap<>();

  public void addDependency(FunNode<T> a, FunNode<T> b, int i) {
    // part 2: sequential function evaluator
    listeners.computeIfAbsent(a, k -> new ArrayList<>())
            .add(nodeValue -> {
              b.setInput(i, nodeValue);
              toEval.add(b);
            });
    //throw new UnsupportedOperationException();
  }

  public void start(List<FunNode<T>> nodes) {
    // part 2: sequential function evaluator
      toEval.addAll(nodes);
      while (!toEval.isEmpty()) {
          FunNode<T> node = toEval.poll();
          node.eval();
          Optional.ofNullable(listeners.get(node))
                  .ifPresent(dependencyList -> dependencyList
                          .forEach(consumer -> consumer.accept(node.getResult()))
                  );
      }

    //throw new UnsupportedOperationException();
  }
}
