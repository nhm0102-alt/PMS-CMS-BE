package com.pms.backend.repository;

import com.pms.backend.model.BaseJsonEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SoftDeleteRepository<E extends BaseJsonEntity> extends JpaRepository<E, String> {
    List<E> findByDeletedFalse();

    Optional<E> findByIdAndDeletedFalse(String id);
}

