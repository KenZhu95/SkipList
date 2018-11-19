package Test;

import Interface.SkipList;

import java.util.*;

public class SkipListTestBench {

    static class Context {	//# of threads, times, range
        int numThreads;
        int times;
        int range;

        public Context(int numThreads, int times, int range) {
            this.numThreads = numThreads;
            this.times = times;
            this.range = range;
        }
    }

    static List<Context> contexts;

    static void setupContexts() {
        contexts = new ArrayList<>();
        contexts.add(new Context(2, 5, 20000));
        contexts.add(new Context(4, 5, 20000));
        contexts.add(new Context(8, 5, 20000));
        contexts.add(new Context(16, 5, 20000));
        contexts.add(new Context(32, 5, 20000));

    }

    static HashMap<String, SkipList> createSkipLists() {
        HashMap<String, SkipList> skipList = new HashMap<>();
        skipList.put("coarseGrained", new CoarseGrained.SkipList());
        skipList.put("fineGrained", new FineGrained.SkipList());
        skipList.put("lockFree", new LockFree.SkipList());
        skipList.put("standard", new Standard.SkipList());
        return skipList;
    }

    public static void main(String args[]) {
        setupContexts();
        runAllTests();
    }

    static void runAllTests() {
    				//		  All A		
        double[][] probs = {{0.9, 0.09}, {0.7, 0.2}, {0.0, 0.5}};
        for (int i = 0; i < 3; i++) {
            double[] prob = probs[i];
            System.out.println(prob[0]+" C, "+(prob[1])+"A, "+(1-prob[1]-prob[0])+"R");
            for (Context context : contexts) {
                System.out.println("Number of Threads: " + context.numThreads);
                Test test = new Test(10000, context.numThreads, context.times, context.range, prob[0], prob[1]);
                HashMap<String, Double> map = test.runTestAll();
                for (Map.Entry<String, Double> entry : map.entrySet()) {
                    System.out.print(entry.getKey() + "\t"+entry.getValue()+ "\t");
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    static class RandomValue {		//Generate Random Value within Given Range for Key
        private final Random random;
        private final int range;

        public RandomValue(Random random, int range) {
            this.random = random;
            this.range = range;
        }

        public int nextValue() {
            return this.random.nextInt(range);
        }
    }

    static class Runner implements Runnable {
        SkipList skipList;
        int numOperations;
        WeightedOperation weightedOperation;
        RandomValue randomValue;
        double cP;
        double aP;

        public Runner(SkipList s, int numOp, RandomValue rv, double pC, double pA) {
            this.skipList = s;
            this.numOperations = numOp;
            this.cP = pC;
            this.aP = pA;
            this.weightedOperation = new WeightedOperation(new Random(System.nanoTime()), pC, pA);
            this.randomValue = rv;
        }

        @Override
        public void run() {
            for (int index = 0; index < numOperations; ++index) {
                //System.out.print(index);
                int key = this.randomValue.nextValue();

                switch (this.weightedOperation.nextOperation()) {
                    case Contains:
                        skipList.contains(key);
                        break;
                    case Add:
                        skipList.add(key);
                        break;
                    case Remove:
                        skipList.remove(key);
                        break;
                }
            }
        }
    }

    static class Test {
        int numOperations;
        int numThreads;
        int times;
        int range;
        double containsP;	//Potential of Contain Operation
        double addP;

        public Test(int numOp, int numTh, int times, int range, double pC, double pA) {
            this.numOperations = numOp;
            this.numThreads = numTh;
            this.times = times;
            this.range = range;
            this.containsP = pC;
            this.addP = pA;
        }

        long runTestOnce(SkipList skipList) {
            List<Thread> threads = new ArrayList<>();
            //Generate threads
            for (int i = 0; i < this.numThreads; ++i) {
                Random random = new Random();
                RandomValue randomValue = new RandomValue(random, range);
                Thread thread = new Thread(new Runner(skipList, numOperations, randomValue, containsP, addP));
                threads.add(thread);
            }
            //Start Time
            long start = System.nanoTime();
            for (Thread t : threads) {
                t.start();
            }
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //Return Running Time
            return System.nanoTime() - start;
        }

        double average(List<Long> list) {
            long sum = 0;
            for (Long time : list) {
                sum += time;
            }
            return ((double)sum) / list.size();
        }

        HashMap<String, Double> runTestAll() {
            HashMap<String, Double> result = new HashMap<>();
            HashMap<String, SkipList> skipListHashMap = createSkipLists();

            for (Map.Entry<String, SkipList> entry : skipListHashMap.entrySet()) {
                String name = entry.getKey();
                SkipList skipList = entry.getValue();

                List<Long> runTime = new ArrayList<>();
                for (int i = 0; i < this.times; ++i) {
                    long time = runTestOnce(skipList);
                    runTime.add(time);
                    skipList.clear();
                }
                double averageRunTime = average(runTime);
                result.put(name+" ", averageRunTime);
            }
            return result;
        }
    }
}


enum Operation {
    Add,
    Remove,
    Contains
}

class WeightedOperation {	//Randomly Generate Next Operation
    double containsP;
    double addP;

    private final Random random;

    public WeightedOperation(Random random, double pC, double pA) {
        this.random = random;
        this.containsP = pC;
        this.addP = pA;
    }

    public Operation nextOperation() {
        double value = random.nextDouble();

        if (value < containsP) {
            return Operation.Contains;
        } else if (value < containsP + addP) {
            return Operation.Add;
        } else {
            return Operation.Remove;
        }
    }

}