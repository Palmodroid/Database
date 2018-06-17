package digitalgarden.mecsek.database.medications;

import android.content.ContentValues;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.templates.GenericListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull_id;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.MEDICATIONS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class MedicationsListFragment extends GenericListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericListFragment newInstance(long limit )
		{
        GenericListFragment listFragmenet = new MedicationsListFragment();

        Bundle args = new Bundle();

        // args.putLong( SELECTED_ITEM , SELECT_DISABLED ); Nincs szelektálás!

        args.putLong( LIMITED_ITEM, limit );
        args.putString( LIMITED_COLUMN, columnFull_id( PILLS ));
        args.putString( ORDERED_COLUMN, columnFull( MedicationsTable.NAME ));
        // args.putString( FILTERED_COLUMN, BooksTable.FULL_SEARCH);
        args.putStringArray( FILTERED_COLUMN, new String[] { columnFull(PillsTable.SEARCH), columnFull(MedicationsTable.SEARCH)});

        listFragmenet.setArguments(args);

        return listFragmenet;
		}

    @Override
    protected int defineTableIndex()
        {
        return MEDICATIONS;
        }

    @Override
	protected String[] getProjection()
		{
		String[] projection = new String[] {
				columnFull(PillsTable.NAME),
				columnFull(PatientsTable.NAME),
				columnFull(PatientsTable.DOB),
				columnFull(MedicationsTable.NAME),
				columnFull_id(MEDICATIONS) };

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
                column(PillsTable.NAME),
                column(PatientsTable.NAME),
                column(PatientsTable.DOB),
                column(MedicationsTable.NAME),
			    column_id() };

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

		values.put( column(MedicationsTable.NAME), "2003.01.02");
		getActivity().getContentResolver().insert( table(MEDICATIONS).contentUri(), values);

		values.put( column(MedicationsTable.NAME), "2017.12.20");
		getActivity().getContentResolver().insert( table(MEDICATIONS).contentUri(), values);

		values.put( column(MedicationsTable.NAME), "2018.05.01");
		getActivity().getContentResolver().insert( table(MEDICATIONS).contentUri(), values);

		}
	}
