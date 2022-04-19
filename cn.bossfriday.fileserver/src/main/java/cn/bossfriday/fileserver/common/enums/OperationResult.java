package cn.bossfriday.fileserver.common.enums;

public enum OperationResult {
	OK(200,"ok"),
	BadRequest(400, "bad request"),
	NotFound(404, "not found"),
	SystemError(500, "internal system error");

	private OperationResult(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	private int code;
	private String msg;

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}
}
