package com.project.product_service.grpc;

import inventory.InventoryRequest;
import inventory.InventoryResponse;
import inventory.InventoryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryServiceGrpcClient {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryServiceGrpcClient.class);

    @Value("${inventory.service.address:localhost}")
    private String serverAddress;   // <-- NOT FINAL

    @Value("${inventory.service.grpc.port:9001}")
    private int serverPort;         // <-- NOT FINAL

    private InventoryServiceGrpc.InventoryServiceBlockingStub blockingStub;

    @PostConstruct
    private void initGrpcClient() {
        log.info("Connecting to Inventory gRPC service at {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();

        blockingStub = InventoryServiceGrpc.newBlockingStub(channel);
    }

    public InventoryResponse createInventoryAccount(String skuCode, int quantity) {

        InventoryRequest request = InventoryRequest.newBuilder()
                .setSkuCode(skuCode)
                .setQuantity(quantity)
                .build();

        InventoryResponse response = blockingStub.createInventoryAccount(request);

        log.info("Received response from inventory service via GRPC: {}", response);

        return response;
    }
}
