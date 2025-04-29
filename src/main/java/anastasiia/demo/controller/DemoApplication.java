package anastasiia.demo.controller;

import anastasiia.demo.dto.DessertRequestDTO;
import anastasiia.demo.dto.DessertResultDTO;
import anastasiia.demo.solver.DessertSolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SpringBootApplication
@ComponentScan(basePackages = "anastasiia.demo")
@RestController
public class DemoApplication {

	private final DessertSolver solver;

	public DemoApplication(DessertSolver solver) {
		this.solver = solver;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@GetMapping("/")
	public String apiRoot() {
		return "Hello World";
	}

	@PostMapping("/test-endpoint")
	public Map<String, String> testEndpoint(@RequestBody Map<String, String> request) {
		String input = request.get("test");
		return Map.of("echo", input != null ? input : "no input");
	}

	@PostMapping("/solve-dessert")
	public DessertResultDTO solveDessert(@RequestBody DessertRequestDTO request) {
		return solver.solve(request);
	}
}
