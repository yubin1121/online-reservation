package kr.co.module.api.Controller;

import kr.co.module.api.admin.dto.CategoryCreateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
)
public class AdminCategoryControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createCategory_success() {
        String url = "http://localhost:" + port + "/admin/category/register";

        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .adminId("test")
                .categoryOrder(1)
                .categoryName("test")
                .categoryDesc("test")
                .build();


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CategoryCreateDto> request = new HttpEntity<>(createDto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        // 결과 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }


}
