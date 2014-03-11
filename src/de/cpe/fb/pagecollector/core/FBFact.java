package de.cpe.fb.pagecollector.core;

public class FBFact {

	private String	time;
	private String	user;
	private String	message;
	private Type		type;
	private boolean	relevant;
	private boolean	indirectRelevant;
	private String	pictureLink;

	public enum Type {
		POST, COMMENT, REPLY
	}

	/**
	 * @return the pictureLink
	 */
	public String getPictureLink() {
		return pictureLink;
	}

	/**
	 * @param pictureLink
	 *          the pictureLink to set
	 */
	public void setPictureLink(String pictureLink) {
		this.pictureLink = pictureLink;
	}

	/**
	 * @return the attachedLink
	 */
	public String getAttachedLink() {
		return attachedLink;
	}

	/**
	 * @param attachedLink
	 *          the attachedLink to set
	 */
	public void setAttachedLink(String attachedLink) {
		this.attachedLink = attachedLink;
	}

	private String	attachedLink;

	public FBFact(Type type) {
		super();
		this.type = type;
	}

	private String	date;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isRelevant() {
		return relevant;
	}

	public void setRelevant(boolean relevant) {
		this.relevant = relevant;
	}

	public boolean isIndirectRelevant() {
		return indirectRelevant;
	}

	public void setIndirectRelevant(boolean indirect) {
		this.indirectRelevant = indirect;
	}

}
