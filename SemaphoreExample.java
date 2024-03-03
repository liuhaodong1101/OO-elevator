import java.util.concurrent.Semaphore;

public class SemaphoreExample {
    private static final int NUMBER_OF_SEMAPHORES = 11;

    private static final int MAX_PERMITS_FOR_SAME_SERVE = 4;

    private static final int MAX_PERMITS_FOR_ONLY_IN = 2;

    private static final Semaphore[] SEMAPHORES_FOR_SERVE = new Semaphore[NUMBER_OF_SEMAPHORES];

    private static final Semaphore[] SEMAPHORES_FOR_ONLY_IN = new Semaphore[NUMBER_OF_SEMAPHORES];

    static  {
        for (int i = 0; i < NUMBER_OF_SEMAPHORES; i++) {
            SEMAPHORES_FOR_SERVE[i] = new Semaphore(MAX_PERMITS_FOR_SAME_SERVE);
            SEMAPHORES_FOR_ONLY_IN[i] = new Semaphore(MAX_PERMITS_FOR_ONLY_IN);
        }
    }

    public static void acquireSemaphoreForServe(int index) {
        try {
            SEMAPHORES_FOR_SERVE[index - 1].acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void releaseSemaphoreForServe(int index) {
        SEMAPHORES_FOR_SERVE[index - 1].release();
    }

    public static void acquireSemaphoreForOnlyIn(int index) {
        try {
            SEMAPHORES_FOR_ONLY_IN[index - 1].acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void releaseSemaphoreForOnlyIn(int index) {
        SEMAPHORES_FOR_ONLY_IN[index - 1].release();
    }
}
