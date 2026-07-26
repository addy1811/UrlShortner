package com.shortner.repository;

import com.shortner.entity.FormField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FormFieldRepository extends JpaRepository<FormField, UUID> {

    // Ordered so the rendered form matches the owner's intended field sequence.
   List<FormField> findByLinkIdOrderByDisplayOrderAsc(UUID linkId);

    void deleteByLinkId(UUID linkId);

    boolean existsByLinkIdAndFieldKey(UUID linkId, String fieldKey);
}