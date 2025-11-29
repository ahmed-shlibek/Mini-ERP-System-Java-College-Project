package main.java.model;

import java.util.UUID;

public class User {
    private UUID userId;
    private String username;
    private String password;
    private String role;


    public User() {
    }

    // The Service Layer uses this to create a user. The userId is expected to be generated automatically by the database upon insertion.
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // The DAO Layer uses this when reading a record from the database, as the record already includes the unique identifier (UUID).
    public User(UUID uuid, String username, String password, String role) {
        this.userId = uuid;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // the getters
    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    //the setters
    public void setUserID(UUID userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }


    //now we add toString so when we print we get something readable
    @Override
    public String toString() {
        return "User info { " +
                "User Id : " + userId +
                ", Username : " + username +
                ", User Role :" + role + " }";
    }

    //we will add an override for equals because if we call a user object then call it again later it
    //gonna create another object these two are the same but have different locations so its gonna think
    //they are different

    @Override
    public boolean equals(Object o){
        // this : is the object that was called
        //checks if they have the same memory location if yes then clearly the same
        if (this == o)return true;

        // checks if object is null then sees if the object that was called is the same type
        // as the user object we have if not then not equal
        if(o == null || getClass() != o.getClass());

        //after checking if the object is a user we then cast to access its user id
        User user = (User) o;

        //this now sees if our current object has the same userid as the one we just casted
        return userId.equals(user.userId);
    }

}
