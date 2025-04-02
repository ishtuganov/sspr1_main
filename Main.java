import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        int[][] matrix = generateMatrix(1000);

        long startTime = System.currentTimeMillis();
        int min = findMinBelowDiagonal(matrix);
        long endTime = System.currentTimeMillis();

        System.out.println("Min below diagonal (single-threaded): " + min);
        System.out.println("Time taken (single-threaded): " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();
        try {
            min = findMinBelowDiagonalThreadPool(matrix);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        endTime = System.currentTimeMillis();

        System.out.println("Min below diagonal (ThreadPoolExecutor): " + min);
        System.out.println("Time taken (ThreadPoolExecutor): " + (endTime - startTime) + " ms");

        ForkJoinPool pool = new ForkJoinPool();
        MinTask task = new MinTask(matrix, 0, matrix.length);
        startTime = System.currentTimeMillis();
        try {
            min = pool.invoke(task);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        endTime = System.currentTimeMillis();

        System.out.println("Min below diagonal (ForkJoinPool): " + min);
        System.out.println("Time taken (ForkJoinPool): " + (endTime - startTime) + " ms");
    }

    private static int findMinBelowDiagonal(int[][] matrix) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < i; j++) {
                if (matrix[i][j] < min) {
                    min = matrix[i][j];
                }
            }
        }
        return min;
    }

    private static int[][]

    generateMatrix(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = (int) (Math.random() * 10000);
            }
        }
        return matrix;
    }

    private static int findMinBelowDiagonalThreadPool(int[][] matrix) throws Exception {
        int min = Integer.MAX_VALUE;
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Future[] futures = new Future[matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            final int row = i;
            futures[i] = executor.submit(() -> {
                int localMin = Integer.MAX_VALUE;
                for (int j = 0; j < row; j++) {
                    if (matrix[row][j] < localMin) {
                        localMin = matrix[row][j];
                    }
                }
                return localMin;
            });
        }

        for (Future<Integer> future : futures) {
            int localMin = future.get();
            if (localMin < min) {
                min = localMin;
            }
        }

        executor.shutdown();
        return min;
    }

    public static class MinTask extends RecursiveTask<Integer> {
        private final int[][] matrix;
        private final int start;
        private final int end;

        public MinTask(int[][] matrix, int start, int end) {
            this.matrix = matrix;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start <= 100) {
                return findMinBelowDiagonal();
            } else {
                int mid = (start + end) / 2;
                MinTask leftTask = new MinTask(matrix, start, mid);
                MinTask rightTask = new MinTask(matrix, mid, end);

                leftTask.fork();
                int rightResult = rightTask.compute();
                int leftResult = leftTask.join();

                return Math.min(leftResult, rightResult);
            }
        }

        private int findMinBelowDiagonal() {
            int min = Integer.MAX_VALUE;
            for (int i = start; i < end; i++) {
                for (int j = 0; j < i; j++) {
                    if (matrix[i][j] < min) {
                        min = matrix[i][j];
                    }
                }
            }
            return min;
        }
    }
}