package hk.ust.comp3021;

import java.util.*;
import java.util.function.Consumer;

public class SeqEvaluator<T> implements Evaluator<T> {
  private HashMap<FunNode<T>, List<Consumer<T>>> listeners = new HashMap<>();

  public void addDependency(FunNode<T> a, FunNode<T> b, int i) {
    // part 2: sequential function evaluator
    listeners.computeIfAbsent(a, k -> new ArrayList<>())
        .add(nodeValue -> {
            if(b.setInput(i, nodeValue).isPresent())
                start(List.of(b));
        });
    //throw new UnsupportedOperationException();
  }

  public void start(List<FunNode<T>> nodes) {
    // part 2: sequential function evaluator
    nodes.forEach(node -> {
        node.eval();
        Optional.ofNullable(listeners.get(node))
              .ifPresent(dependencyList -> dependencyList
                      .forEach(consumer -> consumer.accept(node.getResult()))
              );
    });
    //throw new UnsupportedOperationException();
  }
}
