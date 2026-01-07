/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.Cambo_MarketPlace.Models;


import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users") // avoid reserved keywords
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // "USER" / "SELLER"

    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationships
    @OneToMany(mappedBy = "seller")
    private List<Product> products;

    @OneToMany(mappedBy = "buyer")
    private List<ChatRoom> boughtChats;

    @OneToMany(mappedBy = "seller")
    private List<ChatRoom> soldChats;

    @OneToMany(mappedBy = "sender")
    private List<Message> messages;


    // getters and setters
}
