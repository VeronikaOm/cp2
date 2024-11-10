import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) {
        int minRange = 0; // Мінімальне значення діапазону
        int maxRange = 100; // Максимальне значення діапазону
        int arraySize = 50; // Кількість елементів у масиві
        int numThreads = 5; // Кількість потоків для обробки

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        Random random = new Random();

        // Заповнення масиву випадковими числами у заданому діапазоні
        int[] numbers = random.ints(arraySize, minRange, maxRange).toArray();
        Set<Integer> resultSet = new CopyOnWriteArraySet<>();
        List<Future<int[]>> futures = new ArrayList<>();

        int partSize = arraySize / numThreads;
        long startTime = System.currentTimeMillis();

        // Розбиття масиву на частини та створення Callable для кожної частини
        for (int i = 0; i < arraySize; i += partSize) {
            int[] part = new int[Math.min(partSize, arraySize - i)];
            System.arraycopy(numbers, i, part, 0, part.length);

            Callable<int[]> task = () -> {
                int[] productArray = new int[part.length / 2];
                for (int j = 0; j < part.length - 1; j += 2) {
                    productArray[j / 2] = part[j] * part[j + 1];
                }
                for (int num : productArray) {
                    resultSet.add(num);
                }
                return productArray;
            };

            Future<int[]> future = executor.submit(task);
            futures.add(future);
        }

        // Збір результатів та перевірка стану виконання
        for (Future<int[]> future : futures) {
            try {
                if (!future.isCancelled()) {
                    int[] partialResult = future.get(); // Отримуємо результат
                    System.out.println("Результат обробки частини масиву: ");
                    for (int num : partialResult) {
                        System.out.print(num + " ");
                    }
                    System.out.println();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Перевірка завершеності всіх задач
        for (Future<int[]> future : futures) {
            if (future.isDone()) {
                System.out.println("Таск виконано.");
            }
        }

        executor.shutdown();

        long endTime = System.currentTimeMillis();
        System.out.println("Набір результатів: " + resultSet);
        System.out.println("Час виконання: " + (endTime - startTime) + " мілісекунд");
    }
}
