package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
public class UserControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    private final String defaultUri = String.format("http://localhost:%d/users", port);

    @Test
    public void addUserTest() throws Exception {
        UserDto userDto = makeDefaultUserDto();

        MockHttpServletResponse servletResponse = mvc.perform(
                        post(defaultUri)
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.CREATED.value(), servletResponse.getStatus());
        assertEquals(userDto, mapper.readValue(servletResponse.getContentAsString(), UserDto.class));
    }

    @Test
    public void shouldBeExceptionForAddUserWithDuplicateEmail() throws Exception {
        UserDto userDto = makeDefaultUserDto();

        mvc.perform(post(defaultUri)
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON));

        userDto.setId(2L);

        MockHttpServletResponse servletResponse = mvc.perform(
                        post(defaultUri)
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), servletResponse.getStatus());
    }

    @Test
    public void getUserTest() throws Exception {
        UserDto userDto = makeDefaultUserDto();

        mvc.perform(post(defaultUri)
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse servletResponse = mvc.perform(
                        get(defaultUri + "/1"))
                .andReturn().getResponse();

        assertEquals(HttpStatus.OK.value(), servletResponse.getStatus());
        assertEquals(userDto, mapper.readValue(servletResponse.getContentAsString(), UserDto.class));
    }

    @Test
    public void shouldBeExceptionForNotFoundUser() throws Exception {

        MockHttpServletResponse servletResponse = mvc.perform(
                        get(defaultUri + "/1"))
                .andReturn().getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), servletResponse.getStatus());
    }

    @Test
    public void getUsersTest() throws Exception {
        UserDto userDto1 = makeDefaultUserDto();

        mvc.perform(post(defaultUri)
                .content(mapper.writeValueAsString(userDto1))
                .contentType(MediaType.APPLICATION_JSON));

        UserDto userDto2 = makeDefaultUserDto();
        userDto2.setId(2L);
        userDto2.setName("User2");
        userDto2.setEmail("newEmail@mail.ru");

        mvc.perform(post(defaultUri)
                .content(mapper.writeValueAsString(userDto2))
                .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse servletResponse = mvc.perform(
                        get(defaultUri)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(List.of(userDto1, userDto2),
                mapper.readValue(servletResponse.getContentAsString(), new TypeReference<List<UserDto>>() {}));
    }

    @Test
    public void updateUserTest() throws Exception {
        UserDto userDto = makeDefaultUserDto();
        userDto.setName("User");
        userDto.setEmail("user@mail.ru");

        mvc.perform(post(defaultUri)
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON));

        userDto.setName(null);
        userDto.setEmail("newEmail@mail.ru");

        MockHttpServletResponse servletResponse = mvc.perform(
                        patch(defaultUri + "/1")
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        userDto.setName("User");

        assertEquals(HttpStatus.OK.value(), servletResponse.getStatus());
        assertEquals(userDto, mapper.readValue(servletResponse.getContentAsString(), UserDto.class));
    }

    @Test
    public void shouldBeExceptionForEmptyRequest() throws Exception {
        UserDto userDto = makeDefaultUserDto();

        mvc.perform(post(defaultUri)
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON));

        userDto.setName(null);
        userDto.setEmail(null);

        MockHttpServletResponse servletResponse = mvc.perform(
                        patch(defaultUri + "/1")
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), servletResponse.getStatus());
    }

    @Test
    public void deleteUserTest() throws Exception {

        mvc.perform(post(defaultUri)
                .content(mapper.writeValueAsString(makeDefaultUserDto()))
                .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse deleteResponse = mvc.perform(
                        delete(defaultUri + "/1"))
                .andReturn().getResponse();

        MockHttpServletResponse listResponse = mvc.perform(
                        get(defaultUri))
                .andReturn().getResponse();

        assertEquals(HttpStatus.OK.value(), deleteResponse.getStatus());
        assertTrue(mapper.readValue(listResponse.getContentAsString(),
                new TypeReference<List<UserDto>>() {
                }).isEmpty());
    }

    @Test
    public void shouldBeExceptionForDeleteNonExistentUser() throws Exception {

        MockHttpServletResponse servletResponse = mvc.perform(
                        delete(defaultUri + "/1"))
                .andReturn().getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), servletResponse.getStatus());
    }

    @Test
    public void shouldBeExceptionForPatchWithDuplicateEmail() throws Exception {
        UserDto userDto1 = makeDefaultUserDto();
        userDto1.setName("User");
        userDto1.setEmail("useremail@mail.ru");

        mvc.perform(post(defaultUri)
                .content(mapper.writeValueAsString(userDto1))
                .contentType(MediaType.APPLICATION_JSON));

        UserDto userDto2 = makeDefaultUserDto();
        userDto2.setId(2L);
        userDto2.setName("User2");
        userDto2.setEmail("newEmail@mail.com");

        mvc.perform(post(defaultUri)
                .content(mapper.writeValueAsString(userDto2))
                .contentType(MediaType.APPLICATION_JSON));

        userDto1.setName(null);

        MockHttpServletResponse response = mvc.perform(
                        patch(defaultUri + "/2")
                                .content(mapper.writeValueAsString(userDto1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    private UserDto makeDefaultUserDto() {
        return UserDto.builder()
                .id(1L)
                .name("User Name")
                .email("email@mail.ru")
                .build();
    }
}
