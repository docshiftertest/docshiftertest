package com.docshifter.core.utils.veeva;

public class VeevaVaultId {
	private long id;
	private String name;
	private String url;
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String toString() {
		StringBuilder sBuf = new StringBuilder();
		sBuf.append("id: ");
		sBuf.append(this.id);
		sBuf.append(", name: ");
		sBuf.append(this.name);
		sBuf.append(", url: ");
		sBuf.append(this.url);
		return sBuf.toString();
	}
}
