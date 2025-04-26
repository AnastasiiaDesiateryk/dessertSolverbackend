package anastasiia.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@SpringBootApplication
@RestController
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	/**
	 * Basic health check endpoint to verify server is running.
	 *
	 * @return "Hello World" message
	 */
	@GetMapping("/")
	public String apiRoot() {
		return "Hello World";
	}

	/**
	 * Simple test endpoint to echo back received input.
	 *
	 * @param request A JSON object containing a "test" field
	 * @return A JSON response with the same "test" value echoed
	 */
	@CrossOrigin(origins = "*")
	@PostMapping("/test-endpoint")
	public Map<String, String> testEndpoint(@RequestBody Map<String, String> request) {
		String input = request.get("test");
		return Map.of("echo", input != null ? input : "no input");
	}

	/**
	 * Main API endpoint to solve the dessert optimization problem.
	 *
	 * @param request A DessertRequestDTO containing ingredients, constraints, and optimization goal
	 * @return A DessertResultDTO with the optimal ingredient quantities and summary information
	 */
	@CrossOrigin(origins = "*")
	@PostMapping("/solve-dessert")
	public DessertResultDTO solveDessert(@RequestBody DessertRequestDTO request) {
		DessertSolver solver = new DessertSolver(); // Create solver instance
		return solver.solve(request);
	}
}
