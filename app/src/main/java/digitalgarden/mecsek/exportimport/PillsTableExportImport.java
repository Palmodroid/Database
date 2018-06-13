package digitalgarden.mecsek.exportimport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class PillsTableExportImport extends GeneralTableExportImport
	{
	public PillsTableExportImport(Context context)
		{
		super(context);
		// TODO Auto-generated constructor stub
		}

	@Override
	protected Uri getContentUri()
		{
		return table(PILLS).contentUri();
		}

	@Override
	protected String[] getProjection()
		{
		return new String[] { field(PillsTable.NAME) };
		}

	@Override
	protected String[] getRowData(Cursor cursor)
		{
		return new String[] 
			{ cursor.getString( cursor.getColumnIndexOrThrow( field(PillsTable.NAME) )) };
		}

	@Override
	protected String getTableName()
		{
		return table(PILLS).name();
		}
	
	@Override
	public void importRow(String[] records)
		{
		// Mivel csak egy adat van, hossz ellenőrzése nem szükséges
		
		records[1] = StringUtils.revertFromEscaped( records[1] );
	
		// Uniqe ellenőrzés kódból. Lehetne adatbázis szinten is, hiba ellenőrzésével
		String[] projection = {
                field(PillsTable.NAME) };
		Cursor cursor = getContentResolver()
			.query( table(PILLS).contentUri(), projection,
                    field(PillsTable.NAME) + "='" + records[1] + "'", null, null);
		
		// http://stackoverflow.com/a/16108435
		if (cursor == null || cursor.getCount() == 0)
			{
			ContentValues values = new ContentValues();
			values.put( field(PillsTable.NAME), records[1]);
			
			getContentResolver()
				.insert( table(PILLS).contentUri(), values);
			Scribe.debug( "Pill [" + records[1] + "] was inserted.");
			}
		else 
			Scribe.note( "Pill [" + records[1] + "] already exists! Item was skipped.");
		
		if ( cursor != null )
			cursor.close();
		}

	}
