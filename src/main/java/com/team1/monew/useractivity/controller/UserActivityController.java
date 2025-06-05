package com.team1.monew.useractivity.controller;


import com.team1.monew.useractivity.dto.UserActivityDto;
import com.team1.monew.useractivity.service.UserActivityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user-activities")
@AllArgsConstructor
public class UserActivityController {

  private UserActivityService userActivityService;

  @GetMapping("/{userId}")
  public ResponseEntity<UserActivityDto> findUserActivity(@PathVariable Long userId){
    return ResponseEntity
        .ok(userActivityService.findUserActivity(userId));
  }
}
