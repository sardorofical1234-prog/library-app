package com.library.demo.service;

import com.library.demo.entity.Book;
import com.library.demo.entity.Order;
import com.library.demo.entity.User;
import com.library.demo.repository.BookRepository;
import com.library.demo.repository.OrderRepository;
import com.library.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public Order buyBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Kitob topilmadi"));

        Order order = new Order();
        order.setUser(user);
        order.setBook(book);
        order.setPricePaid(book.getPrice());
        order.setStatus("PAID");
        order.setCreatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}