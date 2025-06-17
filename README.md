# 모뉴(MoNew) – 다양한 출처의 뉴스를 관심사에 맞게 통합하는 개인 맞춤형 뉴스 플랫폼

---
## 📝 프로젝트 소개
### 📰 여러 뉴스, 하나의 플랫폼에서 관심 주제만 골라보세요!

**모뉴(MoNew)** 는 모두의 뉴스 줄임말로 Naver API, 조선일보 등 다양한 뉴스 소스를 하나로 통합해 사용자의 관심사에 따라 뉴스를 큐레이션하는 개인 맞춤형 뉴스 플랫폼입니다.

<img width="100%" alt="모뉴 대시보드" src="https://github.com/user-attachments/assets/0265d8f0-bca1-44b8-8eaa-4f5790fd3a58">

### 📌 프로젝트 정보

| 항목 | 내용                                                                                             |
|------|------------------------------------------------------------------------------------------------|
| **📆 프로젝트 기간** | 2025.05.28 ~ 2025.06.18                                                                        |
| **🔗 배포 링크** | [모두의 뉴스, 모뉴](http://monew-loadbalancer-1478949927.ap-northeast-2.elb.amazonaws.com/login)      |
| **📋 협업 문서** | [Notion 페이지](https://tabby-store-8af.notion.site/201be05ff34b8088b901e60c410bf360)             |
| **📘 API 문서** | [Swegger 문서](http://monew-loadbalancer-1478949927.ap-northeast-2.elb.amazonaws.com/swagger-ui/index.html) |

회원가입 시 비밀번호에 특수문자 하나 이상을 꼭 포함하세요!

## 🛠️ 기술 스택

### 백엔드
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Web](https://img.shields.io/badge/Spring_Web-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white)


### 데이터 액세스
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Data MongoDB](https://img.shields.io/badge/Spring_Data_MongoDB-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-007ACC?style=for-the-badge&logo=java&logoColor=white)
![JDBC](https://img.shields.io/badge/JDBC-007396?style=for-the-badge&logo=java&logoColor=white)

### 데이터베이스
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)

### 클라우드/인프라
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)
![AWS ECS](https://img.shields.io/badge/AWS_ECS-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white)
![AWS ECR](https://img.shields.io/badge/AWS_ECR-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS_RDS-D22128?style=for-the-badge&logo=amazonaws&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

### 코드 품질 및 생산성
![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge&logo=java&logoColor=white)

### 테스트
![JUnit Jupiter](https://img.shields.io/badge/JUnit_Jupiter-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![TestContainers](https://img.shields.io/badge/TestContainers-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![H2](https://img.shields.io/badge/H2_Database-0074BD?style=for-the-badge&logo=h2&logoColor=white)

### 모니터링/관리
![Spring Actuator](https://img.shields.io/badge/Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### 협업
![Jira](https://img.shields.io/badge/Jira-326CE5?style=for-the-badge&logo=jira&logoColor=white)
![Apache Commons](https://img.shields.io/badge/Apache_Commons-D22128?style=for-the-badge&logo=apache&logoColor=white)



## 👥 팀원 구성 및 R&R

| 이름                                     | 역할 및 기여                                                                                                                                                                                                           |
|----------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [양성준(팀장)](https://github.com/GogiDosirak) | • Interest / Subscription 도메인 설계<br> • 관심사 목록 QueryDsl 기반 페이지네이션 구현<br> • 유저 활동 내역 관리 도메인 설계<br> • CQRS를 이용한 활동 내역 조회 최적화<br> • 이벤트 기반 구독/기사 조회 쿼리 모델 동기화<br> • Scheduler를 통한 Query <-> Command 모델 데이터 동기화 후보정 작업 |
| [박상혁](https://github.com/manleKim)     | • Comment / CommentLike 도메인 설계<br>• 댓글 목록 QueryDsl 기반 페이지네이션 구현<br>• 유저 활동 내역 관리 도메인 설계<br>• CQRS를 이용한 활동 내역 조회 최적화<br>• 이벤트 기반 댓글/댓글 좋아요 쿼리 모델 동기화<br>• Scheduler를 통한 Query <-> Command 모델 데이터 동기화 후보정 작업        |
| [이경빈](https://github.com/Leekb0804)       | • User, Notification 도메인 설계<br> • Spring Security를 통한 헤더 인증 구현<br> • Spring Event를 통한 알림 이벤트 발행<br> • Scheduler를 통한 기사 일괄 삭제<br> • QueryDsl을 통한 알림 목록 페이지네이션 구현<br>                                               |
| [이유나](https://github.com/nayu-yuna)    | • Article 도메인 설계<br>• 기사 RSS 및 API 수집 구현<br>• 기사 목록 QueryDsl 기반 페이지네이션 구현<br>• 이벤트 기반 ArticleInterest 설계<br>• Scheduler를 통한 S3 백업 배치 구현<br>• 날짜 기반 데이터 무결성 검증 및 복원                                                |

## 🏗️ 시스템 아키텍처
![시스템 아키텍처](https://github.com/user-attachments/assets/65d96612-a5b4-4f23-9a60-9aeaa64c13a0)

모뉴 시스템은 다음과 같은 아키텍처로 구성되어 있습니다.

1. **CI/CD 파이프라인**
- GitHub을 통한 코드 관리
- GitHub Actions를 활용한 자동화된 테스트 및 배포
- AWS ECR을 통한 컨테이너 이미지 관리

2. **백엔드 시스템**
- Amazon Fargate를 활용한 컨테이너 기반 서비스 배포
- MongoDB와 PostgreSQL을 활용한 데이터 저장
- AWS S3를 활용한 기사 백업 관리
- Scheduler를 통한 정기적인 데이터 처리 및 백업 작업

3. **데이터베이스 구조**
- PostgreSQL: 관계형 데이터를 위한 주 데이터베이스
- MongoDB: 사용자 활동 내역과 같은 조회 최적화를 위한 비관계형 데이터베이스

## 💾 데이터베이스 스키마

### PostgreSQL
<img src="https://private-user-images.githubusercontent.com/154963143/455775971-912e8a5a-33b5-417f-a72a-429b24299e99.png?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NTAxMjUxNzcsIm5iZiI6MTc1MDEyNDg3NywicGF0aCI6Ii8xNTQ5NjMxNDMvNDU1Nzc1OTcxLTkxMmU4YTVhLTMzYjUtNDE3Zi1hNzJhLTQyOWIyNDI5OWU5OS5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BS0lBVkNPRFlMU0E1M1BRSzRaQSUyRjIwMjUwNjE3JTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI1MDYxN1QwMTQ3NTdaJlgtQW16LUV4cGlyZXM9MzAwJlgtQW16LVNpZ25hdHVyZT0yZTBlNzU2NGVhYTJmMmI4NmRhMDZlYzM3YTk2ODE2ZmQyZTYyNjUxZDk3Yzk5OTRjZTVmMDljYWRiYjA4MGM2JlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCJ9.ajTAaOLze0lVW-sxorT0yv0mQjUiwM210I1iukQ7_R0" width="650" alt="PostgreSQL ERD">

### MongoDB
<img src="https://private-user-images.githubusercontent.com/154963143/455808724-a5d3694c-8f57-4c1a-a502-3b0a2105bfce.png?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NTAxMzM3NDAsIm5iZiI6MTc1MDEzMzQ0MCwicGF0aCI6Ii8xNTQ5NjMxNDMvNDU1ODA4NzI0LWE1ZDM2OTRjLThmNTctNGMxYS1hNTAyLTNiMGEyMTA1YmZjZS5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BS0lBVkNPRFlMU0E1M1BRSzRaQSUyRjIwMjUwNjE3JTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI1MDYxN1QwNDEwNDBaJlgtQW16LUV4cGlyZXM9MzAwJlgtQW16LVNpZ25hdHVyZT0xMTc2NWQ4Y2YxNDcyZjY5MmY4YWM0ZGRiMWI4OTFjMTNlM2MzZWNhZTkzNTY0NThmYzI2ZWUzZjE0MDllNjE4JlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCJ9.cOWqLB58BzmGyfwdp3JfX4J3rUl4zDQYIE6j0P-UnU4" width="650" alt="MongoDB 스키마">

## 🚀 주요 기능

### 사용자 관리
- 회원가입, 로그인, 닉네임 수정 기능
- 논리적 삭제 지원으로 데이터 무결성 유지
- Spring Security를 통한 헤더 id 검사

### 관심사 관리
- 관심사 등록, 수정, 삭제 기능
- 커서 기반 페이지네이션 + QueryDSL을 이용한 정렬 및 검색
- 키워드 기반 관심사 설정
- 관심사 구독 시스템
- 유사도 기반 중복 관심사 등록 방지

### 뉴스 기사 관리
- 다양한 출처(API, RSS 등)에서 관심사 키워드 기반 뉴스 기사 수집
- 관심사, 출처, 날짜 등 조건 복합 필터링 및 제목/요약 검색 지원
- 날짜, 댓글 수, 조회 수 기준의 단일 정렬 및 커서 기반 페이지네이션 구현 (QueryDSL 활용)
- 논리 삭제 기본, 물리 삭제 시 관련 데이터 일괄 삭제 지원
- 매일 단위 AWS S3 백업 및 배치 작업으로 데이터 안정성 확보
- 백업 데이터와 현행 데이터 비교를 통한 날짜별 유실 데이터 복구 기능

### 댓글 관리
- 기사별 댓글 등록, 수정, 삭제
- 댓글 좋아요 등록 및 취소
- 최신순 정렬 커서 기반 페이지네이션
- 댓글 작성/수정/삭제 시 사용자 활동 내역과 연동

### 활동 내역 관리
- MongoDB를 활용한 사용자 활동 내역 조회 최적화
- 구독 중인 관심사, 최근 작성 댓글, 좋아요, 조회 기사 추적
- CQRS 패턴을 통한 활동 내역 조회 최적화
- 이벤트 기반으로 활동 내역 쿼리 모델 동기화
- Scheduler를 통한 Query <-> Command 모델 데이터 동기화 후보정 작업
- Spring Retry 기반 재시도 로직으로 이벤트 및 스케줄링 누락 최소화

### 알림 관리
- 관심사 관련 새 기사, 댓글 좋아요 알림
- 알림 개별 확인 및 전체 확인
- 알림 자동 삭제 배치 처리

## 💬 Commit Message Convention
**형식**: `[타입]: [지라칸반코드] [설명]`

| 타입     | 사용 시나리오                                    |
|----------|--------------------------------------------|
| `feat`   | 새로운 기능을 추가한 경우                             |
| `fix`    | 버그를 고친 경우                                  |
| `refactor`| 성능 개선 및 코드 리팩토링, 파일 또는 디렉토리명 수정, 경로 변경한 경우 |
| `test`	 | 테스트 코드, 리팩토링 테스트 코드 추가                     |
| `style`	 | 코드 포맷을 변경한 경우                              |
| `docs`	 | 문서를 작성 및 수정한 경우                            |
| `remove`	 | 파일 삭제한 경우                                  |
| `chore`  | 의존성이나 설정을 변경한 경우                           |

## 🌿 Branch Naming Convention

**형식**: `[타입]/[지라칸반코드]-[브랜치설명]`

| 라벨 유형      | 접두사        | 예시                         | 비고      |
|------------|------------|----------------------------|---------|
| `feat`     | `feat`     | `feat/KAN-15-댓글-관리`        | 신기능 개발  |
| `fix`      | `fix`      | `fix/KAN-230-1차-스프린트-fix`  | 긴급 수정   |
| `refactor` | `refactor` | `refactor/KAN-371-코드-리팩토링` | 리팩토링 작업 |
| `test`     | `test`     | `test/KAN-330-테스트-코드-관리`   | 테스트 작업  |
| `docs`     | `docs`     | `docs/KAN-402-readme-작성`   | 문서 작업   |


### 🔄 Branch 전략
![깃 브랜치 전략](https://github.com/user-attachments/assets/4122e52c-8931-4feb-adab-3eb63bfd92c1)

### 📊 지라 칸반보드
<img width="470" alt="지라 칸반보드" src="https://github.com/user-attachments/assets/d6a8f1ce-3030-4e13-992a-3a2efddd329b" />