package digitalgarden.mecsek.formtypes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import digitalgarden.mecsek.generic.GenericEditFragment;


// Ez a mező csak annyival tud többet, hogy az értékváltozást jelzi
// 18.06.14 - és belepakoljuk a hozzárendelt értékekekt is
public abstract class EditField extends EditText
	{
    public EditField(Context context)
    	{
        super(context);
    	}

    public EditField(Context context, AttributeSet attrs)
    	{
        super(context, attrs);
    	}

    public EditField(Context context, AttributeSet attrs, int defStyle)
    	{
        super(context, attrs, defStyle);
    	}

    private int fieldIndex;

    public int getFieldIndex()
        {
        return fieldIndex;
        }

    private boolean edited = false;

    // Pl. EditFieldDate automatikusan javítja a beírást focus váltásakor.
    protected boolean isEdited()
        {
        return edited;
        }
    protected void clearEdited()
        {
        edited = false;
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
                    {
                    edited = true;
                    form.setEdited();
                    }
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

    abstract public void pullData(Cursor cursor );

    abstract public void pushData(ContentValues values);
    }
