package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * @return a List with all Users
     */
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserGetDTO> getAllUsers() {
        // fetch all users in the internal representation
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();
        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    /**
     * @param id of the user requested
     * @return a single User object with the requested id
     */
    @GetMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUser(@PathVariable long id) {
        //fetch requested User
        User requestedUser = userService.getUser(id);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(requestedUser);
    }

    /**
     * Create a new User
     * @param userPostDTO with Username, Email, and Password
     * @return the new created User
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        // create user
        User createdUser = userService.createUser(userInput);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    }

    /**
     * Takes Username and Password to check if those match for login
     *
     * @param userPostDTO with Username and Password
     * @return the User object if login was successful
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO login(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        // create new User
        User loggedInUser = userService.loginUser(userInput);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);
    }

    /**
     * Updates the Username and Birthdate if the request giver has access
     *
     * @param userPutDTO with updated Username and Birthdate
     * @param id         of the User to be changed
     * @return the update User object
     */
    @PutMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public UserGetDTO updateUser(@RequestBody UserPutDTO userPutDTO, @PathVariable long id) {
        User userInput = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);
        //Update the User
        User userUpdate = userService.updateUser(userInput, id);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(userUpdate);
    }

    /**
     * Set the Status of the User to Offline
     *
     * @param id of the user logging out
     * @return the User that was set Offline
     */
    @PutMapping("/users/offline/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO setOffline(@RequestBody UserPutDTO userPutDTO, @PathVariable long id) {
        //Transformation of representation
        User userToken = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

        //Set User Offline
        User userOffline = userService.setUserOffline(userToken, id);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(userOffline);
    }
}
