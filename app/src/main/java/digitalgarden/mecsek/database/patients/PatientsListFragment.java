package digitalgarden.mecsek.database.patients;

import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.templates.GenericListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;


public class PatientsListFragment extends GenericListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericListFragment newInstance(long select )
		{
		GenericListFragment listFragmenet = new PatientsListFragment();
	
		Bundle args = new Bundle();

		args.putLong( SELECTED_ITEM, select );
		// args.putString( LIMITED_COLUMN, null); Sem ez, sem LIMITED_ITEM nem kell!

		args.putStringArray( FILTERED_COLUMN, new String[] { column(PatientsTable.SEARCH) });
		args.putString( ORDERED_COLUMN, column(PatientsTable.NAME) );

		listFragmenet.setArguments(args);
		
		return listFragmenet;
		}

    @Override
    protected int defineTableIndex()
        {
        return PATIENTS;
        }

    @Override
	protected String[] getProjection()
		{
		String[] projection = new String[] {
				column_id(),
				column(PatientsTable.NAME),
				column(PatientsTable.DOB),
				column(PatientsTable.TAJ),
				column(PatientsTable.PHONE),
				column(PatientsTable.NOTE) };

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
				column(PatientsTable.NAME),
				column(PatientsTable.DOB),
				column(PatientsTable.TAJ),
				column(PatientsTable.PHONE),
				column(PatientsTable.NOTE),
				column_id() };

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
