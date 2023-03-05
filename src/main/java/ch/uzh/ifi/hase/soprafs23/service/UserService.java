package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User getUser(long id) {
        User user = userRepository.findById(id);

        checkIfUserFromIdIsNull(user);
        return user;
    }

    public User createUser(User newUser) {

        String stringDate = getStringDate();

        Date date = new Date();

        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE);
        newUser.setRegistrationDate(stringDate);
        newUser.setCreationDate(date);
        checkIfUserExists(newUser);
        // saves the given entity but data is only persisted in the database once
        // flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush();

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    private static String getStringDate() {
        Date date = new Date();
        SimpleDateFormat DateFor = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return DateFor.format(date);
    }


    public User loginUser(User userInput) {
        User realUser = userRepository.findByUsername(userInput.getUsername());

        if (realUser == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username not found");
        }

        //Check if the input password equals the one matching with the username
        if (realUser.getPassword().equals(userInput.getPassword())) {

            //Overwrite Online Status
            realUser.setStatus(UserStatus.ONLINE);
            userRepository.saveAndFlush(realUser);

            log.debug("Login worked {}", userInput);
            return realUser;
        }
        //if bug check for the exception
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "password does not match with username");
    }

    public User updateUser(User userInput, long id) {

        User userFromDB = userRepository.findById(id);
        checkIfUserFromIdIsNull(userFromDB);

        //check for access
        checkAccess(userInput, userFromDB);

        //Overwrite
        userFromDB.setBirthdate(userInput.getBirthdate());
        userFromDB.setUsername(userInput.getUsername());
        userRepository.saveAndFlush(userFromDB);

        return userFromDB;
    }


    private static void checkAccess(User userInput, User userFromDB) {
        if (!Objects.equals(userInput.getToken(), userFromDB.getToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You have no access to change this Users Information");
        }
    }


    /**
     * This is a helper method that will check the uniqueness criteria of the
     * username and the name
     * defined in the User entity. The method will do nothing if the input is unique
     * and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */
    private void checkIfUserExists(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
        User userByEmail = userRepository.findByEmail(userToBeCreated.getEmail());

        String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
        if (userByUsername != null && userByEmail != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format(baseErrorMessage, "username and the email", "are"));
        }
        else if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
        }
        else if (userByEmail != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "email", "is"));
        }
    }

    private void checkIfUserFromIdIsNull(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User with this ID does not exist");
        }
    }

    private void checkIfUsernameExists(String username) {
        User userByUsername = userRepository.findByUsername(username);

        if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("username already taken"));
        }
    }

    private void checkIfEmailExists(String email) {
        User userByUsername = userRepository.findByEmail(email);

        if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "email already taken");
        }
    }

    public User getUserById(long id) {
        User userById = userRepository.findById(id);
        //check if the user even exists
        if (userById == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "The user with this id does not exist!");
        }
        return userById;
    }

    public User setUserOffline(User userToken, long id) {
        User userOffline = userRepository.findById(id);

        //Check Access
        checkAccess(userToken, userOffline);

        userOffline.setStatus(UserStatus.OFFLINE);
        userRepository.saveAndFlush(userOffline);

        return userOffline;
    }
}
