package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.enums.PayState;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayConfig;
import com.leyou.order.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@EnableConfigurationProperties(PayConfig.class)
public class PayHelper {
    @Autowired
    private WXPay wxPay;

    @Autowired
    private PayConfig payConfig;
    @Autowired
    private OrderServiceImpl orderService;

    public String createOrder(Long orderId, Long totalPay, String desc) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("body", desc);
            //. 订单号
            data.put("out_trade_no", orderId.toString());
            //金额，单位是分
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端IP
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url", payConfig.getNotifyUrl());
            //交易类型为扫码支付
            data.put("trade_type", "NATIVE");
            //利用wxPay工具完成下单
            Map<String, String> result = wxPay.unifiedOrder(data);
            //判断标识和业务是否成功
            isSuccess(result);
            //获取支付链接
            String url = result.get("code_url");
            return url;
        } catch (Exception e) {
            log.error(" [微信下单]创建预交易订单异常失败", e);
            return null;
        }
    }

    //数据校验
    public void isSuccess(Map<String, String> result) {
        String return_code = result.get("return_code");
        if (WXPayConstants.FAIL.equals(return_code)) {
            //通信失败
            log.error("[微信下单] 微信下单通信失败 ，失败原因：{} ", result.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
        //判断业务标识
        String result_code = result.get("result_code");
        if (WXPayConstants.FAIL.equals(result_code)) {
            //通信失败
            log.error("[微信下单] 微信下单业务失败 ，错误码：{}，错误原因：{} ", result.get("err_code"), result.get("err_code_des"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
    }

    public void isValidSign(Map<String, String> data) {
        //重新生成签名
        try {
            String sign1 = WXPayUtil.generateSignature(data, payConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
            String sign2 = WXPayUtil.generateSignature(data, payConfig.getKey(), WXPayConstants.SignType.MD5);
            //与传过来的签名进行对比
            String sign = data.get("sign");
            if (!StringUtils.equals(sign, sign1) && !StringUtils.equals(sign, sign2)) {
                //签名有误，抛出异常
                throw new LyException(ExceptionEnum.INVALID_SIGN);
            }
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_SIGN);
        }
    }

    public Integer queryPayStatus(Long id) {
        try {
            //组织请求参数
            Map<String, String> data = new HashMap<>();
            //. 订单号
            data.put("out_trade_no", id.toString());
            Map<String, String> result = wxPay.orderQuery(data);
            //校验通信状态
            isSuccess(result);
            //  校验签名
            isValidSign(result);
            //校验金额
            orderService.isValidTotalFee(result);
            //查询支付状态
            String state = result.get("trade_state");
            if (WXPayConstants.SUCCESS.equals(state)) {
                //修改订单状态
                orderService.updateOrderStatus(id);
                return PayState.SUCCESS.getValue();
            }
            if ("NOTPAY".equals(state) || "USERPAYING".equals(state)) {
                return PayState.NOT_PAY.getValue();
            }
            return PayState.FAIL.getValue();
        } catch (Exception e) {
            return PayState.NOT_PAY.getValue();
        }
    }
}
