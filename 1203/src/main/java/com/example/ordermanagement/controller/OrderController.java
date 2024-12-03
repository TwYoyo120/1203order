package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Order;
import com.example.ordermanagement.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 映射到訂單管理頁面
    @GetMapping("/order-management")
    public String adminOrderManagement() {
        return "adminOrderManagement"; // 映射至 resources/templates/adminOrderManagement.html
    }

    // 獲取訂單列表，支持多種篩選條件和分頁
    @ResponseBody
    @GetMapping
    public Page<Order> getAllOrders(
            @RequestParam(required = false) Long buyerId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return orderService.getOrdersByFilters(buyerId, sellerId, orderNumber, status, startDate, endDate, PageRequest.of(page - 1, size));
    }

    // 根據訂單ID獲取訂單詳情
    @ResponseBody
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 更新訂單詳情
    @ResponseBody
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetails(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updatedFields) {
        try {
            boolean updated = orderService.updateOrderDetails(
                    id,
                    (String) updatedFields.get("paymentStatus"),
                    (String) updatedFields.get("shippingStatus"),
                    (String) updatedFields.get("status")
            );
            return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 棄單功能
    @ResponseBody
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            boolean canceled = orderService.cancelOrder(id);
            return canceled ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}