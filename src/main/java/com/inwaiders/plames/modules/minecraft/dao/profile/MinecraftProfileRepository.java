package com.inwaiders.plames.modules.minecraft.dao.profile;

import java.util.List;
import java.util.UUID;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.inwaiders.plames.modules.minecraft.domain.profile.MinecraftProfile;

@Repository
public interface MinecraftProfileRepository extends JpaRepository<MinecraftProfile, Long>{

	@QueryHints({
		@QueryHint(name = "org.hibernate.cacheable", value = "true")
	})
	@Query("SELECT p FROM MinecraftProfile p WHERE p.mojangUUID = :uuid AND p.deleted != true")
	public MinecraftProfile getByMojangUUID(@Param("uuid") UUID uuid);
	
	@QueryHints({
		@QueryHint(name = "org.hibernate.cacheable", value = "true")
	})
	@Query("SELECT p FROM MinecraftProfile p WHERE p.playerName = :pn AND p.deleted != true")
	public MinecraftProfile getByPlayerName(@Param("pn") String playerName);
	
	@QueryHints({
		@QueryHint(name = "org.hibernate.cacheable", value = "true")
	})
	@Override
	@Query("SELECT p FROM MinecraftProfile p WHERE p.id = :id AND p.deleted != true")
	public MinecraftProfile getOne(@Param(value = "id") Long id);
	
	@Override
	@Query("SELECT p FROM MinecraftProfile p WHERE p.deleted != true")
	public List<MinecraftProfile> findAll();
	
	@Override
	@Query("SELECT COUNT(*) FROM MinecraftProfile p WHERE p.deleted != true")
	public long count();
}
