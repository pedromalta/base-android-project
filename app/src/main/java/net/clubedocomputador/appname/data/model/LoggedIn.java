package net.clubedocomputador.appname.data.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by pedromalta on 11/03/18.
 */

public class LoggedIn extends BaseModel{

    @Expose
    private String id;

    @Expose
    private String path;

    @Expose
    private String uid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
