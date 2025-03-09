package dev.ime.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthWebController {

	@GetMapping("/success")
	public String successLogin() {
		return "success";
	}

}
