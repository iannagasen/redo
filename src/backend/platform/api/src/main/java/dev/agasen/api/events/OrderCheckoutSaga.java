package dev.agasen.api.events;

import dev.agasen.common.event.Saga;

public interface OrderCheckoutSaga extends Saga {

   Long orderId();

   String sagaName();

}
