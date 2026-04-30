package com.contacts.dto;

import com.contacts.entity.ContactGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ContactRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^$|^[+]?[0-9]{7,15}$", message = "Invalid phone number")
    private String phone;

    private String address;

    private ContactGroup contactGroup;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public ContactGroup getContactGroup() { return contactGroup; }
    public void setContactGroup(ContactGroup contactGroup) { this.contactGroup = contactGroup; }
}
