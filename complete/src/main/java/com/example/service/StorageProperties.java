package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

	@Value("${app.storage.zip.path}")
	private String zipLocation;

	@Value("${app.storage.unzip.path}")
	private String unzipLocation;

	public String getZipLocation() {
		return zipLocation;
	}

	public void setZipLocation(String zipLocation) {
		this.zipLocation = zipLocation;
	}

	public String getUnzipLocation() {
		return unzipLocation;
	}

	public void setUnzipLocation(String unzipLocation) {
		this.unzipLocation = unzipLocation;
	}
}
