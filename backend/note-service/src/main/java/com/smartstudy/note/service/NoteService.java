package com.smartstudy.note.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.smartstudy.note.entity.Note;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NoteService {

    private static final TypeReference<List<Note>> NOTE_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final Path storageFile;
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final CopyOnWriteArrayList<Note> notes = new CopyOnWriteArrayList<>();

    @Autowired
    public NoteService(ObjectMapper objectMapper, @Value("${smartstudy.storage.note-file:.smartstudyassistant/notes.json}") String storageFile) {
        this(objectMapper, Path.of(storageFile));
    }

    NoteService(Path storageFile) {
        this(new ObjectMapper().findAndRegisterModules(), storageFile);
    }

    NoteService(ObjectMapper objectMapper, Path storageFile) {
        this.objectMapper = objectMapper.findAndRegisterModules();
        this.storageFile = storageFile;
        loadFromDisk();
    }

    public Note create(String title, String content, boolean publicVisible) {
        Note note = new Note(idGenerator.getAndIncrement(), title, content, publicVisible, Instant.now());
        notes.add(note);
        saveToDisk();
        return note;
    }

    public List<Note> listAllNotes() {
        return notes.stream()
                .sorted(
                        Comparator.comparing(Note::createdAt, Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(Note::id, Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .toList();
    }

    public List<Note> listPublicNotes() {
        return listAllNotes().stream().filter(Note::publicVisible).toList();
    }

    private void loadFromDisk() {
        if (!Files.exists(storageFile)) {
            return;
        }

        try {
            List<Note> storedNotes = migrateLegacyNotes(objectMapper.readValue(storageFile.toFile(), NOTE_LIST_TYPE));
            notes.addAll(storedNotes);
            for (Note note : storedNotes) {
                if (note.id() != null) {
                    idGenerator.set(Math.max(idGenerator.get(), note.id() + 1));
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load notes from " + storageFile, exception);
        }
    }

    private void saveToDisk() {
        try {
            Path parent = storageFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(storageFile.toFile(), notes);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save notes to " + storageFile, exception);
        }
    }

    private List<Note> migrateLegacyNotes(List<Note> storedNotes) throws IOException {
        boolean needsRewrite = storedNotes.stream().anyMatch(note -> note.createdAt() == null);
        if (!needsRewrite) {
            return storedNotes;
        }

        Instant baseInstant = Files.getLastModifiedTime(storageFile).toInstant().truncatedTo(ChronoUnit.MINUTES);
        List<Note> migratedNotes = new ArrayList<>(storedNotes.size());
        for (int index = 0; index < storedNotes.size(); index++) {
            Note note = storedNotes.get(index);
            Instant createdAt = note.createdAt() != null
                    ? note.createdAt()
                    : baseInstant.minus(storedNotes.size() - 1L - index, ChronoUnit.MINUTES);
            migratedNotes.add(new Note(note.id(), note.title(), note.content(), note.publicVisible(), createdAt));
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(storageFile.toFile(), migratedNotes);
        return migratedNotes;
    }
}
