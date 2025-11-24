package main.java.model;
import java.util.UUID;
import java.util.Objects;


public class User {
    private UUID userId;
    private String username;
    private String passwordHash;
    private String role;


    public User() {
    }

    public User(String username, String passwordHash, String role) {

        //this gives ur a random id each time there is a user
        this.userId = UUID.randomUUID();
        //
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // the getters
    public UUID getUserID() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
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

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
