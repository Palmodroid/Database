package digitalgarden.mecsek.exportimport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import digitalgarden.mecsek.database.authors.AuthorsTable;
import digitalgarden.mecsek.database.books.BooksTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.field_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.BOOKS;


public class BooksTableExportImport extends GeneralTableExportImport
	{
	public BooksTableExportImport(Context context)
		{
		super(context);
		// TODO Auto-generated constructor stub
		}

	@Override
	protected Uri getContentUri()
		{
		return table(BOOKS).contentUri();
		}

	@Override
	protected String[] getProjection()
		{
		return new String[] {
                field(AuthorsTable.NAME),
                field(BooksTable.TITLE) };
		}

	@Override
	protected String[] getRowData(Cursor cursor)
		{
		return new String[] { 
			cursor.getString( cursor.getColumnIndexOrThrow( field(AuthorsTable.NAME ))),
			cursor.getString( cursor.getColumnIndexOrThrow( field(BooksTable.TITLE ))) };
		}

	@Override
	protected String getTableName()
		{
		return table(BOOKS).name();
		}

	@Override
	public void importRow(String[] records)
		{
		// Két adat miatt itt szükséges a hossz ellenőrzése
		if ( records.length < 3 )
			{
			Scribe.note( "Parameters missing from BOOKS row. Item was skipped.");
			return;
			}
		
		long authorId = findAuthorId( records[1] );
		if ( authorId == ID_MISSING )
			{
			Scribe.note( "Author [" + records[1] + "] does not exists! Item was skipped.");
			return;
			}

		ContentValues values = new ContentValues();
		
		if ( authorId == ID_NULL )
    		values.putNull( field(BooksTable.AUTHOR_ID) );
		else
    		values.put( field(BooksTable.AUTHOR_ID), authorId );
		
		records[2] = StringUtils.revertFromEscaped( records[2] );
		values.put( field(BooksTable.TITLE), records[2] );
				
		getContentResolver()
			.insert( table(BOOKS).contentUri(), values);
		Scribe.debug( "Book [" + records[2] + "] was inserted.");
		}

	
	private long ID_MISSING = -2L;
	private long ID_NULL = -1L;
	
	// Ezt a keresőrutint nehéz generalizálni, mert az azonosító paraméterek típusa is különböző lehet
	private long findAuthorId(String authorName)
		{
		if ( authorName == null )
			{
    		return ID_NULL;
			}

		long authorId = ID_MISSING;
		
		String[] projection = {
			    field_id(),
                field(AuthorsTable.NAME) };
		Cursor cursor = getContentResolver()
			.query(table(AUTHORS).contentUri(), projection,
				   field(AuthorsTable.NAME) + "=\'" + StringUtils.revertFromEscaped( authorName ) + "\'",
				   null, null);

		if ( cursor != null)
			{
			if (cursor.moveToFirst())
				authorId = cursor.getLong( cursor.getColumnIndexOrThrow( field_id() ) );
			cursor.close();
			}
		
		return authorId;
		}
	}
