package study.nexon_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CharacterController {

    @Value("${nexon.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @GetMapping("/maplestory/v1/id")
    public String getCharacterId(@RequestParam("character_name") String characterName, Model model) {
        try {
            //URI 생성 및 URL 인코딩
            String encodedCharacterName = URLEncoder.encode(characterName, StandardCharsets.UTF_8.toString());
            log.info("Encoded character name: {}", encodedCharacterName);
            String urlString = "https://open.api.nexon.com/maplestory/v1/id?character_name=" + encodedCharacterName;
            URI url = new URI(urlString);

            //HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-nxopen-api-key", apiKey);

            //HTTP 요청 구성
            HttpEntity<String> entity = new HttpEntity<>(headers);

            //API 호출
            log.info("Calling API with URL: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            log.info("Response code: {}", response.getStatusCodeValue());
            log.info("Response body: {}", response.getBody());

            //응답 상태 코드 확인
            if (response.getStatusCodeValue() == 200) {
                String responseBody = response.getBody();
                //JSON 응답에서 ocid 추출
                String ocid = extractOcidFromResponse(responseBody);

                log.info("ocid: {}", ocid);
                model.addAttribute("ocid", ocid);
            } else if (response.getStatusCodeValue() == 400) {
                model.addAttribute("error", "존재하지 않는 캐릭터명입니다.");
                log.error("Invalid character name. Status code: 400");
            } else {
                model.addAttribute("error", "잘못된 요청입니다. 다시 시도해주세요. 에러 메시지: " + response.getStatusCodeValue());
                log.error("Failed to retrieve character ID. Status code: {}", response.getStatusCodeValue());
            }
        } catch (HttpClientErrorException e) {
            //400 상태 코드 처리
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                model.addAttribute("error", "존재하지 않는 캐릭터명입니다.");
                log.error("Invalid character name. Status code: 400");
            } else {
                model.addAttribute("error", "잘못된 요청입니다. 다시 시도해주세요. 에러 메시지: " + e.getMessage());
                log.error("An error occurred", e);
            }
        } catch (Exception e) {
            model.addAttribute("error", "오류가 발생했습니다: " + e.getMessage());
            log.error("An error occurred", e);
        }
        return "result";
    }

    //JSON 응답에서 ocid를 추출하는 메서드
    private String extractOcidFromResponse(String responseBody) {
        try {
            //ObjectMapper를 사용하여 JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            //"ocid" 필드 값 추출
            JsonNode ocidNode = jsonNode.get("ocid");
            return ocidNode != null ? ocidNode.asText() : "ocid not found";
        } catch (Exception e) {
            log.error("Error parsing JSON response", e);
            return "JSON 응답을 파싱하는 중 오류 발생: " + e.getMessage();
        }
    }
}