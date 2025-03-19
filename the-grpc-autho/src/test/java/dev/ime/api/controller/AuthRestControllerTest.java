package dev.ime.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private static final String PATH = "/callback";
	
	@Test
	void handleCallback_shouldReturnString() throws Exception {
		
		mockMvc.perform(MockMvcRequestBuilders.get(PATH)
				.param("code", "-1")
				.param("state", "-1"))
		.andExpect(MockMvcResultMatchers.status().isOk());
		
	}

}
