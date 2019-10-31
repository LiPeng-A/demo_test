package com.leyou.order.service;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Address;
import com.leyou.order.pojo.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {

    //创建订单
    Long createOrder(OrderDTO orderDTO);

    //查询当前用户的地址信息
    List<Address> queryAddress();

    //添加收货地址
    void inertAddress(Address address);

    //根据id查询订单信息
    Order queryOrderByOrderId(Long orderId);

    //创建支付链接
    String createPayUrl(Long orderId);

    //微信处理回调
    void handleNotify(Map<String, String> result);

    //根据id查询订单状态
    Integer queryOrderStatus(Long id);
}
