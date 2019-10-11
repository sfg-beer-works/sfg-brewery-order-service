package guru.sfg.brewery.order.service.services.testcomponents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.sfg.brewery.model.events.AllocateBeerOrderResult;
import guru.sfg.brewery.order.service.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by jt on 2019-09-27.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message msg) throws IOException, JMSException {

        String jsonString = msg.getBody(String.class);

        JsonNode event = objectMapper.readTree(jsonString);
        log.debug("Beer Order Allocation Mock received request");

        JsonNode beerOrder = event.get("beerOrder");

        Boolean isValid = true;
        JsonNode order = beerOrder.get("id");

        if(order.get("customerRef") != null) {
            if (order.get("customerRef").asText() == "fail") isValid = false;
        }

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESULT_QUEUE, AllocateBeerOrderResult.builder()
                .beerOrderId(UUID.fromString(order.asText()))
                .allocated(isValid)
                .pendingInventory(false)
                .build());
    }
}
