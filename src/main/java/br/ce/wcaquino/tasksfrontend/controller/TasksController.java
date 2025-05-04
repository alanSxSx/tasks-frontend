package br.ce.wcaquino.tasksfrontend.controller;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.ce.wcaquino.tasksfrontend.model.Todo;

@Controller
public class TasksController {

	@Value("${backend.host}")
	private String BACKEND_HOST;

	@Value("${backend.port}")
	private String BACKEND_PORT;

	@Value("${app.version}")
	private String VERSION;

	public String getBackendURL() {
		return "http://" + BACKEND_HOST + ":" + BACKEND_PORT;
	}

	@GetMapping("")
	public String index(Model model) {
		model.addAttribute("todos", getTodos());
		if(VERSION.startsWith("build"))
			model.addAttribute("version", VERSION);
		return "index";
	}

	@GetMapping("add")
	public String add(Model model) {
		model.addAttribute("todo", new Todo());
		return "add";
	}

	@PostMapping("save")
	public String save(Todo todo, Model model, RedirectAttributes redirectAttributes) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.postForObject(getBackendURL() + "/tasks-backend/todo", todo, Object.class);
			redirectAttributes.addFlashAttribute("success", "Task saved successfully!");
			return "redirect:/"; // ✅ redireciona para evitar reenvio no F5
		} catch(Exception e) {
			Pattern compile = Pattern.compile("message\":\"(.*)\",");
			Matcher m = compile.matcher(e.getMessage());
			m.find();
			model.addAttribute("error", m.group(1));
			model.addAttribute("todo", todo);
			return "add"; // erro: volta para o form
		}
	}

	@GetMapping("delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.delete(getBackendURL() + "/tasks-backend/todo/" + id);
		redirectAttributes.addFlashAttribute("success", "Task deleted successfully!");
		return "redirect:/"; // ✅ redireciona para evitar reexecução no F5
	}


	@SuppressWarnings("unchecked")
	private List<Todo> getTodos() {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.getForObject(
				getBackendURL() + "/tasks-backend/todo", List.class);
	}
}
