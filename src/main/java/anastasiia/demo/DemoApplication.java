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

	// Проверка, что сервер работает
	@GetMapping("/")
	public String apiRoot() {
		return "Hello World";
	}

	// Простой тестовый endpoint
	@CrossOrigin(origins = "*")
	@PostMapping("/test-endpoint")
	public Map<String, String> testEndpoint(@RequestBody Map<String, String> request) {
		String input = request.get("test");
		return Map.of("echo", input != null ? input : "no input");
	}

	// Основной десерт-солвер
	@CrossOrigin(origins = "*")
	@PostMapping("/solve-dessert")
	public Map<String, Object> solveDessert(@RequestBody DessertRequestDTO request) {
		return DessertSolver.solve(request);
	}
}
