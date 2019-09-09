package guru.sfg.brewery.order.service.services;

import guru.sfg.brewery.order.service.domain.BeerOrder;
import guru.sfg.brewery.order.service.domain.BeerOrderEventEnum;
import guru.sfg.brewery.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.brewery.order.service.repositories.BeerOrderRepository;
import guru.sfg.brewery.order.service.sm.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by jt on 2019-09-08.
 */
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID";
    public static final String ORDER_OBJECT_HEADER = "BEER_ORDER";

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Override
    public void newBeerOrder(BeerOrder beerOrder) {
        //send validation event
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
    }

    @Override
    public void beerOrderPassedValidation(UUID beerOrderId) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);
    }

    @Override
    public void beerOrderFailedValidation(UUID beerOrderId) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
    }

    @Override
    public void beerOrderAllocationPassed(UUID beerOrderId) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
    }

    @Override
    public void beerOrderAllocationPendingInventory(UUID beerOrderId) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
    }

    @Override
    public void beerOrderAllocationFailed(UUID beerOrderId) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
    }

    @Override
    public void pickupBeerOrder(UUID beerOrderId) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEER_ORDER_PICKED_UP);
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum event){

        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);

        Message msg = MessageBuilder.withPayload(event)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString())
                .build();

        sm.sendEvent(msg);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {

        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null,
                            null, null));
                });

        sm.getExtendedState().getVariables().put(ORDER_OBJECT_HEADER, beerOrder);

        sm.start();

        return sm;
    }
}
