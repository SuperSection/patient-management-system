package com.supersection.patientservice.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.supersection.grpc.BillingRequest;
import com.supersection.grpc.BillingResponse;
import com.supersection.grpc.BillingServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Service
public class BillingServiceGrpcClient {

  private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;
  private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);

  public BillingServiceGrpcClient(
    @Value("${billing.service.address:localhost}") String serverAddress,
    @Value("${billing.service.Grpc.port:9001}") int serverPort
  ) {
    log.info("Connecting to Billing Service GRPC service at {}:{}", serverAddress, serverPort);

    ManagedChannel channel = ManagedChannelBuilder
        .forAddress(serverAddress, serverPort)
        .usePlaintext().build();

    blockingStub = BillingServiceGrpc.newBlockingStub(channel);
  }

  public BillingResponse createBillingAccount(
      String patientId,
      String name,
      String email
  ) {
    BillingRequest request = BillingRequest.newBuilder()
            .setPatientId(patientId)
            .setName(name)
            .setEmail(email)
            .build();

    BillingResponse response = blockingStub.createBillingAccount(request);
    log.info("Received response from billing service via GRPC: {}", response);
    return response;
  }
}
