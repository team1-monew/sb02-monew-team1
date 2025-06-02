package com.team1.monew.article.collector;

import com.team1.monew.article.dto.CollectedArticleDto;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import java.util.List;

public interface NewsCollector {
  List<CollectedArticleDto> collect(Interest interest, Keyword keyword);
}