package net.clubedocomputador.appname.data.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by pedromalta on 15/03/18.
 */

public class BaseModel {

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
