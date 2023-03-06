package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */


@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
        // given
        User user = new User();
        user.setEmail("Email");
        user.setUsername("Username");
        user.setStatus(UserStatus.ONLINE);

        List<User> allUsers = Collections.singletonList(user);

        // this mocks the UserService -> we define above what the userService should
        // return when getUsers() is called
        given(userService.getUsers()).willReturn(allUsers);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is(user.getEmail())))
                .andExpect(jsonPath("$[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
    }


    @Test
    public void givenUser_whenGetUser_thenReturnUser() throws Exception {
        //Given
        User user = new User();
        user.setId(1L);
        user.setEmail("Email");
        user.setUsername("testUsername");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);
        user.setPassword("1234");

        // when
        given(userService.getUser(Mockito.anyLong())).willReturn(user);

        MockHttpServletRequestBuilder getRequest = get("/users/1").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }


    @Test
    public void UserDoesNotExist_whenGetUser_Error() throws Exception {

        // this mocks the UserService -> we define above what the userService should return when getUsers() is called
        //
        Mockito.when(userService.getUser(Mockito.anyLong())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("No user found with Id %d", 1000)));

        // when/then -> do the request + validate the result (REST API Test)
        MockHttpServletRequestBuilder getRequest = get("/users/1")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }


    @Test
    public void createUser_validInput_userCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setEmail("Email");
        user.setUsername("testUsername");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);
        user.setPassword("1234");

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setEmail("Test User");
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("1234");

        given(userService.createUser(Mockito.any())).willReturn(user);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    @Test
    public void createUser_invalidInput_userNotCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setEmail("Email");
        user.setUsername("testUsername");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);
        user.setPassword("1234");

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setEmail("Test User");
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("1234");

        Mockito.when(userService.createUser(Mockito.any())).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, String.format("No user found with Id %d", 1000)));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }


    @Test
    public void updateUser_validInput() throws Exception {
        Date tempDate = new Date();

        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("password123");
        user.setUsername("updateUsername");
        user.setEmail("Email");
        user.setBirthdate("20-20-2022");
        user.setToken("555");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("updateUsername");
        userPutDTO.setBirthdate("20-20-2022");
        userPutDTO.setToken("555");

        given(userService.updateUser(Mockito.any(), Mockito.anyLong())).willReturn(user);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.birthdate", is(user.getBirthdate())))
                .andExpect(jsonPath("$.username", is(user.getUsername())));
    }

    @Test
    public void updateUser_IdDoesNotExist() throws Exception {
        Date tempDate = new Date();

        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("password123");
        user.setUsername("updateUsername");
        user.setEmail("Email");
        user.setBirthdate("20-20-2022");
        user.setToken("555");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("updateUsername");
        userPutDTO.setBirthdate("20-20-2022");
        userPutDTO.setToken("555");

        Mockito.when(userService.updateUser(Mockito.any(), Mockito.anyLong())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "STRING"));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUser_unauthorized() throws Exception {
        Date tempDate = new Date();

        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("password123");
        user.setUsername("updateUsername");
        user.setEmail("Email");
        user.setBirthdate("20-20-2022");
        user.setToken("555");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("updateUsername");
        userPutDTO.setBirthdate("20-20-2022");
        userPutDTO.setToken("555");

        Mockito.when(userService.updateUser(Mockito.any(), Mockito.anyLong())).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "STRING"));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void updateUser_usernameTaken() throws Exception {
        Date tempDate = new Date();

        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("password123");
        user.setUsername("updateUsername");
        user.setEmail("Email");
        user.setBirthdate("20-20-2022");
        user.setToken("555");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("updateUsername");
        userPutDTO.setBirthdate("20-20-2022");
        userPutDTO.setToken("555");

        Mockito.when(userService.updateUser(Mockito.any(), Mockito.anyLong())).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "STRING"));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isConflict());
    }


    @Test
    public void successful_login() throws Exception {
        //Given
        User user = new User();
        user.setId(1L);
        user.setEmail("Email");
        user.setUsername("testUsername");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);
        user.setPassword("1234");


        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("1234");


        given(userService.loginUser(Mockito.any())).willReturn(user);


        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));


        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }


    @Test
    public void unsuccessful_login() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");

        Mockito.when(userService.loginUser(Mockito.any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "STRING"));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest());
    }


    @Test
    public void successful_SetUserOffline() throws Exception {
        //Given
        User user = new User();
        user.setId(1L);
        user.setEmail("Email");
        user.setUsername("testUsername");
        user.setToken("555");
        user.setStatus(UserStatus.OFFLINE);
        user.setPassword("1234");


        given(userService.setUserOffline(Mockito.any(), Mockito.anyLong())).willReturn(user);


        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setToken("555");


        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/users/offline/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));


        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }


    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input
     * can be processed
     * Input will look like this: {"name": "Test User", "username": "testUsername"}
     *
     * @param object
     * @return string
     */
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }

}
