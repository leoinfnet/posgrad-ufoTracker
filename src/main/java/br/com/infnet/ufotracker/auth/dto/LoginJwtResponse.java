package br.com.infnet.ufotracker.auth.dto;

import java.util.List;

public record LoginJwtResponse(String username, List<String> roles, String token) {



}
