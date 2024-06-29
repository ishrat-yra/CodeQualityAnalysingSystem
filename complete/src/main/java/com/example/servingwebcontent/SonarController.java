package com.example.servingwebcontent;

import com.example.dto.SonarProject;
import com.example.entity.Project;
import com.example.service.ProjectService;
import com.example.service.SonarScannerService;
import com.example.service.StorageService;
import com.example.util.Url;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;
/**
 * @author ishrat.jahan
 * @since 06/08,2024
 */
@Controller
@RequestMapping("/sonar")
public class SonarController {

	@Value("${api.sonar.server.url}")
	private String apiUrl;

	@Value("${api.username}")
	private String username;

	@Value("${api.password:}")
	private String password;

	@Value("${app.storage.unzip.path}")
	private String unzipLocation;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private StorageService storageService;

	@Autowired
	private SonarScannerService scannerService;

	@Autowired
	private ProjectService projectService;


	@GetMapping("/project")
	public String show(@RequestParam(defaultValue = "false") boolean create,
					   Model model,
					   HttpServletRequest request) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		model.addAttribute("allProjects", projectService.findAll());
		model.addAttribute("createProject", create);

		if (create) {
			model.addAttribute("project", new SonarProject());
		}

		return "sonarProject";
	}

	@PostMapping("/project")
	public String save(@Valid @ModelAttribute("project") SonarProject project,
					   BindingResult result,
					   HttpServletRequest request,
					   Model model) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		if (result.hasErrors()) {
			model.addAttribute("project", project);
			return "login";
		}

		String auth = username + ":" + password;
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
		String authHeader = "Basic " + new String(encodedAuth);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", authHeader);

		// Set up query parameters
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("name", project.getName());
		params.add("project", project.getKey());

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

		// Single Transaction
		// Create Sonar Project
		// Save in local db
		String url = apiUrl + Url.SONAR_PROJECT_CREATE_API;

		ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

		// Process the response if needed
		model.addAttribute("response", response.getBody());

		System.out.println(response.getBody());

		projectService.saveProject(new Project(project.getName(), project.getKey()));

		return REDIRECT_URL_PREFIX + "/sonar/project";
	}

	@GetMapping("/projectDetails/{id}")
	public String projectDetails(@PathVariable Long id,
								 Model model,
								 HttpServletRequest request) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		model.addAttribute("project", projectService.find(id));
		model.addAttribute("files", storageService.loadAll().map(
														  path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
														 "serveFile", path.getFileName().toString()).build().toUri().toString())
												  .collect(Collectors.toList()));


		return "projectDetails";
	}

	@GetMapping("/runScanner")
	public String runScanner(HttpServletRequest request) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		scannerService.runSonarScannerAsync("SimpleSpringBoot", unzipLocation + "/gs-serving-web-content/complete");

		return REDIRECT_URL_PREFIX + "/sonar/project";
	}

	@GetMapping("/report")
	public String report(@RequestParam(defaultValue = "false") boolean create,
					   Model model,
					   HttpServletRequest request) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		model.addAttribute("allProjects", projectService.findAll());
		model.addAttribute("createProject", create);

		if (create) {
			model.addAttribute("project", new SonarProject());
		}

		return "report";
	}

	@GetMapping("/reportDetails/{id}")
	public String reportDetails(@PathVariable Long id,
								 Model model,
								 HttpServletRequest request) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		model.addAttribute("project", projectService.find(id));

		return "reportDetails";
	}

	@GetMapping("/downloadReport/{projectKey}")
	public void downloadReport(@PathVariable String projectKey,
							   HttpServletRequest request,
							   HttpServletResponse response) throws IOException {

		if (!isUserLoggedIn(request.getSession(false))) {
			throw new RuntimeException("Something Went Wrong!");
		}

		String url = apiUrl + "/api/issues/search";
		String auth = username + ":" + password;
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
		String authHeader = "Basic " + new String(encodedAuth);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", authHeader);

		// Set up query parameters
		Map<String, String> params = new HashMap<>();
		params.put("projects", projectKey);
		params.put("format", "json");

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

		String  responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, params).getBody();

		// Set response headers
		response.setContentType("application/json");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"data.json\"");

		System.out.println("TEST3");
		// Write JSON to response
		response.getWriter().write(responseEntity);
	}

//	@GetMapping("/files/{filename:.+}")
//	@ResponseBody
//	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
//
//		Resource file = storageService.loadAsResource(filename);
//
//		if (file == null)
//			return ResponseEntity.notFound().build();
//
//		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
//				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
//	}
//
//	@PostMapping("/")
//	public String handleFileUpload(@RequestParam("file") MultipartFile file,
//								   HttpServletRequest request,
//								   RedirectAttributes redirectAttributes) {
//
//		if (!isUserLoggedIn(request.getSession(false))) {
//			return "redirect:/login";
//		}
//
//		storageService.store(file);
//		redirectAttributes.addFlashAttribute("message",
//				"You successfully uploaded " + file.getOriginalFilename() + "!");
//
//		return "redirect:/attachment/";
//	}
//
//	@ExceptionHandler(StorageFileNotFoundException.class)
//	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
//		return ResponseEntity.notFound().build();
//	}
//
//	private boolean isUserLoggedIn(HttpSession session) {
//		return Objects.nonNull(session) && Objects.nonNull(session.getAttribute("loggedInUser"));
//	}

	private boolean isUserLoggedIn(HttpSession session) {
		return Objects.nonNull(session) && Objects.nonNull(session.getAttribute("loggedInUser"));
	}

}
