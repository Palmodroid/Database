package digitalgarden.mecsek.formtypes;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import digitalgarden.mecsek.generic.Connection;
import digitalgarden.mecsek.generic.GenericEditFragment;


// Ez a mező csak annyival tud többet, hogy az értékváltozást jelzi
// 18.06.14 - és belepakoljuk a hozzárendelt értékekekt is
public abstract class EditField extends EditText implements Connection.Connectable
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

    private int columnIndex;

    public int getColumnIndex()
        {
        return columnIndex;
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

    public void connect(final GenericEditFragment form, int columnIndex)
		{
        this.columnIndex = columnIndex;

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

    @Override
    public void saveData(Bundle data)
        {
        // Nincs rá szükség
        }

    @Override
    public void retrieveData(Bundle data)
        {
        // Nincs rá szükség
        }

    @Override
    public void pushSource( int tableIndex, long rowIndex )
        {
        // No source for edit fields
        }
    }
