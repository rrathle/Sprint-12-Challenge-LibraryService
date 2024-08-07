package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.CheckableNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.Library;
import com.bloomtech.library.models.checkableTypes.*;
import com.bloomtech.library.repositories.CheckableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@SpringBootTest
public class CheckableServiceTest {

    //TODO: Inject dependencies and mocks
    @Autowired
    private CheckableService checkableService;
    @MockBean
    private CheckableRepository checkableRepository;

    private List<Checkable> checkables;

    @BeforeEach
    void init() {
        //Initialize test data
        checkables = new ArrayList<>();

        checkables.addAll(
                Arrays.asList(
                        new Media("1-0", "The White Whale", "Melvin H", MediaType.BOOK),
                        new Media("1-1", "The Sorcerer's Quest", "Ana T", MediaType.BOOK),
                        new Media("1-2", "When You're Gone", "Complaining at the Disco", MediaType.MUSIC),
                        new Media("1-3", "Nature Around the World", "DocuSpecialists", MediaType.VIDEO),
                        new ScienceKit("2-0", "Anatomy Model"),
                        new ScienceKit("2-1", "Robotics Kit"),
                        new Ticket("3-0", "Science Museum Tickets"),
                        new Ticket("3-1", "National Park Day Pass")
                )
        );
    }

    @Test
    void getAll_ReturnsAllCheckables() {
        //Arrange
        Mockito.when(checkableRepository.findAll()).thenReturn(checkables);

        //Act
        List<Checkable> result = checkableService.getAll();

        //Assert
        assertEquals(checkables.size(), result.size());
        assertIterableEquals(checkables, result);

    }
    @Test
    void getByIsbn_existingIsbn_returnsCheckable() {
        //Arrange
        String isbn = "1-0";
        Mockito.when(checkableRepository.findByIsbn(isbn)).thenReturn(Optional.of(checkables.get(0)));

        //Act
        Checkable result = checkableService.getByIsbn(isbn);

        //Assert
        assertEquals(checkables.get(0), result);

    }
    @Test
    void getByIsb_NonExistingIsbn_throwsCheckableNotFoundException() {
        //Arrange
        String isbn = "non Existent";
        Mockito.when(checkableRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
        //Act & assert
        assertThrows(CheckableNotFoundException.class, () -> {
            checkableService.getByIsbn(isbn);
        });
    }
    @Test
    void getByType_ExistingType_returnsCheckable() {
        //Arrange
        Class<Media> type = Media.class;
        Mockito.when(checkableRepository.findByType(type)).thenReturn(Optional.of(checkables.get(0)));
        //Act
        Checkable result = checkableService.getByType(type);
        // Assert
        assertEquals(checkables.get(0), result);

    }
    @Test
    void getByType_NonExistingType_throwsCheckableNotFoundException() {
        //Arrange
        Class<Media> type = Media.class;
        Mockito.when(checkableRepository.findByType(type)).thenReturn(Optional.empty());
        //Act &Assert
        assertThrows(CheckableNotFoundException.class, () -> {
            checkableService.getByType(type);
        });

    }
    @Test
    void save_newCheckable_SavesToRepository() {
        //Arrange
        Checkable newCheckable = new Media("4-0", "New Book", "Author", MediaType.BOOK);
        Mockito.when(checkableRepository.findAll()).thenReturn(checkables);
        //Act
        checkableService.save(newCheckable);

        //Assert
        Mockito.verify(checkableRepository, times(1)).save(newCheckable);

    }
    @Test
    void save_existingCheckable_throwsResourceExistsException() {
        // Arrange
        Checkable existingCheckable = checkables.get(0);
        Mockito.when(checkableRepository.findAll()).thenReturn(checkables);

        // Act & Assert
        assertThrows(ResourceExistsException.class, () -> {
            checkableService.save(existingCheckable);
        });
        Mockito.verify(checkableRepository, times(0)).save(existingCheckable);
    }

    //TODO: Write Unit Tests for all CheckableService methods and possible Exceptions
}