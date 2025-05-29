package com.team1.monew.interest.dto;

import java.util.List;

public record InterestRegisterRequest(
    String name,
    List<String> keywords
){

}