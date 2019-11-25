package org.springframework.samples.petclinic.service;

import com.github.mryf323.tractatus.*;
import org.junit.Test;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.*;
import org.springframework.samples.petclinic.repository.jpa.*;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import static org.junit.Assert.*;

public class ClinicServiceImplTest {

    public ClinicService initialClinicService(Owner owner,Pet cat ,int petbirthYear ,int pbm ,int pbd ,int vy ,int vm ,int vd , boolean haveVet){
        //initial
        cat.setBirthDate(new Date(petbirthYear,pbm,pbd));
        Visit visit = new Visit();
        visit.setDate(new Date(vy,vm,vd));
        cat.addVisit(visit);
        cat.setOwner(owner);
        owner.addPet(cat);

        PetRepository petRepository = new JpaPetRepositoryImpl();
        VetRepository vetRepository = new JpaVetRepositoryImpl();
        OwnerRepository ownerRepository = new JpaOwnerRepositoryImpl();
        VisitRepository visitRepository = new JpaVisitRepositoryImpl();
        SpecialtyRepository specialtyRepository = new JpaSpecialtyRepositoryImpl();
        PetTypeRepository petTypeRepository = new JpaPetTypeRepositoryImpl();


        ClinicServiceImpl clinicService = new ClinicServiceImpl(petRepository ,vetRepository,ownerRepository,visitRepository,specialtyRepository,petTypeRepository);
        if(haveVet) {
            Vet vet = new Vet();
            clinicService.saveVet(vet);
        }
        clinicService.saveOwner(owner);
        clinicService.savePet(cat);
        return clinicService;

    }
    private boolean assertVisit(Iterator<Visit> visitIterator) {
        while (visitIterator.hasNext()){
            Visit next = visitIterator.next();
            if(next.getDescription().equals("health check up")&& next.getDate().equals(new Date())){
                return true;
            }
        }
        return false;
    }

    @ClauseDefinition(clause ='b' ,def= "age>3")
    @ClauseDefinition(clause ='c', def ="daysFromLastVisit>364")
    @ClauseDefinition(clause ='d' ,def = "daysFromLastVisit>182 ")
    @ClauseDefinition(clause ='e',def = "vet.isPresent()")
    @ClauseCoverage(
        predicate = "(( b && c ) || (~b && d )) && e ",
        valuations ={
            @Valuation(clause = 'b',valuation = true),
            @Valuation(clause = 'c',valuation = true),
            @Valuation(clause = 'e' ,valuation = true)
        }
    )
    @Test
    public void visitOwnerPetsClauseCoverage1() {

        Owner owner = new Owner();
        Pet cat = new Pet();
        ClinicService clinicService =initialClinicService(owner,cat,2014 , 4, 5, 2017 , 6 , 8 , true);

        clinicService.visitOwnerPets(owner);

        Collection<Visit> visits =clinicService.findVisitsByPetId(cat.getId());
        Iterator<Visit> visitIterator = visits.iterator();
        assertTrue(assertVisit(visitIterator));
    }
    @ClauseCoverage(
        predicate = " (b && c))|| (~b && d ) && e ",
        valuations ={
            @Valuation(clause = 'b',valuation = false),
            @Valuation(clause = 'd',valuation = true),
            @Valuation(clause = 'e' ,valuation = true)
        }
    )
    @Test
    public void visitOwnerPetsClauseCoverage2() {

        Owner owner = new Owner();
        Pet cat = new Pet();
        ClinicService clinicService =initialClinicService(owner,cat,2017, 4, 5, 2017 , 6 , 8 , true);

        clinicService.visitOwnerPets(owner);

        Collection<Visit> visits =clinicService.findVisitsByPetId(cat.getId());
        Iterator<Visit> visitIterator = visits.iterator();
        assertTrue(assertVisit(visitIterator));
    }
    @ClauseCoverage(
        predicate = " (b && c))|| (~b && d ) && e ",
        valuations ={
            @Valuation(clause = 'b',valuation = false),
            @Valuation(clause = 'd',valuation = true),
            @Valuation(clause = 'e' ,valuation = false)
        }
    )
    @Test(expected = ClinicServiceImpl.VisitException.class)
    public void visitOwnerPetsClauseCoverage3() {

        Owner owner = new Owner();
        Pet cat = new Pet();
        ClinicService clinicService =initialClinicService(owner,cat,2014 , 4, 5, 2017 , 6 , 8 , false);

        clinicService.visitOwnerPets(owner);
    }
    @UniqueTruePoint(
        predicate = "(( b && c ) || (~b && d )) && e ",
        cnf = "bce + ~bde",
        implicant = "bce",
        valuations = {
            @Valuation(clause = 'd', valuation = false),
            @Valuation(clause = 'b', valuation = true),
            @Valuation(clause = 'c', valuation = true),
            @Valuation(clause = 'e', valuation = true)
        }
    )
    @NearFalsePoint(
        predicate = "(( b && c ) || (~b && d )) && e ",
        cnf = "bce + ~bde",
        implicant = "bce",
        clause = 'b',
        valuations = {
            @Valuation(clause = 'd', valuation = false),
            @Valuation(clause = 'b', valuation = false),
            @Valuation(clause = 'c', valuation = true),
            @Valuation(clause = 'e', valuation = true)
        }
    )
    @Test
    public void visitOwnerPetsUniqueTruePoint1() {
        Owner owner = new Owner();
        Pet cat = new Pet();
        ClinicService clinicService =initialClinicService(owner,cat,2018 , 4, 5, 2018, 6 , 8,true);

        clinicService.visitOwnerPets(owner);

        Collection<Visit> visits =clinicService.findVisitsByPetId(cat.getId());
        Iterator<Visit> visitIterator = visits.iterator();
        assertTrue(assertVisit(visitIterator));
    }
    @UniqueTruePoint(
        predicate = "(( b && c ) || (~b && d )) && e ",
        cnf = "bce + ~bde",
        implicant = "bce",
        valuations = {
            @Valuation(clause = 'd', valuation = false),
            @Valuation(clause = 'b', valuation = true),
            @Valuation(clause = 'c', valuation = true),
            @Valuation(clause = 'e', valuation = true)
        }
    )
    @NearFalsePoint(
        predicate = "(( b && c ) || (~b && d )) && e ",
        cnf = "bce + ~bde",
        implicant = "bce",
        clause = 'c',
        valuations = {
            @Valuation(clause = 'd', valuation = false),
            @Valuation(clause = 'b', valuation = true),
            @Valuation(clause = 'c', valuation = false),
            @Valuation(clause = 'e', valuation = true)
        }
    )
    @Test
    public void visitOwnerPetsUniqueTruePoint2() {
        Owner owner = new Owner();
        Pet cat = new Pet();
        ClinicService clinicService =initialClinicService(owner,cat,2014 , 4, 5, 2018 , 12 , 8,true);

        clinicService.visitOwnerPets(owner);

        Collection<Visit> visits =clinicService.findVisitsByPetId(cat.getId());
        Iterator<Visit> visitIterator = visits.iterator();
        assertTrue(assertVisit(visitIterator));
    }
    @CACC(
        predicate = "(( b && c ) || (~b && d )) && e",
        majorClause = 'e',
        valuations = {
            @Valuation(clause = 'b', valuation = false),
            @Valuation(clause = 'd', valuation = true),
            @Valuation(clause = 'c', valuation = false),
            @Valuation(clause = 'e', valuation = true)
        },
        predicateValue = true
    )
    @Test
    public void visitOwnerPetsCACC1() {
        Owner owner = new Owner();
        Pet cat = new Pet();
        ClinicService clinicService =initialClinicService(owner,cat,2018 , 4, 5, 2018 , 6, 8,true);

        clinicService.visitOwnerPets(owner);

        Collection<Visit> visits =clinicService.findVisitsByPetId(cat.getId());
        Iterator<Visit> visitIterator = visits.iterator();
        assertTrue(assertVisit(visitIterator));
    }





}
