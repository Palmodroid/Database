package digitalgarden.mecsek.database.medications;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.patients.PatientsControllActivity;
import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.database.pills.PillsControllActivity;
import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.formtypes.EditFieldDate;
import digitalgarden.mecsek.formtypes.EditFieldText;
import digitalgarden.mecsek.formtypes.ForeignKey;
import digitalgarden.mecsek.generic.GenericEditFragment;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.database.library.LibraryDatabase.MEDICATIONS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class MedicationsEditFragment extends GenericEditFragment
	{
    @Override
    protected int defineTableIndex()
        {
        return MEDICATIONS;
        }

	@Override
	protected int defineFormLayout()
		{
		return 	R.layout.medication_edit_fragment_form;
		}

	@Override
	protected void setupFormLayout()
		{
		Scribe.note("MedicationsEditFragment setupFormLayout");

        // EditTextField
        EditFieldText medicationNameField = (EditFieldText)addEditField( R.id.edittextfield_medication_name, MedicationsTable.NAME );

        // EditTextField
        EditFieldDate medicationDateField = (EditFieldDate)addEditField( R.id.editdatefield_medication_date, MedicationsTable.DATE );

        // ForeignKey
        ForeignKey pillKey = addForeignKey( MedicationsTable.PILL_ID,
                PillsControllActivity.class,
                getActivity().getString( R.string.select_pill ),
                medicationNameField );

		// ForeignKey
        ForeignKey patientKey = addForeignKey( MedicationsTable.PATIENT_ID,
                PatientsControllActivity.class,
				getActivity().getString( R.string.select_patient ),
				medicationNameField );

		// ForeignTextField
        addForeignTextField( pillKey, R.id.foreigntext_pill_name, PILLS, PillsTable.NAME );

		// ForeignTextField
		addForeignTextField( patientKey, R.id.foreigntext_patient_name, PATIENTS, PatientsTable.NAME );
        addForeignTextField( patientKey, R.id.foreigntext_patient_dob, PATIENTS, PatientsTable.DOB );

    	/*
		setupListButton( BooksControllActivity.class,
    			getActivity().getString( R.string.button_books_list ), 
    			getActivity().getString( R.string.books_of ),
    			nameField );
    	*/
		}
	}
