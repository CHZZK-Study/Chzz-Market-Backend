package org.chzz.market.domain.payment.error;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Getter
@AllArgsConstructor
public enum TossPaymentErrorCode implements ErrorCode {
    ALREADY_PROCESSED_PAYMENT(HttpStatus.BAD_REQUEST, "이미 처리된 결제 입니다."),
    PROVIDER_ERROR(HttpStatus.BAD_REQUEST, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    EXCEED_MAX_CARD_INSTALLMENT_PLAN(HttpStatus.BAD_REQUEST, "설정 가능한 최대 할부 개월 수를 초과했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NOT_ALLOWED_POINT_USE(HttpStatus.BAD_REQUEST, "포인트 사용이 불가한 카드로 카드 포인트 결제에 실패했습니다."),
    INVALID_API_KEY(HttpStatus.BAD_REQUEST, "잘못된 시크릿키 연동 정보 입니다."),
    INVALID_REJECT_CARD(HttpStatus.BAD_REQUEST, "카드 사용이 거절되었습니다. 카드사 문의가 필요합니다."),
    BELOW_MINIMUM_AMOUNT(HttpStatus.BAD_REQUEST, "신용카드는 결제금액이 100원 이상, 계좌는 200원이상부터 결제가 가능합니다."),
    INVALID_CARD_EXPIRATION(HttpStatus.BAD_REQUEST, "카드 정보를 다시 확인해주세요. (유효기간)"),
    INVALID_STOPPED_CARD(HttpStatus.BAD_REQUEST, "정지된 카드 입니다."),
    EXCEED_MAX_DAILY_PAYMENT_COUNT(HttpStatus.BAD_REQUEST, "하루 결제 가능 횟수를 초과했습니다."),
    NOT_SUPPORTED_INSTALLMENT_PLAN_CARD_OR_MERCHANT(HttpStatus.BAD_REQUEST, "할부가 지원되지 않는 카드 또는 가맹점 입니다."),
    INVALID_CARD_INSTALLMENT_PLAN(HttpStatus.BAD_REQUEST, "할부 개월 정보가 잘못되었습니다."),
    NOT_SUPPORTED_MONTHLY_INSTALLMENT_PLAN(HttpStatus.BAD_REQUEST, "할부가 지원되지 않는 카드입니다."),
    EXCEED_MAX_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "하루 결제 가능 금액을 초과했습니다."),
    NOT_FOUND_TERMINAL_ID(HttpStatus.BAD_REQUEST, "단말기번호(Terminal Id)가 없습니다. 토스페이먼츠로 문의 바랍니다."),
    INVALID_AUTHORIZE_AUTH(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 방식입니다."),
    INVALID_CARD_LOST_OR_STOLEN(HttpStatus.BAD_REQUEST, "분실 혹은 도난 카드입니다."),
    RESTRICTED_TRANSFER_ACCOUNT(HttpStatus.BAD_REQUEST, "계좌는 등록 후 12시간 뒤부터 결제할 수 있습니다. 관련 정책은 해당 은행으로 문의해주세요."),
    INVALID_CARD_NUMBER(HttpStatus.BAD_REQUEST, "카드번호를 다시 확인해주세요."),
    INVALID_UNREGISTERED_SUBMALL(HttpStatus.BAD_REQUEST, "등록되지 않은 서브몰입니다. 서브몰이 없는 가맹점이라면 안심클릭이나 ISP 결제가 필요합니다."),
    NOT_REGISTERED_BUSINESS(HttpStatus.BAD_REQUEST, "등록되지 않은 사업자 번호입니다."),
    EXCEED_MAX_ONE_DAY_WITHDRAW_AMOUNT(HttpStatus.BAD_REQUEST, "1일 출금 한도를 초과했습니다."),
    EXCEED_MAX_ONE_TIME_WITHDRAW_AMOUNT(HttpStatus.BAD_REQUEST, "1회 출금 한도를 초과했습니다."),
    CARD_PROCESSING_ERROR(HttpStatus.BAD_REQUEST, "카드사에서 오류가 발생했습니다."),
    EXCEED_MAX_AMOUNT(HttpStatus.BAD_REQUEST, "거래금액 한도를 초과했습니다."),
    INVALID_ACCOUNT_INFO_RE_REGISTER(HttpStatus.BAD_REQUEST, "유효하지 않은 계좌입니다. 계좌 재등록 후 시도해주세요."),
    NOT_AVAILABLE_PAYMENT(HttpStatus.BAD_REQUEST, "결제가 불가능한 시간대입니다"),
    UNAPPROVED_ORDER_ID(HttpStatus.BAD_REQUEST, "아직 승인되지 않은 주문번호입니다."),
    EXCEED_MAX_MONTHLY_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "당월 결제 가능금액인 1,000,000원을 초과 하셨습니다."),
    UNAUTHORIZED_KEY(HttpStatus.UNAUTHORIZED, "인증되지 않은 시크릿 키 혹은 클라이언트 키 입니다."),
    REJECT_ACCOUNT_PAYMENT(HttpStatus.FORBIDDEN, "잔액부족으로 결제에 실패했습니다."),
    REJECT_CARD_PAYMENT(HttpStatus.FORBIDDEN, "한도초과 혹은 잔액부족으로 결제에 실패했습니다."),
    REJECT_CARD_COMPANY(HttpStatus.FORBIDDEN, "결제 승인이 거절되었습니다."),
    FORBIDDEN_REQUEST(HttpStatus.FORBIDDEN, "허용되지 않은 요청입니다."),
    REJECT_TOSSPAY_INVALID_ACCOUNT(HttpStatus.FORBIDDEN, "선택하신 출금 계좌가 출금이체 등록이 되어 있지 않아요. 계좌를 다시 등록해 주세요."),
    EXCEED_MAX_AUTH_COUNT(HttpStatus.FORBIDDEN, "최대 인증 횟수를 초과했습니다. 카드사로 문의해주세요."),
    EXCEED_MAX_ONE_DAY_AMOUNT(HttpStatus.FORBIDDEN, "일일 한도를 초과했습니다."),
    NOT_AVAILABLE_BANK(HttpStatus.FORBIDDEN, "은행 서비스 시간이 아닙니다."),
    INVALID_PASSWORD(HttpStatus.FORBIDDEN, "결제 비밀번호가 일치하지 않습니다."),
    INCORRECT_BASIC_AUTH_FORMAT(HttpStatus.FORBIDDEN, "잘못된 요청입니다. ':' 를 포함해 인코딩해주세요."),
    FDS_ERROR(HttpStatus.FORBIDDEN, "[토스페이먼츠] 위험거래가 감지되어 결제가 제한됩니다. 발송된 문자에 포함된 링크를 통해 본인인증 후 결제가 가능합니다. (고객센터: 1644-8051)"),
    NOT_FOUND_PAYMENT(HttpStatus.NOT_FOUND, "존재하지 않는 결제 정보 입니다."),
    NOT_FOUND_PAYMENT_SESSION(HttpStatus.NOT_FOUND, "결제 시간이 만료되어 결제 진행 데이터가 존재하지 않습니다."),
    FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING(HttpStatus.INTERNAL_SERVER_ERROR, "결제가 완료되지 않았어요. 다시 시도해주세요."),
    FAILED_INTERNAL_SYSTEM_PROCESSING(HttpStatus.INTERNAL_SERVER_ERROR, "내부 시스템 처리 작업이 실패했습니다. 잠시 후 다시 시도해주세요."),
    UNKNOWN_PAYMENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제에 실패했어요. 같은 문제가 반복된다면 은행이나 카드사로 문의해주세요.");

    private final HttpStatus httpStatus;
    private final String message;

    public static TossPaymentErrorCode from(WebClientResponseException exception) {
        return Objects.requireNonNull(exception.getResponseBodyAs(TossErrorResponse.class))
                .asErrorCode();
    }

    private record TossErrorResponse(String code,
                             String message) {
        TossPaymentErrorCode asErrorCode() {
            return TossPaymentErrorCode.valueOf(code);
        }
    }
}
