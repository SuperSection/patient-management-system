package com.supersection.billingservice.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.supersection.grpc.BillingRequest;
import com.supersection.grpc.BillingResponse;
import com.supersection.grpc.BillingServiceGrpc.BillingServiceImplBase;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

  private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

  @Override
  public void createBillingAccount(
      BillingRequest billingRequest,
      StreamObserver<BillingResponse> responseObserver
  ) {
    log.info("createBillingAccount request received {}", billingRequest.toString());

    // Business logic - e.g. save to database, perform caculates etc.

    BillingResponse response = BillingResponse.newBuilder()
        .setAccountId("123abc")
        .setStatus("ACTIVE")
        .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
