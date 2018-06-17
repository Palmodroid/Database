package digitalgarden.mecsek.exportimport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import digitalgarden.mecsek.database.authors.AuthorsTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;

public class AuthorsTableExportImport extends GeneralTableExportImport
	{
	public AuthorsTableExportImport(Context context)
		{
		super(context);
		// TODO Auto-generated constructor stub
		}

	@Override
	protected Uri getContentUri()
		{
		return table(AUTHORS).contentUri();
		}

	@Override
	protected String[] getProjection()
		{
		return new String[] { column(AuthorsTable.NAME) };
		}

	@Override
	protected String[] getRowData(Cursor cursor)
		{
		return new String[] 
			{ cursor.getString( cursor.getColumnIndexOrThrow( column(AuthorsTable.NAME) )) };
		}

	@Override
	protected String getTableName()
		{
		return table(AUTHORS).name();
		}
	
	@Override
	public void importRow(String[] records)
		{
		// Mivel csak egy adat van, hossz ellenőrzése nem szükséges
		
		records[1] = StringUtils.revertFromEscaped( records[1] );
	
		// Uniqe ellenőrzés kódból. Lehetne adatbázis szinten is, hiba ellenőrzésével
		String[] projection = { 
			column(AuthorsTable.NAME) };
		Cursor cursor = getContentResolver()
			.query( table(AUTHORS).contentUri(), projection,
			column(AuthorsTable.NAME) + "='" + records[1] + "'", null, null);
		
		// http://stackoverflow.com/a/16108435
		if (cursor == null || cursor.getCount() == 0)
			{
			ContentValues values = new ContentValues();
			values.put( column(AuthorsTable.NAME), records[1]);
			
			getContentResolver()
				.insert( table(AUTHORS).contentUri(), values);
			Scribe.debug( "Author [" + records[1] + "] was inserted.");
			}
		else 
			Scribe.note( "Author [" + records[1] + "] already exists! Item was skipped.");
		
		if ( cursor != null )
			cursor.close();
		}

	}
