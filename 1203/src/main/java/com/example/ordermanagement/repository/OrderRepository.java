package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    // 根據買家ID查詢訂單列表
    List<Order> findByBuyerId(Long buyerId);

    // 根據賣家ID查詢訂單列表（如果需要）
    List<Order> findBySellerId(Long sellerId);

    // 根據訂單編號查詢訂單（如果需要）
    Order findByOrderNumber(String orderNumber);
}