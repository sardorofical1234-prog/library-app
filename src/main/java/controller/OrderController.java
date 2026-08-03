package com.library.demo.controller;

import com.library.demo.entity.Order;
import com.library.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public Order buyBook(@RequestParam Long userId, @RequestParam Long bookId) {
        return orderService.buyBook(userId, bookId);
    }

    @GetMapping("/my/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        return orderService.getUserOrders(userId);
    }
}