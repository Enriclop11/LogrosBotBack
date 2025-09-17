package com.enriclop.logrosbot.servicio;

import com.enriclop.logrosbot.dto.user.UserDto;
import com.enriclop.logrosbot.modelo.Aura;
import com.enriclop.logrosbot.modelo.User;
import com.enriclop.logrosbot.repositorio.IUserRepository;
import com.enriclop.logrosbot.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> getUsers() {
        return userRepository.getUsersLeaderboard();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsernameLike(username);
    }

    public User getUserByTwitchId(String twitchId) {
        return userRepository.findByTwitchIdLike(twitchId);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id).get();
    }

    public void deleteUserById(Integer id) {
        userRepository.deleteById(id);
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtil.generateToken(userDetails.getUsername());
    }

    public User getUserByToken(String token) {
        String username = jwtUtil.extractUsername(token);
        return getUserByUsername(username);
    }

    public void setBadge(String username, Aura aura) {
        User user = getUserByUsername(username);

        if (user == null) {
            return;
        }

        List<Aura> auras = user.getAuras();

        if (auras.contains(aura)) {
            return;
        }

        auras.add(aura);

        user.setAuras(auras);

        saveUser(user);
    }
}
