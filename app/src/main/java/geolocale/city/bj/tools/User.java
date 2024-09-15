package geolocale.city.bj.tools;

import android.content.Context;

public class User {
    String [] infos;
    String firstname, lastname, phone, user_id, token, user_communeId, genre, password;
    public User(Context context){
        infos= Cte.retrieveUserInfo(context);
        user_id=infos[0];
        lastname=infos[1];
        firstname=infos[2];
        phone=infos[3];
        user_communeId=infos[4];
        genre=infos[5];
        token=infos[8];
        password=infos[9];
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPhone() {
        return phone;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getToken() {
        return token;
    }

    public String getUser_communeId() {
        return user_communeId;
    }

    public String getGenre() {
        return genre;
    }

    public String getPassword() {
        return password;
    }

    public String getCompleName(){
        return firstname+" "+lastname;
    }
}
