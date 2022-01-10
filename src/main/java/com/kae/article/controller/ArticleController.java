package com.kae.article.controller;

import com.kae.article.exception.NotFoundException;
import com.kae.article.exception.UnauthorizedException;
import com.kae.article.model.Article;
import com.kae.article.model.User;
import com.kae.article.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleRepository articleRepository;

    @GetMapping("{id}")
    public Article article(@PathVariable Long id) {
        return findByIdOrThrow(id);
    }

    private Article findByIdOrThrow(Long id) {
        if (id != null)
            return articleRepository.findById(id)
                    .orElseThrow(NotFoundException::new);
        else throw new NotFoundException();
    }

    @PostMapping
    public Article add(
        @AuthenticationPrincipal User user,
        @Valid @RequestBody Article article
    ) {
        article.setAuthor(user.getUsername());
        article.setUser(user);
        articleRepository.save(article);
        return article;
    }

    @PutMapping("{id}")
    public Article update(
        @AuthenticationPrincipal User user,
        @Valid @RequestBody Article article,
        @PathVariable Long id
    ) {
        Article articleFormDb = findByIdOrThrow(id);
        if (!user.isAdmin()) {
            if (user != article.getUser())
                throw new UnauthorizedException();
            article.setUser(user);
        }
        article.setAuthor(user.getUsername());
        article.setId(articleFormDb.getId());
        articleRepository.save(article);
        return article;
    }

    @GetMapping
    List<Article> articles(
        @RequestParam Integer page,
        @RequestParam Integer size
    ) {
        return articleRepository
                .findAll(PageRequest.of(page != null ? page : 0, size != null ? size : 1)).toList();
    }
}