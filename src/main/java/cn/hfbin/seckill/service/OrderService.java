package cn.hfbin.seckill.service;

import cn.hfbin.seckill.entity.OrderInfo;

/**
 * Created by: HuangFuBin
 * Date: 2018/7/17
 * Time: 10:49
 * Such description:
 */
public interface OrderService {

    long addOrder(OrderInfo orderInfo);

    OrderInfo getOrderInfo(long rderId);

    /**
     * 支付订单（仅本人、仅未支付状态可支付）
     *
     * @return true 支付成功
     */
    boolean payOrder(long userId, long orderId);
}
