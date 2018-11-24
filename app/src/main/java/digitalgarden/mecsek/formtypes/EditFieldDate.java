package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.Longtime;

import static digitalgarden.mecsek.database.DatabaseMirror.column;


// Ez a mező csak annyival tud többet, hogy az értékváltozást jelzi
public class EditFieldDate extends EditField implements View.OnFocusChangeListener
	{
    long originalLongtime;
    Longtime longtime = new Longtime();


    public EditFieldDate(Context context)
    	{
        super(context);
        init();
    	}

    public EditFieldDate(Context context, AttributeSet attrs)
    	{
        super(context, attrs);
        init();
    	}

    public EditFieldDate(Context context, AttributeSet attrs, int defStyle)
    	{
        super(context, attrs, defStyle);
        init();
    	}

    private void init()
        {
        setOnFocusChangeListener( this );
        }


    @Override
    public void onFocusChange(View v, boolean hasFocus)
        {
        Scribe.debug( ((EditFieldDate)v).getText().toString() + " has focus: " + hasFocus);
        if ( !hasFocus )
            getTimeFromText();
        setTextFromTime( !hasFocus );
        }


  public void getTimeFromText()
        {
        if (longtime.setDate(getText().toString()))
            {
            Toast.makeText(getContext(), "Date is set: " + longtime.toString( false ), Toast.LENGTH_SHORT).show();
            }
        }

    public void setTextFromTime(boolean isTextEnabled )
        {
        setText( longtime.toString( isTextEnabled ));
        }

    public void pullData(Cursor cursor )
        {
        longtime.set(cursor.getLong(cursor.getColumnIndexOrThrow( column( columnIndex ))));
        clearEdited();
        setTextFromTime( true );
        }

    public void pushData(ContentValues values)
        {
        if ( isEdited() )
            getTimeFromText(); // Elvileg felesleges, mert focusváltás nélkül nem lehet ide jutni
        values.put(column( columnIndex ), longtime.get() );
        clearEdited();
        }

    public void clearEdited()
        {
        originalLongtime = longtime.get();
        }

    @Override
    public boolean isEdited()
        {
        return longtime.get() != originalLongtime;
        }
    }
