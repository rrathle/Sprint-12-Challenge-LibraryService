package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.LibraryNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.*;
import com.bloomtech.library.models.checkableTypes.Checkable;
import com.bloomtech.library.models.checkableTypes.Media;
import com.bloomtech.library.repositories.LibraryRepository;
import com.bloomtech.library.models.CheckableAmount;
import com.bloomtech.library.views.LibraryAvailableCheckouts;
import com.bloomtech.library.views.OverdueCheckout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    //TODO: Implement behavior described by the unit tests in tst.com.bloomtech.library.services.LibraryService

    @Autowired
    private LibraryRepository libraryRepository;
    @Autowired
    private CheckableService checkableService;

    public List<Library> getLibraries() {
        return libraryRepository.findAll();
    }

    public Library getLibraryByName(String name) {
        return libraryRepository.findByName(name).orElseThrow(() -> new LibraryNotFoundException("Library not found"));
    }

    public void save(Library library) {
        List<Library> libraries = libraryRepository.findAll();
        if (libraries.stream().filter(p->p.getName().equals(library.getName())).findFirst().isPresent()) {
            throw new ResourceExistsException("Library with name: " + library.getName() + " already exists!");
        }
        libraryRepository.save(library);
    }

    public CheckableAmount getCheckableAmount(String libraryName, String checkableIsbn) {
        Library library = libraryRepository.findByName(libraryName).orElseThrow(() -> new LibraryNotFoundException("library not found"));
        Checkable checkable = checkableService.getByIsbn(checkableIsbn);

        Optional<CheckableAmount> checkableAmount = library.getCheckables().stream()
                .filter(ca -> ca.getCheckable().getIsbn().equals(checkableIsbn))
                .findFirst();

        if (checkableAmount.isPresent()) {
            return checkableAmount.get();
        } else {
            return new CheckableAmount(checkable, 0);
        }

    }

    public List<LibraryAvailableCheckouts> getLibrariesWithAvailableCheckout(String isbn) {
        Checkable checkable = checkableService.getByIsbn(isbn);
        List<Library> libraries = libraryRepository.findAll();
        List<LibraryAvailableCheckouts> availableCheckouts = new ArrayList<>();

        for (Library library : libraries) {
            CheckableAmount foundCheckableAmount = null;
            for (CheckableAmount checkableAmount : library.getCheckables()) {
                if (checkableAmount.getCheckable().getIsbn().equals(isbn)) {
                    foundCheckableAmount = checkableAmount;
                    break;
                }
            }
            int available = (foundCheckableAmount != null) ? foundCheckableAmount.getAmount() : 0;
            availableCheckouts.add(new LibraryAvailableCheckouts(available, library.getName()));
        }

        return availableCheckouts;
    }


    public List<OverdueCheckout> getOverdueCheckouts(String libraryName) {
        Library library = libraryRepository.findByName(libraryName)
                .orElseThrow(() -> new LibraryNotFoundException("Library not found"));

        List<OverdueCheckout> overdueCheckouts = new ArrayList<>();

        for (LibraryCard card : library.getLibraryCards()) {
            for (Checkout checkout : card.getCheckouts()) {
                if (checkout.getDueDate().isBefore(LocalDateTime.now())) {
                    overdueCheckouts.add(new OverdueCheckout(card.getPatron(), checkout));
                }
            }
        }

        return overdueCheckouts;
    }
}
