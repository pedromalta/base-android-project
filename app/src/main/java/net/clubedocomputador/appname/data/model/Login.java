package net.clubedocomputador.appname.data.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class Login extends BaseModel {

    @Expose
    private String username;

    @Expose
    private String password;

    @Expose
    private byte[] face;

    public Login(){

    }

    public Login(byte[] face){
        this.face = face;
    }

    public byte[] getFace() {
        return face;
    }

    public void setFace(byte[] face) {
        this.face = face;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
