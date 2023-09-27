package com.MobileSubscribers.MobileSubscribers;

import com.MobileSubscribers.MobileSubscribers.MobileSubscribers.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.MobileSubscribers.MobileSubscribers.MobileSubscribers.RegisterRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;


@SpringBootApplication
@ComponentScan(basePackages = "com.MobileSubscribers") // Replace with your actual package structure
public class MobileSubscribersApplication {

	public static void main(String[] args) {
		SpringApplication.run(MobileSubscribersApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			SubscriberRepository repository
	) {
		return args -> {
			var admin = createSubscriber(
					RegisterRequest.builder()
							.firstname("Admin")
							.lastname("Admin")
							.email("admin@mail.com")
							.password("password")
							.role(Role.ADMIN)
							.msisdn("0248189134")
							.customer_id_owner(99)
							.customer_id_user(100)
							.serviceType(ServiceType.MobilePostpaid)
							.unixEpochMillis(System.currentTimeMillis())
							.build()
			);

			var manager = createSubscriber(
					RegisterRequest.builder()
							.firstname("Manager")
							.lastname("Manager")
							.email("manager@mail.com")
							.password("password999")
							.role(Role.MANAGER)
							.msisdn("0204314933")
							.customer_id_owner(23)
							.customer_id_user(99)
							.serviceType(ServiceType.MobilePrepaid)
							.unixEpochMillis(System.currentTimeMillis())
							.build()
			);

			// Create a list of subscribers
			List<Subscribers> subscribersList = Arrays.asList(admin, manager);

			// Save the Subscribers to the repository
			repository.saveAll(subscribersList);
		};
	}

	// Create a Subscribers object from a RegisterRequest object
	private Subscribers createSubscriber(RegisterRequest request) {
		return new Subscribers(
				request.getMsisdn(),
				request.getFirstname(),
				request.getLastname(),
				request.getEmail(),
				request.getPassword(),
				request.getRole(),
				request.getCustomer_id_owner(),
				request.getCustomer_id_user(),
				request.getServiceType(),
				request.getUnixEpochMillis()
		);
	}
}