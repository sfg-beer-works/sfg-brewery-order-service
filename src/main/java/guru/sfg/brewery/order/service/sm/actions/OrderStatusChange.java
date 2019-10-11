package guru.sfg.brewery.order.service.sm.actions;

import guru.sfg.brewery.model.OrderStatusUpdate;
import guru.sfg.brewery.order.service.domain.BeerOrder;
import guru.sfg.brewery.order.service.domain.BeerOrderEventEnum;
import guru.sfg.brewery.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.brewery.order.service.web.mappers.DateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static guru.sfg.brewery.order.service.services.BeerOrderManagerImpl.ORDER_OBJECT_HEADER;

/**
 * Created by jt on 2019-09-22.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderStatusChange implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final RestTemplateBuilder restTemplateBuilder;
    private final DateMapper dateMapper = new DateMapper();

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        BeerOrder beerOrder = context.getStateMachine().getExtendedState()
                .get(ORDER_OBJECT_HEADER, BeerOrder.class);

        OrderStatusUpdate update = OrderStatusUpdate.builder()
                .id(beerOrder.getId())
                .orderId(beerOrder.getId())
                .version(beerOrder.getVersion() != null ? beerOrder.getVersion().intValue() : null)
                .createdDate(dateMapper.asOffsetDateTime(beerOrder.getCreatedDate()))
                .lastModifiedDate(dateMapper.asOffsetDateTime(beerOrder.getLastModifiedDate()))
                .orderStatus(beerOrder.getOrderStatus() != null ? beerOrder.getOrderStatus().toString() : null)
                .customerRef(beerOrder.getCustomerRef())
                .build();

        try{
            if (beerOrder.getOrderStatusCallbackUrl() != null) {
                log.debug("Posting to callback url");
                RestTemplate restTemplate = restTemplateBuilder.build();
                restTemplate.postForObject(beerOrder.getOrderStatusCallbackUrl(), update, String.class);
            }
        } catch (Throwable t){
            log.error("Error Preforming callback for order: " + beerOrder.getId(), t);
        }
    }

}
