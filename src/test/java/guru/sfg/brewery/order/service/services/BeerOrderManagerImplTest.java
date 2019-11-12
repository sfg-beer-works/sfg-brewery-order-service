package guru.sfg.brewery.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.sfg.brewery.model.BeerDto;
import guru.sfg.brewery.order.service.domain.BeerOrder;
import guru.sfg.brewery.order.service.domain.BeerOrderLine;
import guru.sfg.brewery.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.brewery.order.service.domain.Customer;
import guru.sfg.brewery.order.service.repositories.BeerOrderRepository;
import guru.sfg.brewery.order.service.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.connection.CachingConnectionFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(WireMockExtension.class)
@SpringBootTest
class BeerOrderManagerImplTest {

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    ObjectMapper objectMapper;

    @TestConfiguration
    static class RestTemplateBuilderProvider {

        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer(){
            WireMockServer server =  with(wireMockConfig().port(8083));
            server.start();
            return server;
        }

    }

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Qualifier("cachingJmsConnectionFactory")
    @Autowired
    CachingConnectionFactory cachingConnectionFactory;

    Customer testCustomer;

    UUID beerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {

        testCustomer = customerRepository.save(Customer.builder()
                .customerName("Test Customer")
                .build());


    }

    @Test
    void testGoodOrderHappyPath() throws  JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("1234").build();

        wireMockServer.stubFor(get(BeerServiceImpl.BEER_PATH_V1 + beerId.toString())
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder orderToSave = createBeerOrder();

        BeerOrder savedOrder = beerOrderManager.newBeerOrder(orderToSave);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.getOne(savedOrder.getId());

            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        BeerOrder foundOrder = beerOrderRepository.getOne(savedOrder.getId());

        assertNotNull(foundOrder);

        System.out.println(foundOrder);

        //pickup order

        beerOrderManager.pickupBeerOrder(foundOrder.getId());

        await().untilAsserted(() -> {
            BeerOrder orderCheck = beerOrderRepository.getOne(savedOrder.getId());

            assertEquals(BeerOrderStatusEnum.PICKED_UP, orderCheck.getOrderStatus());
        });
    }

    @Test
    void testCanxOrder() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("1234").build();

        wireMockServer.stubFor(get(BeerServiceImpl.BEER_PATH_V1 + beerId.toString())
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder orderToSave = createBeerOrder();

        BeerOrder savedOrder = beerOrderManager.newBeerOrder(orderToSave);

        beerOrderManager.cancelBeerOrder(savedOrder.getId());

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.getOne(savedOrder.getId());

            assertEquals(BeerOrderStatusEnum.CANCELED, foundOrder.getOrderStatus());
        });

    }

    @Test
    void beerOrderPassedValidation() {
    }

    @Test
    void beerOrderFailedValidation() {
    }

    @Test
    void beerOrderAllocationPassed() {
    }

    @Test
    void beerOrderAllocationPendingInventory() {
    }

    @Test
    void beerOrderAllocationFailed() {
    }

    @Test
    void pickupBeerOrder() {
    }

    public BeerOrder createBeerOrder(){
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder()
                .beerId(beerId)
                .orderQuantity(1)
                .beerOrder(beerOrder)
                .build());

        beerOrder.setBeerOrderLines(lines);

        return beerOrder;
    }
}