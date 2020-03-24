package com.inwaiders.plames.modules.minecraft.dao.server;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;

@Service
public class MinecraftServerRepositoryInjector {

	@Autowired
	private MinecraftServerRepository repository;
	
	@PostConstruct
	private void inject() {
		
		MinecraftServer.setRepository(repository);
	}
}
