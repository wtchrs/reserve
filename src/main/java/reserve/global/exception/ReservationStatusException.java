package reserve.global.exception;

public class ReservationStatusException extends ErrorCodeException {

    public ReservationStatusException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReservationStatusException(ErrorCode errorCode, Throwable e) {
        super(errorCode, e);
    }

}
