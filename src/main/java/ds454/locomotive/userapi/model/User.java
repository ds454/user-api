package ds454.locomotive.userapi.model;

import java.util.HashSet;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    private String username;
    
    @Getter
    @Setter
    private String steamAccountName;

    @Getter
    @Setter
    private String steamAccountId;
    
    @Getter 
    @Setter 
    private String password;
    
    // Stores friend IDs in a separate table: user_friends(user_id, friend_id)
    @ElementCollection
    @CollectionTable(name = "user_friends", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "friend_id", nullable = false)
    private Set<UUID> friends = new HashSet<>();

    // Required by JPA/Hibernate. protected keeps app code from using it.
    // TODO: change to protected when done testing.
    public User() {
    }

    public User(String username, String steamAccountName, String steamAccountId, String password) {
        // UUID is assigned on call to save(), which makes sense to me.
        this.username = username;
        this.steamAccountName = steamAccountName;
        this.steamAccountId = steamAccountId;
        this.password = password;
    }
    
    // Manipulate the friends list.
    public Set<UUID> getFriends() {
        return Collections.unmodifiableSet(friends);
    }

    public boolean addFriend(UUID friendId) {
        return friends.add(friendId);
    }

    public boolean removeFriend(UUID friendId) {
        return friends.remove(friendId);
    }
    
}
