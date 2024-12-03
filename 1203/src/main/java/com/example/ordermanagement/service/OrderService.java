package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Order;
import com.example.ordermanagement.model.ShippingInfo;
import com.example.ordermanagement.repository.OrderRepository;
import com.example.ordermanagement.repository.ShippingInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShippingInfoRepository shippingInfoRepository;

    // 獲取所有訂單
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    // 根據篩選條件查詢訂單，支持分頁
    public Page<Order> getOrdersByFilters(Long buyerId, Long sellerId, String orderNumber, String status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // 使用規格查詢
        Specification<Order> spec = Specification.where(null);

        if (orderNumber != null && !orderNumber.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("orderNumber"), orderNumber));
        }
        if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get("orderDate"), startDateTime, endDateTime));
        }
        if (buyerId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("buyerId"), buyerId));
        }
        if (sellerId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("sellerId"), sellerId));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }

        return orderRepository.findAll(spec, pageable);
    }

    // 根據ID獲取訂單詳情
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // 更新訂單詳情（支持多字段更新）
    public boolean updateOrderDetails(Long orderId, String paymentStatus, String shippingStatus, String status) {
        return orderRepository.findById(orderId).map(order -> {
            // 棄單後禁止修改
            if ("Canceled".equals(order.getStatus())) {
                throw new IllegalStateException("訂單已被棄單，無法修改");
            }

            // 更新付款情形
            if (paymentStatus != null) {
                order.setPaymentStatus(paymentStatus);
            }

            // 更新物流情形
            if (shippingStatus != null) {
                order.setShippingStatus(shippingStatus);
            }

            // 更新訂單狀態
            if (status != null) {
                order.setStatus(status);
            }

            orderRepository.save(order);
            return true;
        }).orElse(false);
    }

    // 棄單功能
    public boolean cancelOrder(Long orderId) {
        return orderRepository.findById(orderId).map(order -> {
            if ("Canceled".equals(order.getStatus())) {
                throw new IllegalStateException("訂單已被標記為棄單");
            }

            // 標記訂單為棄單
            order.setStatus("Canceled");
            orderRepository.save(order);

            // 同步更新物流信息為棄單狀態
            List<ShippingInfo> shippingInfos = shippingInfoRepository.findByOrderId(orderId);
            for (ShippingInfo shippingInfo : shippingInfos) {
                shippingInfo.setStatus("Canceled");
                shippingInfoRepository.save(shippingInfo);
            }
            return true;
        }).orElse(false);
    }
}