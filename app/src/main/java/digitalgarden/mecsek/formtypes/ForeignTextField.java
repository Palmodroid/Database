package digitalgarden.mecsek.formtypes;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.TextView;

import digitalgarden.mecsek.formtypes.ForeignKey.ForeignField;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;


// http://stackoverflow.com/questions/9387000/android-execute-code-on-variable-change

public class ForeignTextField extends TextView implements ForeignField
	{
	// a kapcsolt foreignKey(tábla és elem), és a mező, amit megjelenítünk
	private ForeignKey foreignKey;
    private int foreignTableIndex;
    private int foreignColumnIndex;
	//private String foreignField;
	
    public ForeignTextField(Context context) 
    	{
        super(context);
    	}

    public ForeignTextField(Context context, AttributeSet attrs) 
    	{
        super(context, attrs);
    	}

    public ForeignTextField(Context context, AttributeSet attrs, int defStyle) 
    	{
        super(context, attrs, defStyle);
    	}

    // Ezt a hozzá tartozó ForeignKey változása hívja meg a listener-en keresztül
    public void onValueChanged(long newId) 
    	{
		if (newId >= 0L)
			{
        	Uri uri = Uri.parse( table(foreignTableIndex).contentUri() + "/" + newId);
    		String[] projection = { 
    			column(foreignColumnIndex) };
    		Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);

    		if (cursor != null) // Ez vajon kell? 
    			{
    			cursor.moveToFirst();

    			String field = cursor.getString(cursor.getColumnIndexOrThrow( column(foreignColumnIndex) ));
    			Scribe.note( "Author: " + field + " set from id: " + newId );
    			setText( field );
    			
    			// Always close the cursor
    			cursor.close();
    			}
			}
		else
			setText(null);
    	}

    // Mielőtt a mezőt használni tudnánk, linkelni kell a megfelelő ForeignKey-jel
    // foreignKey - a táblát és elemet meghatározó foreign key
    // foreignField - és a mező, amelyet az előbbiek alapján megjelenítünk 
    public void link( ForeignKey foreignKey, int foreignTableIndex, int foreignColumnIndex )
		{
		// bejegyeztetjük magunkat a Foreign Key listenerei közé
		foreignKey.setForeignField( this );
		
		this.foreignKey = foreignKey;
		this.foreignTableIndex = foreignTableIndex;
        this.foreignColumnIndex = foreignColumnIndex;
		}

    }
