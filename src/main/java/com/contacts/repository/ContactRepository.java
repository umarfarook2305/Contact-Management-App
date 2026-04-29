package com.contacts.repository;

import com.contacts.entity.Contact;
import com.contacts.entity.ContactGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByUserId(Long userId);
    
    Optional<Contact> findByIdAndUserId(Long id, Long userId);

    
}
