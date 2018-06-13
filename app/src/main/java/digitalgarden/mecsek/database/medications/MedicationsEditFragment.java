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
import digitalgarden.mecsek.templates.GeneralEditFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.MEDICATIONS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class MedicationsEditFragment extends GeneralEditFragment
	{
	private EditTextField medicationNameField;
	private ForeignKey pillId = new ForeignKey( table(PILLS).contentUri() );
	// Míg a címet a szöveg azonosítja, a szerzőt az id
	// Nem lehet null, ezért -1 azonosítja a null-t Ezt ellenőrizni kell!!
	private ForeignTextField foreignTextPill;
	private ForeignKey patientId = new ForeignKey( table(PATIENTS).contentUri() );
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

        // EditTextField
        medicationNameField = (EditTextField) view.findViewById( R.id.edittext_medication_name );
        medicationNameField.connect( this );

        // ForeignKey
        pillId.connect( this );
        pillId.setupSelector( PillsControllActivity.class,
                getActivity().getString( R.string.select_pill ),
                medicationNameField );

		// ForeignKey
		patientId.connect( this );
		patientId.setupSelector( PatientsControllActivity.class,
				getActivity().getString( R.string.select_patient ),
				medicationNameField );

		// ForeignTextField
        foreignTextPill = (ForeignTextField) view.findViewById(R.id.edittext_pill);
        foreignTextPill.link( pillId, field(PillsTable.NAME ));

		// ForeignTextField
		foreignTextPatient = (ForeignTextField) view.findViewById(R.id.edittext_patient);
		foreignTextPatient.link( patientId, field(PatientsTable.NAME ));

        // ForeignTextField
        foreignTextPatientDob = (ForeignTextField) view.findViewById(R.id.edittext_patient_dob);
        foreignTextPatientDob.link( patientId, field(PatientsTable.DOB ));

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
				field(MedicationsTable.PILL_ID),
				field(MedicationsTable.PATIENT_ID),
				field(MedicationsTable.NAME) };
		Cursor cursor = getActivity().getContentResolver().query(getItemContentUri(), projection, null, null, null);

		if (cursor != null) // Ez vajon kell? 
			{
			cursor.moveToFirst();

            int column = cursor.getColumnIndexOrThrow( field(MedicationsTable.PILL_ID) );
            if ( cursor.isNull( column ) )
                pillId.setValue( -1L );
            else
                pillId.setValue( cursor.getLong( column ) );

			column = cursor.getColumnIndexOrThrow( field(MedicationsTable.PATIENT_ID) );
			if ( cursor.isNull( column ) )
				patientId.setValue( -1L );
			else
				patientId.setValue( cursor.getLong( column ) );

			medicationNameField.setText(cursor.getString(cursor.getColumnIndexOrThrow( field(MedicationsTable.NAME) )));

            // Always close the cursor
			cursor.close();
			}
		}

	@Override
	protected ContentValues getFieldsData()
		{
		Scribe.note("MedicationsEditFragment getFieldsData");

		String name = medicationNameField.getText().toString();

        ContentValues values = new ContentValues();
        if (pillId.getValue() >= 0)
            values.put( field(MedicationsTable.PILL_ID), pillId.getValue());
        else
            values.putNull( field(MedicationsTable.PILL_ID) );

        if (patientId.getValue() >= 0)
            values.put( field(MedicationsTable.PATIENT_ID), patientId.getValue());
        else
            values.putNull( field(MedicationsTable.PATIENT_ID) );

        values.put( field(MedicationsTable.NAME), name);

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
