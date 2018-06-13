package digitalgarden.mecsek.database.pills;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.templates.GeneralListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.field_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class PillsListFragment extends GeneralListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GeneralListFragment newInstance( long select )
		{
		GeneralListFragment listFragmenet = new PillsListFragment();
	
		Bundle args = new Bundle();

		args.putLong( SELECTED_ITEM, select );
		// args.putString( LIMITED_COLUMN, null); Sem ez, sem LIMITED_ITEM nem kell!

		args.putStringArray( FILTERED_COLUMN, new String[] { field(PillsTable.SEARCH)});
		args.putString( ORDERED_COLUMN, field(PillsTable.NAME));

		listFragmenet.setArguments(args);
		
		return listFragmenet;
		}

	
	protected int getLoaderId()
		{
		return table(PILLS).id();
		}

	@Override
	protected Uri getContentUri()
		{
		return table(PILLS).contentUri();
		}

	@Override
	protected String[] getProjection()
		{
		String[] projection = new String[] {
				field_id(),
				field(PillsTable.NAME) };

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
				field(PillsTable.NAME),
				field_id() };

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

		values.put( field(PillsTable.NAME), "Algopyrin");
		getActivity().getContentResolver().insert( table(PILLS).contentUri(), values);

		values.put( field(PillsTable.NAME), "Proxelan");
		getActivity().getContentResolver().insert( table(PILLS).contentUri(), values);

		values.put( field(PillsTable.NAME), "Politrate");
		getActivity().getContentResolver().insert( table(PILLS).contentUri(), values);

		values.put( field(PillsTable.NAME), "Abirateron");
		getActivity().getContentResolver().insert( table(PILLS).contentUri(), values);

		values.put( field(PillsTable.NAME), "Enzalutamid");
		getActivity().getContentResolver().insert( table(PILLS).contentUri(), values);
		}
	}
