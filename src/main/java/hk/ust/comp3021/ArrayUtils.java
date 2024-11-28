package hk.ust.comp3021;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ArrayUtils {
  // HINT: try to use this chunk size to partition your work.
  static final int CHUNK_SIZE = 1 << 16;

  public static int[] seqMap(int[] input, IntUnaryOperator map) {
    int[] output = new int[input.length];
    IntStream.range(0, input.length).forEach(i -> {
      output[i] = map.applyAsInt(input[i]);
    });
    return output;
  }

  public static int[] parMap(int[] input, IntUnaryOperator map, TaskPool pool) {
    // Bonus part
    int numChunks = (input.length + CHUNK_SIZE - 1) / CHUNK_SIZE;
    int[] output = new int[input.length];

      pool.addTasks(IntStream.range(0, numChunks).mapToObj(chunk -> {
          return (Runnable) () -> {
              int start = chunk * CHUNK_SIZE;
              int end = start + CHUNK_SIZE;
              for (int i = start; i < end; i++) {
                  output[i] = map.applyAsInt(input[i]);
              }
          };
      }).toList());

    return output;
    //throw new UnsupportedOperationException();
  }

  public static void seqInclusivePrefixSum(int[] input, IntBinaryOperator op) {
    IntStream.range(1, input.length).forEach(i -> {
      input[i] = op.applyAsInt(input[i - 1], input[i]);
    });
  }

  public static void parInclusivePrefixSum(int[] input, IntBinaryOperator op,
                                           TaskPool pool) {
    // Bonus part
      //Compute zi in parallel
      int numChunks = (input.length + CHUNK_SIZE - 1) / CHUNK_SIZE;
      int[] z = new int[numChunks];

      pool.addTasks(IntStream.range(0, numChunks).mapToObj(chunk -> {
          return (Runnable) () -> {
              int sum = 0;
              int start = chunk * CHUNK_SIZE;
              int end = Math.min(start + CHUNK_SIZE, input.length);
              for (int i = start; i < end; i++) {
                  sum = op.applyAsInt(sum, input[i]);
              }
              z[chunk] = sum;
          };
      }).toList());


 /*
      IntStream.range(0, numChunks).forEach(chunk -> {
                  z[chunk] = 0;
                  for (int i = chunk * CHUNK_SIZE; i < chunk * CHUNK_SIZE + CHUNK_SIZE; i++) {
                      z[chunk] += op.applyAsInt(z[chunk], input[i]);
                  }
              });

 */


      // Compute the prefix sum zi' of zi
      IntStream.range(1, numChunks).forEach(i -> {
            z[i] = op.applyAsInt(z[i - 1], z[i]);
        });

      // Compute yi in parallel
      pool.addTasks(IntStream.range(0, numChunks).mapToObj(chunk -> {
          return (Runnable) () -> {
              int offset = (chunk > 0) ? z[chunk - 1] : 0;
              int start = chunk * CHUNK_SIZE;
              int end = Math.min(start + CHUNK_SIZE, input.length);
              for (int i = start; i < end; i++) {
                  int prev = (i != start) ? input[i - 1] : offset;
                  input[i] = op.applyAsInt(input[i], prev);
              }
          };
      }).toList());

           /*
          IntStream.range(0, numChunks).forEach(chunk2 -> {
                      int offset = (chunk2 > 0) ? z[chunk2 - 1] : 0;
                      for (int i = chunk2 * CHUNK_SIZE; i < chunk2 * CHUNK_SIZE + CHUNK_SIZE; i++) {
                          int prev = (i > 0) ? input[i - 1] : 0;
                          input[i] = op.applyAsInt(input[i], prev + offset);
                      }
                  });

           */



      //throw new UnsupportedOperationException();
  }
}
