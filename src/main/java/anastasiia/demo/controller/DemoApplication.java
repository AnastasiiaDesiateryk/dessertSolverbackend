package anastasiia.demo.controller;

import anastasiia.demo.dto.DessertRequestDTO;
import anastasiia.demo.dto.DessertResultDTO;
import anastasiia.demo.solver.DessertSolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SpringBootApplication
@RestController
public class DemoApplication {

	private final DessertSolver solver;


	public DemoApplication(DessertSolver solver) {
		this.solver = solver;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	/**
	 * Basic health check endpoint to verify server is running.
	 */
	@GetMapping("/")
	public String apiRoot() {
		return "Hello World";
	}

	/**
	 * Simple test endpoint to echo back received input.
	 */
	@PostMapping("/test-endpoint")
	public Map<String, String> testEndpoint(@RequestBody Map<String, String> request) {
		String input = request.get("test");
		return Map.of("echo", input != null ? input : "no input");
	}

	/**
	 * Main API endpoint to solve the dessert optimization problem.
	 */
	@PostMapping("/solve-dessert")
	public DessertResultDTO solveDessert(@RequestBody DessertRequestDTO request) {
		return solver.solve(request);
	}
}
