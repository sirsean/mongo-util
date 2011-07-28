package com.vikinghammer.mongo.dao;

import com.vikinghammer.mongo.model.MongoModel;

/**
 * 
 * 
 * @author sschulte
 * @version $Revision: $
 */
public interface MongoDao<T extends MongoModel> {

    public T getById(String id);

    public void store(T model);

    public void delete(T model);

}
