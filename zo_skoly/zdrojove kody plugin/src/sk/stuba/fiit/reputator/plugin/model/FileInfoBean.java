package sk.stuba.fiit.reputator.plugin.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class FileInfoBean {
	
	private String filePath;
	private String fileInfo;
	private Set<String> lines;
	
	
	
	public FileInfoBean(String filePath, String fileInfo) {
		this.filePath = filePath;
		this.fileInfo = fileInfo;
		this.lines = new LinkedHashSet<>();
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFileInfo() {
		return fileInfo;
	}
	public void setFileInfo(String fileInfo) {
		this.fileInfo = fileInfo;
	}
	public Set<String> getLines() {
		return lines;
	}
	public void setLines(Set<String> lines) {
		this.lines = lines;
	}

	
}
