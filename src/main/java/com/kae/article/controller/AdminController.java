package com.kae.article.controller;

import com.kae.article.model.Article;
import com.kae.article.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private ArticleRepository articleRepository;

    @GetMapping("/statistics")
    List<Article> statistics(
        @RequestParam Integer page,
        @RequestParam Integer size
    ) {
        LocalDate dateStart = LocalDate.now().minusDays(7);
        LocalDate dateEnd = LocalDate.now();
        return articleRepository
            .findByDateBetween(
                dateStart,
                dateEnd,
                PageRequest.of(page != null ? page : 0, size != null ? size : 1)
            ).toList();
    }
}