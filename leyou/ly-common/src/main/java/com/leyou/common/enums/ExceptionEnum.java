package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum  ExceptionEnum {

    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    SAVE_GOODS_FAIL(500,"新增商品失败"),
    DELETE_GOODS_FAIL(500,"删除商品sku失败"),
    SPEC_GROUP_DELETE_FAIL(500,"删除分组信息失败"),
    SPEC_GROUP_SAVE_ERROR(500,"新增分组信息失败"),
    CATEGORY_NOT_FOUND(404,"商品分类没查到"),
    GOODS_SPU_NOT_FOUND(404,"商品详情没查到"),
    GOODS_SKU_NOT_FOUND(404,"商品SKU没查到"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格组没找到"),
    SPEC_GROUP_UPDATE_FAIL(404,"商品规格组更新失败"),
    SPEC_PARAM_NOT_FOUND(404,"商品参数没找到"),
    GOODS_NOT_FOUND(404,"商品不存在"),
    BRAND_CATEGORY_NOT_FOUND(404,"商品分类没查到"),
    BRAND_NOT_FOUND(404,"品牌没找到"),
    DELETE_BRAND_BAD(404,"删除品牌失败"),
    GOODS_ID_CANNOT_NULL(404,"商品id不能null"),
    UPLOAD_ERROR(500,"文件上传失败"),
    UPDATE_BRAND_ERROR(500,"更新品牌信息失败"),
    UPDATE_GOODS_ERROR(500,"更新商品信息失败"),
    INVALID_FILE_TYPE(400,"无效的文件类型"),
    INVALID_USER_DATA_TYPE(400,"无效的用户数据类型"),
    INVALID_VERIFY_CODE(400,"无效的验证码"),
    INVALID_USERNAME_PASSWORD(400,"用户名或密码错误"),
    CREATE_TOKEN_ERROR(500,"用户凭证生成失败"),
    UNAUTHORIZED(403,"未授权"),
    CART_NOT_FIND(404,"购物车为空"),
    ADDRESS_NOT_FIND(404,"收货地址为空"),
    CREATE_ORDER_FAIL(500,"新增订单失败"),
    DECREASE_STOCK_FAIL(500,"减库存失败"),
    ORDER_NOT_FOUND(404,"订单没找到"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情没找到"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态没找到"),
    WX_PAY_ORDER_FAIL(500,"微信下单失败"),
    ORDER_STATUS_ERROR(400,"订单状态异常"),
    INVALID_SIGN(400,"无效的签名"),
    INVALID_ORDER_PARAM(400,"无效的订单参数"),
    UPDATE_ORDER_STATUS_ERROR(500,"修改订单状态失败"),

    ;
    private Integer status;
    private String msg;
}
