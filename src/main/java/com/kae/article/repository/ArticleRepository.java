package com.kae.article.repository;

import com.kae.article.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ArticleRepository extends PagingAndSortingRepository<Article, Long> {
    Page<Article> findByDateBetween(LocalDate dateStart, LocalDate dateEnd, Pageable pageable);
}