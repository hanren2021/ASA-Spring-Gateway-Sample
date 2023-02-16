package com.gatewayexample.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/demo2")
	public String index() {
		return "Greetings from demo 2!";
	}

}
