package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;

@Data
public class ExceptionResult {
    private Integer status;
    private String message;
    private Long timestamp;

    public ExceptionResult(ExceptionEnum em){
        this.status=em.getStatus();
        this.message=em.getMsg();
        this.timestamp=System.currentTimeMillis();
    }
}
