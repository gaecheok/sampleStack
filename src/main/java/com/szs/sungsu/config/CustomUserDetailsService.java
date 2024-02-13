package com.szs.sungsu.config;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        /*
         NOTE(sss) 권한은 임의로 지정 하였습니다.
         용도에 따라 디비나 토큰에 권한을 저장하고 가져와 사용하도록 구현이 가능할거 같습니다.
        */
        return new User(userId, "",
                List.of(new SimpleGrantedAuthority("role_refund")));
    }
}
