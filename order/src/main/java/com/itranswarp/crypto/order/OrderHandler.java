package com.itranswarp.crypto.order;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.itranswarp.crypto.account.AccountService;
import com.itranswarp.crypto.enums.OrderStatus;
import com.itranswarp.crypto.enums.OrderType;
import com.itranswarp.crypto.store.AbstractService;
import com.itranswarp.crypto.symbol.Symbol;

@Component
@Transactional
public class OrderHandler extends AbstractService {

	@Autowired
	AccountService accountService;

	public Order createBuyLimitOrder(long userId, Symbol symbol, BigDecimal price, BigDecimal amount)
			throws InterruptedException {
		amount = symbol.base.adjust(amount);
		price = symbol.quote.adjust(price);
		// frozen:
		accountService.freeze(userId, symbol.quote, price.multiply(amount));
		// create order:
		Order order = new Order();
		order.userId = userId;
		order.symbol = symbol;
		order.amount = amount;
		order.price = price;
		order.filledAmount = BigDecimal.ZERO;
		order.type = OrderType.BUY_LIMIT;
		order.status = OrderStatus.SUBMITTED;
		db.save(order);
		return order;
	}

	public Order createSellLimitOrder(long userId, Symbol symbol, BigDecimal price, BigDecimal amount)
			throws InterruptedException {
		amount = symbol.base.adjust(amount);
		price = symbol.quote.adjust(price);
		// frozen:
		accountService.freeze(userId, symbol.base, amount);
		// create order:
		Order order = new Order();
		order.userId = userId;
		order.symbol = symbol;
		order.amount = amount;
		order.price = price;
		order.filledAmount = BigDecimal.ZERO;
		order.type = OrderType.SELL_LIMIT;
		order.status = OrderStatus.SUBMITTED;
		db.save(order);
		return order;
	}

	public Order createCancelOrder(OrderType cancelType, Order orderToBeCancelled) {
		Order order = new Order();
		order.type = cancelType;
		order.refOrderId = orderToBeCancelled.id;
		order.refSeqId = orderToBeCancelled.seqId;
		order.userId = orderToBeCancelled.userId;
		order.symbol = orderToBeCancelled.symbol;
		order.status = OrderStatus.SUBMITTED;
		// IMPORTANT: a cancelled order MUST have the same price with the
		// order-to-be-cancelled,
		// otherwise it cannot be found in order book:
		order.price = orderToBeCancelled.price;
		// not used in cancelled-type order:
		order.amount = BigDecimal.ZERO;
		order.filledAmount = BigDecimal.ZERO;
		db.save(order);
		return order;
	}
}
