package com.team1.monew.useractivity.scheduler;

import com.team1.monew.useractivity.scheduler.service.ArticleViewActivityBatchService;
import com.team1.monew.useractivity.scheduler.service.CommentActivityBatchService;
import com.team1.monew.useractivity.scheduler.service.CommentLikeActivityBatchService;
import com.team1.monew.useractivity.scheduler.service.SubscriptionActivityBatchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class UserActivitySyncScheduler {

    private final CommentActivityBatchService commentActivityBatchService;
    private final CommentLikeActivityBatchService commentLikeActivityBatchService;
    private final ArticleViewActivityBatchService articleViewActivityBatchService;
    private final SubscriptionActivityBatchService subscriptionActivityBatchService;

    @Scheduled(cron = "0 0 0,12 * * *")
    @SchedulerLock(name = "batchUserActivitySync", lockAtMostFor = "10m")
    public void batchUserActivitySync ()
    {
        log.info("[배치 시작] 사용자 활동 MongoDB 동기화");

        commentActivityBatchService.syncAll();
        commentLikeActivityBatchService.syncAll();
        articleViewActivityBatchService.syncAll();
        subscriptionActivityBatchService.syncAll();

        log.info("[배치 종료] 사용자 활동 MongoDB 동기화 완료");
    }
}


