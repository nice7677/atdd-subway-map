package subway;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import subway.line.LineResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@DisplayName("지하철 노선 관련 기능")
@Sql("/insert-station.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LineAcceptanceTest extends LineAcceptConstants {

    /**
     * When 지하철 노선을 생성하면
     * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다
     */
    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLineTest() {
        // given
        지하철_노선_생성(이호선);

        // when
        List<String> lineNames = 지하철_노선_이름_목록_조회();

        // then
        assertThat(lineNames.size()).isEqualTo(1);
        assertThat(lineNames).containsOnly((String) 이호선.get(LINE_NAME));
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLinesTest() {
        // given
        지하철_노선_생성(이호선);
        지하철_노선_생성(신분당선);

        // when
        final List<String> lineNames = 지하철_노선_이름_목록_조회();

        // then
        assertThat(lineNames.size()).isEqualTo(2);
        assertThat(lineNames).containsOnly((String) 신분당선.get(LINE_NAME), (String) 이호선.get(LINE_NAME));
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @DisplayName("지하철 노선을 단건 조회 한다.")
    @Test
    void getLineTest() {
        // given
        지하철_노선_생성(이호선);

        // when
        final LineResponse lineResponse = 지하철_노선_단건_조회();

        // then
        assertThat(lineResponse.getName()).isEqualTo((String) 이호선.get(LINE_NAME));
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다
     */
    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLineTest() {
        // given
        지하철_노선_생성(신분당선);

        // when
        final String newLineName = "구분당선";
        final String newLinColor = "bg-red-600";
        지하철_노선_수정(newLineName, newLinColor);

        final LineResponse lineResponse = 지하철_노선_단건_조회();

        // then
        assertThat(lineResponse.getName()).isEqualTo(newLineName);
        assertThat(lineResponse.getColor()).isEqualTo(newLinColor);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 삭제하면
     * Then 해당 지하철 노선 정보는 삭제된다
     */
    @DisplayName("지하철 노선을 삭제한다.")
    @Test
    void deleteLineTest() {
        // given
        지하철_노선_생성(신분당선);

        // when
        지하철_노선_삭제();

        // then
        final List<LineResponse> lines = 지하철_노선_목록_조회();
        assertThat(lines.isEmpty()).isTrue();
    }

    private void 지하철_노선_생성(final Map<String, Object> line) {
        RestAssured
                .given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .body(line)
                .when()
                    .post("/lines")
                .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("name", Matchers.equalTo(line.get(LINE_NAME)));
    }

    private List<LineResponse> 지하철_노선_목록_조회() {
        return RestAssured
                .given()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get("/lines")
                .then()
                    .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList("", LineResponse.class);
    }

    private List<String> 지하철_노선_이름_목록_조회() {
        return 지하철_노선_목록_조회().stream()
                .map(LineResponse::getName)
                .collect(Collectors.toList());
    }

    private static LineResponse 지하철_노선_단건_조회() {
        return RestAssured
                .given()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get("/lines/{id}", 1)
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(APPLICATION_JSON_VALUE)
                .extract()
                .jsonPath()
                .getObject("", LineResponse.class);
    }

    private void 지하철_노선_수정(final String newLineName, final String newLinColor) {
        RestAssured
                .given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(Map.of(LINE_NAME, newLineName, LINE_COLOR, newLinColor))
                .when()
                   .put("/lines/{id}", 1)
                .then()
                   .statusCode(HttpStatus.OK.value());
    }

    private void 지하철_노선_삭제() {
        RestAssured
                .given()
                .when()
                    .delete("/lines/{id}", 1)
                .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
    }
}