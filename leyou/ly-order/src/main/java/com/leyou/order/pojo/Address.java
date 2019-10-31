package com.leyou.order.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name ="tb_address")
public class Address {

    @KeySql(useGeneratedKeys = true)
    @Id
    private Long id;

    private String addressee;

    private String phone;

    private String province;

    private String city;

    private String district;

    private String street;

    private String zipCode;

    private String isDefault;

    private Long user_id;
}
