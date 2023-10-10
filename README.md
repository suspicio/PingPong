gRPC Leaderless
==============================================

The leaderless require `grpc-java` to already be built. You are strongly encouraged
to check out a git release tag, since there will already be a build of gRPC
available. Otherwise you must follow [COMPILING](../COMPILING.md).

You may want to read through the
[Quick Start](https://grpc.io/docs/languages/java/quickstart)
before trying out the leaderless.

## Basic leaderless

- [Hello world](../../../lab1/src/main/java/io/grpc/leaderless/helloworld)

- [Route guide](../../../lab1/src/main/java/io/grpc/leaderless/routeguide)

- [Metadata](../../../lab1/src/main/java/io/grpc/leaderless/header)

- [Error handling](../../../lab1/src/main/java/io/grpc/leaderless/errorhandling)

- [Compression](../../../lab1/src/main/java/io/grpc/leaderless/experimental)

- [Flow control](../../../lab1/src/main/java/io/grpc/leaderless/manualflowcontrol)

- [Wait For Ready](../../../lab1/src/main/java/io/grpc/leaderless/waitforready)

- [Json serialization](../../../lab1/src/main/java/io/grpc/leaderless/advanced)

- <details>
  <summary>Hedging</summary>

  The [hedging example](../../../lab1/src/main/java/io/grpc/leaderless/hedging) demonstrates that enabling hedging
  can reduce tail latency. (Users should note that enabling hedging may introduce other overhead;
  and in some scenarios, such as when some server resource gets exhausted for a period of time and
  almost every RPC during that time has high latency or fails, hedging may make things worse.
  Setting a throttle in the service config is recommended to protect the server from too many
  inappropriate retry or hedging requests.)

  The server and the client in the example are basically the same as those in the
  [hello world](../../../lab1/src/main/java/io/grpc/leaderless/helloworld) example, except that the server mimics a
  long tail of latency, and the client sends 2000 requests and can turn on and off hedging.

  To mimic the latency, the server randomly delays the RPC handling by 2 seconds at 10% chance, 5
  seconds at 5% chance, and 10 seconds at 1% chance.

  When running the client enabling the following hedging policy

  ```json
        "hedgingPolicy": {
          "maxAttempts": 3,
          "hedgingDelay": "1s"
        }
  ```
  Then the latency summary in the client log is like the following

  ```text
  Total RPCs sent: 2,000. Total RPCs failed: 0
  [Hedging enabled]
  ========================
  50% latency: 0ms
  90% latency: 6ms
  95% latency: 1,003ms
  99% latency: 2,002ms
  99.9% latency: 2,011ms
  Max latency: 5,272ms
  ========================
  ```

  See [the section below](#to-build-the-leaderless) for how to build and run the example. The
  executables for the server and the client are `hedging-hello-world-server` and
  `hedging-hello-world-client`.

  To disable hedging, set environment variable `DISABLE_HEDGING_IN_HEDGING_EXAMPLE=true` before
  running the client. That produces a latency summary in the client log like the following

  ```text
  Total RPCs sent: 2,000. Total RPCs failed: 0
  [Hedging disabled]
  ========================
  50% latency: 0ms
  90% latency: 2,002ms
  95% latency: 5,002ms
  99% latency: 10,004ms
  99.9% latency: 10,007ms
  Max latency: 10,007ms
  ========================
  ```

</details>

- <details>
  <summary>Retrying</summary>

  The [retrying example](../../../lab1/src/main/java/io/grpc/leaderless/retrying) provides a HelloWorld gRPC client &
  server which demos the effect of client retry policy configured on the [ManagedChannel](
  ../api/src/main/java/io/grpc/ManagedChannel.java) via [gRPC ServiceConfig](
  https://github.com/grpc/grpc/blob/master/doc/service_config.md). Retry policy implementation &
  configuration details are outlined in the [proposal](https://github.com/grpc/proposal/blob/master/A6-client-retries.md).

  This retrying example is very similar to the [hedging example](../../../lab1/src/main/java/io/grpc/leaderless/hedging) in its setup.
  The [RetryingHelloWorldServer](../../../lab1/src/main/java/io/grpc/leaderless/retrying/RetryingHelloWorldServer.java) responds with
  a status UNAVAILABLE error response to a specified percentage of requests to simulate server resource exhaustion and
  general flakiness. The [RetryingHelloWorldClient](../../../lab1/src/main/java/io/grpc/leaderless/retrying/RetryingHelloWorldClient.java) makes
  a number of sequential requests to the server, several of which will be retried depending on the configured policy in
  [retrying_service_config.json](../../../lab1/src/main/resources/io/grpc/leaderless/retrying/retrying_service_config.json). Although
  the requests are blocking unary calls for simplicity, these could easily be changed to future unary calls in order to
  test the result of request concurrency with retry policy enabled.

  One can experiment with the [RetryingHelloWorldServer](../../../lab1/src/main/java/io/grpc/leaderless/retrying/RetryingHelloWorldServer.java)
  failure conditions to simulate server throttling, as well as alter policy values in the [retrying_service_config.json](
  ../../../lab1/src/main/resources/io/grpc/leaderless/retrying/retrying_service_config.json) to see their effects. To disable retrying
  entirely, set environment variable `DISABLE_RETRYING_IN_RETRYING_EXAMPLE=true` before running the client.
  Disabling the retry policy should produce many more failed gRPC calls as seen in the output log.

  See [the section below](#to-build-the-leaderless) for how to build and run the example. The
  executables for the server and the client are `retrying-hello-world-server` and
  `retrying-hello-world-client`.

</details>

- <details>
  <summary>Health Service</summary>

  The [health service example](../../../lab1/src/main/java/io/grpc/leaderless/healthservice)
  provides a HelloWorld gRPC server that doesn't like short names along with a
  health service.  It also provides a client application which makes HelloWorld 
  calls and checks the health status.  

  The client application also shows how the round robin load balancer can
  utilize the health status to avoid making calls to a service that is
  not actively serving.
</details>


- [Keep Alive](../../../lab1/src/main/java/io/grpc/leaderless/keepalive)

### <a name="to-build-the-leaderless"></a> To build the leaderless

1. **[Install gRPC Java library SNAPSHOT locally, including code generation plugin](../COMPILING.md) (Only need this step for non-released versions, e.g. master HEAD).**

2. From grpc-java/leaderless directory:
```
$ ./gradlew installDist
```

This creates the scripts `hello-world-server`, `hello-world-client`,
`route-guide-server`, `route-guide-client`, etc. in the
`build/install/leaderless/bin/` directory that run the leaderless. Each
example requires the server to be running before starting the client.

For example, to try the hello world example first run:

```
$ ./build/install/leaderless/bin/hello-world-server
```

And in a different terminal window run:

```
$ ./build/install/leaderless/bin/hello-world-client
```

That's it!

For more information, refer to gRPC Java's [README](../README.md) and
[tutorial](https://grpc.io/docs/languages/java/basics).

### Maven

If you prefer to use Maven:
1. **[Install gRPC Java library SNAPSHOT locally, including code generation plugin](../COMPILING.md) (Only need this step for non-released versions, e.g. master HEAD).**

2. Run in this directory:
```
$ mvn verify
$ # Run the server
$ mvn exec:java -Dexec.mainClass=io.grpc.leaderless.DBServer
$ # In another terminal run the client
$ mvn exec:java -Dexec.mainClass=io.grpc.leaderless.BenchmarkClient
```

### Bazel

If you prefer to use Bazel:
```
$ bazel build :hello-world-server :hello-world-client
$ # Run the server
$ bazel-bin/hello-world-server
$ # In another terminal run the client
$ bazel-bin/hello-world-client
```

## Other leaderless

- [Android leaderless](android)

- Secure channel leaderless

  + [TLS leaderless](example-tls)

  + [ALTS leaderless](example-alts)

- [Google Authentication](example-gauth)

- [JWT-based Authentication](example-jwt-auth)

- [Pre-serialized messages](../../../lab1/src/main/java/io/grpc/leaderless/preserialized)

## Unit test leaderless

leaderless for unit testing gRPC clients and servers are located in [leaderless/src/test](../../../lab1/src/test).

In general, we DO NOT allow overriding the client stub and we DO NOT support mocking final methods
in gRPC-Java library. Users should be cautious that using tools like PowerMock or
[mockito-inline](https://search.maven.org/search?q=g:org.mockito%20a:mockito-inline) can easily
break this rule of thumb. We encourage users to leverage `InProcessTransport` as demonstrated in the
leaderless to write unit tests. `InProcessTransport` is light-weight and runs the server
and client in the same process without any socket/TCP connection.

Mocking the client stub provides a false sense of security when writing tests. Mocking stubs and responses
allows for tests that don't map to reality, causing the tests to pass, but the system-under-test to fail.
The gRPC client library is complicated, and accurately reproducing that complexity with mocks is very hard.
You will be better off and write less code by using `InProcessTransport` instead.

Example bugs not caught by mocked stub tests include:

* Calling the stub with a `null` message
* Not calling `close()`
* Sending invalid headers
* Ignoring deadlines
* Ignoring cancellation

For testing a gRPC client, create the client with a real stub
using an
[InProcessChannel](../core/src/main/java/io/grpc/inprocess/InProcessChannelBuilder.java),
and test it against an
[InProcessServer](../core/src/main/java/io/grpc/inprocess/InProcessServerBuilder.java)
with a mock/fake service implementation.

For testing a gRPC server, create the server as an InProcessServer,
and test it against a real client stub with an InProcessChannel.

The gRPC-java library also provides a JUnit rule,
[GrpcCleanupRule](../testing/src/main/java/io/grpc/testing/GrpcCleanupRule.java), to do the graceful
shutdown boilerplate for you.

## Even more leaderless

A wide variety of third-party leaderless can be found [here](https://github.com/saturnism/grpc-java-by-example).
