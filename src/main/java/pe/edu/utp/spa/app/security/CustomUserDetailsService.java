package pe.edu.utp.spa.app.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.utp.spa.app.dao.PermisoDao;
import pe.edu.utp.spa.app.dao.RolDao;
import pe.edu.utp.spa.app.dao.UsuarioDao;
import pe.edu.utp.spa.app.model.Usuario;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioDao usuarioDao;
    private final RolDao rolDao;
    private final PermisoDao permisoDao;

    public CustomUserDetailsService(UsuarioDao usuarioDao, RolDao rolDao, PermisoDao permisoDao) {
        this.usuarioDao = usuarioDao;
        this.rolDao = rolDao;
        this.permisoDao = permisoDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioDao.findActivoByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        rolDao.findByUsuario(usuario.getUsuarioId())
                .forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getNombreRol())));
        permisoDao.findByUsuario(usuario.getUsuarioId())
                .forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getNombrePermiso())));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .authorities(authorities)
                .accountLocked(!"A".equalsIgnoreCase(usuario.getEstado()))
                .build();
    }
}
