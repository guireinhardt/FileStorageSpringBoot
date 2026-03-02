package com.secretaria.FileStorage.service;

import com.secretaria.FileStorage.entity.KeywordEntity;
import com.secretaria.FileStorage.entity.SearchLog;
import com.secretaria.FileStorage.entity.SubkeywordEntity;
import com.secretaria.FileStorage.entity.UsersEntity;
import com.secretaria.FileStorage.repository.KeywordRepository;
import com.secretaria.FileStorage.repository.SearchLogRepository;
import com.secretaria.FileStorage.repository.SubkeywordRepository;
import com.secretaria.FileStorage.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SearchLoggingService {

    @Autowired
    private SearchLogRepository searchLogRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private SubkeywordRepository subkeywordRepository;

    public void log(String username,
                    String keyword,
                    List<String> subkeywords,
                    String query) {

        try {
            // 1) Garante KeywordEntity
            KeywordEntity keywordEntity = null;
            if (keyword != null && !keyword.isBlank()) {
                keywordEntity = keywordRepository.findByPalavra(keyword);
                if (keywordEntity == null) {
                    keywordEntity = new KeywordEntity();
                    keywordEntity.setPalavra(keyword);
                    keywordEntity = keywordRepository.save(keywordEntity);
                }
            }

            // 2) Garante SubkeywordEntity (usa a primeira subkeyword da lista, se houver)

            SubkeywordEntity subkeywordEntity = null;

            if (keywordEntity != null && subkeywords != null && !subkeywords.isEmpty()) {

                String firstSub = subkeywords.get(0);

                Optional<SubkeywordEntity> optionalSub =
                        subkeywordRepository.findByPalavraAndKeyword(firstSub, keywordEntity);

                if (optionalSub.isPresent()) {
                    subkeywordEntity = optionalSub.get();
                } else {
                    SubkeywordEntity sub = new SubkeywordEntity();
                    sub.setPalavra(firstSub);
                    sub.setKeyword(keywordEntity);
                    subkeywordEntity = subkeywordRepository.save(sub);
                }
            }


            LocalDate today = LocalDate.now();

            // 3) Verifica se já existe log pra (keyword [+ subkeyword]) nesse dia
            Optional<SearchLog> existingLogOpt;
            if (subkeywordEntity != null) {
                existingLogOpt = searchLogRepository
                        .findByKeywordAndSubkeywordAndOccurredAt(keywordEntity, subkeywordEntity, today);
            } else {
                existingLogOpt = searchLogRepository
                        .findByKeywordAndOccurredAt(keywordEntity, today);
            }

            SearchLog log;
            if (existingLogOpt.isPresent()) {
                log = existingLogOpt.get();
                log.setSearchCount(log.getSearchCount() + 1);
            } else {
                log = new SearchLog();
                log.setKeyword(keywordEntity);
                log.setSubkeyword(subkeywordEntity);   // 👈 isso vai preencher subkeyword_id
                log.setOccurredAt(today);
                log.setSearchCount(1);
            }

            log.setQuery(query);

            // 4) Usuário (do jeito que está hoje seu UsersRepository)
            if (username != null) {
                UserDetails ud = usersRepository.findByUsername(username);
                if (ud instanceof UsersEntity ue) {
                    log.setUser(ue);
                }
            }

            searchLogRepository.save(log);

        } catch (Exception e) {
            System.err.println("Erro ao registrar log de busca: " + e.getMessage());
        }
    }
}









