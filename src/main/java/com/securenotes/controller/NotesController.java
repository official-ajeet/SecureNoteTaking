package com.securenotes.controller;

import com.securenotes.dto.CreateNoteRequest;
import com.securenotes.model.Notes;
import com.securenotes.model.User;
import com.securenotes.repository.NotesRepository;
import com.securenotes.service.NotesService;
import com.securenotes.service.OurUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("notes")
public class NotesController {

    @Autowired
    NotesService notesService;

    @Autowired
    OurUserDetailService ourUserDetailService;

    @PostMapping("/add")
    public ResponseEntity<Notes> addNote(@RequestBody CreateNoteRequest createNoteRequest){
        return ResponseEntity.ok(notesService.addNote(createNoteRequest) );
    }

    @GetMapping("/getAll")
    public List<Notes> getAll(){
        return notesService.getAllNotes();
    }

    @GetMapping("/get/{id}")
    public Notes getById(@PathVariable("id") int id){
        return notesService.getNoteById(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Notes>deleteNote(@PathVariable("id")int id){
        return ResponseEntity.ok(notesService.delete(id));
    }

    @DeleteMapping("deleteSecureNote/{id}")
    public ResponseEntity<Notes>deleteSecureNote(@PathVariable("id")int id, @RequestParam(required = true) String password){

        return ResponseEntity.ok(notesService.deleteSecuredNote(id, password));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Notes>updateNote(@PathVariable("id")int id,  @RequestBody CreateNoteRequest createNoteRequest){
        return ResponseEntity.ok(notesService.update(id, createNoteRequest));
    }

    @PutMapping("updateSecureNote/{id}")
    public ResponseEntity<Notes>updateSecureNote(@PathVariable("id")int id,
                                                 @RequestParam(required = true) String password,
                                                 @RequestBody CreateNoteRequest createNoteRequest){


        return ResponseEntity.ok(notesService.updateSecuredNote(id, password, createNoteRequest));
    }




    @PutMapping("/setpassword/{id}")
    public ResponseEntity<Notes> setPasswordForNote(@PathVariable int id, @RequestBody CreateNoteRequest createNoteRequest) {
        return ResponseEntity.ok(notesService.setPasswordForNote(id,createNoteRequest));
    }

    @GetMapping("/getByIdPassword/{id}")
    public ResponseEntity<Notes> getNoteByIdAndPassword(@PathVariable int id, @RequestBody CreateNoteRequest createNoteRequest) {
        return ResponseEntity.ok(notesService.getNoteByIdAndPassword(id, createNoteRequest));
    }

    @GetMapping("/search/{searchKey}")
    public ResponseEntity<List<Notes>>search(@PathVariable("searchKey")String searchKey){
        User loggedInUser = (User)ourUserDetailService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.ok(notesService.search(searchKey, loggedInUser.getUserId()));
    }


}
