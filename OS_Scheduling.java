import java.util.*;

/**
 * Assignment to simulate OS scheduling algorithms:
 * 1. FCFS (first come first serve)
 * 2. SJF (shortest job first)
 * 3. SRT (shortest remaining time first)
 */

public class Assignment2 {
    private static int K;       // all processes arrive within time [0-k] uniform random
    private static double D;    // CPU times of processes have average D, and
    private static double V;    // sigma V from a Gaussian distribution
    private static int N;       // number of processes

    private static final boolean DEBUG = false;  // set to false to see less output

    private static final Random rand = new Random();
    //private static Random rand = new Random(1234);  // use this for reproducible Randomness..

    // 3 copies of the same system - for 3 different scheduling algos.
    private static int[][] system1;
    private static int[][] system2;
    private static int[][] system3;

    public static void main(String[] args) {
        // part 1: get all params (K, D, V, N)

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter max arrival time for all processes (k): ");
        Assignment2.K = sc.nextInt();

        System.out.print("Enter Avg. CPU time (D): ");
        Assignment2.D = sc.nextDouble();

        System.out.print("Enter Std Dev. of CPU times (V): ");
        Assignment2.V = sc.nextDouble();

        System.out.print("Enter number of processes (N): ");
        Assignment2.N = sc.nextInt();

        if (!validParams()) {
            System.out.print("One or more simulation params are invalid");
            return;
        }

        // Now all params are valid; initialize the system
        initSystem();     // inits 3 copies: system1, system2 and system3
        simulateFCFS();   // simulates on system1
        simulateSJF();    // system2
        simulateSRT();    // system3

        printResults("FCFS", system1);
        printResults("SJF", system2);
        printResults("SRT", system3);
    }

    /**
     * Are the simulation params valid?
     * We just check if D, V, N > 0, K > 0
     * @return true if params are valid; false otherwise.
     */
    private static boolean validParams() {
        if (D <= 0 || V <= 0 || N <= 0 || K <= 0) {
            System.out.println("D, V, N and K must be > 0");
            return false;
        }
        return true;
    }

    /**
     * Initialize the system based on the simulation params.
     * For each of the N processes; the system will have the following info:
     * Active          : 0/1 (1 at process arrival and 0 on termination)
     * Arrival Time    : Uniform Random in [0, K)
     * Total CPU time  : CPU time with mean D, deviation V (gaussian distribution)
     * Left CPU time   : initially Total CPU time
     * Turnaround time : Finish time - arrival time (computed at the end)
     */
    private static void initSystem() {
        system1 = new int[N][5];
        system2 = new int[N][5];
        system3 = new int[N][5];

        for (int i = 0; i < N; i++) {
            int active = 1;
            int arrivalTime  = rand.nextInt(K);  // 0 <= AT < K

            //https://stackoverflow.com/questions/31754209/can-random-nextgaussian-sample-values-from-a-distribution-with-different-mean
            int totalCPUTime = (int) (rand.nextGaussian()*V + D) + 1;  // add 1 so CPU time is never 0

            system1[i][0] = active;
            system1[i][1] = arrivalTime;
            system1[i][2] = totalCPUTime;
            system1[i][3] = totalCPUTime;
            system1[i][4] = -1;    // turnaround time - will be computed after sim

            // copy the same in system2 and system3 - used for
            // 3 different scheduling algorithms used
            system2[i][0] = active;
            system2[i][1] = arrivalTime;
            system2[i][2] = totalCPUTime;
            system2[i][3] = totalCPUTime;
            system2[i][4] = -1;    // turnaround time - will be computed after sim

            system3[i][0] = active;
            system3[i][1] = arrivalTime;
            system3[i][2] = totalCPUTime;
            system3[i][3] = totalCPUTime;
            system3[i][4] = -1;    // turnaround time - will be computed after sim
        }
    }

    // simulate FCFS on system1
    private static void simulateFCFS() {
        int t = 0;   // simulation time step
        int currentProcess = -1;
        LinkedList<Integer> queue = new LinkedList<>();
        int[][] system = system1;

        while (anyRemaining(system)) {
            // FCFS: the process that arrived first will run first.

            // check if any process arrived?
            for (int i = 0; i < N; i++) {
                // this process arrives at current time
                // if multiple processes arrive at the same time,
                // the one with smaller i goes first
                int arrTime = system[i][1];
                if (t == arrTime) {
                    queue.addLast(i);
                }
            }

            if (currentProcess < 0) {
                // no process is running. Check FIFO queue?
                if (!queue.isEmpty()) {
                    currentProcess = queue.removeFirst();
                } else {
                    // no process has arrived yet
                    t++;
                    continue;
                }
            }

            if (DEBUG) {
                System.out.printf("Time: %d Process: %d\n", t, currentProcess);
            }

            // current process runs for 1 time step
            system[currentProcess][3]--;                      // decrement remaining time

            if (system[currentProcess][3] == 0) {             // process finished?
                int arrTime = system[currentProcess][1];
                system[currentProcess][4] = t - arrTime + 1;  // turnaround time
                system[currentProcess][0] = 0;                // inactive
                currentProcess = -1;
            }
            t++;
        }
    }

    // returns true if any Ri != 0 in the system; false otherwise
    private static boolean anyRemaining(int[][] system) {
        for (int i = 0; i < N; i++) {
            if (system[i][3] > 0) {  // [i][3] is RemainingTime_i
                return true;
            }
        }
        return false;
    }

    // SJF: the shortest job always runs first (non-preemptive)
    private static void simulateSJF() {
        int t = 0;
        int currentProcess = -1;
        LinkedList<Integer> queue = new LinkedList<>();
        int[][] system = system2;

        while (anyRemaining(system)) {
            if (t > 100) break;
            // check if any process arrived?
            for (int i = 0; i < N; i++) {
                int arrTime = system[i][1];
                if (t == arrTime) {
                    queue.addLast(i);
                }
            }

            // SJF: select the job with shortest CPU time
            if (currentProcess < 0) {
                if (!queue.isEmpty()) {
                    int leastCPU = Integer.MAX_VALUE;
                    int shortestJob = -1;
                    for (int p : queue) {
                        int CPU = system[p][2];
                        if (CPU < leastCPU) {
                            shortestJob = p;
                            leastCPU = CPU;
                        }
                    }
                    if (shortestJob != -1) {
                        currentProcess = shortestJob;
                        queue.removeFirstOccurrence(shortestJob);
                    }
                } else {
                    // no process has arrived yet
                    t++;
                    continue;
                }
            }

            if (DEBUG) {
                System.out.printf("Time: %d Process: %d\n", t, currentProcess);
            }

            // current process runs for 1 time step
            system[currentProcess][3]--;                      // decrement remaining time

            if (system[currentProcess][3] == 0) {             // process finished?
                int arrTime = system[currentProcess][1];
                system[currentProcess][4] = t - arrTime + 1;  // turnaround time
                system[currentProcess][0] = 0;                // inactive
                currentProcess = -1;
            }
            t++;
        }
    }

    // SRT: the process with least remaining time runs next (preemptive)
    private static void simulateSRT() {
        int t = 0;
        int currentProcess = -1;
        LinkedList<Integer> queue = new LinkedList<>();
        int[][] system = system3;

        while (anyRemaining(system)) {

            // check if any process arrived?
            for (int i = 0; i < N; i++) {
                int arrTime = system[i][1];
                if (t == arrTime) {
                    queue.addLast(i);
                }
            }

            // this is preemptive: at every time step chose the
            // process with the least rem. time.
            int leastRemTime = Integer.MAX_VALUE;
            int leastRemJob  = -1;
            for (int p : queue) {
                int remTime = system[p][3];
                if (remTime < leastRemTime) {
                    leastRemTime = remTime;
                    leastRemJob = p;
                }
            }

            if (leastRemJob >= 0) {
                queue.removeFirstOccurrence(leastRemJob);

                if (currentProcess >= 0 && currentProcess != leastRemJob) {
                    // currently running process got preempted
                    queue.addLast(currentProcess);
                }
                currentProcess = leastRemJob;
            }

            if (currentProcess < 0) {
                // processes haven't arrived yet
                t++;
                continue;
            }

            if (DEBUG) {
                System.out.printf("Time: %d Process: %d\n", t, currentProcess);
            }

            // current process runs for 1 time step
            system[currentProcess][3]--;                      // decrement remaining time

            if (system[currentProcess][3] == 0) {             // process finished?
                int arrTime = system[currentProcess][1];
                system[currentProcess][4] = t - arrTime + 1;  // turnaround time
                system[currentProcess][0] = 0;                // inactive
                currentProcess = -1;
            }
            t++;
        }
    }

    private static void printResults(String algo, int[][] system) {
        int total = 0;
        System.out.printf("\n**** %s ****\n", algo);
        System.out.println("No.| A| Arr| CPU| Rem| TT |");
        System.out.println("---------------------------");
        for (int i = 0; i < N; i++) {
            int active = system[i][0];
            int arrTim = system[i][1];
            int CPUTim = system[i][2];
            int remTim = system[i][3];
            int TTTime = system[i][4];
            total += TTTime;
            System.out.printf("%-3d %1d %4d %4d %4d %4d\n", i, active, arrTim, CPUTim, remTim, TTTime);
        }
        System.out.printf("Average TT: %.4f\n", total * 1.0 / N);
    }
}