package io.github.pureza.happynews.user;

import io.github.pureza.happynews.config.Config;

/**
 * Factory for users
 */
public class UserFactory {

    /**
     * Creates a new user with the given details
     */
    public static User createUser(String username, String password, User.Role role, Config config) {
        final User user;
        switch (role) {
            case READER:
                user = new Reader(username, password);
                break;
            case EDITOR:
                user = new Editor(username, password, config.usersHome().resolve(username));
                break;
            case ADMIN:
                user = new Admin(username, password, config.usersHome().resolve(username));
                break;
            default:
                throw new IllegalArgumentException("Unexpected role: " + role);
        }

        if (!user.getRole().equals(role)) {
            throw new IllegalStateException(String.format("Unexpected role. user.getRole()=%s, role=%s", user.getRole(), role));
        }

        return user;
    }
}
