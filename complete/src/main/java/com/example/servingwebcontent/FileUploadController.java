package com.example.servingwebcontent;

import com.example.service.StorageException;
import com.example.service.StorageFileNotFoundException;
import com.example.service.StorageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.util.FileUtils;

@Controller
@RequestMapping("/attachment")
public class FileUploadController {

	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String listUploadedFiles(Model model, HttpServletRequest request) throws IOException {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		model.addAttribute("files", storageService.loadAll().map(
				path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);

		if (file == null)
			return ResponseEntity.notFound().build();

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
								   HttpServletRequest request,
								   RedirectAttributes redirectAttributes) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		if (!FileUtils.isZipFile(file)) {
			redirectAttributes.addFlashAttribute("message","Only Zip Files are allowed!");
			return "redirect:/attachment/";
		}

		try {
			storageService.store(file);
			redirectAttributes.addFlashAttribute("message",
												 "You successfully uploaded " + file.getOriginalFilename());

		} catch (StorageException storageException) {
			redirectAttributes.addFlashAttribute("message",
												 "Error occured while uploading " + file.getOriginalFilename());

		}

		return "redirect:/attachment/";
	}

	@GetMapping("/unzip")
	public String unzipFile(@RequestParam("fileName") String fileName,
						   HttpServletRequest request,
						   RedirectAttributes redirectAttributes) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		try {
			storageService.unzip(fileName);

			redirectAttributes.addFlashAttribute("message",
												 "You successfully unzipped " + fileName + "!");

		} catch (StorageException storageException) {
			redirectAttributes.addFlashAttribute("message",
												 "Error occured while unzipping " + fileName + "!");

		}

		return "redirect:/attachment/";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

	private boolean isUserLoggedIn(HttpSession session) {
		return Objects.nonNull(session) && Objects.nonNull(session.getAttribute("loggedInUser"));
	}

}
