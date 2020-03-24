package com.inwaiders.plames.modules.minecraft.dao.profile;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inwaiders.plames.modules.minecraft.domain.profile.MinecraftProfile;

@Service
public class MinecraftProfileRepositoryInjector {

	@Autowired
	private MinecraftProfileRepository repository;

	@PostConstruct
	private void inject() {
		
		MinecraftProfile.setRepository(repository);
	}
}
