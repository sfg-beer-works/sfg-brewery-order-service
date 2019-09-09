package guru.sfg.brewery.order.service.sm.actions;

import guru.sfg.brewery.model.events.ValidateBeerOrderRequest;
import guru.sfg.brewery.order.service.domain.BeerOrder;
import guru.sfg.brewery.order.service.domain.BeerOrderEventEnum;
import guru.sfg.brewery.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.brewery.order.service.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;

import static guru.sfg.brewery.order.service.services.BeerOrderManagerImpl.ORDER_OBJECT_HEADER;

/**
 * Created by jt on 2019-09-09.
 */
@Slf4j
@RequiredArgsConstructor
public class JmsAction {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    void sendJmsMessage(String queueName, StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext){

        BeerOrder beerOrder = stateContext.getStateMachine().getExtendedState()
                .get(ORDER_OBJECT_HEADER, BeerOrder.class);


        jmsTemplate.convertAndSend(queueName, ValidateBeerOrderRequest
                .builder()
                .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder)));

        log.debug("Sent request to queue" + queueName + "for Beer Order Id: " + beerOrder.getId().toString());
    }
}
