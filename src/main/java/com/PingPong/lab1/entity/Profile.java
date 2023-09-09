package com.PingPong.lab1.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.Objects;
import java.util.UUID;


public class Profile {
    private UUID id;
    private final String name;
    private final String surname;
    private final String email;

    private final String birthday;

    @PersistenceCreator
    public Profile(UUID id, String name, String surname, String email, String birthday) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.birthday = birthday;
    }

    @JsonCreator
    public Profile(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("surname") String surname,
                   @JsonProperty("email") String email, @JsonProperty("birthday") String birthday) {
        if (Objects.equals(id, "")) {
            this.id = UUID.randomUUID();
        }
        else {
            this.id = UUID.fromString(id);
        }
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.birthday = birthday;
    }


    public void setUUID(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getBirthday() {
        return birthday;
    }

    public String toString() {
        return "ProfileTableElement {\n" +
                "ID='" + id + "',\n" +
                "email='" + email + "',\n" +
                "name='" + name + "',\n" +
                "surname='" + surname + "',\n" +
                "birthday='" + birthday + "'\n" +
                "}\n";
    }

    public JsonNode toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("id", this.id.toString());
        json.put("name", this.name);
        json.put("surname", this.surname);
        json.put("email", this.email);
        json.put("birthday", this.birthday);
        return json;
    }
}
