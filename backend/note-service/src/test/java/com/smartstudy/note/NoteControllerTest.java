package com.smartstudy.note;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "smartstudy.storage.note-file=target/test-data/note-controller-test.json")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class NoteControllerTest {

    private static final Path STORAGE_FILE = Path.of("target/test-data/note-controller-test.json");

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanUpStorage() throws Exception {
        Files.deleteIfExists(STORAGE_FILE);
    }

    @Test
    void createStoresPublicNote() throws Exception {
        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"微服务学习笔记","content":"Gateway 的作用是统一入口","publicVisible":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("微服务学习笔记"))
                .andExpect(jsonPath("$.data.createdAt").isString())
                .andExpect(jsonPath("$.data.publicVisible").value(true));
    }

    @Test
    void squareReturnsOnlyPublicNotes() throws Exception {
        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"公开笔记","content":"Gateway 的作用是统一入口","publicVisible":true}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"私有笔记","content":"只给自己看","publicVisible":false}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notes/square"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("公开笔记"));
    }

    @Test
    void listReturnsAllNotesIncludingPrivateOnes() throws Exception {
        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"公开笔记","content":"大家都能看","publicVisible":true}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"私人草稿","content":"先自己整理","publicVisible":false}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("私人草稿"))
                .andExpect(jsonPath("$.data[0].publicVisible").value(false))
                .andExpect(jsonPath("$.data[1].title").value("公开笔记"))
                .andExpect(jsonPath("$.data[1].publicVisible").value(true));
    }

    @Test
    void squareReturnsNewestPublicNotesFirst() throws Exception {
        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"第一篇公开笔记","content":"先创建的内容","publicVisible":true}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"第二篇公开笔记","content":"后创建的内容","publicVisible":true}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notes/square"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("第二篇公开笔记"))
                .andExpect(jsonPath("$.data[1].title").value("第一篇公开笔记"));
    }

    @Test
    void createReturnsBadRequestForBlankTitle() throws Exception {
        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","content":"Gateway 的作用是统一入口","publicVisible":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isString());
    }
}
