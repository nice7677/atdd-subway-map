package subway.utils;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static subway.utils.AssertUtil.*;

public class LineUtil {

    public static JsonPath createLineResultResponse(String lineName, String color, Long upStationId, Long downStationId, int distance) {
        ExtractableResponse<Response> response = RestAssured
                .given().log().all().body(getCreateParam(lineName, color, upStationId, downStationId, distance)).contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();

        assertSuccessCreate(response);

        return response.body().jsonPath();
    }

    private static Map<String, Object> getCreateParam(String lineName, String color, Long upStationId, Long downStationId, int distance) {
        Map<String, Object> param = new HashMap<>();
        param.put("name", lineName);
        param.put("color", color);
        param.put("upStationId", upStationId);
        param.put("downStationId", downStationId);
        param.put("distance", distance);

        return param;
    }

    public static JsonPath showLinesResultResponse() {
        ExtractableResponse<Response> response = RestAssured
                .given().log().all().contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/lines")
                .then().log().all()
                .extract();

        assertSuccessOk(response);

        return response.body().jsonPath();
    }

    public static JsonPath showLineResultResponse(Long id) {
        ExtractableResponse<Response> response = RestAssured
                .given().log().all().contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/lines/{id}", id)
                .then().log().all()
                .extract();

        assertSuccessOk(response);

        return response.body().jsonPath();
    }

    public static void updateLineResult(Long id, String lineName, String color) {
        ExtractableResponse<Response> response = RestAssured
                .given().log().all().body(getUpdateParam(lineName, color)).contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().put("/lines/{id}", id)
                .then().log().all()
                .extract();

        assertSuccessOk(response);
    }

    private static Map<String, String> getUpdateParam(String lineName, String color) {
        Map<String, String> param = new HashMap<>();
        param.put("name", lineName);
        param.put("color", color);

        return param;
    }

    public static void deleteLineResult(Long id) {
        ExtractableResponse<Response> response = RestAssured
                .given().log().all().contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().delete("/lines/{id}", id)
                .then().log().all()
                .extract();

        assertSuccessNoContent(response);
    }

    public static ExtractableResponse<Response> addSectionResponse(Long lineId, Long downStationId, Long newStationId, int distance) {
        return RestAssured
                .given().log().all().body(getAddParam(downStationId, newStationId, distance)).contentType(APPLICATION_JSON_VALUE)
                .when().post("/lines/{id}/sections", lineId)
                .then().log().all()
                .extract();
    }

    private static Map<String, Object> getAddParam(Long downStationId, Long newStationId, int distance) {
        Map<String, Object> param = new HashMap<>();
        param.put("upStationId", downStationId);
        param.put("downStationId", newStationId);
        param.put("distance", distance);
        return param;
    }

    public static ExtractableResponse<Response> deleteSectionResponse(Long lineId, Long stationId) {
        return RestAssured
                .given().log().all().params("stationId", stationId)
                .when().delete("/lines/{id}/sections", lineId)
                .then().log().all()
                .extract();
    }
}