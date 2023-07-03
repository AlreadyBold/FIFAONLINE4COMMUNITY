package com.fifatoy.SocialLoginTestController;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ObjectBuffer;
import com.fifatoy.service.GoogleOauthParam;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/social")
public class socialLoginTestController<googleOauthParams> {

    @Value("${KakaoApiKey}")
    private String kakaoApiKey;

    @Value("${KakaoRedirectURL}")
    private String KakaoRedirectURL;

    /**
     * kakao callback
     * [GET] /social/kakaotest
     */
    @ResponseBody
    @GetMapping("/kakaotest")

    public void kakaoCallback(@RequestParam String code) {
        // 인가 코드
        // 컨트롤러 접속 시 로그인 후 인가코드 발급확인
        System.out.println(code);

        // accessToken 받기
        String access_Token = "";
        String refresh_Token = "";
        // Token 발급 url
        String reqURL = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // POST 요청을 위해 기본값이 false인 setDoOutput을 true로
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=" + kakaoApiKey); // TODO REST_API_KEY 입력
            sb.append("&redirect_uri=" + KakaoRedirectURL); // TODO 인가코드 받은 redirect_uri 입력
            sb.append("&code=" + code);
            bw.write(sb.toString());
            bw.flush();

            // 결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            // 요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            // Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            access_Token = element.getAsJsonObject().get("access_token").getAsString();
            refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();

            System.out.println("access_token : " + access_Token);
            System.out.println("refresh_token : " + refresh_Token);

            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(access_Token);

        String reqURL2 = "https://kapi.kakao.com/v2/user/me";

        // access_token을 이용하여 사용자 정보 조회
        try {
            URL url = new URL(reqURL2);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + access_Token); // 전송할 header 작성, access_token전송

            // 결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            // 요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * 결과
         * response body :
         * {
         * "id":2883786726,
         * "connected_at":"2023-07-02T15:23:48Z",
         * "properties":
         * {"nickname":"이준형",
         * "profile_image":
         * "http://k.kakaocdn.net/dn/iSN9j/btslafjB4C8/3OSpThTQw7AU4AT4380a30/img_640x640.jpg",
         * "thumbnail_image":
         * "http://k.kakaocdn.net/dn/iSN9j/btslafjB4C8/3OSpThTQw7AU4AT4380a30/img_110x110.jpg"
         * },
         * "kakao_account":{
         * "profile_nickname_needs_agreement":false,
         * "profile_image_needs_agreement":false,
         * "profile":
         * {"nickname":"이준형",
         * "thumbnail_image_url":
         * "http://k.kakaocdn.net/dn/iSN9j/btslafjB4C8/3OSpThTQw7AU4AT4380a30/img_110x110.jpg",
         * "profile_image_url":
         * "http://k.kakaocdn.net/dn/iSN9j/btslafjB4C8/3OSpThTQw7AU4AT4380a30/img_640x640.jpg",
         * "is_default_image":false
         * },
         * "has_email":true,
         * "email_needs_agreement":false,
         * "is_email_valid":true,
         * "is_email_verified":true,
         * "email":"junheong@nate.com",
         * "has_age_range":true,
         * "age_range_needs_agreement":false,
         * "age_range":"20~29",
         * "has_birthday":true,
         * "birthday_needs_agreement":false,
         * "birthday":"0623",
         * "birthday_type":"SOLAR",
         * "has_gender":true,
         * "gender_needs_agreement":false,"gender":"male"
         * }
         * }
         */
    }

    @Value("${GoogleClientId}")
    private String GoogleClientId;

    @Value("${GoogleClientSecret}")
    private String GoogleClientSecret;

    @Value("${GoogleRedirectURL}")
    private String GoogleRedirectURL;

    /**
     * google callback
     * [GET] /social/googletest
     */
    @ResponseBody
    @GetMapping("/googletest")
    public void googleCallback(@RequestParam String code) {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", GoogleClientId);
        params.add("client_secret", GoogleClientSecret);
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", GoogleRedirectURL);

        HttpEntity<MultiValueMap<String, String>> accessTokenRequest = new HttpEntity<>(params, headers);

        ResponseEntity<String> accessTokenResponse = rt.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                accessTokenRequest,
                String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        GoogleOauthParam googleOauthParams = null;

        try {
            googleOauthParams = objectMapper.readValue(accessTokenResponse.getBody(), GoogleOauthParam.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        HttpHeaders headers1 = new HttpHeaders();
        headers1.add("Authorization", "Bearer " + googleOauthParams.getAccess_token());
        System.out.println(headers1);

        HttpEntity profileRequest = new HttpEntity(headers1);

        /*
         * <200 OK OK,
         * {"access_token":
         * "ya29.a0AbVbY6NmxPU3cQftcs4iNGkjFCYNvsLENpff_2mpCFGa5xfLfjGNx9eiafIL6V0jJ1X8-Uvrrx9xDJPBMg7Z962uccNRyw_xaVH1NOM2C2kCPfpkpJLo7Q-B58uMyfdkzIipriEqr46VTe-z2XzJmM9QE4V4aCgYKAaISARESFQFWKvPlXqEw6S2VMqqveqIOU270Gw0163",
         * "scope":"https:\/\/www.googleapis.com\/auth\/drive.metadata.readonly",
         * "token_type":"Bearer",
         * "expires_in":3599}
         * ,[Cache-Control:"no-cache, no-store, max-age=0, must-revalidate",
         * Date:"Mon, 03 Jul 2023 06:30:32 GMT",
         * Expires:"Mon, 01 Jan 1990 00:00:00 GMT",
         * Pragma:"no-cache",
         * Content-Type:"application/json; charset=utf-8",
         * Vary:"X-Origin",
         * "Referer",
         * "Origin,Accept-Encoding",
         * Server:"scaffolding on HTTPServer2",
         * X-XSS-Protection:"0",
         * X-Frame-Options:"SAMEORIGIN",
         * X-Content-Type-Options:"nosniff",
         * Alt-Svc:"h3=":443"; ma=2592000,h3-29=":443"; ma=2592000",
         * Accept-Ranges:"none",
         * Transfer-Encoding:"chunked"]>
         */

        ResponseEntity<JsonObject> profileResponse = rt.exchange(
                "https://oauth2.googleapis.com/tokeninfo?id_token=",
                HttpMethod.GET,
                profileRequest,
                JsonObject.class);

        System.out.println("profileResponse == " + profileResponse);
        System.out.println(googleOauthParams.getId_token());

    }

    @Value("${NaverClientId}")
    private String NaverClientId;

    @Value("${NaverClientSecret}")
    private String NaverClientSecret;

    private String NaverSESSION_STATE = "oauth_state";

    /* 프로필 조회 API URL */
    private String PROFILE_API_URL = "https://openapi.naver.com/v1/nid/me";

    /**
     * naver callback
     * [GET] /social/navertest
     */
    @ResponseBody
    @GetMapping("/navertest")
    public void naverCallback(@RequestParam String code, @RequestParam String state) {

        RestTemplate rt = new RestTemplate();

        HttpHeaders accessTokenHeaders = new HttpHeaders();
        accessTokenHeaders.add("Content-type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> accessTokenParams = new LinkedMultiValueMap<>();
        accessTokenParams.add("grant_type", "authorization_code");
        accessTokenParams.add("client_id", NaverClientId);
        accessTokenParams.add("client_secret", NaverClientSecret);
        accessTokenParams.add("code", code); // 응답으로 받은 코드
        accessTokenParams.add("state", NaverSESSION_STATE); // 응답으로 받은 상태

        HttpEntity<MultiValueMap<String, String>> accessTokenRequest = new HttpEntity<>(accessTokenParams,
                accessTokenHeaders);

        ResponseEntity<JSONObject> accessTokenResponseJson = rt.exchange(
                "https://nid.naver.com/oauth2.0/token",
                HttpMethod.POST,
                accessTokenRequest,
                JSONObject.class);

        System.out.println("accessTokenResponse ==== " +
                accessTokenResponseJson.getBody().get("access_token"));

        String accessTokenResponse = (String) accessTokenResponseJson.getBody().get("access_token");

        // header를 생성해서 access token을 넣어줍니다.
        HttpHeaders profileRequestHeader = new HttpHeaders();
        profileRequestHeader.add("Authorization", "Bearer " + accessTokenResponse);

        HttpEntity<HttpHeaders> profileHttpEntity = new HttpEntity<>(profileRequestHeader);

        // profile api로 생성해둔 헤더를 담아서 요청을 보냅니다.
        ResponseEntity<String> profileResponse = rt.exchange(
                PROFILE_API_URL,
                HttpMethod.POST,
                profileHttpEntity,
                String.class);

        System.out.println(profileResponse);
        /*
         * 결과조회
         * "response":
         * {
         * "id":"VaZTe11QQS3hPPyAayRlBxVxaT_QtgwrFO3hAL7RfMc" ,
         * "nickname":"HUMON",
         * "profile_image":
         * "https:\/\/phinf.pstatic.net\/contact\/20220512_297\/165232881520953nCk_PNG\/avatar_profile.png",
         * "age":"20-29",
         * "gender":"M",
         * "email":"1187410@naver.com",
         * "mobile":"010-6388-9706",
         * "mobile_e164":"+821063889706",
         * "name":"\uc774\uc900\ud615" ( UNICODE ),
         * "birthday":"06-23",
         * "birthyear":"1997"
         * }
         */

    }

}
