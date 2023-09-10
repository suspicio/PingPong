package io.grpc.examples.helloworld;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.utils.SingletonInstance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class HelloWorldClient {
  private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

  private final GreeterGrpc.GreeterBlockingStub blockingStub;

  /** Construct client for accessing HelloWorld server using the existing channel. */
  public HelloWorldClient(Channel channel) {
    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
    blockingStub = GreeterGrpc.newBlockingStub(channel);
  }

  public void op() {
    String mess = "a".repeat(128);
    HelloRequest request = HelloRequest.newBuilder().setName(mess).build();
    HelloReply response;
    try {
      Instant start = Instant.now();
      response = blockingStub.sayHello(request);
      Duration duration = Duration.between(start, Instant.now());
      SingletonInstance.timeSpans.add(duration.toMillis());
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
  }

  public void writeData() throws IOException {

    // Create a File object
    String filePath = "data.txt";
    File file = new File(filePath);

    // Create a FileWriter object with append mode (true) or overwrite mode (false)
    FileWriter fileWriter = new FileWriter(file, false); // Set to true for append mode

    // Create a BufferedWriter for efficient writing
    BufferedWriter writer = new BufferedWriter(fileWriter);

    StringBuilder stringBuilder = new StringBuilder();

    // Iterate through the ArrayList and convert each Duration to a string
    for (Long duration : SingletonInstance.timeSpans) {
      // Convert the duration to a string in the format "PT1H30M" (for example)
      String durationString = Long.toString(duration);

      // Append the duration string followed by a newline character
      stringBuilder.append(durationString).append("\n");
    }

    // Sort the latency data in ascending order
    Collections.sort(SingletonInstance.timeSpans);

    // Compute average latency
    double average = SingletonInstance.timeSpans.stream().mapToLong(Long::longValue).average().orElse(0.0);

    // Compute median latency
    long median;
    int dataSize = SingletonInstance.timeSpans.size();
    if (dataSize % 2 == 0) {
      median = (SingletonInstance.timeSpans.get(dataSize / 2 - 1) + SingletonInstance.timeSpans.get(dataSize / 2)) / 2;
    } else {
      median = SingletonInstance.timeSpans.get(dataSize / 2);
    }

    // Compute 99th percentile latency
    int percentile99Index = (int) Math.ceil(0.99 * dataSize) - 1;
    long percentile99 = SingletonInstance.timeSpans.get(percentile99Index);

    // Compute 99.9th percentile latency
    int percentile999Index = (int) Math.ceil(0.999 * dataSize) - 1;
    long percentile999 = SingletonInstance.timeSpans.get(percentile999Index);

    System.out.println("Average Latency: " + average);
    System.out.println("Median Latency: " + median);
    System.out.println("99th Percentile Latency: " + percentile99);
    System.out.println("99.9th Percentile Latency: " + percentile999);


    stringBuilder.append("Average Latency: ").append(average).append("\n");
    stringBuilder.append("Median Latency: ").append(median).append("\n");
    stringBuilder.append("99th Percentile Latency: ").append(percentile99).append("\n");
    stringBuilder.append("99.9th Percentile Latency: ").append(percentile999).append("\n");


    // Write data to the file
    writer.write(stringBuilder.toString());

    // Close the BufferedWriter to flush and close the file
    writer.close();

    SingletonInstance.timeSpans.clear();

    System.out.println("Data has been written to the file successfully.");
  }


  /** Say hello to server. */
  public void greet(Integer left) throws IOException {
    while (left-- > 0) {
      op();
    }

    writeData();
  }

  /**
   * Greet server. If provided, the first element of {@code args} is the name to use in the
   * greeting. The second argument is the target server.
   */
  public static void main(String[] args) throws Exception {
    String user = "world";
    // Access a service running on the local machine on port 50051
    String target = "4.236.182.54:50051";
    // Allow passing in the user and target strings as command line arguments
    if (args.length > 0) {
      if ("--help".equals(args[0])) {
        System.err.println("Usage: [name [target]]");
        System.err.println("");
        System.err.println("  name    The name you wish to be greeted by. Defaults to " + user);
        System.err.println("  target  The server to connect to. Defaults to " + target);
        System.exit(1);
      }
      user = args[0];
    }
    if (args.length > 1) {
      target = args[1];
    }

    // Create a communication channel to the server, known as a Channel. Channels are thread-safe
    // and reusable. It is common to create channels at the beginning of your application and reuse
    // them until the application shuts down.
    //
    // For the example we use plaintext insecure credentials to avoid needing TLS certificates. To
    // use TLS, use TlsChannelCredentials instead.
    ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
        .build();
    try {
      HelloWorldClient client = new HelloWorldClient(channel);
      while (true) {
        client.greet(10000);
      }
    } finally {
      // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
      // resources the channel should be shut down when it will no longer be used. If it may be used
      // again leave it running.
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
