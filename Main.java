public class Main {

    // ==================== Класс потока-зверушки ====================
    static class AnimalThread extends Thread {

        private final String animalName;   // имя потока (зверушки)
        private int priority;              // приоритет потока
        private int meters = 0;            // счётчик пройденных метров
        public static final int DISTANCE = 100; // дистанция забега

        public AnimalThread(String animalName, int priority) {
            super(animalName);
            this.animalName = animalName;
            this.priority = priority;
            setPriority(priority);
        }

        @Override
        public void run() {
            while (meters < DISTANCE) {
                meters++;

                // немного "работы", чтобы потоки конкурировали за процессор
                double sum = 0;
                for (int i = 0; i < 1_000_000; i++) {
                    sum += Math.sqrt(i);
                }

                System.out.printf("%-10s пробежал: %3d м (приоритет: %d)%n",
                        animalName, meters, getPriority());
            }
            System.out.println(">>> " + animalName + " ФИНИШИРОВАЛ! <<<");
        }

        public int getMeters() {
            return meters;
        }
    }

    // ==================== Точка входа ====================
    public static void main(String[] args) throws InterruptedException {

        // ---------- ЧАСТЬ 1. Статический забег (фиксированные приоритеты) ----------
        System.out.println("========== ЗАБЕГ №1: ФИКСИРОВАННЫЕ ПРИОРИТЕТЫ ==========");

        AnimalThread rabbit1 = new AnimalThread("Кролик", Thread.MAX_PRIORITY);
        AnimalThread turtle1 = new AnimalThread("Черепаха", Thread.MIN_PRIORITY);

        rabbit1.start();
        turtle1.start();

        rabbit1.join();
        turtle1.join();

        System.out.println("Победитель забега №1: " +
                (rabbit1.getMeters() >= AnimalThread.DISTANCE ? "Кролик" : "Черепаха"));
        System.out.println();

        // ---------- ЧАСТЬ 2. Динамический забег (приоритеты меняются) ----------
        System.out.println("========== ЗАБЕГ №2: ДИНАМИЧЕСКИЕ ПРИОРИТЕТЫ ==========");

        AnimalThread rabbit = new AnimalThread("Кролик", Thread.NORM_PRIORITY);
        AnimalThread turtle = new AnimalThread("Черепаха", Thread.NORM_PRIORITY);

        rabbit.start();
        turtle.start();

        // Пока оба бегут — выравниваем приоритеты:
        // отстающий получает больший приоритет и догоняет лидера.
        while (rabbit.isAlive() && turtle.isAlive()) {

            int rMeters = rabbit.getMeters();
            int tMeters = turtle.getMeters();

            if (tMeters < rMeters) {
                // Черепаха отстаёт — повышаем ей приоритет, Кролику понижаем
                if (rabbit.getPriority() > Thread.MIN_PRIORITY)
                    rabbit.setPriority(rabbit.getPriority() - 1);
                if (turtle.getPriority() < Thread.MAX_PRIORITY)
                    turtle.setPriority(turtle.getPriority() + 1);

            } else if (rMeters < tMeters) {
                // Кролик отстаёт — повышаем ему приоритет, Черепахе понижаем
                if (turtle.getPriority() > Thread.MIN_PRIORITY)
                    turtle.setPriority(turtle.getPriority() - 1);
                if (rabbit.getPriority() < Thread.MAX_PRIORITY)
                    rabbit.setPriority(rabbit.getPriority() + 1);
            }

            Thread.sleep(50); // не «крутимся» слишком часто
        }

        rabbit.join();
        turtle.join();
        System.out.println("========== ВСЕ ЗАБЕГИ ЗАВЕРШЕНЫ ==========");
    }
}