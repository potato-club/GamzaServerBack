# 🥔 Gmaza Club 

## 📚 프로젝트 개요
- 감자 동아리 주니어 개발자들이 배포에서 많은 어려움을 겪어 코드개발에 시간을 많이 쏟지 못하는 문제점이 발생함
- Docker OpenAPI를 활용하여 프론트 코드파일, 백엔드 서버(.jar)파일을 올리면 자동으로 서버가 열리는 개발 사이트를 구상함
- 감자 내 프로젝트를 편리하게 배포함으로써 프로젝트까지 관리할 수 있는 포트폴리오 관리도 가능한 사이트까지 구상함

## 🛠️ 기술 스택

### 💻 Back-End
- Java
- Spring Framework
- Spring Security
- Restful API

### 💻 Database
- MySQL
- Redis

### 💻 DevOps
- AWS EC2, RDS, Route53, S3
- Docker
- Docker OpenAPI
- Nginx
- Nohup

### 💻 Tools
- Git
- Github
- Postman
- Swagger 

## 📍 ERD
<img width="800" alt="image" src="https://github.com/user-attachments/assets/6ddfcf4f-c522-4eb2-81b3-e2edddfa02d9" />

## 📱 서비스 소개

### Screen UI
----

### 로그인 & 회원가입
- 프로젝트 내부 회원가입 / 로그인 로직만 이용 가능
- 기본 회원가입 사용자는 어드민 승인을 받기 전까지 User 권한으로 프로젝트 읽기만 가능
- 회원가입시 학사 정보를 입력받게 됨
<img width="400" alt="image" src="https://github.com/user-attachments/assets/9294ace2-66d6-439c-9964-252bcc354094" />
<img width="400" alt="image" src="https://github.com/user-attachments/assets/a5b7bcd6-e52b-4636-9445-1469fdd694a4" />
<img width="400" alt="image" src="https://github.com/user-attachments/assets/5c47a61d-9481-4b7f-870c-d24f958ff6ce" />
<img width="400" alt="image" src="https://github.com/user-attachments/assets/5187c8a0-f417-44bc-94a3-6eada6c6355f" />

----

### 프로젝트 관리
- 회원인 유저 모두 현재 진행중인 프로젝트를 볼 수 있다
- 프로젝트에 참여한 인원들은 로그와 프로젝트 수정을 진행할 수 있다.
<img width="400" alt="image" src="https://github.com/user-attachments/assets/4eebc1a7-0c7e-4043-8a14-ff126b2606b5" />
<img width="400" alt="image" src="https://github.com/user-attachments/assets/3999f263-c684-443c-83c2-077e3b96b892" />

----

### 어드민 권한) 프로젝트 승인, 유저 승인
- 어드민 권한을 가진 유저만 회원 등급을 'MEMBER'로 바꿀 수 있다.
- 어드민의 프로젝트 승인 허가가 있어야지 서버내에서 Dockerfile, 프론트 프로젝트 파일을 실행시킨다.
<img width="400" alt="image" src="https://github.com/user-attachments/assets/5fead4ca-acff-4db0-8ef7-c3f3e69308ec" />
<img width="400" alt="image" src="https://github.com/user-attachments/assets/ce622275-9923-424b-95f5-dcd49072db85" />

----
### 프로젝트 생성
- 프로젝트 생성시 이름, 상태, 생성시기, 데드라인, 포트, 환경변수, 포트 값을 입력받아 생성한다.
<img width="300" alt="image" src="https://github.com/user-attachments/assets/78980e86-a68b-41f5-bb13-4b9f849fb48a" />
<img width="300" alt="image" src="https://github.com/user-attachments/assets/8c9b8cad-d365-400d-b133-917bf8e6e75a" />
<img width="300" alt="image" src="https://github.com/user-attachments/assets/82f4ea14-4dec-4d99-9d36-d0b47f99ddec" />

----

### 프로젝트 재등록
- 프로젝트 재등록은 서버파일 재등록 / 프로젝트 이름, 설명, 멤버 등.. 수정하는 것으로 나뉜다.
- 프로젝트 서버파일 재등록은 어드민의 승인이 있어야만 실행된다.
<img width="400" alt="image" src="https://github.com/user-attachments/assets/6d3504c8-3617-4171-8c35-3d02b16559c0" />
<img width="400" alt="image" src="https://github.com/user-attachments/assets/9ed6edec-32ca-4be2-8be7-5475bbde08c2" />



### 🍀 프론트 Github

Front Github : <https://github.com/potato-club/gamza_club>










### 



