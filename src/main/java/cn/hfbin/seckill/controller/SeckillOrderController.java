package cn.hfbin.seckill.controller;

import cn.hfbin.seckill.bo.GoodsBo;
import cn.hfbin.seckill.entity.OrderInfo;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.redis.UserKey;
import cn.hfbin.seckill.result.CodeMsg;
import cn.hfbin.seckill.result.Result;
import cn.hfbin.seckill.service.OrderService;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import cn.hfbin.seckill.util.CookieUtil;
import cn.hfbin.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by: HuangFuBin
 * Date: 2018/7/19
 * Time: 0:56
 * Such description:
 */
@Controller
@RequestMapping("/order")
public class SeckillOrderController {
    @Autowired
    RedisService redisService;
    @Autowired
    SeckillOrderService seckillOrderService;
    @Autowired
    SeckillGoodsService seckillGoodsService;
    @Autowired
    OrderService orderService;

    /**
     * 支付订单：仅本人、仅未支付(status=0)的订单可支付，成功后 status=1、记录支付时间
     */
    @RequestMapping(value = "/pay", method = RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> pay(@RequestParam("orderId") long orderId, HttpServletRequest request) {
        String loginToken = CookieUtil.readLoginToken(request);
        User user = redisService.get(UserKey.getByName, loginToken, User.class);
        if (user == null) {
            return Result.error(CodeMsg.USER_NO_LOGIN);
        }
        boolean paid = orderService.payOrder((long) user.getId(), orderId);
        if (!paid) {
            return Result.error(CodeMsg.ORDER_PAY_NOT_ALLOWED);
        }
        return Result.success(true);
    }

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model,
                                      @RequestParam("orderId") long orderId , HttpServletRequest request) {
        String loginToken = CookieUtil.readLoginToken(request);
        User user = redisService.get(UserKey.getByName, loginToken, User.class);
        if(user == null) {
            return Result.error(CodeMsg.USER_NO_LOGIN);
        }
        // TODO: 可自行扩展缓存中获取，请勿吐槽，此教程只是为了让大家知道整个流程，细节东西自行拓展
        OrderInfo order = seckillOrderService.getOrderInfo(orderId);
        if(order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = order.getGoodsId();
        GoodsBo goods = seckillGoodsService.getseckillGoodsBoByGoodsId(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goods);
        return Result.success(vo);
    }
}
