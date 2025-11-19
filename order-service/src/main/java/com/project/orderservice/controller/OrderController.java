package com.project.orderservice.controller;

import com.project.orderservice.dto.OrderRequest;
import com.project.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

//import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        log.info("Placing Order");
        return  orderService.placeOrder(orderRequest);
    }

//    public String fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
//        log.info("Cannot Place Order Executing Fallback logic");
//        return  "Oops! Something went wrong, please order after some time!";
//    }
}
