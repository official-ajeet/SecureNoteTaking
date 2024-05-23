package com.securenotes.service;

import com.securenotes.dto.CreateNoteRequest;
import com.securenotes.exceptions.NotesNotFoundException;
import com.securenotes.model.Notes;
import com.securenotes.model.User;
import com.securenotes.repository.NotesRepository;
import com.securenotes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotesService {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotesRepository notesRepository;
    @Autowired
    OurUserDetailService ourUserDetailService;

    @Autowired
    PasswordEncoder passwordEncoder;

    public Notes addNote(CreateNoteRequest createNoteRequest) {
        Notes notes = createNoteRequest.to();

        User loggedInUser = (User)ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        notes.setTitle( createNoteRequest.getTitle());
        notes.setUserId(loggedInUser.getUserId());
        notes.setDescription(createNoteRequest.getDescription());

        return notesRepository.save(notes);
    }

    public Notes getNoteById(int id){
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Notes notes = notesRepository.findByNotesId(id);
        if(notes != null && notes.getUserId() == loggedInUser.getUserId() && notes.getPassword() == null){
            return notes;
        }else{
            throw new NotesNotFoundException("Note not found or user does not have permission to access the note");
        }
    }

    public Notes getSecureNoteById(int id){
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Notes notes = notesRepository.findByNotesId(id);
        if(notes != null && notes.getUserId() == loggedInUser.getUserId()){
            return notes;
        }else{
            throw new NotesNotFoundException("Note not found or user does not have permission to access the note");
        }
    }

    public List<Notes> getAllNotes(){
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        return notesRepository.findAllNotesWithoutPasswordByUserId(loggedInUser.getUserId());
    }

    public Notes delete(int id){
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Notes note = getNoteById(id); // Retrieve the note before deletion
        if (note != null && note.getUserId() == loggedInUser.getUserId() && note.getPassword() == null) {
            notesRepository.deleteById(id); // Delete the note
        }
        return note; // Return the deleted note or null if not found
    }

    public Notes deleteSecuredNote(int id, String password){
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Notes note = getSecureNoteById(id);
        if(note != null
                && note.getPassword() != null
                && note.getUserId() == loggedInUser.getUserId()
                && passwordEncoder.matches(password, note.getPassword())){
            notesRepository.deleteById(id);
        }
        return note;
    }

    public Notes update(int id, CreateNoteRequest createNoteRequest){
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Notes notes = getNoteById(id);
        if(notes != null&&notes.getUserId() == loggedInUser.getUserId() && notes.getPassword() == null){
            notes.setTitle(createNoteRequest.getTitle());
            notes.setDescription(createNoteRequest.getDescription());
            if (createNoteRequest.getPassword() != null) {
                notes.setPassword(passwordEncoder.encode(createNoteRequest.getPassword()));
            }
            return notesRepository.save(notes);
        }else{
            throw new NotesNotFoundException("Note not found or user does not have permission to update the particular note");
        }
    }

    public Notes updateSecuredNote(int id, String password, CreateNoteRequest createNoteRequest){
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Notes notes = getSecureNoteById(id);
        if(notes != null
                &&notes.getUserId() == loggedInUser.getUserId()
                && passwordEncoder.matches(password,notes.getPassword())){
            notes.setTitle(createNoteRequest.getTitle());
            notes.setDescription(createNoteRequest.getDescription());
            if (createNoteRequest.getPassword() != null) {
                notes.setPassword(passwordEncoder.encode(createNoteRequest.getPassword()));
            }
            return notesRepository.save(notes);
        }else{
            throw new NotesNotFoundException("Note not found or user does not have permission to update the particular note");
        }
    }

    public Notes setPasswordForNote(int notesId, CreateNoteRequest createNoteRequest) {
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        Notes note = notesRepository.findById(notesId).orElse(null);

        if(note == null || note.getUserId() != loggedInUser.getUserId() || note.getPassword() != null){
            throw new NotesNotFoundException("Note not found or user does not have permission the note");
        }

        note.setPassword(passwordEncoder.encode(createNoteRequest.getPassword()));
        return notesRepository.save(note);
    }

    public Notes getNoteByIdAndPassword(int notesId, CreateNoteRequest createNoteRequest) {
        Notes existingNote = notesRepository.findByNotesId(notesId);
        String encodePassword = passwordEncoder.encode(createNoteRequest.getPassword());
        String notePassword = existingNote.getPassword();
        User loggedInUser = (User)ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if(passwordEncoder.matches(createNoteRequest.getPassword(), notePassword) && existingNote.getUserId() == loggedInUser.getUserId()){
            return existingNote;
        }else{
            throw new NotesNotFoundException("Notes not found or not have permission for this note");
        }
    }

    public List<Notes> search(String searchKey, int loggedInUserId){
        List<Notes> allNotes = notesRepository.findAllNotesWithoutPasswordByUserId(loggedInUserId);// Fetch all notes from database
                List<Notes> searchResults = new ArrayList<>();

        for (Notes note : allNotes) {
            String title = note.getTitle();
            String description = note.getDescription();
            if (title.toLowerCase().contains(searchKey.toLowerCase()) ||
                    description.toLowerCase().contains(searchKey.toLowerCase())) {
                searchResults.add(note);
            }
        }

        return searchResults;

    }

}
