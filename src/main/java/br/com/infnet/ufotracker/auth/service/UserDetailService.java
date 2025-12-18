package br.com.infnet.ufotracker.auth.service;

import br.com.infnet.ufotracker.auth.model.Usuario;
import br.com.infnet.ufotracker.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserDetailService implements UserDetailsService  {
    private final UsuarioRepository usuarioRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        List<SimpleGrantedAuthority> roles = usuario.getRoles()
                .stream().map(role -> new SimpleGrantedAuthority(role.getNome())).toList();

        return User.builder().username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(roles)
                .disabled(!usuario.isAtivo())
                .build();
    }
}
