# YouTube API Data Fetcher

YouTube 데이터 수집기는 YouTube에서 원하는 키워드를 검색하고 검색 결과를 추출하여 비디오 및 채널 정보를 가져오는 프로그램입니다.

![image](https://github.com/KathleenJung/youtube_crawler/assets/85939045/d26f6193-62a8-492f-88c3-29ab2d79f2c1)

## 개발 환경

- Java 17
- IntelliJ IDEA 2023

## 종속성

- Lombok
- JSON Simple

## 폴더 구조

![image](https://github.com/KathleenJung/youtube_crawler/assets/85939045/64b7c772-df3c-4b73-bf0a-9c09270491bb)


## 사용 방법

1. `config` 폴더를 만듭니다.
2. `config` 폴더 안에 `application.properties` 파일을 만들고 하나 이상의 YouTube API 키를 추가합니다:

   ```properties
   api_key1=여러분의API키1
   api_key2=여러분의API키2
   api_key3=여러분의API키3
   # 필요한 경우 더 많은 API 키를 추가하세요.
   ```
3. `config` 폴더 내에 `data.csv` 파일을 생성합니다.
4. `config` 폴더 내에 `keywords.txt` 파일을 생성하고 수집하려는 키워드를 줄바꿈으로 나누어 나열합니다.
5. `Main.java`를 실행합니다.
6. `config` 폴더 내에 위치한 `data.csv` 파일을 엽니다.
7. 텍스트 편집기를 사용하여 파일을 열고 ANSI 인코딩으로 저장합니다. 파일 이름을 변경하고 CSV 파일로 저장할 수 있습니다.
8. `config` 폴더 내에서 방금 저장한 파일을 엽니다.

## 참고 사항
출력이 CSV 형식이기 때문에 데이터의 줄 바꿈(\n)이나 쉼표(,)는 적절한 서식을 위해 모두 공백으로 대체되었습니다.
