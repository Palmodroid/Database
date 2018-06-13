package digitalgarden.mecsek.database.medications;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.templates.GeneralListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.fieldFull;
import static digitalgarden.mecsek.database.DatabaseMirror.fieldFull_id;
import static digitalgarden.mecsek.database.DatabaseMirror.field_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.MEDICATIONS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class MedicationsListFragment extends GeneralListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GeneralListFragment newInstance( long limit )
		{
        GeneralListFragment listFragmenet = new MedicationsListFragment();

        Bundle args = new Bundle();

        // args.putLong( SELECTED_ITEM , SELECT_DISABLED ); Nincs szelektálás!

        args.putLong( LIMITED_ITEM, limit );
        args.putString( LIMITED_COLUMN, fieldFull_id( PILLS ));
        args.putString( ORDERED_COLUMN, fieldFull( MedicationsTable.NAME ));
        // args.putString( FILTERED_COLUMN, BooksTable.FULL_SEARCH);
        args.putStringArray( FILTERED_COLUMN, new String[] { fieldFull(PillsTable.SEARCH), fieldFull(MedicationsTable.SEARCH)});

        listFragmenet.setArguments(args);

        return listFragmenet;
		}


	protected int getLoaderId()
		{
		return table(MEDICATIONS).id();
		}

	@Override
	protected Uri getContentUri()
		{
		return table(MEDICATIONS).contentUri();
		}

	@Override
	protected String[] getProjection()
		{
		String[] projection = new String[] {
				fieldFull(PillsTable.NAME),
				fieldFull(PatientsTable.NAME),
				fieldFull(PatientsTable.DOB),
				fieldFull(MedicationsTable.NAME),
				fieldFull_id(MEDICATIONS) };

		return projection;
		}

	@Override
	protected int getRowView()
		{
		return R.layout.medication_list_row_view;
		}

	@Override
	protected String[] getFrom()
		{
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] {
                field(PillsTable.NAME),
                field(PatientsTable.NAME),
                field(PatientsTable.DOB),
                field(MedicationsTable.NAME),
			    field_id() };

		return from;
		}

	@Override
	protected int[] getTo()
		{
		// the XML defined views which the data will be bound to
		int[] to = new int[] {
            R.id.pill,
            R.id.patient,
            R.id.patient_dob,
			R.id.medication,
			R.id.id };

		return to;
		}

	@Override
	protected void addExamples()
		{
		ContentValues values = new ContentValues();

		values.put( field(MedicationsTable.NAME), "2003.01.02");
		getActivity().getContentResolver().insert( table(MEDICATIONS).contentUri(), values);

		values.put( field(MedicationsTable.NAME), "2017.12.20");
		getActivity().getContentResolver().insert( table(MEDICATIONS).contentUri(), values);

		values.put( field(MedicationsTable.NAME), "2018.05.01");
		getActivity().getContentResolver().insert( table(MEDICATIONS).contentUri(), values);

		}
	}
