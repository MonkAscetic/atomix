package io.atomix.server.protocol.impl;

import java.util.concurrent.CompletableFuture;

import com.google.protobuf.Message;
import io.atomix.server.protocol.ProtocolClient;
import io.atomix.service.client.ServiceClient;
import io.atomix.service.operation.OperationId;
import io.atomix.service.protocol.CommandRequest;
import io.atomix.service.protocol.CommandResponse;
import io.atomix.service.protocol.CreateRequest;
import io.atomix.service.protocol.DeleteRequest;
import io.atomix.service.protocol.QueryRequest;
import io.atomix.service.protocol.QueryResponse;
import io.atomix.service.protocol.RequestContext;
import io.atomix.service.protocol.ResponseContext;
import io.atomix.service.protocol.ServiceId;
import io.atomix.service.protocol.ServiceRequest;
import io.atomix.service.protocol.ServiceResponse;
import io.atomix.service.protocol.StreamContext;
import io.atomix.service.protocol.StreamResponse;
import io.atomix.service.util.ByteArrayDecoder;
import io.atomix.service.util.ByteBufferDecoder;
import io.atomix.service.util.ByteStringEncoder;
import io.atomix.utils.concurrent.Futures;
import io.atomix.utils.stream.StreamHandler;
import org.apache.commons.lang3.tuple.Pair;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default primitive client.
 */
public class DefaultServiceClient implements ServiceClient {
  private final ServiceId serviceId;
  private final ProtocolClient client;

  public DefaultServiceClient(ServiceId serviceId, ProtocolClient client) {
    this.serviceId = checkNotNull(serviceId);
    this.client = checkNotNull(client);
  }

  @Override
  public String name() {
    return serviceId.getName();
  }

  @Override
  public String type() {
    return serviceId.getType();
  }

  @Override
  public CompletableFuture<Void> create() {
    return client.command(ServiceRequest.newBuilder()
        .setId(serviceId)
        .setCreate(CreateRequest.newBuilder().build())
        .build()
        .toByteArray())
        .thenApply(v -> null);
  }

  @Override
  public CompletableFuture<Void> delete() {
    return client.command(ServiceRequest.newBuilder()
        .setId(serviceId)
        .setDelete(DeleteRequest.newBuilder().build())
        .build()
        .toByteArray())
        .thenApply(v -> null);
  }

  @Override
  public <T extends Message, U extends Message> CompletableFuture<Pair<ResponseContext, U>> execute(
      OperationId operation,
      RequestContext context,
      T request,
      ByteStringEncoder<T> encoder,
      ByteBufferDecoder<U> decoder) {
    switch (operation.type()) {
      case COMMAND:
        return command(operation, context, request, encoder, decoder);
      case QUERY:
        return query(operation, context, request, encoder, decoder);
      default:
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }
  }

  private <T extends Message, U extends Message> CompletableFuture<Pair<ResponseContext, U>> command(
      OperationId operation,
      RequestContext context,
      T request,
      ByteStringEncoder<T> encoder,
      ByteBufferDecoder<U> decoder) {
    return client.command(ServiceRequest.newBuilder()
        .setId(serviceId)
        .setCommand(CommandRequest.newBuilder()
            .setName(operation.id())
            .setCommand(ByteStringEncoder.encode(request, encoder))
            .setContext(context)
            .build()
            .toByteString())
        .build()
        .toByteArray())
        .thenApply(response -> ByteArrayDecoder.decode(response, ServiceResponse::parseFrom))
        .thenApply(response -> ByteBufferDecoder.decode(response.getCommand().asReadOnlyByteBuffer(), CommandResponse::parseFrom))
        .thenApply(response -> Pair.of(response.getContext(), ByteBufferDecoder.decode(response.getOutput().asReadOnlyByteBuffer(), decoder)));
  }

  private <T extends Message, U extends Message> CompletableFuture<Pair<ResponseContext, U>> query(
      OperationId operation,
      RequestContext context,
      T request,
      ByteStringEncoder<T> encoder,
      ByteBufferDecoder<U> decoder) {
    return client.query(ServiceRequest.newBuilder()
        .setId(serviceId)
        .setQuery(QueryRequest.newBuilder()
            .setName(operation.id())
            .setQuery(ByteStringEncoder.encode(request, encoder))
            .setContext(context)
            .build()
            .toByteString())
        .build()
        .toByteArray())
        .thenApply(response -> ByteArrayDecoder.decode(response, ServiceResponse::parseFrom))
        .thenApply(response -> ByteBufferDecoder.decode(response.getQuery().asReadOnlyByteBuffer(), QueryResponse::parseFrom))
        .thenApply(response -> Pair.of(response.getContext(), ByteBufferDecoder.decode(response.getOutput().asReadOnlyByteBuffer(), decoder)));
  }

  @Override
  public <T extends Message, U extends Message> CompletableFuture<Void> execute(
      OperationId operation,
      RequestContext context,
      T request,
      ByteStringEncoder<T> encoder,
      StreamHandler<Pair<StreamContext, U>> handler,
      ByteBufferDecoder<U> decoder) {
    switch (operation.type()) {
      case COMMAND:
        return command(operation, context, request, encoder, handler, decoder);
      case QUERY:
        return query(operation, context, request, encoder, handler, decoder);
      default:
        return Futures.exceptionalFuture(new UnsupportedOperationException());
    }
  }

  private <T extends Message, U extends Message> CompletableFuture<Void> command(
      OperationId operation,
      RequestContext context,
      T request,
      ByteStringEncoder<T> encoder,
      StreamHandler<Pair<StreamContext, U>> handler,
      ByteBufferDecoder<U> decoder) {
    return client.command(ServiceRequest.newBuilder()
        .setId(serviceId)
        .setCommand(CommandRequest.newBuilder()
            .setName(operation.id())
            .setCommand(ByteStringEncoder.encode(request, encoder))
            .setContext(context)
            .build()
            .toByteString())
        .build()
        .toByteArray(), new StreamHandler<byte[]>() {
      @Override
      public void next(byte[] value) {
        ServiceResponse serviceResponse = ByteArrayDecoder.decode(value, ServiceResponse::parseFrom);
        StreamResponse streamResponse = ByteBufferDecoder.decode(serviceResponse.getCommand().asReadOnlyByteBuffer(), StreamResponse::parseFrom);
        U response = ByteBufferDecoder.decode(streamResponse.getOutput().asReadOnlyByteBuffer(), decoder);
        handler.next(Pair.of(streamResponse.getContext(), response));
      }

      @Override
      public void complete() {
        handler.complete();
      }

      @Override
      public void error(Throwable error) {
        handler.error(error);
      }
    });
  }

  private <T extends Message, U extends Message> CompletableFuture<Void> query(
      OperationId operation,
      RequestContext context,
      T request,
      ByteStringEncoder<T> encoder,
      StreamHandler<Pair<StreamContext, U>> handler,
      ByteBufferDecoder<U> decoder) {
    return client.query(ServiceRequest.newBuilder()
        .setId(serviceId)
        .setQuery(QueryRequest.newBuilder()
            .setName(operation.id())
            .setQuery(ByteStringEncoder.encode(request, encoder))
            .setContext(context)
            .build()
            .toByteString())
        .build()
        .toByteArray(), new StreamHandler<byte[]>() {
      @Override
      public void next(byte[] value) {
        ServiceResponse serviceResponse = ByteArrayDecoder.decode(value, ServiceResponse::parseFrom);
        StreamResponse streamResponse = ByteBufferDecoder.decode(serviceResponse.getQuery().asReadOnlyByteBuffer(), StreamResponse::parseFrom);
        U response = ByteBufferDecoder.decode(streamResponse.getOutput().asReadOnlyByteBuffer(), decoder);
        handler.next(Pair.of(streamResponse.getContext(), response));
      }

      @Override
      public void complete() {
        handler.complete();
      }

      @Override
      public void error(Throwable error) {
        handler.error(error);
      }
    });
  }
}
