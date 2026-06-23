package io.centralweb.backend.service;

import io.centralweb.backend.exception.ObjectAlreadyExistsException;
import io.centralweb.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    protected void verifyUserAlreadyExists(String email) {
        log.debug("Verificando se o usuário já existe para o e-mail: '{}'", email);
        boolean userAlreadyExists = userRepository.existsByEmail(email);
        if (userAlreadyExists) {
            log.warn("Falha na criação do usuário: O e-mail '{}' já existe", email);
            throw new ObjectAlreadyExistsException("Email ou senha inválidos");
        }
    }
}
