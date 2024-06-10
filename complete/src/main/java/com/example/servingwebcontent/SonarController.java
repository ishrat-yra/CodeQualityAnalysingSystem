package com.example.servingwebcontent;

import com.example.dto.SonarProject;
import com.example.service.SonarScannerService;
import com.example.util.Url;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

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

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private SonarScannerService scannerService;

	@GetMapping("/project")
	public String show(Model model, HttpServletRequest request) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		model.addAttribute("project", new SonarProject());

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

		return REDIRECT_URL_PREFIX + "/projectList";
	}

	@GetMapping("/runScanner")
	public String runScanner(HttpServletRequest request) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		scannerService.runSonarScannerAsync("SimpleSpringBoot", "/Users/yra/projects/therap/gs-serving-web-content/complete");

		return "sonarProject";
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
