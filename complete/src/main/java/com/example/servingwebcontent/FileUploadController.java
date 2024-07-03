package com.example.servingwebcontent;

import com.example.entity.Project;
import com.example.entity.Scans;
import com.example.entity.User;
import com.example.service.*;

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

	@Autowired
	private StorageService storageService;

	@Autowired
	private ScanService scanService;

	@Autowired
	private ProjectService projectService;

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
	public String handleFileUpload(@RequestParam MultipartFile file,
								   @RequestParam Long projectId,
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

			// unzip
			// save path and other info in db
			// map with projectja

			System.out.println("projectId : " + projectId);
			System.out.println("getName : " + file.getName());
			System.out.println("getOriginalFilename : " + file.getOriginalFilename());

			Scans scans = new Scans(file.getOriginalFilename(),
									file.getOriginalFilename().replace(".zip", ""),
									(User) request.getSession().getAttribute("loggedInUser"));

			scanService.saveScan(scans);

			storageService.unzip(file.getOriginalFilename());

			Project project = projectService.find(projectId);
			project.getScans().add(scans);
			projectService.saveProject(project);

			redirectAttributes.addFlashAttribute("message",
												 "You successfully uploaded " + file.getOriginalFilename());

		} catch (StorageException storageException) {
			redirectAttributes.addFlashAttribute("message",
												 "Error occured while uploading " + file.getOriginalFilename());

		}

		return "redirect:/sonar/project";
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
