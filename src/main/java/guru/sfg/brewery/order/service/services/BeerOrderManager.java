package guru.sfg.brewery.order.service.services;

import guru.sfg.brewery.order.service.domain.BeerOrder;

import java.util.UUID;

/**
 * Created by jt on 2019-09-08.
 */
public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);

    void beerOrderPassedValidation(UUID beerOrderId);

    void beerOrderFailedValidation(UUID beerOrderId);

    void beerOrderAllocationPassed(UUID beerOrderId);

    void beerOrderAllocationPendingInventory(UUID beerOrderId);

    void beerOrderAllocationFailed(UUID beerOrderId);

    void pickupBeerOrder(UUID beerOrderId);

    void cancelBeerOrder(UUID beerOrderId);
}
