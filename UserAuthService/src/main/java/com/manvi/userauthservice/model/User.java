package com.manvi.userauthservice.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)

    /*
     The database is the last line of defence. Trusting only the code check for unique emails is wrong.
     Two signups at the same moment can both pass existsByEmail in authService.
      Only the database constraint stops the second one. And update adds that constraint only when it creates the table.
    * */
    // The database refuses a second row with the same email. The database is the last line of defence
    /*ddl-auto=update does not add a unique constraint to a table that already exists.
     Here the table is brand new, so Hibernate creates it with the constraint.
     If you ever create the table first and add unique = true later, the constraint will not appear.
    * */
    private String email;


    //passwordHash, not password. The name tells everyone: we never store the password. Hibernate turns the field name into the column password_hash.
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;
}