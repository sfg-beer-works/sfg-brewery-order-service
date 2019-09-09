package guru.sfg.brewery.order.service.sm.actions;

import guru.sfg.brewery.order.service.config.JmsConfig;
import guru.sfg.brewery.order.service.domain.BeerOrderEventEnum;
import guru.sfg.brewery.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.brewery.order.service.web.mappers.BeerOrderMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 2019-09-08.
 */
@Component
public class AllocateBeerOrder extends JmsAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    public AllocateBeerOrder(JmsTemplate jmsTemplate, BeerOrderMapper beerOrderMapper) {
        super(jmsTemplate, beerOrderMapper);
    }

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        sendJmsMessage(JmsConfig.ALLOCATE_ORDER_QUEUE, context);
    }
}
