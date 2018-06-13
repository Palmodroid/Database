package digitalgarden.mecsek.exportimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.database.DatabaseMirror.database;


class AsyncTaskImport extends TimeConsumingAsyncTask 
	{
	// Átadott adatok
	File inputFile;
	
	protected AsyncTaskImport(AsyncTaskDialogFragment asyncTaskDialogFragment, File inputFile)
		{
		super(asyncTaskDialogFragment);
		Scribe.note("AsyncTaskIMPORT from " + inputFile.getName());
		
		this.inputFile = inputFile;
		}
	
	private int length = 0;
	
	// Indítás előtt elvégzendő előkészítések
    @Override
    protected void onPreExecute() 
    	{
		// Elvileg az inputFile már megfelelő, de létezését ellenőrizzük
		
/*    	// http://lattilad.org/android/?x=entry:entry121231-185235
		// SD Card nem írható és olvasható
    	if ( !Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() )) 
    		{
   			setReturnedMessage( R.string.msg_error_sdcard );
    		return;
    		} 

    	// Előkészítjük a könyvtárat. Ez később igen nehéz, mert a cursorról is gondoskodni kell 
    	File directory = new File(Environment.getExternalStorageDirectory(), applicationContext.getString(R.string.directory));
		if ( !directory.exists() ) 
			{
   			setReturnedMessage( R.string.msg_error_directory );
			}
		
		File inputFile = new File(directory, applicationContext.getString(R.string.file));
*/		
		if ( inputFile==null || !inputFile.exists() || !inputFile.isFile())
			{
   			setReturnedMessage( R.string.msg_error_file );
			}
		
		length = (int) inputFile.length(); 
		// Nem jó az átalakítás!!!!
		// És ráadásul long-ról int-re is alakítunk
		
		Scribe.debug("AsyncTaskIMPORT file length:" + length );
		
    	callerFragment.setProgressMax( length );
   		callerFragment.updateLayout();
    	}

    
	// A tényleges, háttérben zajló feladat
	// UI-szál elérése TILOS!
	@Override
	protected Void doInBackground(Void... params) 
		{
		if ( !isRunning() )
			return null;

		BufferedReader bufferedReader = null;
		try
			{
	    	// File directory = new File(Environment.getExternalStorageDirectory(), applicationContext.getString(R.string.directory));
			// File inputFile = new File(directory, applicationContext.getString(R.string.file));

			// FileInputStream fileInputStream = new FileInputStream( inputFile );
			// InputStreamReader inputStreamReader = new InputStreamReader( fileInputStream, "UTF-8" );
			// bufferedReader = new BufferedReader( inputStreamReader, 1024 );
			bufferedReader = new BufferedReader( new InputStreamReader( new FileInputStream( inputFile ), "UTF-8" ) );
			
			int count = 0;
			String row;
			String[] records;
			
			AuthorsTableExportImport authorsImport = new AuthorsTableExportImport( applicationContext );
			BooksTableExportImport booksImport = new BooksTableExportImport( applicationContext );
			PillsTableExportImport pillsImport = new PillsTableExportImport( applicationContext );
			PatientsTableExportImport patientsImport = new PatientsTableExportImport( applicationContext );
			MedicationsTableExportImport medicationsImport = new MedicationsTableExportImport( applicationContext );

			while ( (row=bufferedReader.readLine()) != null )
				{
				records = row.split("\\t", -1);

				if (count == 0)
					{
					// Ez a legelső sor ellenőrzése
					if (records.length < 2 ||
						!records[0].equals( database().name() ) ||
						!records[1].equals( Integer.toString(database().version() )) )
						{
						setReturnedMessage( R.string.msg_error_database_version );
						Scribe.note( "[" + row + "] and database: " + database().name() + " (" + database().version() + ") does not match!");
						break;
						}
					else
						{
						Scribe.debug( "Database: " + database().name() + " (" + database().version() + ") matches!");
						}
					}

				else if (records.length < 2)
					{
					// Nincs, legfeljebb az elso rekord
					Scribe.debug( "Empty row!");
					}
				
				else if ( records[0].equals( authorsImport.getTableName() ))
					{
					authorsImport.importRow( records );
					}
				
				else if ( records[0].equals( booksImport.getTableName() ))
					{
					booksImport.importRow( records );
					}

				else if ( records[0].equals( pillsImport.getTableName() ))
					{
					pillsImport.importRow( records );
					}

				else if ( records[0].equals( patientsImport.getTableName() ))
					{
					patientsImport.importRow( records );
					}

				else if ( records[0].equals( medicationsImport.getTableName() ))
					{
					medicationsImport.importRow( records );
					}

				else
					{
					Scribe.note("[" + row + "]: malformed row skipped!");
					}
				
				count += row.length()+1;
				publishProgress( count );

				if (isCancelled())
					break;
				}

			// A file hosszat a beolvasott karakterekkel vetjuk ossze
			// Ez az utf kodolas miatt nem lesz pontos, de a puffereles miatt nem latjuk, hol tartunk
			// Itt javitjuk a hibat, es 100%-t szimulalunk
			if ( row == null )
				publishProgress( length );
		
			}
		catch (IOException ioe)
			{
   			setReturnedMessage( R.string.msg_error_io + ioe.toString());
     		return null;
			}
		finally 
			{
			if (bufferedReader != null) 
				{
				try 
					{
					bufferedReader.close();
					}
				catch (IOException ioe)
					{
					Scribe.note("ERROR IN CLOSE (AsyncTaskImport) " + ioe.toString());
					}
				}
			}
		return null;
		}      
	}   
