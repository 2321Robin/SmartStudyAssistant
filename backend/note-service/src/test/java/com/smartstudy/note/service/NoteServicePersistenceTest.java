package com.smartstudy.note.service;

import com.smartstudy.note.entity.Note;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoteServicePersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void publicNotesRemainAvailableAfterServiceRestart() {
        Path storageFile = tempDir.resolve("notes.json");

        NoteService firstService = new NoteService(storageFile);
        Note createdNote = firstService.create("微服务学习笔记", "Gateway 负责统一入口", true);

        NoteService restartedService = new NoteService(storageFile);
        List<Note> publicNotes = restartedService.listPublicNotes();

        assertThat(publicNotes).hasSize(1);
        assertThat(publicNotes.get(0).id()).isEqualTo(createdNote.id());
        assertThat(publicNotes.get(0).title()).isEqualTo("微服务学习笔记");
        assertThat(publicNotes.get(0).createdAt()).isEqualTo(createdNote.createdAt());
    }

    @Test
    void legacyNotesWithoutCreatedAtAreBackfilledOnLoad() throws IOException {
        Path storageFile = tempDir.resolve("notes.json");
        Files.writeString(storageFile, """
                [
                  {
                    "id": 1,
                    "title": "旧笔记",
                    "content": "这是升级前保存的数据",
                    "publicVisible": true
                  }
                ]
                """);

        NoteService noteService = new NoteService(storageFile);
        List<Note> publicNotes = noteService.listPublicNotes();

        assertThat(publicNotes).hasSize(1);
        assertThat(publicNotes.get(0).createdAt()).isNotNull();
        assertThat(Files.readString(storageFile)).contains("createdAt");
    }
}
