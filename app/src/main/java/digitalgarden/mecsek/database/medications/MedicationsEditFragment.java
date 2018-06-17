package digitalgarden.mecsek.database.medications;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.patients.PatientsControllActivity;
import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.database.pills.PillsControllActivity;
import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.formtypes.EditTextField;
import digitalgarden.mecsek.formtypes.ForeignKey;
import digitalgarden.mecsek.formtypes.ForeignTextField;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.templates.GenericEditFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.MEDICATIONS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class MedicationsEditFragment extends GenericEditFragment
	{
	private EditTextField medicationNameField;
	private ForeignKey pillId;
	// Míg a címet a szöveg azonosítja, a szerzőt az id
	// Nem lehet null, ezért -1 azonosítja a null-t Ezt ellenőrizni kell!!
	private ForeignTextField foreignTextPill;
	private ForeignKey patientId;
	private ForeignTextField foreignTextPatient;
    private ForeignTextField foreignTextPatientDob;

	@Override
	protected Uri getTableContentUri()
		{
		return table(MEDICATIONS).contentUri();
		}

	@Override
	protected int getFormLayout()
		{
		return 	R.layout.medication_edit_fragment_form;
		}

	@Override
	protected void setupFormLayout( View view )
		{
		Scribe.note("MedicationsEditFragment setupFormLayout");

        /**
        addEditTextField( R.id.edittext_medication_name, MedicationsTable.NAME*** );

        pillId = addForeignKey( MedicationsTable.PILL_ID***, PILLS, ((PillsControllActivity.class)), R.string.select_pill, medicationNameField);
        patientId = addForeignKey( MedicationsTable.PATIENT_ID***, PATIENTS, ((PatientsControllActivity.class)), R.string.select_patient, medicationNameField);

        addForeignTextField( R.id.edittext_pill, pillId, PillsTable.NAME );
        addForeignTextField( R.id.edittext_patient, patientId, PatientsTable.NAME );
        addForeignTextField( R.id.edittext_patient_dob, patientId, PatientsTable.DOB );
        **/

        // EditTextField
        medicationNameField = (EditTextField) view.findViewById( R.id.edittext_medication_name );
        medicationNameField.connect( this, MedicationsTable.NAME );

        // ForeignKey
        pillId = new ForeignKey( table(PILLS).contentUri() );
        pillId.connect( this );
        pillId.setupSelector( PillsControllActivity.class,
                getActivity().getString( R.string.select_pill ),
                medicationNameField );

		// ForeignKey
        patientId = new ForeignKey( table(PATIENTS).contentUri() );
		patientId.connect( this );
		patientId.setupSelector( PatientsControllActivity.class,
				getActivity().getString( R.string.select_patient ),
				medicationNameField );

		// ForeignTextField
        foreignTextPill = (ForeignTextField) view.findViewById(R.id.edittext_pill);
        foreignTextPill.link( pillId, column(PillsTable.NAME ));

		// ForeignTextField
		foreignTextPatient = (ForeignTextField) view.findViewById(R.id.edittext_patient);
		foreignTextPatient.link( patientId, column(PatientsTable.NAME ));

        // ForeignTextField
        foreignTextPatientDob = (ForeignTextField) view.findViewById(R.id.edittext_patient_dob);
        foreignTextPatientDob.link( patientId, column(PatientsTable.DOB ));

    	/*
		setupListButton( BooksControllActivity.class,
    			getActivity().getString( R.string.button_books_list ), 
    			getActivity().getString( R.string.books_of ),
    			nameField );
    	*/
		}

	@Override
	protected void setupFieldsData(long id)
		{
		Scribe.note("MedicationsEditFragment setupFieldsData");

		String[] projection = {
				column(MedicationsTable.PILL_ID),
				column(MedicationsTable.PATIENT_ID),
				column(MedicationsTable.NAME) };
		Cursor cursor = getActivity().getContentResolver().query(getItemContentUri(), projection, null, null, null);

		if (cursor != null) // Ez vajon kell? 
			{
			cursor.moveToFirst();

            int column = cursor.getColumnIndexOrThrow( column(MedicationsTable.PILL_ID) );
            if ( cursor.isNull( column ) )
                pillId.setValue( -1L );
            else
                pillId.setValue( cursor.getLong( column ) );

			column = cursor.getColumnIndexOrThrow( column(MedicationsTable.PATIENT_ID) );
			if ( cursor.isNull( column ) )
				patientId.setValue( -1L );
			else
				patientId.setValue( cursor.getLong( column ) );

			medicationNameField.setText(cursor.getString(cursor.getColumnIndexOrThrow( column(MedicationsTable.NAME) )));

            // Always close the cursor
			cursor.close();
			}
		}

	@Override
	protected ContentValues getFieldsData()
		{
		Scribe.note("MedicationsEditFragment getFieldsData");

        ContentValues values = new ContentValues();

        if (pillId.getValue() >= 0)
            values.put( column(MedicationsTable.PILL_ID), pillId.getValue());
        else
            values.putNull( column(MedicationsTable.PILL_ID) );

        if (patientId.getValue() >= 0)
            values.put( column(MedicationsTable.PATIENT_ID), patientId.getValue());
        else
            values.putNull( column(MedicationsTable.PATIENT_ID) );

        String name = medicationNameField.getText().toString();
        values.put( column(MedicationsTable.NAME), name);

        return values;
		}

	@Override
	protected void saveFieldData(Bundle data)
		{
        data.putLong("PILL_ID", pillId.getValue() );
		data.putLong("PATIENT_ID", patientId.getValue() );
		}

	@Override
	protected void retrieveFieldData(Bundle data)
		{
        pillId.setValue( data.getLong( "PILL_ID" ) );
		patientId.setValue( data.getLong( "PATIENT_ID" ) );
		}

	@Override
	protected void checkReturningSelector(int requestCode, long selectedId)
		{
        pillId.checkReturningSelector( requestCode, selectedId );
		patientId.checkReturningSelector( requestCode, selectedId );
        Scribe.note("Pill id: " + selectedId + " was selected in onActivityResult?");
		Scribe.note("Patient id: " + selectedId + " was selected in onActivityResult?");
		}
	}
