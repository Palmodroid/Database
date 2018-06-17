package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import digitalgarden.mecsek.templates.GenericEditFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;


// Ez a mező csak annyival tud többet, hogy az értékváltozást jelzi
// 18.06.14 - és belepakoljuk a hozzárendelt értékekekt is
public class EditTextField extends EditText
	{
    public EditTextField(Context context) 
    	{
        super(context);
    	}

    public EditTextField(Context context, AttributeSet attrs) 
    	{
        super(context, attrs);
    	}

    public EditTextField(Context context, AttributeSet attrs, int defStyle) 
    	{
        super(context, attrs, defStyle);
    	}

    private int fieldIndex;

    public int getFieldIndex()
        {
        return fieldIndex;
        }

	public void connect(final GenericEditFragment form, int fieldIndex)
		{
        this.fieldIndex = fieldIndex;

		addTextChangedListener(new TextWatcher() 
        	{
	        @Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) 
	        	{
	        	// A felhasználó csak Resumed állapotban változtat, egyébként értékadás történt!
	        	if (form.isResumed())
	        		form.setEdited();
	        	}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
				;
				}

			@Override
			public void afterTextChanged(Editable s)
				{
				;
				} 
        	});
		}

    public void setField( Cursor cursor )
        {
        setText(cursor.getString(cursor.getColumnIndexOrThrow( column(getFieldIndex() ))));
        }

    public void getField(ContentValues values)
        {
        values.put(column( getFieldIndex()), getText().toString() );
        }
    }
