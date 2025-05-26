package kr.co.module.core.code;

public enum ErrorCode {

    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "카테고리가 존재하지 않습니다."),
    CATEGORY_CREATE_FAIL("CATEGORY_CREATE_FAIL", "카테고리 등록 실패"),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "상품이 존재하지 않습니다."),
    PRODUCT_CREATE_FAIL("PRODUCT_CREATE_FAIL", "상품 등록 실패"),
    PRODUCT_REQUIRED_CONDITION("PRODUCT_REQUIRED_CONDITION", "상품 검색 조건(상품관리자, 카테고리, 장소, 날짜 범위 중 하나)은 필수입니다."),
    RESERVATION_FAIL("RESERVATION_FAIL", "예약 실패"),
    RESERVATION_UPDATE_FAIL("RESERVATION_UPDATE_FAIL", "예약 수정 실패"),
    RESERVATION_CANCEL_FAIL("RESERVATION_CANCEL_FAIL", "예약 취소 실패"),

    ;

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() { return code; }
    public String message() { return message; }
}
