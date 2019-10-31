package com.leyou.cart.service;

import com.leyou.cart.pojo.Cart;

import java.util.List;

public interface CartService {
    //添加购物车
    void addCart(Cart cart);

    //查询购物车
    List<Cart> loadCart();

    //给购物车中的商品添加数量
    void updateNum(Long id, Integer num);

    //删除购物车中的商品
    void deleteCartItem(Long skuId);

    //购物车合并
    void cartCombine(List<Cart> carts);
}
