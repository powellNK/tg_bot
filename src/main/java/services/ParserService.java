package services;

import infrastructure.Db.repositories.ParserRepository;
import infrastructure.Db.repositories.UserRepository;

public class ParserService {
    private final ParserRepository parserRepository;
    public ParserService(ParserRepository parserRepository) {
        this.parserRepository = parserRepository;
    }

    public void parse() {
    }
}
