package com.team1.monew.article.collector;

import com.team1.monew.article.dto.CollectedArticleDto;
import com.team1.monew.article.mapper.CollectedArticleMapper;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverNewsCollector{

  @Value("${naver.client-id}")
  private String clientId;

  @Value("${naver.client-secret}")
  private String clientSecret;

  @Value("${naver.news-api-url}")
  private String newsApiUrl;

  @Value("${naver.source-name}")
  private String sourceName;

  public List<CollectedArticleDto> collect(Interest interest, Keyword keyword) {
    try {
      String query = URLEncoder.encode(keyword.getKeyword(), StandardCharsets.UTF_8);
      String apiUrl = newsApiUrl + "?query=" + query + "&display=20&sort=date";

      HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("X-Naver-Client-Id", clientId);
      con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

      int responseCode = con.getResponseCode();
      if (responseCode != 200) {
        log.error("뉴스 수집 실패: 상태 코드 {}", responseCode);
        throw new RestException(ErrorCode.INTERNAL_SERVER_ERROR, Map.of(
            "interest", interest.getName(),
            "keyword", keyword.getKeyword(),
            "status", responseCode
        ));
      }

      try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
        String response = br.lines().collect(Collectors.joining());
        return parseArticles(response);
      }

    } catch (Exception e) {
      log.error("뉴스 수집 중 예외 발생: {}", e.getMessage());
      throw new RestException(ErrorCode.INTERNAL_SERVER_ERROR,
          Map.of("interest", interest.getName(), "keyword", keyword.getKeyword(), "error",
              e.getMessage()));
    }
  }

  private List<CollectedArticleDto> parseArticles(String json) {
    JSONObject jsonObject = new JSONObject(json);
    JSONArray items = jsonObject.getJSONArray("items");
    DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

    return IntStream.range(0, items.length())
        .mapToObj(i -> {
          JSONObject item = items.getJSONObject(i);
          String rawPubDate = item.getString("pubDate");

          LocalDateTime publishDate;
          try {
            ZonedDateTime parsedDate = ZonedDateTime.parse(rawPubDate, formatter);
            publishDate = parsedDate.toLocalDateTime();
          } catch (Exception e) {
            log.warn("발행일자 파싱 실패: {}, 현재 시간으로 대체합니다.", rawPubDate);
            publishDate = LocalDateTime.now();
          }

          String title = Jsoup.parse(item.getString("title")).text();
          String summary = Jsoup.parse(item.getString("description")).text();
          String sourceUrl = item.optString("originallink", item.getString("link"));

          return CollectedArticleMapper.toDto(title, summary, sourceUrl, sourceName, publishDate);
        })
        .collect(Collectors.toList());
  }
}