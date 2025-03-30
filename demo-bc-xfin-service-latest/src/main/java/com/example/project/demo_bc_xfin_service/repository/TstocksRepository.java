package com.example.project.demo_bc_xfin_service.repository;

import org.springframework.stereotype.Repository;
import com.example.project.demo_bc_xfin_service.entity.fiveMinsEntity.TstocksEntity;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface TstocksRepository extends JpaRepository<TstocksEntity, Integer>{
}
