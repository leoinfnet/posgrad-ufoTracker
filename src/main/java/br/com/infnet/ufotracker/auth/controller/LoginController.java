package br.com.infnet.ufotracker.auth.controller;

import br.com.infnet.ufotracker.auth.dto.LoginJwtResponse;
import br.com.infnet.ufotracker.auth.dto.LoginRequest;
import br.com.infnet.ufotracker.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "*")@RequiredArgsConstructor
public class LoginController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        try {
            var authToken = new UsernamePasswordAuthenticationToken(request.username(), request.password());
            Authentication authenticate = authenticationManager.authenticate(authToken);
            String token = jwtService.generateToken(authenticate);
            String username = authenticate.getName();
            List<String> roles = authenticate.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            //return ResponseEntity.ok(Map.of("token",token));
            return ResponseEntity.ok(new LoginJwtResponse(username, roles, token));
        }catch (BadCredentialsException bce){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "invalid Credendials"));
        }
    }
}
