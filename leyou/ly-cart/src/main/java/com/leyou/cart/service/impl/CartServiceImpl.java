package com.leyou.cart.service.impl;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.inerceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:user:id";

    @Override
    public void addCart(Cart cart) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key （最外层用户的key）
        String key = KEY_PREFIX + user.getId();
        //hashKey（属于这个用户的商品的id）
        String hashKey = cart.getSkuId().toString();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        //记录当前cart的数量
        Integer num = cart.getNum();
        //判断当前购物车商品，是否存在
        if (operations.hasKey(hashKey)) {
            //是,修改数量
            String json = operations.get(hashKey).toString();
            cart = JsonUtils.parse(json, Cart.class);
            cart.setNum(cart.getNum() + num);
        }
        //否,添加到购物车
        operations.put(hashKey, JsonUtils.serialize(cart));
     }

    /**
     * 加载购物车
     * @return
     */
    @Override
    public List<Cart> loadCart() {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key （最外层用户的key）
        String key = KEY_PREFIX + user.getId();
        if(!redisTemplate.hasKey(key))
        {
            throw new LyException(ExceptionEnum.CART_NOT_FIND);
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        //处理集合
        List<Cart> carts = operations.values().stream()
                .map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());

        return carts;
    }

    /**
     * 修改购物车中商品数量
     * @param skuId
     * @param num
     */
    @Override
    public void updateNum(Long skuId, Integer num) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key （最外层用户的key）
        String key = KEY_PREFIX + user.getId();
        //hashKey 该商品的key
        String hashKey=skuId.toString();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        if(!operations.hasKey(hashKey))
        {
            throw new LyException(ExceptionEnum.CART_NOT_FIND);
        }
        //先从redis中取出数据
        String json = operations.get(hashKey).toString();
        Cart cart = JsonUtils.parse(json, Cart.class);
        //将新的num设置到商品中
        cart.setNum(num);
        //回写到redis中
        operations.put(hashKey,JsonUtils.serialize(cart));
    }

    /**
     * 删除购物车中商品
     * @param skuId
     */
    @Override
    public void deleteCartItem(Long skuId) {

        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key （最外层用户的key）
        String key = KEY_PREFIX + user.getId();
        //hashKey 该商品的key
        String hashKey=skuId.toString();

        redisTemplate.opsForHash().delete(key,hashKey);
    }

    /**
     * 合并购物车
     * @param carts
     */
    @Override
    public void cartCombine(List<Cart> carts) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key id+前缀
        String key = KEY_PREFIX + user.getId();
        if(CollectionUtils.isEmpty(carts))
        {
            log.error("{购物车服务}，本地购物车为空,用户：{}",user.getUsername());
            throw new LyException(ExceptionEnum.CART_NOT_FIND);
        }
        //将购物车中的数据存入redis
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        //创建map集合 保存cart
        Map<String,String> cartMap=new HashMap<>();
        for (Cart cart : carts) {
            //hashKey 该商品的key
            String hashKey=cart.getSkuId().toString();
            cartMap.put(hashKey,JsonUtils.serialize(cart));
        }
        //将本地购物车中的商品合并到redis中
        operations.putAll(cartMap);
    }
}
