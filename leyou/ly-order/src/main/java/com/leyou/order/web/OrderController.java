package com.leyou.order.web;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Address;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;
import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO){
        //创建订单
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    /**
     * 查询当前用户的收货地址信息
     * @return
     */
    @GetMapping("address")
    public ResponseEntity<List<Address>> queryAddress(){

        return ResponseEntity.ok(orderService.queryAddress());
    }

    @PostMapping("silt")
    public ResponseEntity<Void> inertAddress(@RequestBody Address address){
            orderService.inertAddress(address);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("{orderId}")
    public ResponseEntity<Order> queryOrderByOrderId(@PathVariable(value = "orderId")Long orderId)
    {
        return ResponseEntity.ok(orderService.queryOrderByOrderId(orderId));
    }

    @GetMapping("url/{orderId}")
    public ResponseEntity<String> createPayUrl(@PathVariable("orderId")Long orderId)
    {
        return ResponseEntity.ok(orderService.createPayUrl(orderId));
    }

    /**
     * 根据id查询订单状态
     * @param id
     * @return
     */
    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryOrderStatusById(@PathVariable(value = "id")Long id){

        return ResponseEntity.ok(orderService.queryOrderStatus(id));
    }
}
