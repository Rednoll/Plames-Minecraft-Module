package com.inwaiders.plames.modules.minecraft.dao.server;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.inwaiders.plames.modules.minecraft.domain.server.MinecraftServer;

@Repository
public interface MinecraftServerRepository extends JpaRepository<MinecraftServer, Long>{

	@QueryHints({
		@QueryHint(name = "org.hibernate.cacheable", value = "true")
	})
	@Override
	@Query("SELECT s FROM MinecraftServer s WHERE s.id = :id AND s.deleted != true")
	public MinecraftServer getOne(@Param(value = "id") Long id);
	
	@Override
	@Query("SELECT s FROM MinecraftServer s WHERE s.deleted != true")
	public List<MinecraftServer> findAll();
	
	@Override
	@Query("SELECT COUNT(*) FROM MinecraftServer s WHERE s.deleted != true")
	public long count();
}
