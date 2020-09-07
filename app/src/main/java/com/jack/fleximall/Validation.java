package com.jack.fleximall;

import android.widget.EditText;
import java.util.regex.Pattern;

public class Validation {

    private Pattern number = Pattern.compile("[0-9]");

    private String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
            "[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
            "A-Z]{2,7}$";
    private Pattern email = Pattern.compile(emailRegex);

    private int passwordLength = 6;

    public boolean checkName(EditText edt){

        String name = edt.getText().toString();

        if (name.isEmpty()){
            edt.setError("fill detail");
            edt.requestFocus();
            return false;
        }
        else if (this.number.matcher(name).find()){
            edt.setError("digit not allowed");
            edt.requestFocus();
            return false;
        }

        return true;
    }

    public boolean checkEmail(EditText edt){

        String email = edt.getText().toString();

        if (email.isEmpty()){
            edt.setError("fill detail");
            edt.requestFocus();
            return false;
        }
        else if (!this.email.matcher(email).find()){
            edt.setError("invalid");
            edt.requestFocus();
            return false;
        }

        return true;
    }

    public boolean checkPhone(EditText edt){

        String phone = edt.getText().toString();

        if (phone.isEmpty()){
            edt.setError("fill detail");
            edt.requestFocus();
            return false;
        }
        else if (phone.length()!=10){
            edt.setError("invalid");
            edt.requestFocus();
            return false;
        }
        else if (!this.number.matcher(phone).find()){
            edt.setError("no character allowed");
            edt.requestFocus();
            return false;
        }

        return true;
    }

    public boolean checkPassword(EditText edt){

        String password = edt.getText().toString();

        if (password.isEmpty()){
            edt.setError("fill detail");
            edt.requestFocus();
            return false;
        }
        else if (!this.number.matcher(password).find()){
            edt.setError("no character allowed");
            edt.requestFocus();
            return false;
        }
        else if (password.length() < passwordLength){
            edt.setError("too short");
            edt.requestFocus();
            return false;
        }

        return true;
    }

    public boolean checkConfirmPassword(EditText pass, EditText cnfPass){

        if (!cnfPass.getText().toString().equals(pass.getText().toString())){
            cnfPass.setError("password not matched");
            cnfPass.requestFocus();
            return false;
        }

        return true;
    }
}
