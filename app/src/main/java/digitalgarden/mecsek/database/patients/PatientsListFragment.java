package digitalgarden.mecsek.database.patients;

import android.net.Uri;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.templates.GeneralListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.field_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;


public class PatientsListFragment extends GeneralListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GeneralListFragment newInstance( long select )
		{
		GeneralListFragment listFragmenet = new PatientsListFragment();
	
		Bundle args = new Bundle();

		args.putLong( SELECTED_ITEM, select );
		// args.putString( LIMITED_COLUMN, null); Sem ez, sem LIMITED_ITEM nem kell!

		args.putStringArray( FILTERED_COLUMN, new String[] { field(PatientsTable.SEARCH) });
		args.putString( ORDERED_COLUMN, field(PatientsTable.NAME) );

		listFragmenet.setArguments(args);
		
		return listFragmenet;
		}

	
	protected int getLoaderId()
		{
		return table(PATIENTS).id();
		}

	@Override
	protected Uri getContentUri()
		{
		return table(PATIENTS).contentUri();
		}

	@Override
	protected String[] getProjection()
		{
		String[] projection = new String[] {
				field_id(),
				field(PatientsTable.NAME),
				field(PatientsTable.DOB),
				field(PatientsTable.TAJ),
				field(PatientsTable.PHONE),
				field(PatientsTable.NOTE) };

		return projection;
		}
	
	@Override
	protected int getRowView()
		{
		return R.layout.patient_list_row_view;
		}

	@Override
	protected String[] getFrom()
		{
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] {
				field(PatientsTable.NAME),
				field(PatientsTable.DOB),
				field(PatientsTable.TAJ),
				field(PatientsTable.PHONE),
				field(PatientsTable.NOTE),
				field_id() };

		return from;
		}

	@Override
	protected int[] getTo()
		{
		// the XML defined views which the data will be bound to
		int[] to = new int[] {
				R.id.name,
				R.id.dob,
				R.id.taj,
				R.id.phone,
				R.id.note,
				R.id.id };

		return to;
		}

	@Override
	protected void addExamples()
		{
		/*
		ContentValues values = new ContentValues();

		values.put( AuthorsTable.NAME, "Láng Attila D.");
		getActivity().getContentResolver().insert( AuthorsTable.CONTENT_URI, values);

		values.put( AuthorsTable.NAME, "Gárdonyi Géza");
		getActivity().getContentResolver().insert( AuthorsTable.CONTENT_URI, values);

		values.put( AuthorsTable.NAME, "Molnár Ferenc");
		getActivity().getContentResolver().insert( AuthorsTable.CONTENT_URI, values);

		values.put( AuthorsTable.NAME, "Szabó Magda");
		getActivity().getContentResolver().insert( AuthorsTable.CONTENT_URI, values);

		values.put( AuthorsTable.NAME, "Fekete István");
		getActivity().getContentResolver().insert( AuthorsTable.CONTENT_URI, values);
		*/
		}
	}
