package guru.sfg.brewery.order.service.services.listeners;

import guru.sfg.brewery.model.events.BeerOrderValidationResult;
import guru.sfg.brewery.order.service.config.JmsConfig;
import guru.sfg.brewery.order.service.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 2019-09-08.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESULT_QUEUE)
    public void listen(BeerOrderValidationResult result) {
        if(result.getIsValid()){
            beerOrderManager.beerOrderPassedValidation(result.getBeerOrderId());
        } else {
            beerOrderManager.beerOrderFailedValidation(result.getBeerOrderId());
        }
    }
}
