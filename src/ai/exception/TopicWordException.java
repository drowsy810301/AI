package ai.exception;

public class TopicWordException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1413831446501325469L;
	private int length;
	
	public TopicWordException(int length) {
		this.length = length;
	}
	
	public String getMessage() {
		return "無法取得 長度="+length+" 的主題";
	}
}
