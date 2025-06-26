package com.securenotes.service;

import com.securenotes.dto.CreateNoteRequest;
import com.securenotes.dto.NotesResponse;
import com.securenotes.exceptions.NotesNotFoundException;
import com.securenotes.model.Notes;
import com.securenotes.model.User;
import com.securenotes.repository.NotesRepository;
import com.securenotes.repository.UserRepository;
import com.securenotes.utils.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public Notes addNote(CreateNoteRequest createNoteRequest) throws Exception {
        Notes notes = createNoteRequest.to();

        User loggedInUser = (User)ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        notes.setTitle(EncryptionUtil.encrypt(createNoteRequest.getTitle()));
        notes.setUserId(loggedInUser.getUserId());
        notes.setDescription(EncryptionUtil.encrypt(createNoteRequest.getDescription()));
        if(createNoteRequest.getPassword() != null && !createNoteRequest.getPassword().isEmpty()){
            notes.setPassword(passwordEncoder.encode(createNoteRequest.getPassword()));
        }else{
            notes.setPassword(null);
        }

        return notesRepository.save(notes);
    }

    public Notes getNoteById(int id) throws Exception {
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Notes notes = notesRepository.findByNotesId(id);
        String decryptedTitle = EncryptionUtil.decrypt(notes.getTitle());
        String decryptedDescription = EncryptionUtil.decrypt(notes.getDescription());
        notes.setTitle(decryptedTitle);
        notes.setDescription(decryptedDescription);

        if(notes.getUserId() == loggedInUser.getUserId() && notes.getPassword() == null){
            return notes;
        }else{
            throw new NotesNotFoundException("Note not found or user does not have permission to access the note");
        }
    }

    public Notes getSecureNoteById(int id) throws Exception {
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Notes notes = notesRepository.findByNotesId(id);
        String decryptedTitle = EncryptionUtil.decrypt(notes.getTitle());
        String decryptedDescription = EncryptionUtil.decrypt(notes.getDescription());
        notes.setTitle(decryptedTitle);
        notes.setDescription(decryptedDescription);
        if(notes.getUserId() == loggedInUser.getUserId()){
            return notes;
        }else{
            throw new NotesNotFoundException("Note not found or user does not have permission to access the note");
        }
    }

    public List<Notes> getAllNotes(){
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        List<Notes> encryptedNotes = notesRepository.findAllNotesByUserId(loggedInUser.getUserId());

        List<Notes> decryptedNotes = encryptedNotes.stream()
                .map(notes -> {
                    try {
                        String decryptedTitle = EncryptionUtil.decrypt(notes.getTitle());
                        String decryptedDescription = EncryptionUtil.decrypt(notes.getDescription());
                        notes.setTitle(decryptedTitle);
                        notes.setDescription(decryptedDescription);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return notes;
                }).collect(Collectors.toList());
        decryptedNotes.stream()
                .filter(n -> n.getPassword() != null)
                .map(n -> {
                    try {
                        n.setDescription(EncryptionUtil.encrypt(n.getDescription()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return n;
                }).toList();

        return decryptedNotes;
    }

    public Notes delete(int id) throws Exception {
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Notes note = getNoteById(id); // Retrieve the note before deletion
        if (note != null && note.getUserId() == loggedInUser.getUserId() && note.getPassword() == null) {
            notesRepository.deleteById(id); // Delete the note
        }
//        String decryptedTitle = EncryptionUtil.decrypt(note.getTitle());
//        String decryptedDescription = EncryptionUtil.decrypt(note.getDescription());
//        note.setTitle(decryptedTitle);
//        note.setDescription(decryptedDescription);
        return note; // Return the deleted note or null if not found
    }

//    public Notes deleteSecuredNote(int id, String password) throws Exception {
//        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
//        Notes note = getSecureNoteById(id);
//        if(note != null
//                && note.getPassword() != null
//                && note.getUserId() == loggedInUser.getUserId()
//                && passwordEncoder.matches(password, note.getPassword())){
//            notesRepository.deleteById(id);
//        }
//        String decryptedTitle = EncryptionUtil.decrypt(note.getTitle());
//        String decryptedDescription = EncryptionUtil.decrypt(note.getDescription());
//        note.setTitle(decryptedTitle);
//        note.setDescription(decryptedDescription);
//        return note;
//    }

public Notes deleteSecuredNote(int id, String password) throws Exception {
    // Get the currently logged-in user
    User loggedInUser = (User) ourUserDetailService.loadUserByUsername(
            SecurityContextHolder.getContext().getAuthentication().getName()
    );

    // Retrieve the secured note by ID
    Notes note = getSecureNoteById(id);

    // Check if the note exists
    if (note == null) {
        throw new Exception("Note not found");
    }

    // Validate the note's ownership, password existence, and password match
    if (note.getPassword() != null &&
            note.getUserId() == loggedInUser.getUserId() &&
            note.getPassword().equals(password)
//            passwordEncoder.matches(password, note.getPassword())
    ) {
        // Delete the note
        notesRepository.deleteById(id);

        // Decrypt the note details
//        String decryptedTitle = EncryptionUtil.decrypt(note.getTitle());
//        String decryptedDescription = EncryptionUtil.decrypt(note.getDescription());
//        note.setTitle(decryptedTitle);
//        note.setDescription(decryptedDescription);

        // Return the decrypted note
        return note;
    } else {
        // Throw an exception for unauthorized access
        throw new Exception("Unauthorized access or invalid password");
    }
}


    public NotesResponse update(int id, CreateNoteRequest createNoteRequest) throws Exception {
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Notes notes = getNoteById(id);
        if(notes != null&&notes.getUserId() == loggedInUser.getUserId() && notes.getPassword() == null){
            notes.setTitle(EncryptionUtil.encrypt(createNoteRequest.getTitle()));
            notes.setDescription(EncryptionUtil.encrypt(createNoteRequest.getDescription()));
//            if (createNoteRequest.getPassword() != null) {
//                notes.setPassword(passwordEncoder.encode(createNoteRequest.getPassword()));
//            }
            if(createNoteRequest.getPassword() != null && !createNoteRequest.getPassword().isEmpty()){
                notes.setPassword(passwordEncoder.encode(createNoteRequest.getPassword()));
            }else{
                notes.setPassword(null);
            }
            notes =  notesRepository.save(notes);
            NotesResponse notesResponse = new NotesResponse();
            notesResponse = NotesResponse.to(notes);
            notesResponse.setMessage("Note updated successfully.");
            return notesResponse;
        }else{
            throw new NotesNotFoundException("Note not found or user does not have permission to update the particular note");
        }

    }

    public NotesResponse updateSecuredNote(int id, String password, CreateNoteRequest createNoteRequest) throws Exception {
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Notes notes = getSecureNoteById(id);
        if(notes != null
                &&notes.getUserId() == loggedInUser.getUserId()
                && passwordEncoder.matches(password,notes.getPassword())){
            notes.setTitle(EncryptionUtil.encrypt(createNoteRequest.getTitle()));
            notes.setDescription(EncryptionUtil.encrypt(createNoteRequest.getDescription()));

            if(createNoteRequest.getPassword() != null && !createNoteRequest.getPassword().isEmpty()){
                notes.setPassword(passwordEncoder.encode(createNoteRequest.getPassword()));
            }else{
                notes.setPassword(null);
            }
            notes = notesRepository.save(notes);
            NotesResponse notesResponse = new NotesResponse();
            notesResponse = NotesResponse.to(notes);
            notesResponse.setMessage("Note updated successfully.");
            return notesResponse;

        }else{
            throw new NotesNotFoundException("Note not found or user does not have permission to update the particular note");
        }
    }

    public NotesResponse setPasswordForNote(int notesId, CreateNoteRequest createNoteRequest) throws Exception {
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        Notes note = notesRepository.findById(notesId).orElse(null);

        if(note == null || note.getUserId() != loggedInUser.getUserId() || note.getPassword() != null){
            throw new NotesNotFoundException("Note not found or user does not have permission the note");
        }

        note.setPassword(passwordEncoder.encode(createNoteRequest.getPassword()));
        note = notesRepository.save(note);
        NotesResponse notesResponse = new NotesResponse();
        notesResponse = NotesResponse.to(note);
        notesResponse.setMessage("Password added successfully to the note.");
        return notesResponse;
    }

    public NotesResponse getNoteByIdAndPassword(int notesId, String password) throws Exception {
        Notes existingNote = notesRepository.findByNotesId(notesId);
        String encodePassword = passwordEncoder.encode(password);
        String notePassword = existingNote.getPassword();
        User loggedInUser = (User)ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if(passwordEncoder.matches(password, notePassword) && existingNote.getUserId() == loggedInUser.getUserId()){
            return NotesResponse.to(existingNote);
        }else{
            throw new NotesNotFoundException("Notes not found or not have permission for this note");
        }
    }

    public List<NotesResponse> search(String searchKey, int loggedInUserId) throws Exception {
        List<Notes> allNotes = notesRepository.findAllNotesWithoutPasswordByUserId(loggedInUserId);// Fetch all notes from database
                List<NotesResponse> searchResults = new ArrayList<>();

                List<NotesResponse> notesResponses = allNotes.stream().map(note -> {
                    try {
                        return NotesResponse.to(note);//to function consist decryption logic
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).toList();

        for (NotesResponse note : notesResponses) {
            String title = note.getTitle();
            String description = note.getDescription();
            if (title.toLowerCase().contains(searchKey.toLowerCase()) ||
                    description.toLowerCase().contains(searchKey.toLowerCase())) {
                searchResults.add(note);
            }
        }

        return searchResults;

    }

    public List<NotesResponse> getAllSecuredNotes() {
        User loggedInUser = (User) ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        List<Notes> notes = notesRepository.findAllSecuredNotes(loggedInUser.getUserId());

        //convert to notes response
        List<NotesResponse>notesResponses = notes.stream()
                .map(note -> {
                    NotesResponse notesResponse = new NotesResponse();
//                    try {
//                        notesResponse = NotesResponse.to(note);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
                    try {
                        notesResponse.setTitle(EncryptionUtil.decrypt(note.getTitle()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    notesResponse.setNotesId(note.getNotesId());
                    notesResponse.setDescription(note.getDescription());
                    notesResponse.setCreatedOn(note.getCreatedOn());
                    notesResponse.setUpdatedOn(note.getUpdatedOn());
                    notesResponse.setMessage("this is secured note enter password to show");
                    return notesResponse;
                }).collect(Collectors.toList());

        return notesResponses;
    }
}
