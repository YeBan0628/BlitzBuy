package cn.hfbin.seckill.dao;

import cn.hfbin.seckill.entity.OrderInfo;
import org.apache.ibatis.annotations.Param;

public interface OrdeInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(OrderInfo record);

    int insertSelective(OrderInfo record);

    OrderInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OrderInfo record);

    int updateByPrimaryKey(OrderInfo record);

    /**
     * 将订单置为已支付。带 user_id 与 status=0 条件，保证只改本人订单且不会重复支付。
     *
     * @return 受影响行数，1 表示支付成功，0 表示订单不存在/非本人/已支付
     */
    int updateToPaid(@Param("orderId") long orderId, @Param("userId") long userId);
}