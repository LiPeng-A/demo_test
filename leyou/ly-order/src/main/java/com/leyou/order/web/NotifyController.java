package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("notify")
@Slf4j
public class NotifyController {

    @Autowired
    private OrderService orderService;

    /**
     * 微信支付的成功回调
     * @param result
     * @return
     */
    @PostMapping(value = "pay",produces = "application/xml")
    public Map<String,String> result(@RequestBody Map<String,String> result)
    {
        //处理回调
        orderService.handleNotify(result);
        //记录日志
        log.info("[微信支付回调]，接收微信支付回调，结果：{}",result);
        //返回成功
        Map<String,String> msg=new HashMap<>();
        msg.put("return_code","SUCCESS");
        msg.put("return_msg","OK");
        return msg;
    }
}
