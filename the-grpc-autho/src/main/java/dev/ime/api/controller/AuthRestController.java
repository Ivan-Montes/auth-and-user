package dev.ime.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthRestController {

	@GetMapping("/callback")
	public String handleCallback(@RequestParam("code") String authorizationCode, @RequestParam String state) {

		return "## Authorization code: " + authorizationCode + ", ## State: " + state;

	}

}
