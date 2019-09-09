package guru.sfg.brewery.order.service.sm.actions;

import guru.sfg.brewery.order.service.config.JmsConfig;
import guru.sfg.brewery.order.service.domain.BeerOrderEventEnum;
import guru.sfg.brewery.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.brewery.order.service.web.mappers.BeerOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * Validate Beer Order with Beer Service
 *
 * Created by jt on 2019-09-07.
 */
@Slf4j
@Component
public class ValidateBeerOrder extends JmsAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    public ValidateBeerOrder(JmsTemplate jmsTemplate, BeerOrderMapper beerOrderMapper) {
        super(jmsTemplate, beerOrderMapper);
    }

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        sendJmsMessage(JmsConfig.VALIDATE_ORDER_QUEUE, stateContext);
    }
}
