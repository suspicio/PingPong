package io.grpc.leaderless;

import io.grpc.ManagedChannel;
import io.grpc.leaderless.database.GetReply;

import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class Coordinator implements Runnable {
    private static final Logger logger = Logger.getLogger(Coordinator.class.getName());
    private ExecutorService executorService;
    private ArrayList<Client> clients = new ArrayList<>();
    private String mode = "Seconds";
    private Integer ID = 0;
    private Long seconds = 0L;
    private Long op = 0L;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setID(Integer id) {
        this.ID = id;
    }

    public Long getSeconds() {
        return seconds;
    }

    public void setSeconds(Long seconds) {
        this.seconds = seconds;
    }

    public Long getOp() {
        return op;
    }

    public void setOp(Long op) {
        this.op = op;
    }

    public Coordinator(ArrayList<ManagedChannel> channels) {
        for (ManagedChannel channel : channels) {
            clients.add(new Client(channel));
        }

        executorService = Executors.newFixedThreadPool(clients.size());
    }

    public boolean create(String name, String data) throws ExecutionException, InterruptedException {
        int totalClients = clients.size();
        int majority = totalClients / 2 + 1; // Majority of clients needed

        ArrayList<Future<Boolean>> futures = new ArrayList<>();
        CountDownLatch finishedLatch = new CountDownLatch(totalClients);
        CountDownLatch majorityFinished = new CountDownLatch(majority);

        for (Client client : clients) {
            Future<Boolean> future = executorService.submit(() -> {
                boolean result = client.create(name, data).call();
                finishedLatch.countDown();
                majorityFinished.countDown();
                return result;
            });
            futures.add(future);
        }

        // Wait for a majority of clients to finish
        majorityFinished.await();

        int successfulCount = 0;

        for (Future<Boolean> future : futures) {
            if (future.isDone()) {
                if (future.get()) {
                    successfulCount++;
                }
            }
        }

        // Check if the majority of clients were successful
        if (successfulCount >= majority) {
            return true;
        }

        finishedLatch.await();

        successfulCount = 0;


        for (Future<Boolean> future : futures) {
            if (future.isDone()) {
                if (future.get()) {
                    successfulCount++;
                }
            }
        }

        // Check if the majority of clients were successful
        return successfulCount >= majority;
    }

    public boolean update(String name, String data) throws ExecutionException, InterruptedException {
        int totalClients = clients.size();
        int majority = totalClients / 2 + 1; // Majority of clients needed

        ArrayList<Future<Boolean>> futures = new ArrayList<>();
        CountDownLatch finishedLatch = new CountDownLatch(totalClients);
        CountDownLatch majorityFinished = new CountDownLatch(majority);

        for (Client client : clients) {
            Future<Boolean> future = executorService.submit(() -> {
                boolean result = client.update(name, data).call();
                finishedLatch.countDown();
                majorityFinished.countDown();
                return result;
            });
            futures.add(future);
        }

        // Wait for a majority of clients to finish
        majorityFinished.await();

        int successfulCount = 0;

        for (Future<Boolean> future : futures) {
            if (future.isDone()) {
                if (future.get()) {
                    successfulCount++;
                }
            }
        }

        // Check if the majority of clients were successful
        if (successfulCount >= majority) {
            return true;
        }

        finishedLatch.await();

        successfulCount = 0;


        for (Future<Boolean> future : futures) {
            if (future.isDone()) {
                if (future.get()) {
                    successfulCount++;
                }
            }
        }

        // Check if the majority of clients were successful
        return successfulCount >= majority;
    }

    public boolean get(String name) throws ExecutionException, InterruptedException {
        int totalClients = clients.size();
        int majority = totalClients / 2 + 1; // Majority of clients needed

        ArrayList<Future<String>> futures = new ArrayList<>();
        CountDownLatch finishedLatch = new CountDownLatch(totalClients);
        CountDownLatch majorityFinished = new CountDownLatch(majority);

        for (Client client : clients) {
            Future<String> future = executorService.submit(() -> {
                String result = client.get(name).call();
                finishedLatch.countDown();
                majorityFinished.countDown();
                return result;
            });
            futures.add(future);
        }

        int successfulCount = 0;
        // Wait for a majority of clients to finish
        majorityFinished.await();

        String first = futures.get(0).get();

        for (Future<String> future : futures) {
            if (future.isDone()) {
                if (first.equals(future.get())) {
                    successfulCount++;
                }
            }
        }

        if (successfulCount >= majority) {
            return true;
        } else {
            System.out.print("[error in " + this.ID + " coordinator] ");
            System.out.println("There was a mismatch: " + successfulCount);

            for (Future<String> future : futures) {
                if (future.isDone()) {
                    System.out.print(future.get() + ", ");
                }
            }

            System.out.println();
        }

        finishedLatch.await();

        first = futures.get(0).get();
        successfulCount = 0;

        for (Future<String> future : futures) {
            if (future.isDone()) {
                if (first.equals(future.get())) {
                    successfulCount++;
                }
            }
        }

        // Check if the majority of clients were successful
        return successfulCount >= majority;
    }

    public boolean delete(String name) throws ExecutionException, InterruptedException {
        int totalClients = clients.size();
        int majority = totalClients / 2 + 1; // Majority of clients needed

        ArrayList<Future<Boolean>> futures = new ArrayList<>();
        CountDownLatch finishedLatch = new CountDownLatch(totalClients);
        CountDownLatch majorityFinished = new CountDownLatch(majority);

        for (Client client : clients) {
            Future<Boolean> future = executorService.submit(() -> {
                boolean result = client.delete(name).call();
                finishedLatch.countDown();
                majorityFinished.countDown();
                return result;
            });
            futures.add(future);
        }

        // Wait for a majority of clients to finish
        majorityFinished.await();

        int successfulCount = 0;

        for (Future<Boolean> future : futures) {
            if (future.isDone()) {
                if (future.get()) {
                    successfulCount++;
                }
            }
        }

        // Check if the majority of clients were successful
        if (successfulCount >= majority) {
            return true;
        }

        finishedLatch.await();

        successfulCount = 0;


        for (Future<Boolean> future : futures) {
            if (future.isDone()) {
                if (future.get()) {
                    successfulCount++;
                }
            }
        }

        // Check if the majority of clients were successful
        return successfulCount >= majority;
    }

    @Override
    public void run() {
        Random random = new Random();
        long time = 0;
        if (Objects.equals(mode, "Operations")) {
            for (int i = 0; i < op; i++) {
                double rand = random.nextDouble();
                Instant start = Instant.now();
                if (rand > 0.95) {
                    try {
                        create("i" + (i % 100), "j" + (i % 10 + random.nextInt() % 10));
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                } else if (rand < 0.05) {
                    try {
                        delete("i" + (i % 100));
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else if (rand < 0.5) {
                    try {
                        Boolean getReply = get("i" + (i % 100));
                        if (!getReply)
                            System.out.print(false);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        update("i" + (i % 100), "j" + (i % 10 + random.nextInt() % 10));
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                Duration duration = Duration.between(start, Instant.now());
                time += duration.getNano();
            }
            System.out.println(time / op);
        }
    }
}
