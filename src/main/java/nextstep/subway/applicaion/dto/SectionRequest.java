package nextstep.subway.applicaion.dto;

public class SectionRequest {

    private Long upStationId;
    private Long downStationId;
    private Long distance;

    public Long getUpStationId() {
        return upStationId;
    }

    public Long getDownStationId() {
        return downStationId;
    }

    public Long getDistance() {
        return distance;
    }
}