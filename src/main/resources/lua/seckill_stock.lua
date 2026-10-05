-- 秒杀库存原子扣减脚本
-- KEYS[1] = 库存 key（如 GoodsKey:gs{goodsId}）
-- 返回值：
--   >=0  扣减成功，返回扣减后的剩余库存
--   -1   库存不足（已售罄），不做扣减，保证库存永远不会被减成负数
--   -2   库存未初始化（key 不存在）
local stock = tonumber(redis.call('get', KEYS[1]))
if stock == nil then
    return -2
end
if stock <= 0 then
    return -1
end
return redis.call('decr', KEYS[1])
