package com.vikinghammer.mongo.model;

import java.io.Serializable;

/**
 * A base class for model objects that can be stored in a MongoDB database.
 *
 * This handles the id for you; a model object is assumed to be "new," or
 * unsaved, if the id is null.
 * 
 * @author Sean Schulte
 */
public abstract class MongoModel implements Serializable {

    private String _id;
    private Integer _version;

    public MongoModel() {
        super();
        _version = null;
    }

    /**
     * @return the id of the model object
     */
    public String getId() {
        return _id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        _id = id;
    }

    /**
     * @return version
     */
    public Integer getVersion() {
        return _version;
    }

    /**
     * @param version
     */
    public void setVersion(Integer version) {
        _version = version;
    }

    /**
     * @return true if the object is new/unsaved, false if it has an id
     */
    public boolean isNew() {
        return (_id == null);
    }
	
}
