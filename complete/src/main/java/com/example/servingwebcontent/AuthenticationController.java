package com.example.servingwebcontent;

import com.example.dto.UserDto;
import com.example.entity.User;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Objects;

@Controller
public class AuthenticationController {

	private UserService userService;

	public AuthenticationController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/login")
	public String loginForm(Model model) {
		model.addAttribute("user", new UserDto());
		return "login";
	}

	@PostMapping("/login")
	public String handleLogin(@Valid @ModelAttribute("user") UserDto user,
							  BindingResult result,
							  HttpServletRequest request,
							  Model model) {

		User existing = userService.findByEmailAndPassword(user.getEmail(), user.getPassword());

		if (existing == null) {
			result.rejectValue("email", null, "There is no account registered with that email");
		}

		if (result.hasErrors()) {
			model.addAttribute("user", user);
			return "login";
		}

		request.getSession().setAttribute("loggedInUser", existing);

		return "redirect:/dashboard";
	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request) {

		request.getSession().invalidate();

		return "redirect:/login";
	}

	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("user", new UserDto());
		return "register";
	}

	@PostMapping("/register/save")
	public String registration(@Valid @ModelAttribute("user") UserDto user,
							   BindingResult result,
							   Model model){

		User existing = userService.findByEmail(user.getEmail());

		if (existing != null) {
			result.rejectValue("email", null, "There is already an account registered with that email");
		}

		if (result.hasErrors()) {
			model.addAttribute("user", user);
			return "register";
		}

		userService.saveUser(user);

		return "redirect:/register?success";
	}

	@GetMapping("/users")
	public String listRegisteredUsers(Model model, HttpServletRequest request) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		List<UserDto> users = userService.findAllUsers();
		model.addAttribute("users", users);

		return "users";
	}

	@GetMapping("/dashboard")
	public String dashboard(HttpServletRequest request) {

		if (!isUserLoggedIn(request.getSession(false))) {
			return "redirect:/login";
		}

		return "dashboard";
	}

	private boolean isUserLoggedIn(HttpSession session) {
		return Objects.nonNull(session) && Objects.nonNull(session.getAttribute("loggedInUser"));
	}
}
