package io.grpc.leaderless;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BenchmarkClient {
    private static final Logger logger = Logger.getLogger(BenchmarkClient.class.getName());
    private static Integer parallelClients = 0;

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting. The second argument is the target server.
     */
    public static void main(String[] args) throws Exception {
        // Access a service running on the local machine on port 50051
        int databaseReplicasCount = 0;
        int coordinatorsNumber = 1;
        // Allow passing in the user and target strings as command line arguments
        if (args.length > 0) {
            parallelClients = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            databaseReplicasCount = Integer.parseInt(args[1]);
        }


        ArrayList<String> target = new ArrayList<>();

        if (args.length > databaseReplicasCount + 2) {
            coordinatorsNumber = databaseReplicasCount;
        }

        if (databaseReplicasCount == 0) {
            target.add("localhost:50051");
            target.add("localhost:50052");
            target.add("localhost:50053");
            databaseReplicasCount = 3;
        } else {
            target = new ArrayList<>(Arrays.asList(args).subList(2, databaseReplicasCount + 2));
        }
        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        //
        // For the example we use plaintext insecure credentials to avoid needing TLS certificates. To
        // use TLS, use TlsChannelCredentials instead.
        ArrayList<ManagedChannel> channels = new ArrayList<>();

        try {
            for (int i = 0; i < databaseReplicasCount; i++) {
                channels.add(Grpc.newChannelBuilder(target.get(i), InsecureChannelCredentials.create()).build());
            }
            ArrayList<Coordinator> coordinators = new ArrayList<>();

            for (int i = 0; i < parallelClients; i++) {
                final int id = i;
                new Thread(() -> {
                    Coordinator coordinator = new Coordinator(channels);
                    coordinator.setMode("Operations");
                    coordinator.setOp(10000L);
                    coordinator.setID(id);
                    coordinator.run();
                }).start();
            }
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            //for (ManagedChannel channel : channels)
              //  channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
