package org.datpham.foodlink.controller;

import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import org.datpham.foodlink.service.impl.StripeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(originPatterns = "*") // Fix CORS error when allowCredentials is true
public class PaymentController {

    private final StripeService stripeService;

    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestParam Long amount) {
        try {
            PaymentIntent intent = stripeService.createPaymentIntent(amount);
            Map<String, String> data = new HashMap<>();
            data.put("clientSecret", intent.getClientSecret());
            return ResponseEntity.ok(data);
        } catch (com.stripe.exception.StripeException e) {
            Map<String, String> errorData = new HashMap<>();
            errorData.put("message", "Stripe Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorData);
        } catch (Exception e) {
            Map<String, String> errorData = new HashMap<>();
            errorData.put("message", "Payment Error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorData);
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> createCheckoutSession(
            @RequestParam Long amount,
            @RequestParam String successUrl,
            @RequestParam String cancelUrl) {
        try {
            Session session = stripeService.createCheckoutSession(amount, successUrl, cancelUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("url", session.getUrl());
            response.put("sessionId", session.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorData = new HashMap<>();
            errorData.put("message", "Checkout Session Error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorData);
        }
    }
}