package io.grpc.leaderless;

import io.grpc.Channel;
import io.grpc.leaderless.database.*;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class Client extends Thread {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private final DatabaseRequestsGrpc.DatabaseRequestsBlockingStub blockingStub;

    public Client(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = DatabaseRequestsGrpc.newBlockingStub(channel);
    }

    public Callable<Boolean> create(String name, String data) {
        CreateRequest createRequest = CreateRequest.newBuilder().setName(name).setData(data).build();
        CreateReply createReply = blockingStub.create(createRequest);
        return createReply::getCreated;
    }

    public Callable<Boolean> update(String name, String data) {
        UpdateRequest updateRequest = UpdateRequest.newBuilder().setName(name).setData(data).build();
        UpdateReply updateReply = blockingStub.update(updateRequest);
        return updateReply::getUpdated;
    }

    public Callable<Boolean> delete(String name) {
        DeleteRequest deleteRequest = DeleteRequest.newBuilder().setName(name).build();
        DeleteReply deleteReply = blockingStub.delete(deleteRequest);
        return deleteReply::getDeleted;
    }

    public Callable<String> get(String name) {
        GetRequest getRequest = GetRequest.newBuilder().setName(name).build();
        GetReply getReply = blockingStub.get(getRequest);
        return getReply::getData;
    }

}
