package com.project.inventoryservice.grpc;

import com.project.inventoryservice.mapper.InventoryMapper;
import com.project.inventoryservice.repository.InventoryRepository;
import inventory.InventoryRequest;
import inventory.InventoryResponse;
import inventory.InventoryServiceGrpc.InventoryServiceImplBase;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
@RequiredArgsConstructor
public class InventoryGrpcService extends InventoryServiceImplBase {

    private final InventoryRepository inventoryRepository;

    private static final Logger
            log = LoggerFactory.getLogger(InventoryGrpcService.class);

    @Override
    public void createInventoryAccount(InventoryRequest inventoryRequest, StreamObserver<InventoryResponse> responseObserver){

        log.info("createBillingAccount request received {}", inventoryRequest.toString());

        // Business logic - e.g save to database, perform calculates etc

        inventoryRepository.save(InventoryMapper.toModel(inventoryRequest));



        InventoryResponse response = InventoryResponse.newBuilder()
                .setSkuCode(inventoryRequest.getSkuCode())
                .setQuantity(inventoryRequest.getQuantity())
                .build();


        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

}
