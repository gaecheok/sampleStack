# 작성코드 부가설명

- JWT
  - secretKey와 만료시간 설정은 application.properties 을 참고하도록 하였습니다.
- DB
  - 스프링부트에서 제공하는 기본 H2 메모리 디비를 이용하도록 별도 설정을 하지 않았습니다.
- 주민등록번호
  - AES 양방향 암호화를 이용했습니다
- 비밀번호
  - BcryptPasswordEncoder 를 이용해 해싱 하였습니다.
- 외부 API 호출
  - @Async 를 이용하여 별도 ThreadPoolExecutor 에서 실행하도록 하였습니다.
  - 성능이나 재시도 등이 중요한 상황이라면 별도 서비스에서 동작하도록 변경이 필요할거 같습니다.