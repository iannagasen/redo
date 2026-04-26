package dev.agasen.platform.contracts.events;

import dev.agasen.platform.core.event.Saga;

public interface OrderCheckoutSaga extends Saga {

   Long orderId();

}
