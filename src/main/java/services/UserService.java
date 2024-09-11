package services;

import domain.User;
import infrastructure.Db.repositories.UserRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Getter
public class UserService {
    private final UserRepository userRepository;
    private final ParserService parserService;

    public UserService(UserRepository userRepository, ParserService parserService) {
        this.userRepository = userRepository;
        this.parserService = parserService;
    }

    private boolean isUserExists(long telegramId) {
        return userRepository.isUserExists(telegramId);
    }

    public void authorization(long telegramId, String telegramUsername) {
        if (!isUserExists(telegramId)) createUser(telegramId, telegramUsername);
    }

    private void createUser(long telegramId, String telegramUsername) {
        userRepository.createUser(User.builder().telegramId(telegramId).telegramUsername(telegramUsername).build());
    }

    public boolean isAdmin(long telegramId) {
        return userRepository.isAdmin(telegramId);
    }

    public void parsing() throws IOException {
        parserService.parsing();
    }


}
