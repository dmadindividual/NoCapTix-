package topg.Event_Platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import topg.Event_Platform.dto.ResolvedBankAccountDto;
import topg.Event_Platform.dto.TicketPurchaseRequest;
import topg.Event_Platform.enums.Role;
import topg.Event_Platform.exceptions.EventNotFoundInDb;
import topg.Event_Platform.exceptions.InvalidTIcketCategory;
import topg.Event_Platform.exceptions.UserNotFoundInDataBase;
import topg.Event_Platform.models.*;
import topg.Event_Platform.repositories.*;

import javax.crypto.Mac;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.crypto.spec.SecretKeySpec;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TicketTypeRepository ticketTypeRepo;
    private final EventRepository eventsRepo;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;
    private final RestTemplate restTemplate;

    @Value("${paystack.secret-key}")
    private String paystackSecretKey;

    @Value("${paystack.base-url}")
    private String paystackBaseUrl;
    private final ObjectMapper objectMapper;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;



    @Transactional
    public String initializePayment(TicketPurchaseRequest ticketRequestDto, String userEmail, String userId) {
        Events events = eventsRepo.findById(ticketRequestDto.eventId())
                .orElseThrow(() -> new EventNotFoundInDb("Event not found"));
        TicketType ticketType = ticketTypeRepo.findByIdForUpdate(ticketRequestDto.ticketTypeId())
                .orElseThrow(() -> new InvalidTIcketCategory("Ticket type not found"));



        if (!ticketType.getEvent().getEventId().equals(events.getEventId())) {
            throw new InvalidTIcketCategory("Ticket type does not belong to the event");
        }

        if (ticketType.getQuantityAvailable() < ticketRequestDto.quantity()) {
            throw new InvalidTIcketCategory("Not enough tickets available");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundInDataBase("User not found"));



        double totalAmount = ticketType.getPrice() * ticketRequestDto.quantity();
        String reference = UUID.randomUUID().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paystackSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "email", userEmail,
                "amount", convertToKobo(totalAmount),
                "reference", reference,
                "callback_url", "https://yourdomain.com/payment-success",
                "subaccount", events.getCreatedBy().getSubaccountCode()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                paystackBaseUrl + "/transaction/initialize",
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

            // Only create the order after successful Paystack init
            Order order = Order.builder()
                    .reference(reference)
                    .status("PENDING")
                    .quantity(ticketRequestDto.quantity())
                    .totalAmount(totalAmount)
                    .ticketType(ticketType)
                    .event(events)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();

            orderRepo.save(order);

            return (String) data.get("authorization_url");
        } else {
            throw new RuntimeException("Failed to initialize payment with Paystack");
        }
    }

    public String processWebhook(String payload, String signature) {
        try {
            String computedHash = hmacSha512(paystackSecretKey, payload);
            if (!computedHash.equalsIgnoreCase(signature)) {
                log.warn("Invalid Paystack signature");
                return "Invalid signature";
            }

            JsonNode root = objectMapper.readTree(payload);
            String event = root.get("event").asText();
            if (!"charge.success".equals(event)) {
                return "Event ignored";
            }

            String reference = root.get("data").get("reference").asText();
            Optional<Order> optionalOrder = orderRepo.findByReference(reference);

            if (optionalOrder.isEmpty()) {
                log.warn("Order not found for reference: {}", reference);
                return "Order not found";
            }

            processSuccessfulPayment(optionalOrder.get());
            return "Payment verified and tickets issued";

        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return "Webhook processing failed";
        }
    }

    @Transactional
    public void processSuccessfulPayment(Order order) {
        if (!"PENDING".equals(order.getStatus())) return;

        TicketType type = order.getTicketType();
        if (type.getQuantityAvailable() < order.getQuantity()) {
            throw new RuntimeException("Not enough tickets available during confirmation");
        }

        order.setStatus("PAID");
        orderRepo.save(order);

        type.setQuantityAvailable(type.getQuantityAvailable() - order.getQuantity());
        ticketTypeRepo.save(type);

        for (int i = 0; i < order.getQuantity(); i++) {
            Ticket ticket = Ticket.builder()
                    .ticketId(generatePaidTicketId())
                    .event(order.getEvent())
                    .ticketType(type)
                    .user(order.getUser())
                    .qrCodePath("generated-later.png")
                    .build();
            ticketRepository.save(ticket);

            try {
                // Generate QR code and get actual saved file path
                String qrFilePath = QRCodeGenerator.generateQRCode(
                        order.getEvent().getName(),
                        order.getUser().getName(),
                        order.getUser().getEmail(),
                        String.valueOf(type.getTicketCategory()),
                        order.getCreatedAt(),
                        ticket.getTicketId()
                );

// Read bytes from the correct file path
                byte[] qrBytes = Files.readAllBytes(Path.of(qrFilePath));

                Map<String, Object> variables = Map.of(
                        "name", order.getUser().getName(),
                        "eventName", order.getEvent().getName(),
                        "ticketId", ticket.getTicketId()
                );

                ticket.setQrCodePath(qrFilePath);
                ticketRepository.save(ticket);
                emailService.sendEmailWithAttachment(
                        order.getUser().getEmail(),
                        "Your Ticket for " + order.getEvent().getName(),
                        "ticket-template",  // name of your Thymeleaf template
                        variables,
                        qrBytes,
                        ticket.getTicketId() + ".png"
                );
            } catch (Exception e) {
                log.error("Failed to generate QR code for ticket {}", ticket.getTicketId(), e);
                throw new RuntimeException("QR code generation failed");
            }
        }

        // Notify the organizer
        User organizer = order.getEvent().getCreatedBy();
        String organizerEmail = organizer.getEmail();

        String organizerMessage = String.format(
                "Hello %s,\n\nA ticket for your event \"%s\" has been successfully purchased.\n" +
                        "Buyer: %s\nAmount Paid: ₦%.2f\nQuantity: %d\n\nRegards,\nEvent Platform",
                organizer.getName(),
                order.getEvent().getName(),
                order.getUser().getName(),
                order.getTotalAmount(),
                order.getQuantity()
        );

        emailService.sendSimpleEmail(
                organizerEmail,
                "Ticket Purchase Notification for " + order.getEvent().getName(),
                organizerMessage
        );

    }

    @Scheduled(fixedRate = 300_000)
    public void verifyAndRecoverFailedOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<Order> orders = orderRepo.findPendingOrdersOlderThanMinutes(cutoff);
        for (Order order : orders) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + paystackSecretKey);

                HttpEntity<String> entity = new HttpEntity<>(headers);
                String url = paystackBaseUrl + "/transaction/verify/" + order.getReference();

                ResponseEntity<JsonNode> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, JsonNode.class
                );

                JsonNode data = response.getBody().get("data");
                if (data != null && "success".equals(data.get("status").asText())) {
                    processSuccessfulPayment(order);
                }
            } catch (Exception e) {
                log.error("Recovery failed for order {}", order.getReference(), e);
            }
        }
    }



    private String hmacSha512(String key, String data) throws Exception {
        SecretKeySpec secret = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA512");
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(secret);
        byte[] hash = mac.doFinal(data.getBytes("UTF-8"));

        // Convert the byte array to a hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();  // Return the hex string
    }
    private  String generatePaidTicketId() {
        String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom RANDOM = new SecureRandom();
        StringBuilder sb = new StringBuilder("tkt_");
        for (int i = 0; i < 9; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(index));
        }
        return sb.toString();
    }
    private int convertToKobo(double nairaAmount) {
        return (int) Math.round(nairaAmount * 100);
    }
    public String createSubaccount(User user) {
        if (user.getRole() != Role.ORGANIZER) {
            throw new IllegalArgumentException("Only organizers can have subaccounts");
        }

        if (user.getBankCode() == null || user.getAccountNumber() == null) {
            throw new IllegalArgumentException("Organizer must have a bank account number and bank code");
        }

        ResolvedBankAccountDto resolved = resolveBankAccount(user.getAccountNumber(), user.getBankCode());
        if (!resolved.success()) {
            throw new RuntimeException("Invalid bank account: " + resolved.message());
        }


        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paystackSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "business_name", user.getName(),
                "settlement_bank", user.getBankCode(),
                "account_number", user.getAccountNumber(),
                "percentage_charge", 10,  // Your platform keeps 10%, organizers get 90%
                "description", "Subaccount for event organizer: " + user.getName()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                paystackBaseUrl + "/subaccount",
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                boolean status = jsonNode.get("status").asBoolean();
                if (!status) {
                    throw new RuntimeException("Failed to create subaccount: " + jsonNode.get("message").asText());
                }

                return jsonNode.get("data").get("subaccount_code").asText();
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error parsing Paystack response", e);
            }
        } else {
            throw new RuntimeException("Failed to create subaccount: HTTP " + response.getStatusCode());
        }
    }
    public ResolvedBankAccountDto resolveBankAccount(String accountNumber, String bankCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paystackSecretKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = paystackBaseUrl + "/bank/resolve?account_number=" + accountNumber + "&bank_code=" + bankCode;

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            JsonNode body = response.getBody();
            if (body != null && body.get("status").asBoolean()) {
                String accountName = body.get("data").get("account_name").asText();
                return ResolvedBankAccountDto.builder()
                        .success(true)
                        .accountName(accountName)
                        .message("Account resolved successfully")
                        .build();
            } else {
                return ResolvedBankAccountDto.builder()
                        .success(false)
                        .accountName(null)
                        .message("Could not resolve account: " + (body != null ? body.get("message").asText() : "No response"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Error resolving bank account: {}", e.getMessage());
            return ResolvedBankAccountDto.builder()
                    .success(false)
                    .accountName(null)
                    .message("Exception occurred while resolving account")
                    .build();
        }
    }





}
