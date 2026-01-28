package org.datpham.foodlink.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) {
        // TODO: Implement lookup from AccountRepository and map roles/authorities.
        throw new UnsupportedOperationException("Not implemented");
    }
}
