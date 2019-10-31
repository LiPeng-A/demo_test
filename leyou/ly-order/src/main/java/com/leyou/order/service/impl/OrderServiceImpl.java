package com.leyou.order.service.impl;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.enums.OrderStatusEnum;
import com.leyou.common.enums.PayState;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.config.JwtProperties;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.AddressMapper;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Address;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.service.OrderService;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private JwtProperties jwtProp;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;


    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @Transactional
    @Override
    public Long createOrder(OrderDTO orderDTO) {
        //1.新增订单数据
        Order order=new Order();
        //1.1订单编号，订单基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());
        //1.2用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        //1.3收货人地址信息
        //根据id查询收货人信息
        Address address = addressMapper.selectByPrimaryKey(orderDTO.getAddressId());
        order.setReceiver(address.getAddressee()); //收货人
        order.setReceiverAddress(address.getStreet()); //地址
        order.setReceiverCity(address.getCity()); //城市
        order.setReceiverDistrict(address.getDistrict());//区
        order.setReceiverMobile(address.getPhone()); //手机
        order.setReceiverState(address.getProvince()); //省
        order.setReceiverZip(address.getZipCode()); //邮编
        //1.4金额
        Map<Long, Long> numMap = orderDTO.getCarts().stream().
                collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //将id集合转换为set集合
        Set<Long> ids = numMap.keySet();
        //根据id集合查询sku集合
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(ids));
        Long totalPay=0L;
        //准备订单详情集合
        List<OrderDetail> details=new ArrayList<>();
        for (Sku sku : skus) {
            //计算商品总价
            totalPay += sku.getPrice() * numMap.get(sku.getId());
            //封装orderDetail
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(),","));//取第一张图
            detail.setNum(numMap.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setPrice(sku.getPrice());
            detail.setTitle(sku.getTitle());
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setSkuId(sku.getId());
            details.add(detail);
        }
        //应付金额
        order.setTotalPay(totalPay);
        //实付金额： 总金额 +邮费 -优惠金额
        order.setActualPay(totalPay+order.getPostFee()-0);
        //1.5将order写回到数据库
        int i = orderMapper.insertSelective(order);
        if(i!=1)
        {
            log.error("{订单微服务} 创建订单失败 orderId {}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_FAIL);
        }
        //2.新增订单详情
        int count = detailMapper.insertList(details);
        if(count!=details.size())
        {
            log.error("{订单微服务}，创建订单详情失败,orderId {}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_FAIL);
        }
        //3.新增订单状态
        //封装orderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(new Date());
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        //写回到数据库
        int i1 = statusMapper.insertSelective(orderStatus);
        if(i1!=1)
        {
            log.error("{订单微服务} 创建订单状态失败 orderId {}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_FAIL);
        }
        //4.减库存
        goodsClient.decreaseStock(orderDTO.getCarts());
        return orderId;
    }

    /**
     * 查询当前用户的地址信息
     * @return
     */
    @Override
    public List<Address> queryAddress() {
        UserInfo user = UserInterceptor.getUser();
        Address address = new Address();
        address.setUser_id(user.getId());
        List<Address> addressList = addressMapper.select(address);
        if(CollectionUtils.isEmpty(addressList))
        {
            log.error("{订单服务}，当前用户收货地址为空，用户名{}",user.getUsername());
            throw new LyException(ExceptionEnum.ADDRESS_NOT_FIND);
        }
        return addressList;
    }

    /**
     * 新增地址
     * @param address
     */
    @Override
    @Transactional
    public void inertAddress(Address address) {
        //获取当前用户
        UserInfo user = UserInterceptor.getUser();
        address.setId(null);
        address.setUser_id(user.getId());
        if(address.getIsDefault()=="true")
        {
            address.setIsDefault("0");
        }
        address.setIsDefault("1");

        addressMapper.insertSelective(address);
    }

    /**
     * 根据id查询订单信息
     * @param orderId
     * @return
     */
    @Override
    public Order queryOrderByOrderId(Long orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order==null)
        {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //查询订单详情
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> details = detailMapper.select(orderDetail);
        if(CollectionUtils.isEmpty(details))
        {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        //写入order中
        order.setOrderDetails(details);
        //查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(orderId);
        if(orderStatus==null)
        {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        //写入order中
        order.setOrderStatus(orderStatus);

        return order;
    }

    /**
     * 统一下单
     * @param orderId
     * @return
     */
    @Override
    public String createPayUrl(Long orderId) {
        //查询订单金额
        Order order = queryOrderByOrderId(orderId);
        //判断订单状态
        OrderStatus orderStatus = order.getOrderStatus();
        if(orderStatus.getStatus()!=OrderStatusEnum.UN_PAY.value())
        {
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //支付金额
        Long actualPay = /*order.getActualPay()*/1L;
        //商品描述
        OrderDetail detail = order.getOrderDetails().get(0);
        String desc = detail.getTitle();
        String url = payHelper.createOrder(orderId, actualPay, desc);
        return url;
    }

    @Override
    public void handleNotify(Map<String, String> result) {
        //1 数据校验
        payHelper.isSuccess(result);

        //2 签名校验
        payHelper.isValidSign(result);

        //校验订单的金额
        Long orderId = isValidTotalFee(result);

        //4 修改订单状态
        updateOrderStatus(orderId);
        //5.记录日志
        log.info("[微信支付回调],订单支付成功,订单编号：{}",orderId);
    }

    public void updateOrderStatus(Long orderId) {
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setStatus(OrderStatusEnum.PAYED.value()); //订单状态
        orderStatus.setOrderId(orderId); //订单编号
        orderStatus.setPaymentTime(new Date()); //付款时间
        int i = statusMapper.updateByPrimaryKeySelective(orderStatus);
        if(i!=1)
        {
            throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
    }

    @NotNull
    public Long isValidTotalFee(Map<String, String> result) {
        String totalFeeStr = result.get("total_fee");
        String orderNoStr = result.get("out_trade_no");
        //3 校验返回的金额
        if(StringUtils.isEmpty(totalFeeStr) ||StringUtils.isEmpty(orderNoStr))
        {
            //无效的订单参数
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        //获取结果中的金额
        Long totalFee = Long.valueOf(totalFeeStr);
        //获取订单编号
        Long orderId = Long.valueOf(orderNoStr);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(totalFee!=/*order.getActualPay()*/1)
        {
            //金额不正确(异常参数不正确)
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        return orderId;
    }

    /**
     * 根据id查询订单状态
     * @param id
     * @return
     */
    @Override
    public Integer queryOrderStatus(Long id) {
        //查询订单状态
        OrderStatus status = statusMapper.selectByPrimaryKey(id);
        if(status==null)
        {
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        Integer status1 = status.getStatus();
        //判断是否支付
        if(status1!=OrderStatusEnum.UN_PAY.value())
        {
            //如果已支付,那就真的是已支付
            return PayState.SUCCESS.getValue();
        }
        //如果未支付，但其实不一定是未支付,必须去微信查询支付状态
        return payHelper.queryPayStatus(id);


    }
}
