package com.inwaiders.plames.modules.minecraft.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;

@Controller
public class ServersPageController {

	@GetMapping("/mc/servers")
	public String mainPage(Model model) {
		
		List<MinecraftServer> servers = MinecraftServer.getAll();
		
		model.addAttribute("servers", servers);
		
		return "mc_servers";
	}
}