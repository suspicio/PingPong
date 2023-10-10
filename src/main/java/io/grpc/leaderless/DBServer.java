/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.leaderless;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.leaderless.database.*;
import io.grpc.leaderless.utils.SingletonInstance;
import io.grpc.stub.StreamObserver;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class DBServer {
  private static final Logger logger = Logger.getLogger(DBServer.class.getName());

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;
    server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
        .addService(new DatabaseImpl())
        .build()
        .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        try {
          DBServer.this.stop();
        } catch (InterruptedException e) {
          e.printStackTrace(System.err);
        }
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final DBServer server = new DBServer();
    server.start();
    server.blockUntilShutdown();
  }

  static class DatabaseImpl extends DatabaseRequestsGrpc.DatabaseRequestsImplBase {

    @Override
    public void get(GetRequest req, StreamObserver<GetReply> responseObserver) {
      String replyData = SingletonInstance.dbTable.get(req.getName());
      GetReply reply = GetReply.newBuilder().setName(req.getName()).setData(replyData).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }

    @Override
    public void update(UpdateRequest req, StreamObserver<UpdateReply> responseObserver) {
      try {
        SingletonInstance.dbTable.put(req.getName(), req.getData());
        UpdateReply reply = UpdateReply.newBuilder().setUpdated(true).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
      } catch (Exception e) {
        logger.severe(e.getMessage());
        UpdateReply reply = UpdateReply.newBuilder().setUpdated(false).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
      }
    }

    @Override
    public void delete(DeleteRequest req, StreamObserver<DeleteReply> responseObserver) {
      try {
        SingletonInstance.dbTable.remove(req.getName());
        DeleteReply reply = DeleteReply.newBuilder().setDeleted(true).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
      } catch (Exception e) {
        logger.severe(e.getMessage());
        DeleteReply reply = DeleteReply.newBuilder().setDeleted(false).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
      }
    }

    @Override
    public void create(CreateRequest req, StreamObserver<CreateReply> responseObserver) {
      try {
        if (SingletonInstance.dbTable.containsKey(req.getName())) {
          throw new KeyAlreadyExistsException("The record with this Key already exists, please use update method.");
        }
        SingletonInstance.dbTable.putIfAbsent(req.getName(), req.getData());
        CreateReply reply = CreateReply.newBuilder().setCreated(true).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
      } catch (Exception e) {
        logger.severe(e.getMessage());
        CreateReply reply = CreateReply.newBuilder().setCreated(false).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
      }
    }
  }
}
