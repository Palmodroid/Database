package digitalgarden.mecsek.database.library;

import digitalgarden.mecsek.database.authors.AuthorsTable;
import digitalgarden.mecsek.database.books.BooksTable;
import digitalgarden.mecsek.database.medications.MedicationsTable;
import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.templates.GenericDatabase;

public class LibraryDatabase extends GenericDatabase
    {
    @Override
    public String name()
        {
        return "library";
        }

    @Override
    public int version()
        {
        return 2;
        }

    @Override
    public String authority()
        {
        return "digitalgarden.mecsek.contentprovider";
        }

    public static int AUTHORS;
    public static int BOOKS;
    public static int MEDICATIONS;
    public static int PILLS;
    public static int PATIENTS;

    @Override
    public void defineTables()
        {
        AUTHORS = addTable( new AuthorsTable(1) );
        BOOKS = addTable( new BooksTable(2) );
        PILLS = addTable( new PillsTable(3) );
        PATIENTS = addTable( new PatientsTable(4) );
        MEDICATIONS = addTable( new MedicationsTable(5) );
        }
    }
