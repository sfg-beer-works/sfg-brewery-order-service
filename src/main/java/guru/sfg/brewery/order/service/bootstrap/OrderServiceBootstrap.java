package guru.sfg.brewery.order.service.bootstrap;

import guru.sfg.brewery.order.service.domain.Customer;
import guru.sfg.brewery.order.service.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by jt on 2019-09-29.
 */
@RequiredArgsConstructor
@Component
public class OrderServiceBootstrap implements CommandLineRunner {

    public static final String CUSTOMER_NAME = "Bird Dog Brewing";


    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {

        Optional<Customer> customerOptional = customerRepository.findByCustomerName(CUSTOMER_NAME);

        if (customerOptional.isEmpty()) {
            //create if not found
            customerRepository.save(Customer.builder()
                    .customerName(CUSTOMER_NAME)
                    .apiKey(UUID.randomUUID())
                    .build());
        }

    }
}
