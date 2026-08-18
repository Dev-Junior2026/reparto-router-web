package com.repartorouter.reparto_router_web.controller;

//import com.repartorouter.reparto_router_web.dto.TokenFcmRequest;
import com.repartorouter.reparto_router_web.model.Chofer;
import com.repartorouter.reparto_router_web.repository.ChoferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.repartorouter.reparto_router_web.controller.dto.TokenFcmRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/choferes")
public class ChoferController {

    @Autowired
    private ChoferRepository choferRepository;

    @PutMapping("/me/token-fcm")
    public ResponseEntity<?> actualizarTokenFcm(
            @RequestBody TokenFcmRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        Optional<Chofer> choferOpt = choferRepository.findByEmail(email);
        if (choferOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Chofer chofer = choferOpt.get();
        chofer.setTokenFcm(request.getToken());
        choferRepository.save(chofer);

        return ResponseEntity.ok().build();
    }
}