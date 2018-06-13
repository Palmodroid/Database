package digitalgarden.mecsek.exportimport;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import digitalgarden.mecsek.utils.StringUtils;

public abstract class GeneralTableExportImport
	{
	private Cursor cursor;
	private Context context;
	
	protected abstract Uri getContentUri();
	protected abstract String[] getProjection();
	protected abstract String[] getRowData( Cursor cursor );
	protected abstract String getTableName();
	public abstract void importRow(String[] records);

	protected ContentResolver getContentResolver()
		{
		return context.getContentResolver();
		}
	
	public GeneralTableExportImport( Context context )
		{
		this.context = context;
		}
	
	public int collateRows()
		{
		cursor = getContentResolver().query( getContentUri(), getProjection(), null, null, null);

		if (cursor == null)
			return 0;
		else
			return cursor.getCount();
		}
	
	public String getNextRow()
		{
		if ( cursor!= null && cursor.moveToNext() )
			{
			StringBuilder builder = new StringBuilder();
			
			builder.append( StringUtils.convertToEscaped( getTableName() ));
			
			String[] data = getRowData(cursor);
			for (int n=0; n < data.length; n++)
				{
				builder.append('\t');
// Null ellenőrzés!!!
				builder.append( StringUtils.convertToEscaped( data[n] ));
				}
			
			builder.append('\n');
			
			return builder.toString();
			}
		else
			return null;
		}
	
	public void close()
		{
		if (cursor != null)
			cursor.close();
		}
	
	}
