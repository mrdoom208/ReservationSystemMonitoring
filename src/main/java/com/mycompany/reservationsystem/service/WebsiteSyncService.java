package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.mysql.cj.conf.PropertyKey.logger;

@Service
public class WebsiteSyncService {

    private final RestTemplate restTemplate;
    private String WebsiteUrl = "http://localhost:8080";
    private String websiteSettings = WebsiteUrl+"/settings/auto-cancel";

    @Autowired
    public WebsiteSyncService(RestTemplate restTemplate) {
         this.restTemplate = restTemplate;
        System.out.println("RestTemplate bean injected: " + restTemplate);
    }

    public void sendAutoCancelTime(long minutes) {
        String url = WebsiteUrl + "/settings/auto-cancel?minutes=" + minutes;
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            System.out.println("Website response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Failed to send auto-cancel time: " + e.getMessage());
        }
    }
    public void sendAutoDeleteMonths(int months) {
        String url = WebsiteUrl + "/settings/auto-delete?months=" + months;
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            System.out.println("Website response: {}" + response.getBody());
        } catch (Exception e) {
            System.out.println("Failed to send auto-delete months");
        }
    }
    public String generateLoginLink(Reservation reservation) {
        String baseUrl = AppSettings.loadApplicationUrl(); // e.g., https://myrestaurant.com
        String phone = URLEncoder.encode(reservation.getCustomer().getPhone(), StandardCharsets.UTF_8);
        String reference = URLEncoder.encode(reservation.getReference(), StandardCharsets.UTF_8);

        return baseUrl + "/login?phone=" + phone + "&reference=" + reference;
    }
}
