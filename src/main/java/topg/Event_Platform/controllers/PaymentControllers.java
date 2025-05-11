package topg.Event_Platform.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import topg.Event_Platform.config.UserDetailsServiceImpl;
import topg.Event_Platform.dto.TicketPurchaseRequest;
import topg.Event_Platform.service.PaymentService;

import java.nio.file.attribute.UserPrincipal;
import java.util.Map;

@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@RestController
public class PaymentControllers {
    private final PaymentService paymentService;



    @PostMapping("/purchase")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> purchaseTicket(@RequestBody TicketPurchaseRequest request,
                                            @AuthenticationPrincipal UserDetailsServiceImpl user) {
        String paystackUrl = paymentService.initializePayment(request, user.getUsername(), user.getId());
        return ResponseEntity.ok(Map.of("payment_url", paystackUrl));
    }

}
