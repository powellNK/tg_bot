package services;

import domain.User;
import infrastructure.Db.repositories.UserRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


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

    public void authorization(long telegramId) {
        if (!isUserExists(telegramId)) createUser(telegramId);
    }

    private void createUser(long telegramId) {
        userRepository.createUser(User.builder().telegramId(telegramId).build());
    }

    public boolean isAdmin(long telegramId) {
        return userRepository.isAdmin(telegramId);
    }

    public void parsing() {
        parserService.parse();
    }
}
