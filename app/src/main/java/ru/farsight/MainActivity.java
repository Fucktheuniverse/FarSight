package ru.farsight;

import android.content.DialogInterface;
import android.app.*;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.*;
import android.location.LocationManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener {
    public Location location;
    String SetPrimary="";
    public String OurLoc = "";
    final static String fileName="check";
    final static String OurLocation="OurLocation";
    final static String ManaStore="MP";
    public String ManaPoints="";
    String ReadingFile="";
    public int progress=0;
    public int fragm;
public int menu_position;
    Button mapBtn, locSet_btn, site_btn,style_btn;
    TextView tvc;
    TextView tvLocation;
    TextView prog_mana;
    TextView version;
    ImageView main_bg,EnabledGPS;
    View settings_field;
    Fragment fragment = null;
    boolean isSettingsActive=false;
    public ProgressBar progressBar;// = (ProgressBar) findViewById(R.id.main_mana_bar);
   // File file = new File(getFilesDir(),"OurLocation");
   // File Tfile = new File(getFilesDir(),fileName);
    public boolean mapactive = false;

    private DrawerLayout myDrawerLayout;
    private ListView myDrawerList;
    private ActionBarDrawerToggle myDrawerToggle;
    // navigation drawer title
    private CharSequence myDrawerTitle;
    // used to store app title
    private CharSequence myTitle;
    private String[] viewsNames;

    private LocationManager locationManager;
    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //progressBar.setMax(100);
        setContentView(R.layout.activity_main);
        myTitle =  getTitle();
        myDrawerTitle = getResources().getString(R.string.menu);
        // load slide menu items
        viewsNames = getResources().getStringArray(R.array.views_array);
        myDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        myDrawerList = (ListView) findViewById(R.id.left_drawer);

        myDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, viewsNames));

        // enabling action bar app icon and behaving it as toggle button
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        myDrawerToggle = new ActionBarDrawerToggle(this, myDrawerLayout,
                R.string.open_menu,
                R.string.close_menu
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(myTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(myDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        myDrawerLayout.setDrawerListener(myDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }



        myDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        progressBar = (ProgressBar) findViewById(R.id.mana_bar);

        mapBtn = (Button) findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(this);
        tvc = (TextView) findViewById(R.id.tvc);


        tvLocation = (TextView) findViewById(R.id.tvLocation);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        prog_mana = (TextView) findViewById(R.id.prog_mana);
//Need to calculate tiles coordinates check
        int create = 0;
        ReadingFile = fileName;
       if (readData(fileName)!=null)
       create ++;
        else{
        while (create == 0)
            {
                Log.d("LOG","Computing started");
                for (double X = 131.840579; X <= 132.100082; X += 0.00924*1.13) {//      0.01232   double X = 131.840579; X <= 132.100082
                    for (double Y = 43.226729; Y >= 43.021234; Y -= 0.0066744*1.142) {//  0.0088992 double Y = 43.226729; Y >= 43.057916
                        SetPrimary += X + " " + Y + " ";
                    }
                }
                Log.d("LOG","Computing completed");
                SaveData();
                create++;
            }
        }

        if(readMP(ManaStore)==null)
        {
            ManaPoints="100";
            SaveMP();
        }
            else
        {
            String[] mana=readMP(ManaStore).split(" ");
            int[] manamas = new int[mana.length];
            for (int i=0;i<mana.length;i++)
            {
                manamas[i]= Integer.valueOf(mana[i]);
            }
            progress=manamas[0];
            if(progress==0)
            {progress=100;
            ManaPoints=""+progress;
            SaveMP();}
            //progressBar.setMax(100);
            progressBar.setProgress(progress);
            prog_mana.setText(""+progress+" Mana");
        }
    }
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("fragment", fragm);
        Log.d("LOG", "onSaveInstanceState " + fragm);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fragm = savedInstanceState.getInt("fragment");
        Log.d("LOG", "onRestoreInstanceState " + fragm);
        if (fragm>0)
        {    displayView(fragm);
        }
        else displayView(0);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(
                AdapterView<?> parent, View view, int position, long id
        ) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    public void displayView(int position) {
        // update the main content by replacing fragments
        settings_field = (View) findViewById(R.id.settings_field);
        version = (TextView) findViewById(R.id.version);
        main_bg = (ImageView) findViewById(R.id.main_bg);
        mapBtn = (Button) findViewById(R.id.mapBtn);

        switch (position) {
            case 0:
                fragm = 0;
                settings_field.setVisibility(View.INVISIBLE);
                version.setVisibility(View.VISIBLE);
                main_bg.setImageResource(R.drawable.nopanda);
                mapBtn.setVisibility(View.VISIBLE);
                isSettingsActive=false;
                fragment = new FirstFragment();
                break;
            case 1:
                fragm=1;
                fragment = new SecondFragment();
                version.setVisibility(View.INVISIBLE);
                isSettingsActive=false;
                break;
            case 2:
                fragm=2;
                settings_field.setVisibility(View.VISIBLE);
                version.setVisibility(View.VISIBLE);
                main_bg.setImageResource(R.drawable.settingsbackgnd);
                mapBtn.setVisibility(View.INVISIBLE);
                isSettingsActive=true;
                fragment = new ThirdFragment();
                break;
            case 3:
                Toast.makeText(getApplicationContext(), "Under construction.", Toast.LENGTH_SHORT).show();
                /*Intent intent = new Intent(this, QuestTaker.class);
                startActivity(intent);*/
                break;
            case 4:
                Toast.makeText(getApplicationContext(), "Under construction.", Toast.LENGTH_SHORT).show();
                /*Intent intent = new Intent(this, QuestTaker.class);
                startActivity(intent);*/
                break;
            case 5:
                Toast.makeText(getApplicationContext(), "Under construction.", Toast.LENGTH_SHORT).show();
                /*Intent intent = new Intent(this, QuestTaker.class);
                startActivity(intent);*/
                break;
            default:
                break;
        }
        if (fragment != null) {
            android.app.FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();

            // update selected item and title, then close the drawer
            myDrawerList.setItemChecked(position, true);
            myDrawerList.setSelection(position);
            setTitle(viewsNames[position]);
            myDrawerLayout.closeDrawer(myDrawerList);
        }
        else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if navigation drawer is opened, hide the action items
        boolean drawerOpen = myDrawerLayout.isDrawerOpen(myDrawerList);
        menu.findItem(R.id.action_clear_cache).setVisible(isSettingsActive);
        menu.findItem(R.id.action_read).setVisible(isSettingsActive);
        menu.findItem(R.id.action_exit).setVisible(isSettingsActive);
        if(isSettingsActive==true)
        {   menu.findItem(R.id.action_clear_cache).setVisible(!drawerOpen);
            menu.findItem(R.id.action_read).setVisible(!drawerOpen);
            menu.findItem(R.id.action_exit).setVisible(!drawerOpen);}
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        myTitle = title;
        getSupportActionBar().setTitle(myTitle);
    }
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        myDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        myDrawerToggle.onConfigurationChanged(newConfig);
    }


//Format of our location file
    private String formatLoc(Location location) {
        if (location == null)
            return "";
        else
        {
            double Y=location.getLatitude();
            double X=location.getLongitude();

            if(readData(OurLocation)!=null){
            ReadingFile = OurLocation+" Check previously";
            String[] ourLocate = readData("OurLocation").split(" ");
            double[] locmas = new double[ourLocate.length];
            for(int L=0;L<ourLocate.length;L++)
            {
                locmas[L]=Double.valueOf(ourLocate[L]);
                // Log.d("LOG","Our Location read in massive");
            }
            Log.d("LOG"," "+locmas.length);
            int part=0;
            for(int i=0;i<locmas.length;i+=2){
            double XXX = locmas[i];
            double YYY = locmas[i + 1];
            Log.d("LOG","Last coordinates "+ XXX+ " " + YYY);
            double XLU = XXX;
            XLU -= 0.00462;
            double XRU = XXX;
            XRU += 0.00462;
            double XLD = XXX;
            XLD -= 0.00462;
            double XRD = XXX;
            XRD += 0.00462;
            double YLU = YYY;
            YLU += 0.0033372;
            double YRU = YYY;
            YRU += 0.0033372;
            double YLD = YYY;
            YLD -= 0.0033372;
            double YRD = YYY;
            YRD -= 0.0033372;
            if    ((X >= XLU && Y <= YLU) &&
                    (X <= XRU && Y <= YRU) &&
                    (X >= XLD && Y >= YLD) &&
                    (X <= XRD && Y >= YRD)) {
                Log.d("LOG", "" + locmas[i] + ", " + locmas[i+1] + " Don't write");
                part = 1;
            }
            if (part==1) return "";
            return readData(OurLocation) + X + " "+Y + " ";}
            }
            else return ""+X+" "+Y + " ";
            return "";
        }
    }
//Saving our location coordinates
    public void SaveOurLocation()
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(OurLocation, MODE_PRIVATE)));
            writer.write(OurLoc);
            writer.close();
            Log.d("LOG", "Our Location writing completed");
        }

        catch(
                FileNotFoundException el
                )

        {
            el.printStackTrace();
        } catch (IOException ep) {
            ep.printStackTrace();
        }

    }
//Saving tiles coordinates
    public void SaveData()
    {
        try
            {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(fileName, MODE_PRIVATE)));
                writer.write(SetPrimary);
                writer.close();
                Log.d("LOG", "Writing completed");
            }
        catch(FileNotFoundException el)
            {
                el.printStackTrace();
            }
        catch (IOException ep)
            {
                ep.printStackTrace();
            }
    }
//Function allowing ro read data
    public String readData(String fileName)
    {   Log.d("LOG", "Read " + ReadingFile + " started");

        try
        {
            //Log.d("LOG", "Try block started");
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(fileName)));
            String line;
            //Log.d("LOG", "Read buffer");
            while((line=reader.readLine())!=null)
            {
                Log.d("LOG", "Read successful");
                return line;
            }
         } catch (FileNotFoundException e) {
            Log.d("LOG", "File not Found!");
            e.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return "IOex";
        }
        return null;
    }
//When opening file from background
    @Override
    protected void onResume() {
        super.onResume();
        if(readMP(ManaStore)==null)
        {
            ManaPoints="100";
            SaveMP();
        }
        else
        {
            String[] mana=readMP(ManaStore).split(" ");
            int[] manamas = new int[mana.length];
            for (int i=0;i<mana.length;i++)
            {
                manamas[i]= Integer.valueOf(mana[i]);
            }
            progress=manamas[0];
            //progressBar.setMax(100);
            progressBar.setProgress(progress);
            prog_mana.setText(""+progress+" Mana");
        }
        mapactive=false;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 240, 0, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 240, 0,
                locationListener);
        checkEnabled();
    }
//When close file to background
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("LOG", "Main Activity Pause");
        //locationManager.removeUpdates(locationListener);

    }
//Function tracks for location
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }
        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }
        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            //showLocation(locationManager.getLastKnownLocation(provider));
            //showLocation(location);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {

            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {

            }
        }
    };
//Function displays the location
    private void showLocation(Location location) {
        int i=0;
        i=0;
        String checking;
        ReadingFile = OurLocation;
        checking=readData(OurLocation);
        if (location == null) {
            Log.d("LOG", "LocationListener->Location NULL");
            return;
        }
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)&&i==0) {
            i=1;
            Log.d("LOG", "GPS data");
            ReadingFile = OurLocation;
            if(checking=="IOex")
            {
                Log.d("LOG", "Second case");
                return;
            }
            if(checking==null)
            {
                Log.d("LOG", "Third case");
                OurLoc ="" + formatLoc(location);
                SaveOurLocation();
                tvLocation.setText(formatLocation(location));
            }
            if (checking!=null&&checking!="IOex")
            {
                Log.d("LOG", "First case");
                OurLoc =readData(OurLocation)+ formatLoc(location);
                SaveOurLocation();
                tvLocation.setText(formatLocation(location));
            }
            else {
                Log.d("LOG", "Fourth case");
                return;
            }
        }
        else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)&&i==0) {
            i=1;
            Log.d("LOG", "Network data");
            ReadingFile = OurLocation;
            if(checking=="IOex")
            {
                Log.d("LOG", "Second case");
                return;
            }
            if(checking==null)
            {
                Log.d("LOG", "Third case");
                OurLoc ="" + formatLoc(location);
                SaveOurLocation();
                tvLocation.setText(formatLocation(location));
            }
            if (checking!=null&&checking!="IOex")
            {
                Log.d("LOG", "First case");
                OurLoc =readData(OurLocation)+ formatLoc(location);
                SaveOurLocation();
                tvLocation.setText(formatLocation(location));
            }
            else
            {
                Log.d("LOG", "Fourth case");
                return;
            }
        }
        i=0;
    }
//Format of main menu location data
    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return "Position located";
    }
//Provider status checking
    private void checkEnabled() {
        EnabledGPS = (ImageView) findViewById(R.id.EnabledGPS);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)==true) {

           EnabledGPS.setImageResource(R.drawable.ledon);
        }
        else{
            EnabledGPS.setImageResource(R.drawable.ledoff);

           }
    }
//BUTTON FUNCTIONS
    public void onClickLocationSettings(View view) {
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    };

    public void onClickSiteBtn(View view){
        Log.d("LOG", "Website Button Pressed");
        Uri uri = Uri.parse("http://farsightapp.wix.com/farsightapp"); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    public void onClickStyleBtn(View view) {
        Toast.makeText(getApplicationContext(), "Under construction.", Toast.LENGTH_SHORT).show();
    }
    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_window, menu);
        return true;
    }
    //What happening when Settings button puched
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (myDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (id)
        {
            case (R.id.action_exit):
                this.finish();
                break;
            case (R.id.action_read):
                ReadingFile = OurLocation + " Read Btn pressed";

                if (readData(OurLocation) != null) {
                    Log.d("LOG", "Our Location file contains " + readData(OurLocation));
                    String[] tilenum = readData(OurLocation).split(" ");
                    int h = 0;
                    h = tilenum.length / 2;

                    tvc.setText("You have " + h+ " location point(s)");
                } else tvc.setText("No location data");
                break;
            case (R.id.action_clear_cache):
                Log.d("LOG", "Clear cache Btn pressed");
                File file = new File (getFilesDir(),OurLocation);
                file.delete();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    public void SaveMP()
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(ManaStore, MODE_PRIVATE)));
            writer.write(ManaPoints);
            writer.close();
            Log.d("LOG", "Writing completed");
        }
        catch(FileNotFoundException el)
        {
            el.printStackTrace();
        }
        catch (IOException ep)
        {
            ep.printStackTrace();
        }
    }
    //Function allowing ro read data
    public String readMP(String MP)
    {   Log.d("LOG", "Read " + ManaStore + " started");

        try
        {
            //Log.d("LOG", "Try block started");
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(ManaStore)));
            String line;
            //Log.d("LOG", "Read buffer");
            while((line=reader.readLine())!=null)
            {
                Log.d("LOG", "You have "+line+" MP");
                return line;
            }
        } catch (FileNotFoundException e) {
            Log.d("LOG", "File not Found!");
            e.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return "IOex";
        }
        return null;
    }


//Button MAP pushed
    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.mapBtn:
                mapactive=true;
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
