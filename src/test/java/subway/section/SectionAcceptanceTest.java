package subway.section;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import subway.common.AcceptanceTestTruncateListener;
import subway.common.AssertUtil;
import subway.exception.SectionAlreadyCreateStationException;
import subway.exception.SectionBadRequestException;
import subway.exception.SectionUpStationNotMatchException;
import subway.line.LineConstant;
import subway.line.LineCreateRequest;
import subway.line.LineStep;
import subway.station.StationConstant;
import subway.station.StationResponse;
import subway.station.StationStep;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


@DisplayName("지하철 구간 관리 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestExecutionListeners(value = {AcceptanceTestTruncateListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class SectionAcceptanceTest {
    public static final String BASE_URL = "/sections";

    long upStationId;
    long downStationId;
    long sectionDownStationId;
    long lineId;

    @BeforeEach
    void setUp() {
        upStationId = StationStep.extractStationId(StationStep.createSubwayStation(StationConstant.GANGNAM));
        downStationId = StationStep.extractStationId(StationStep.createSubwayStation(StationConstant.YANGJAE));

        sectionDownStationId = StationStep.extractStationId(StationStep.createSubwayStation(StationConstant.JUNGGYE));

        ExtractableResponse<Response> lineResponse =
                LineStep.createLine(new LineCreateRequest(LineConstant.NAME_VALUE1, LineConstant.COLOR, upStationId, downStationId, LineConstant.DISTANCE_VALUE));
        lineId = LineStep.extractId(lineResponse);
    }

    /**
     * Given 노선이 주어졌을 때
     * When  해당 노선에 지하철 구간을 생성하고
     * Then  생성한 구간의 노선을 조회해보면 노선에 새로운 구간에 대한 역이 추가되어 있다.
     */
    @DisplayName("지하철 구간을 생성합니다.")
    @Test
    void createSectionTest() {
        //given
        //setUp

        // when
        ExtractableResponse<Response> response = 지하철_구간_생성(lineId, downStationId, sectionDownStationId);

        // then
        AssertUtil.상태코드_CREATED(response);

        ExtractableResponse<Response> line = LineStep.getLine(lineId);

        StationResponse 구간하행역 = new StationResponse(sectionDownStationId, StationConstant.JUNGGYE);
        노선의_하행역에_특정역이_존재하는지_검증(구간하행역, line);
    }



    /**
     * Given 지하철 구간이 주어졌을 때
     * When  지하철 구간을 제거하고
     * Then  노선을 조회하면 구간의 상행역이 노선의 하행역으로 조회된다.
     */
    @DisplayName("지하철 구간을 제거합니다.")
    @Test
    void deleteSectionTest() {
        // given
        지하철_구간_생성(lineId, downStationId, sectionDownStationId);

        // when
        ExtractableResponse<Response> response = 지하철_구간_삭제(lineId, sectionDownStationId);

        // then
        AssertUtil.상태코드_NO_CONTENT(response);

        ExtractableResponse<Response> line = LineStep.getLine(lineId);
        StationResponse 구간상행역 = new StationResponse(downStationId, StationConstant.YANGJAE);
        노선의_하행역에_특정역이_존재하는지_검증(구간상행역, line);
    }


    /**
     * Given 노선이 주어졌을 때
     * When  노선의 하행역과 구간의 상행역이 다를 때 구간을 생성하면
     * Then  예외가 발생한다.
     */
    @DisplayName("지하철 구간을 생성 시 새로운 구간의 상행역이 노선의 하행역이 아니라면 예외가 발생한다.")
    @Test
    void createSectionAnotherSectionUpStationTest() {
        //given
        long anotherStationId = StationStep.extractStationId(StationStep.createSubwayStation(StationConstant.JUNGGYE));

        // when
        ExtractableResponse<Response> response = 지하철_구간_생성(lineId, anotherStationId, sectionDownStationId);

        // then
        AssertUtil.상태코드_BAD_REQUEST(response);
        assertThat(response.jsonPath().getString("message")).isEqualTo(SectionUpStationNotMatchException.MESSAGE);
    }

    /**
     * Given 노선이 주어졌을 때
     * When  노선의 하행역이 구간에 등록되어 있다면
     * Then  예외가 발생한다.
     */
    @DisplayName("지하철 구간을 생성 시 노선의 하행역이 구간에 이미 등록되어 있다면 예외가 발생한다.")
    @Test
    void createSectionAlreadyUseStationException() {
        //given

        // when
        ExtractableResponse<Response> response = 지하철_구간_생성(lineId, downStationId, upStationId);

        // then
        AssertUtil.상태코드_BAD_REQUEST(response);
        assertThat(response.jsonPath().getString("message")).isEqualTo(SectionAlreadyCreateStationException.MESSAGE);
    }

    /**
     * Given 지하철 구간이 주어졌을 때
     * When  노선의 하행종점역이 아닌 지하철역을 제거할 때
     * Then  예외가 발생한다.
     */
    @DisplayName("노선의 하행종점역이 아닌 지하철역을 제거할 때 예외가 발생한다.")
    @Test
    void deleteSectionExceptionTest() {
        // given
        지하철_구간_생성(lineId, downStationId, sectionDownStationId);

        // when
        ExtractableResponse<Response> response = 지하철_구간_삭제(lineId, downStationId);

        // then
        AssertUtil.상태코드_BAD_REQUEST(response);
        assertThat(response.jsonPath().getString("message")).isEqualTo(SectionBadRequestException.MESSAGE);
    }

    /**
     * given 노선이 상행 종점역과 하행종점역 둘만 있는 노선이 주어진다.
     * When  노선에 등록된 지하철역을 삭제한다.
     * Then  예외가 발생한다.
     */
    @DisplayName("노선에 상행,하행역 둘만 있을 때 노선에 등록된 지하철역을 삭제하면 예외가 발생한다.")
    @Test
    void deleteSectionExceptionTest2() {
        // given

        // when
        ExtractableResponse<Response> response = 지하철_구간_삭제(lineId, downStationId);

        // then
        AssertUtil.상태코드_BAD_REQUEST(response);
        assertThat(response.jsonPath().getString("message")).isEqualTo(SectionBadRequestException.MESSAGE);
    }

    private ExtractableResponse<Response> 지하철_구간_생성(long lineId, long upStationId, long downStationId) {
        HashMap<Object, Object> paramMap = new HashMap<>();
        paramMap.put("upStationId", upStationId);
        paramMap.put("downStationId", downStationId);
        paramMap.put("distance", 10);
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(paramMap)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post(LineConstant.BASE_URL + "/" + lineId + BASE_URL)
                .then().log().all()
                .extract();
        return response;
    }

    private ExtractableResponse<Response> 지하철_구간_삭제(long lineId, long stationId) {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("stationId", stationId)
                .when().delete(LineConstant.BASE_URL + "/" + lineId + BASE_URL)
                .then().log().all()
                .extract();
        return response;
    }

    private void 노선의_하행역에_특정역이_존재하는지_검증(StationResponse stationResponse, ExtractableResponse<Response> line) {
        List<StationResponse> lineStations = line.jsonPath().getList("stations", StationResponse.class);
        if (lineStations.size() < 2) {
            fail("노선에 역이 두 개 미만으로 존재할 수 없습니다.");
        }
        StationResponse lastStation = lineStations.get(lineStations.size() - 1);
        assertThat(lastStation).isEqualTo(stationResponse);
    }
}