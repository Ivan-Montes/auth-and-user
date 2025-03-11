package dev.ime.api.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(AuthWebController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthWebControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Test
	void MainController_success_ReturnView() throws Exception {
		
		mockMvc.perform(MockMvcRequestBuilders.get("/success"))
		.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
		.andExpect(MockMvcResultMatchers.view().name("success"));
	}

}
