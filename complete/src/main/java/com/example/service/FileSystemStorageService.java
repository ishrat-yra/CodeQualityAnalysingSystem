package com.example.service;

import com.example.util.ZipUnzipUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

	private final Path zipLocation;
	private final Path unzipLocation;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {

		if (properties.getZipLocation().length() == 0 || properties.getUnzipLocation().length() == 0) {
			throw new StorageException("File upload location can not be Empty.");
		}

		this.zipLocation = Paths.get(properties.getZipLocation());
		this.unzipLocation = Paths.get(properties.getUnzipLocation());
	}

	@Override
	public void store(MultipartFile file) {
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}
			Path destinationFile = this.zipLocation.resolve(
					Paths.get(file.getOriginalFilename())).normalize().toAbsolutePath();

			if (!destinationFile.getParent().equals(this.zipLocation.toAbsolutePath())) {
				// This is a security check
				throw new StorageException(
						"Cannot store file outside current directory.");
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile,
					StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file.", e);
		}
	}

	@Override
	public void unzip(String fileName) {
		try {

			ZipUnzipUtils.unzip(this.zipLocation.toString() + "/" + fileName,
								this.unzipLocation.toString() + "/" + fileName.replace(".zip", ""));

		} catch (IOException e) {
			throw new StorageException("Failed to store file.", e);
		}
	}

	@Override
	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.zipLocation, 1)
				.filter(path -> !path.equals(this.zipLocation))
				.map(this.zipLocation::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}

	@Override
	public Path load(String filename) {
		return zipLocation.resolve(filename);
	}

	@Override
	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
			else {
				throw new StorageFileNotFoundException(
						"Could not read file: " + filename);

			}
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(zipLocation.toFile());
	}

	@Override
	public void init() {
		try {
			Files.createDirectories(zipLocation);
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}
}
