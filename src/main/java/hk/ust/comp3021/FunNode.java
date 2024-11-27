package hk.ust.comp3021;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FunNode<T> {
  private List<Optional<T>> inputs;
  private Optional<T> output = Optional.empty();
  private Function<List<T>, T> f;

  public FunNode(int arity, Function<List<T>, T> fun) {
    // part 1: function data dependency graph node
    this.inputs = new ArrayList<>(Collections.nCopies(arity,Optional.empty()));
    this.f = fun;
    // throw new UnsupportedOperationException();
  }

  public Optional<FunNode<T>> setInput(int i, T value) {
    // part 1: function data dependency graph node
    inputs.set(i, Optional.of(value));
    if(inputs.stream().allMatch(Optional::isPresent))
        return Optional.of(this);
    else
        return Optional.empty();
    //throw new UnsupportedOperationException();
  }

  public  T getResult() { synchronized(this){return output.get(); }}

  public synchronized void eval() {
    // part 1: function data dependency graph node
    if(inputs.stream().allMatch(Optional::isPresent)) {
      List<T> function_inputs = inputs.stream().map(a -> a.get()).collect(Collectors.toList());
      output = Optional.of(this.f.apply(function_inputs));
    }
    //throw new UnsupportedOperationException();
  }
}
