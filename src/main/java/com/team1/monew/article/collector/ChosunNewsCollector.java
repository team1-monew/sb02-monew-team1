package com.team1.monew.article.collector;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.team1.monew.article.dto.CollectedArticleDto;
import com.team1.monew.article.mapper.CollectedArticleMapper;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChosunNewsCollector implements NewsCollector {

  @Value("${rss.chosun-url}")
  private String rssUrl;

  @Value("${rss.chosun-source-name}")
  private String sourceName;

  @Override
  public List<CollectedArticleDto> collect(Interest interest, Keyword keyword) {
    try {
      if (rssUrl == null || rssUrl.isBlank()) {
        log.error("RSS 수집 실패: URL 미설정 [interest: {}, keyword: {}]", interest.getName(), keyword.getKeyword());
        throw new RestException(ErrorCode.INTERNAL_SERVER_ERROR, Map.of(
            "interest", interest.getName(),
            "keyword", keyword.getKeyword(),
            "error", "RSS_URL_MISSING"
        ));
      }

      URL url = new URL(rssUrl);
      String rawXml = new String(url.openStream().readAllBytes(), StandardCharsets.UTF_8);

      String cleanedXml = rawXml.replaceAll("<!DOCTYPE[^>]*>", "");

      Document doc = Jsoup.parse(cleanedXml, "", Parser.xmlParser());
      cleanedXml = doc.outerHtml();

      return parseArticles(cleanedXml, interest);

    } catch (Exception e) {
      log.error("RSS 뉴스 수집 중 예외 발생: {}", e.getMessage(), e);
      throw new RestException(ErrorCode.INTERNAL_SERVER_ERROR, Map.of(
          "interest", interest.getName(),
          "keyword", keyword.getKeyword(),
          "rssUrl", rssUrl,
          "error", e.getMessage()
      ));
    }
  }

  private List<CollectedArticleDto> parseArticles(String xml, Interest interest) throws Exception {
    SyndFeedInput input = new SyndFeedInput();
    input.setPreserveWireFeed(true);

    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
      SyndFeed feed = input.build(new XmlReader(inputStream));

      return feed.getEntries().stream()
          .map(entry -> {
            String title = Jsoup.parse(entry.getTitle()).text();

            String summary;
            if (entry.getDescription() != null &&
                entry.getDescription().getValue() != null &&
                !entry.getDescription().getValue().isBlank()) {
              summary = Jsoup.parse(entry.getDescription().getValue()).text();
            } else {
              summary = title;
            }

            String sourceUrl = entry.getLink();

            LocalDateTime publishDate;
            if (entry.getPublishedDate() != null) {
              publishDate = entry.getPublishedDate().toInstant()
                  .atZone(ZoneId.systemDefault())
                  .toLocalDateTime();
            } else {
              log.warn("RSS 뉴스에 발행일자가 없어 현재 시간으로 대체합니다. [title: {}]", title);
              publishDate = LocalDateTime.now();
            }

            return CollectedArticleMapper.toDto(title, summary, sourceUrl, sourceName, publishDate);
          })
          .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("RSS XML 파싱 중 오류 발생: {}", e.getMessage(), e);
      throw new RestException(ErrorCode.INTERNAL_SERVER_ERROR, Map.of(
          "error", "RSS_PARSE_ERROR",
          "detail", e.getMessage()
      ));
    }
  }
}
