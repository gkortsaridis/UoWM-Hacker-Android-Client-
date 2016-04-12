package gr.gkortsaridis.uowmhack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    EditText command_input;
    String previous_commands;
    String current_command_to_run;
    TextView commands;
    String welcome_message="Welcome to UowmHack!";
    ScrollView scroller;

    private String whereLocal = "home";
    private String whereUown  = "home";
    private String hacker_name = "hacker";
    private String where = "local";

    private boolean ssh = false;
    private boolean des = false;
    private int what_des = 0;

    private boolean database_use = false;

    SharedPreferences sharedpreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        sharedpreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        command_input=(EditText)findViewById(R.id.command_input);
        commands=(TextView)findViewById(R.id.commands);

        commands.setText(welcome_message);

        previous_commands="";

        scroller = (ScrollView) findViewById(R.id.scrollView);
        command_input.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                //scroll_till_the_end();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                scroll_till_the_end();
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //scroll_till_the_end();
            }
        });


        final View activityRootView = findViewById(R.id.activityRoot);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    scroll_till_the_end();
                }
            }
        });

        commands.setText("~~WELCOME HACKER~~\nTry the command 'help' to get a list of possible commands\n");
        appendOutput(hacker_name + "@" + where + "/" + whereLocal + " >");

        command_input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    run_command();
                    return true;
                }
                return false;
            }
        });

    }

    public void scroll_till_the_end() {
        scroller.post(new Runnable() {
            public void run() {
                scroller.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void run_command() {
        current_command_to_run=command_input.getText().toString();

        commands.setText(commands.getText().toString() + current_command_to_run);
        command_input.setText("");
        String command_reply = checkInput(current_command_to_run);
        if(!command_reply.equals(""))appendOutput(command_reply);
        if(!ssh && !des){
            if(where.equals("local"))appendOutput(hacker_name+"@"+where+"/"+whereLocal+" >");
            else if(where.equals("uowm"))appendOutput(hacker_name+"@"+where+"/"+whereUown+" >");
            else appendOutput("mysql >");
        }


    }

    private void appendOutput(String out){
        commands.setText(commands.getText().toString() + "\n" + out);
    }

    public String checkInput(String command){
        if(command.equals("exit")){
            if(where.equals("local")) System.exit(0);
            else if(where.equals("uowm")){
                where = "local";
                hacker_name = "hacker";
                return "";
            }else{
                where = "uowm";
                return "";
            }
        }
        else if(command.equals("clear")){
            commands.setText("");
            return "";
        }
        else if(command.equals("pwd")){
            if(where.equals("local"))
                return where+"/"+whereLocal;
            else if(where.equals("uowm")){
                return where+"/"+whereUown;
            }
        }
        else if(where.equals("local")) return checkLocal(command);
        else if(where.equals("uowm")) return checkUowm(command);
        else if(where.equals("mysql")) return checkSql(command);

        return "Unknown command '"+command+"'";
    }

    private String checkLocal(String command){
        String[] splited = command.split(" ");

        if(!ssh) {
            if (splited[0].equals("")) return "";
            if (splited[0].equals("ls")) {
                if(splited.length == 1) {

                    String reply = "";
                    if (whereLocal.equals("home")) {
                        reply = Strings.home_ls;

                        if (load("usernametxt").equals("true")) {
                            reply += Strings.usernametxt_ls;
                        }
                        if (load("passwordtxt").equals("true")) {
                            reply += Strings.passwordtxt_ls;
                        }
                        if (load("usernamedecr").equals("true")) {
                            reply += Strings.username_decr_ls;
                        }
                        if (load("passworddecr").equals("true")) {
                            reply += Strings.password_decr_ls;
                        }
                        return reply;
                    } else if (whereLocal.equals("Downloads")) {
                        return "Total 0";
                    } else if (whereLocal.equals("MyMail")) {
                        return Strings.home_mail_1;
                    }

                }else{
                    return "ls takes no arguements";
                }
            } else if (splited[0].equals("cd")) {
                if(splited.length == 2) {
                    if (whereLocal.equals("home")) {
                        if (splited[1].equals("Downloads")) {
                            whereLocal = "Downloads";
                            return "";
                        } else if (splited[1].equals("MyMail")) {
                            whereLocal = "MyMail";
                            return "";
                        } else return "No Folder/link named '" + splited[1] + "' found.";

                    } else if (whereLocal.equals("Downloads")) {
                        if (splited[1].equals("../")) {
                            whereLocal = "home";
                            return "";
                        } else {
                            return "No Folder/link named '" + splited[1] + "' found.";
                        }
                    } else if (whereLocal.equals("MyMail")) {
                        if (splited[1].equals("../")) {
                            whereLocal = "home";
                            return "";
                        } else {
                            return "No Folder/link named '" + splited[1] + "' found.";
                        }
                    }
                }else{
                    return "cd only takes 1 arguement";
                }
            } else if (splited[0].equals("help")) {
                if(splited.length == 1) {
                    return Strings.local_help;
                }else{
                    return "help doesn't take any arguements";
                }
            } else if (splited[0].equals("cat")) {
                if(splited.length == 2) {
                    if (whereLocal.equals("home")) {
                        if (splited[1].equals("readme.txt")) {
                            return Strings.readmetxt;
                        } else if (splited[1].equals("Downloads") || splited[1].equals("MyMail")) {
                            return splited[1] + " is a folder. You cannot use cat";
                        } else if (splited[1].equals("username.txt")) {
                            if (load("usernametxt").equals("true")) {
                                return Strings.username_encrypted;
                            } else return "Could not find '" + splited[1] + "'";
                        } else if (splited[1].equals("password.txt")) {
                            if (load("passwordtxt").equals("true")) {
                                return Strings.password_encrypted;
                            } else return "Could not find '" + splited[1] + "'";
                        } else if (splited[1].equals("username_decr.txt")) {
                            if (load("usernamedecr").equals("true")) {
                                return Strings.usernane_decrypted;
                            } else return "Could not find '" + splited[1] + "'";
                        } else if (splited[1].equals("password_decr.txt")) {
                            if (load("passworddecr").equals("true")) {
                                return Strings.password_decrypted;
                            } else return "Could not find '" + splited[1] + "'";
                        } else {
                            return "Could not find '" + splited[1] + "'";
                        }
                    } else if (whereLocal.equals("MyMail")) {

                        if (splited[1].equals("I_need_help")) {
                            return Strings.email_help;
                        } else return "No Subject name '" + splited[1] + "' found";
                    }
                }else{
                    return "cat ont takes one arguement";
                }

            } else if (splited[0].equals("ping")) {
                if(splited.length == 2) {
                    if (splited[1].equals("www.icte.uowm.gr")) {
                        save("uni_vpn", "true");
                        return Strings.ping_uowm;
                    } else {
                        return "ping: cannot resolve " + splited[1] + ": Unknown host";
                    }
                }else{
                    return "ping command takes 1 parameter";
                }
            } else if (splited[0].equals("tcpdump")) {
                if(splited.length == 1) {
                    if (load("uni_vpn").equals("true")) {
                        return Strings.tcpdump_uowm;
                    } else {
                        return Strings.tcpdump_local;
                    }
                }else{
                    return "tcpdump doesn't take any arguements";
                }
            } else if (splited[0].equals("echo")) {

                if(splited.length == 4) {
                    if (splited[splited.length - 1].equals("username.txt")) {
                        if (splited[1].equals("15:45:53.289264")) {
                            save("usernametxt", "true");
                            if (load("passwordtxt").equals("true")) {
                                return "username.txt file created succesfully!\n\n" + Strings.rsa_hint;

                            } else {
                                return "username.txt file created succesfully!";
                            }
                        } else {
                            return "Incorrect datetime for Username packet... Try again. File nto created";
                        }
                    } else if (splited[splited.length - 1].equals("password.txt")) {
                        if (splited[1].equals("15:46:04.282864")) {
                            save("passwordtxt", "true");
                            if (load("usernametxt").equals("true")) {

                                return "password.txt file created succesfully!\n\n" + Strings.rsa_hint;
                            } else {
                                return "password.txt file created succesfully!";
                            }

                        } else {
                            return "Incorrect datetime for Password packet... Try again. File nto created";
                        }
                    }
                }else{
                    return "echo takes 3 parameters";
                }
            } else if (splited[0].equals("openssl")) {
                if(splited.length == 9) {
                    if (splited[1].equals("rsauth") && splited[2].equals("-decrypt") && splited[3].equals("-inkey") && splited[4].equals("public/shared/rsa/private.pem") && splited[5].equals("-in") && splited[7].equals("-out")) {
                        //openssl rsauth -decrypt -inkey public/shared/rsa/private.pem -in username.txt -out username
                        if (splited[6].equals("username.txt")) {
                            save("usernamedecr", "true");
                            return "Success! File decrypted! new file name : username_decr.txt";
                        } else if (splited[6].equals("password.txt")) {
                            save("passworddecr", "true");
                            return "Success! File decrypted! new file name : password_decr.txt";
                        } else {
                            return "Wrong command input for openssl";
                        }
                    } else return "Wrong parameters for openssl decryption";
                }else{
                    return "openssl for RSA decryption takes 8 arguements";
                }
            } else if (splited[0].equals("ssh")) {
                if(splited.length == 2) {
                    String[] temp = splited[1].split("@");
                    if (temp[1].equals("83.212.16.16") || temp[1].equals("www.icte.uowm.gr")) {
                        if (temp[0].equals("doctorx")) {
                            ssh = true;
                            return temp[0] + "@" + temp[1] + "'s password: ";
                        } else return "There is no user named : " + temp[0];

                    } else {
                        return "ssh: Could not resolve hostname " + temp[1] + ": nodename nor servname provided, or not known";
                    }
                }else{
                    return "ssh takes 1 arguement";
                }
            } else {
                return "No command named '" + splited[0] + "' found.";
            }

        }else{
            //Perimeno password SSH
            if(splited[0].equals("i_am_the_man_2016")){
                where = "uowm";
                hacker_name = "doctor_x";
                ssh = false;
                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                String day = new SimpleDateFormat("EE", Locale.ENGLISH).format(date.getTime());
                String month = new SimpleDateFormat("MMMM").format(calendar.getTime());
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                String time = dateFormat.format(date);
                return "Welcome to Ubuntu 14.04.4 LTS (GNU/Linux 3.13.0-79-generic x86_64)\n" +
                        "\n" +
                        " * Documentation:  https://help.ubuntu.com/\n" +
                        "\n" +
                        "  System information as of "+day+" "+month+" "+dayOfMonth+" "+time+" EEST 2016\n" +
                        "\n" +
                        "  System load:  0.0               Processes:           100\n" +
                        "  Usage of /:   4.5% of 58.93GB   Users logged in:     0\n" +
                        "  Memory usage: 14%               IP address for eth1: 83.212.16.16\n" +
                        "  Swap usage:   0%\n" +
                        "\n" +
                        "  Graph this data and manage this system at:\n" +
                        "    https://landscape.canonical.com/\n" +
                        "\n" +
                        "++NEW MESSAGE++\n" +
                        "Congratulations on cracking doc's password and getting inside his machine. Now you will need to access the MySql database, and change my grade! Search a bit on the files and folders... You might find something useful";
            }else{
                ssh = false;
                return "Wrong password!";
            }
        }


        return "HACKER YOU!!";
    }
    private String checkUowm(String command){
        String[] splited = command.split(" ");

        if(!des) {
            if (splited[0].equals("")) return "";
            else if (splited[0].equals("ls")) {
                if (whereUown.equals("home")) {
                    return Strings.uowm_home_ls;
                } else if (whereUown.equals("bin")) {
                    return Strings.uowm_bin_ls;
                } else if (whereUown.equals("etc")) {
                    return Strings.uown_etc_ls;
                } else if (whereUown.equals("tmp")) {
                    return Strings.uowm_tmp_ls;
                } else if (whereUown.equals("var")) {
                    return Strings.uowm_var_ls;
                } else if (whereUown.equals("Downloads")) {
                    String ret = Strings.uowm_downloads_ls;
                    if (load("passwordsuowm1").equals("true")) {
                        ret += "\n4 -rwxr-xr-x  2 root root   4096 Jun  5 10:10 passwords_rsa_decrypted.txt";
                    }
                    if (load("passwordsuowm2").equals("true")) {
                        ret += "\n4 -rwxr-xr-x  2 root root   4096 Jun  5 10:10 username_sdes_enc.txt\n" +
                                "4 -rwxr-xr-x  2 root root   4096 Jun  5 10:10 password_sdes_enc.txt";
                    }
                    if( load("database_username").equals("true")){
                        ret += "\n4 -rwxr-xr-x  2 root root   4096 Jun  5 10:10 username_des_decrypted.txt";
                    }
                    if( load("database_password").equals("true")){
                        ret += "\n4 -rwxr-xr-x  2 root root   4096 Jun  5 10:10 password_des_decrypted.txt";
                    }
                    return ret;
                }
            } else if (splited[0].equals("cd")) {
                if (whereUown.equals("home")) {
                    if (splited[1].equals("bin")) {
                        whereUown = "bin";
                        return "";
                    } else if (splited[1].equals("etc")) {
                        whereUown = "etc";
                        return "";
                    } else if (splited[1].equals("tmp")) {
                        whereUown = "tmp";
                        return "";
                    } else if (splited[1].equals("var")) {
                        whereUown = "var";
                        return "";
                    } else if (splited[1].equals("Downloads")) {
                        whereUown = "Downloads";
                        return "";
                    } else return "No Folder/link named '" + splited[1] + "' found.";
                } else if (whereUown.equals("bin") || whereUown.equals("etc") || whereUown.equals("tmp") || whereUown.equals("var") || whereUown.equals("Downloads")) {
                    if (splited[1].equals("../")) {
                        whereUown = "home";
                        return "";
                    } else {
                        return "Nowhere to go from here...";
                    }
                }
            } else if (splited[0].equals("cat")) {
                if (whereUown.equals("Downloads")) {
                    if (splited[1].equals("eud_program.pdf") || splited[1].equals("Thesis_statements.pdf") || splited[1].equals("theory_final.pdf")) {
                        return "You cannot cat a .pdf file";
                    } else if (splited[1].equals("passwords_rsa_encrypted.txt")) {
                        return Strings.uowm_passwords_1;
                    } else if (splited[1].equals("passwords_rsa_decrypted.txt")) {
                        if (load("passwordsuowm1").equals("true")) {
                            return Strings.uowm_passwords_2;
                        }
                    } else if (splited[1].equals("username_sdes_enc.txt")) {
                        if (load("passwordsuowm2").equals("true")) {
                            return Strings.database_username;
                        }else return "There is no file named " + splited[1];
                    } else if (splited[1].equals("password_sdes_enc.txt")) {
                        if (load("passwordsuowm2").equals("true")) {
                            return Strings.database_password;
                        }else return "There is no file named " + splited[1];
                    } else if (splited[1].equals("username_des_decrypted.txt")) {
                        if (load("database_username").equals("true")) {
                            return Strings.database_username_decr;
                        }else return "There is no file named " + splited[1];
                    } else if (splited[1].equals("password_des_decrypted.txt")) {
                        if (load("database_username").equals("true")) {
                            return Strings.database_password_decr;
                        }else return "There is no file named " + splited[1];
                    }
                    else return "There is no file named " + splited[1];
                } else {
                    return "You cannot cat anything here";
                }
            } else if (splited[0].equals("openssl")) {

                if (splited[1].equals("rsauth") && splited[2].equals("-decrypt") && splited[3].equals("-inkey") && splited[4].equals("public/shared/rsa/private.pem") && splited[5].equals("-in") && splited[7].equals("-out")) {
                    //openssl rsauth -decrypt -inkey public/shared/rsa/private.pem -in username.txt -out username
                    if (splited[6].equals("passwords_rsa_encrypted.txt")) {
                        save("passwordsuowm1", "true");
                        return "Success! File decrypted! new file name : passwords_rsa_decrypted.txt";
                    } else {
                        return "Wrong command input for openssl";
                    }
                } else if (splited[1].equals("des") && splited[2].equals("-d") && splited[3].equals("-in") && splited[5].equals("-out")) {
                    //openssl des -d -in task3.des -out task3
                    if (splited[4].equals("username_sdes_enc.txt")) {
                        des = true;
                        what_des = 1;
                        return "enter des-cbc decryption password:";
                    }else if(splited[4].equals("password_sdes_enc.txt")) {
                        des = true;
                        what_des = 2;
                        return "enter des-cbc decryption password:";
                    }
                    else return "Wrong file to decrypt.";
                } else return "Wrong parameters for openssl decryption";


            } else if (splited[0].equals("create_files")) {
                if (load("passwordsuowm1").equals("true")) {
                    save("passwordsuowm2", "true");
                    return "username_sdes_enc.txt AND password_sdes_enc.txt sucesfully created.";
                } else {
                    return "Unknown command create_files";
                }
            } else if(splited[0].equals("mysql") && splited[1].equals("-u") && splited[3].equals("-p")){
                //mysql -u root -p
                if(splited[2].equals(Strings.database_username_decr)){
                    des = true;
                    what_des = 3;
                    return "Enter password: ";
                }
            }

        }else{
            des = false;

            if(what_des == 3) {
                //EDO DEN PERIMENO DES, ALLA INPUT PASSWORD GIA DATABASE
                if(splited[0].equals(Strings.database_password_decr)){
                    where = "mysql";
                    return Strings.database_welcome;
                }else{
                    return "Wrong password";
                }
            }else {
                //perimeno password gia sdes
                if (splited[0].equals("i_am_the_man_2016")) {
                    if (what_des == 1) {
                        save("database_username", "true");
                        return "Sucessfully decrypted! New file name : username_des_decrypted.txt";
                    } else if (what_des == 2) {
                        save("database_password", "true");
                        return "Sucessfully decrypted! New file name : password_des_decrypted.txt";
                    }
                } else return "Wrong password";
            }

        }

        return "Unknown command " + splited[0];
    }
    private String checkSql(String command){
        String[] splited = command.split(" ");
        if(endsOnQuestionmark(command)) {
            if (splited[0].equals("show") || splited[0].equals("SHOW")) {

                if (splited[1].equals("databases;") || splited[1].equals("DATABASES;")) {
                    return Strings.show_databases;
                } else if (splited[1].equals("tables;") || splited[1].equals("TABLES;")) {
                    if (database_use) {
                        return Strings.show_tables;
                    } else {
                        return "You have to use a database first";
                    }
                } else {
                    return "ERROR 1064 (42000): You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '" + splited[1] + "' at line 1";
                }

            } else if (splited[0].equals("use") || splited[0].equals("USE")) {
                if (splited[1].equals("Grades;")) {
                    database_use = true;
                    return "Database changed";
                } else {
                    return "No database name :" + splited[1];
                }
            } else if (splited[0].equals("select") || splited[0].equals("SELECT")) {
                if (database_use) {
                    if (splited[1].equals("*") && (splited[2].equals("from") || splited[2].equals("FROM"))) {
                        if (splited[3].equals("MK21_Applied_Mathematics_II;")) {
                            return Strings.mk21_select;
                        } else if (splited[3].equals("MK26_Mathematical_Modelling_and_Numerical_Analysis;")) {
                            return Strings.mk26_select;
                        } else if (splited[3].equals("MK25_Electronics_I;")) {
                            return Strings.mk25_select;
                        } else if (splited[3].equals("MK24_Computer_Networks_II;")) {
                            return Strings.mk24_select;
                        } else if (splited[3].equals("Y5_Mobile_Communication_Networks;")) {
                            return Strings.y5_select;
                        } else if (splited[3].equals("Y6_Optical_Communications_and_Networks;")) {
                            return Strings.y6_select;
                        } else if (splited[3].equals("MK30_Electronics_II;")) {
                            return Strings.mk30_select;
                        } else if (splited[3].equals("Y11_Computer_and_Network_Security;")) {
                            if (load("y11_changed").equals("true")) return Strings.y11_select_changed;
                            else return Strings.y11_select;
                        } else if (splited[3].equals("MK37_Analysis_and_Design_of_Algorithms;")) {
                            return Strings.mk37_select;
                        } else return "No table named :" + splited[3];
                    }
                } else {
                    return "You have to use a database first";
                }
            }else if(splited[0].equals("update") || splited[0].equals("UPDATE")){
                if(splited[1].equals("Y11_Computer_and_Network_Security")){
                    if(splited[2].equals("set") && splited[3].equals("grade=10") && splited[4].equals("where") && splited[5].equals("AM=350;")){
                        save("y11_changed","true");
                        return Strings.end_message;
                    }else return "Wrong parameters. Try again (Template : update TABLE set XX=YY where AM=ZZZ)";
                }else{
                    return "DON'T BE A FOOL...! Changing anything else besides this ONE grade can be easily detected!";
                }
            }


            return "No command name : " + splited[0];
        }else{
            return "MySql commands end with questionmark (;)";
        }
    }

    private void save(String name, String data){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(name, data);
        editor.commit();
    }
    private String load(String name){
        return sharedpreferences.getString(name,"");
    }

    private boolean endsOnQuestionmark(String command){
        return command.substring(command.length() - 1).equals(";");
    }

}
