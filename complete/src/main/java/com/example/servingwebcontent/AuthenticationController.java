package com.example.servingwebcontent;

import com.example.dto.UserDto;
import com.example.entity.User;
import com.example.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class AuthenticationController {

	private UserService userService;

	public AuthenticationController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/login")
	public String loginForm() {
		return "login";
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
	public String listRegisteredUsers(Model model){
		List<UserDto> users = userService.findAllUsers();
		model.addAttribute("users", users);
		return "users";
	}
}
