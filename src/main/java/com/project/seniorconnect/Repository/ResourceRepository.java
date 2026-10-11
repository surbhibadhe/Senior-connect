
package com.project.seniorconnect.Repository;

import com.project.seniorconnect.Entity.Resource;
import com.project.seniorconnect.Entity.ResourceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository
        extends JpaRepository<Resource, Long> {

    List<Resource> findAllByOrderByCreatedAtDesc();

    List<Resource> findByCategoryOrderByCreatedAtDesc(
            ResourceCategory category);
}
