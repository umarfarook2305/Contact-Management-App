package com.contacts.service;

import com.contacts.dto.ContactRequest;
import com.contacts.dto.ContactResponse;
import com.contacts.entity.Contact;
import com.contacts.entity.ContactGroup;
import com.contacts.entity.User;
import com.contacts.repository.ContactRepository;
import com.contacts.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactService(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    public ContactResponse create(ContactRequest request) {
        User user = getCurrentUser();

        Contact contact = new Contact();
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setAddress(request.getAddress());
        contact.setContactGroup(request.getContactGroup());
        contact.setUser(user);

        contactRepository.save(contact);
        return toResponse(contact);
    }

    public List<ContactResponse> getAll() {
        List<Contact> contacts = contactRepository.findByUserId(getCurrentUser().getId());

        List<ContactResponse> result = new ArrayList<>();
        for (Contact contact : contacts) {
            result.add(toResponse(contact));
        }
        return result;
    }

    public ContactResponse getById(Long id) {
        Contact contact = getContactOwnedByCurrentUser(id);
        return toResponse(contact);
    }

    public ContactResponse update(Long id, ContactRequest request) {
        Contact contact = getContactOwnedByCurrentUser(id);

        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setAddress(request.getAddress());
        contact.setContactGroup(request.getContactGroup());

        contactRepository.save(contact);
        return toResponse(contact);
    }

    public void delete(Long id) {
        Contact contact = getContactOwnedByCurrentUser(id);
        contactRepository.delete(contact);
    }

    public List<ContactResponse> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAll();
        }
        String lowerKeyword = keyword.toLowerCase();
        Long userId = getCurrentUser().getId();

        List<Contact> contacts = contactRepository.findByUserId(userId);

        List<ContactResponse> filtered = contacts.stream()
                .filter(c -> 
                (c.getFirstName() != null && c.getFirstName().toLowerCase().contains(lowerKeyword))
                || (c.getLastName() != null && c.getLastName().toLowerCase().contains(lowerKeyword))
                || (c.getEmail() != null && c.getEmail().toLowerCase().contains(lowerKeyword))
                || (c.getAddress() != null && c.getAddress().toLowerCase().contains(lowerKeyword))
                || (c.getContactGroup() != null && c.getContactGroup().name().toLowerCase().contains(lowerKeyword))
                || (c.getPhone() != null && c.getPhone().contains(keyword)))
                .map(this::toResponse)
                .toList();
        return filtered;
    }

    private Contact getContactOwnedByCurrentUser(Long contactId) {
        Long userId = getCurrentUser().getId();
        return contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No contact found with id " + contactId));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired, please login again"));
    }

    private ContactResponse toResponse(Contact contact) {
        ContactResponse response = new ContactResponse();
        response.setId(contact.getId());
        response.setFirstName(contact.getFirstName());
        response.setLastName(contact.getLastName());
        response.setEmail(contact.getEmail());
        response.setPhone(contact.getPhone());
        response.setAddress(contact.getAddress());
        response.setContactGroup(contact.getContactGroup());
        return response;
    }
}
