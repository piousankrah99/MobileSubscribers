package com.MobileSubscribers.MobileSubscribers.MobileSubscribers;


import java.util.List;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.Files;

@Controller
@RequestMapping(path = "/Subscribers")
@Slf4j
public class SubscriberController {

    Subscribers subscriber = new Subscribers();

    private SubscriberService subscriberService;

    public SubscriberController(AuthenticationService authenticationService,SubscriberService subscriberService) {
        this.authenticationService = authenticationService;
        this.subscriberService = subscriberService;
    }

    private final AuthenticationService authenticationService;


    @GetMapping("/index")
    public String listSubscribers(Model model) {
        return "index"; // Assuming you have a "mobileSubscriber.html" template
    }

    @GetMapping("/sign_up")
    public String sign_up(Model model) {

        model.addAttribute("subscriber", new Subscribers());
        return "sign_up";
    }

    @PostMapping("/sign_up/save")
    public String register(@Valid @ModelAttribute("subscriber") Subscribers subscribers,
                           BindingResult result, Model model) {
    log.info("Subscriber -> {}",subscribers.getEmail());
        var findByEmail = subscriberService.findByEmail(subscribers.getEmail());

        if (findByEmail != null && findByEmail.getEmail() != null) {
            result.rejectValue("id", "Cannot Register Again.");
        }

        if (result.hasErrors()) {
            model.addAttribute("subscriber", subscribers);
            return "index";
        }

        subscriberService.addNewSubscriber(subscribers);

        // Return the view name "mobileSubscriber" without a redirect
        return "redirect:/mobileSubscriber?success";
    }


    @GetMapping("/login")
    public String login(Model model) {
        return "login"; // Assuming you have a "mobileSubscriber.html" template
    }


    @GetMapping(value = "/css/styles.css", produces = "text/css")
    @ResponseBody
    public ResponseEntity<byte[]> getStyles() throws IOException, java.io.IOException {
        // Load the CSS file from the classpath resource
        Resource resource = new ClassPathResource("/static/css/styles.css");

        if (!resource.exists()) {
            // Handle resource not found error
            return ResponseEntity.notFound().build();
        }

        // Read the CSS file into a byte array
        byte[] cssBytes = Files.readAllBytes(resource.getFile().toPath());

        // Set appropriate headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("text/css"));

        // Return the CSS content as a ResponseEntity
        return ResponseEntity.ok().headers(headers).body(cssBytes);
    }


    @GetMapping("/mobileSubscriber")
    public String index(Model model) {

        model.addAttribute("subscriber", new Subscribers());

        model.addAttribute("subscribers", subscriberService.getAllSubscribers());
        return "mobileSubscriber"; // Assuming you have a "mobileSubscriber.html" template
    }

 @GetMapping("/mobileSubscribers")
    @ResponseBody // This annotation tells Spring to directly return the data as the response body
    public List<Subscribers> mobileSubscribers(Model model) {
     model.addAttribute("subscriber", new Subscribers());

     return subscriberService.getAllSubscribers();
    }


    @GetMapping("/{id}")
    @ResponseBody
    public Subscribers getSubscriber(@PathVariable("id") Long id, Model model){
        model.addAttribute("subscriber", new Subscribers());

        return subscriberService.getSubscriberById(id);
    }

    @GetMapping("/oneMobileSubscriber")
    @ResponseBody
    public ResponseEntity<Subscribers> getSubscriberByMsisdn(@RequestParam String msisdn) {
        Subscribers subscriber = subscriberService.getSubscriberByMsisdn(msisdn);

        if (subscriber != null) {
            return ResponseEntity.ok(subscriber);
        } else {
            return  ResponseEntity.notFound().build();
        }
    }

//

    @GetMapping("/displayImage")
    public String displayImage(Model model) {
        String imageUrl = "/images/super_tech_logo.jpg"; // Path to the image in the static directory
        model.addAttribute("imageUrl", imageUrl);
        return "mobileSubscriber";
    }

    // Other endpoint mappings and methods...

    @PostMapping("/addNewSubscriber")
    public ResponseEntity<String> addNewSubscriber(@RequestBody Subscribers newSubscriber) {
        // Check if a subscriber with the same MSISDN already exists
        if (subscriberService.isMSISDNAlreadyExists(newSubscriber.getMsisdn())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("MSISDN already exists");
        }

        // If no duplicate, proceed with adding the new subscriber
        subscriberService.addNewSubscriber(newSubscriber);
        return ResponseEntity.ok("Subscriber added successfully");
    }


    @GetMapping("/updateSubscriberForm/{id}")
    public String showUpdateSubscriberForm(@PathVariable("id") Long id, Model model) {


        model.addAttribute("subscriber", subscriberService.getSubscriberId(id));


        return "mobileSubscriber";
    }


    @PutMapping("/update/{id}")
    public String updateSubscriber(@PathVariable("id")Long id, @RequestBody Subscribers subscriber, Model model) {

        model.addAttribute("subscriber", subscriber);


            Subscribers existingSubscriber = subscriberService.findById(id);
            existingSubscriber.setMsisdn(subscriber.getMsisdn());
            existingSubscriber.setCustomer_id_user(subscriber.getCustomer_id_user());
            existingSubscriber.setCustomer_id_owner(subscriber.getCustomer_id_owner());
            existingSubscriber.setServiceType(subscriber.getServiceType());

            subscriberService.updateSubscriber(existingSubscriber);
            return "mobileSubscriber";
        }


    @GetMapping("/detailsModal/{id}")
    public String showDetailModal(@PathVariable("id") Long id, Model model) {
        model.addAttribute("subscriberDetails", subscriberService.getSubscriberId(id));
        return "mobileSubscriber";
    }


        //Delete
        @DeleteMapping("/{id}")
        public ResponseEntity<String> deleteSubscribers (@PathVariable Long id){
            try {
                subscriberService.deleteSubscriber(id);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

    @GetMapping("/stats")
    public ResponseEntity<SubscriberCount> getStats() {
        long count = subscriberService.countSubscribers();
        long countPrepaid = subscriberService.countSubscribersPrepaid();
        long countPostpaid = subscriberService.countSubscribersPostpaid();

        SubscriberCount subscriberCount = SubscriberCount.builder()
                .totalCount(count)
                .totalPrepaidCount(countPrepaid)
                .totalPostpaidCount(countPostpaid)
                .build();

        // Return the subscriberCount as a JSON response
        return ResponseEntity.ok(subscriberCount);
    }

        @DeleteMapping("/deleteAll")
        public void deleteAllSubscribers () {
            subscriberService.deleteAllSubscribers();
        }


    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, java.io.IOException {
        authenticationService.refreshToken(request, response);
    }


}
