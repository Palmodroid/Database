package digitalgarden.mecsek.formtypes;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import digitalgarden.mecsek.templates.GeneralEditFragment;


// Ez a mező csak annyival tud többet, hogy az értékváltozást jelzi
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

	public void connect(final GeneralEditFragment form)
		{
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

	}
