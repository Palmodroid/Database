package digitalgarden.mecsek.database.pills;

import android.content.ContentValues;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.templates.GenericListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class PillsListFragment extends GenericListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericListFragment newInstance(long select )
		{
		GenericListFragment listFragmenet = new PillsListFragment();
	
		Bundle args = new Bundle();

		args.putLong( SELECTED_ITEM, select );
		// args.putString( LIMITED_COLUMN, null); Sem ez, sem LIMITED_ITEM nem kell!

		args.putStringArray( FILTERED_COLUMN, new String[] { column(PillsTable.SEARCH)});
		args.putString( ORDERED_COLUMN, column(PillsTable.NAME));

		listFragmenet.setArguments(args);
		
		return listFragmenet;
		}

    @Override
    protected int defineTableIndex()
        {
        return PILLS;
        }

    @Override
	protected String[] getProjection()
		{
		String[] projection = new String[] {
				column_id(),
				column(PillsTable.NAME) };

		return projection;
		}
	
	@Override
	protected int getRowView()
		{
		return R.layout.pill_list_row_view;
		}

	@Override
	protected String[] getFrom()
		{
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] {
				column(PillsTable.NAME),
				column_id() };

		return from;
		}

	@Override
	protected int[] getTo()
		{
		// the XML defined views which the data will be bound to
		int[] to = new int[] {
			R.id.pill,
			R.id.id };

		return to;
		}

	@Override
	protected void addExamples()
		{
		ContentValues values = new ContentValues();

		values.put( column(PillsTable.NAME), "Algopyrin");
		getActivity().getContentResolver().insert( table(PILLS).contentUri(), values);

		values.put( column(PillsTable.NAME), "Proxelan");
		getActivity().getContentResolver().insert( table(PILLS).contentUri(), values);

		values.put( column(PillsTable.NAME), "Politrate");
		getActivity().getContentResolver().insert( table(PILLS).contentUri(), values);

		values.put( column(PillsTable.NAME), "Abirateron");
		getActivity().getContentResolver().insert( table(PILLS).contentUri(), values);

		values.put( column(PillsTable.NAME), "Enzalutamid");
		getActivity().getContentResolver().insert( table(PILLS).contentUri(), values);
		}
	}
