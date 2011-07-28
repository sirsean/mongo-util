package com.vikinghammer.mongo.dao;

import com.vikinghammer.mongo.collection.MongoCollection;
import com.vikinghammer.mongo.factory.ModelFactory;
import com.vikinghammer.mongo.model.MongoModel;
import com.vikinghammer.mongo.query.MongoQueryRunner;
import com.vikinghammer.mongo.query.impl.MongoQueryRunnerImpl;

import org.bson.types.ObjectId;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

/**
 * 
 * 
 * @author sschulte
 */
public abstract class AbstractMongoDao<T extends MongoModel> implements MongoDao<T> {

    protected MongoCollection _collection;
    protected ModelFactory<T> _modelFactory;
    protected MongoQueryRunner<T> _queryRunner;

    public AbstractMongoDao(
        MongoCollection collection,
        ModelFactory<T> modelFactory
    ) {
        super();
        _collection = collection;
        _modelFactory = modelFactory;

        _queryRunner = new MongoQueryRunnerImpl<T>(collection, modelFactory);
    }

    @Override
    public T getById(String id) {
        DBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(id));

        return _queryRunner.one(query);
    }

    @Override
    public void store(T model) {
        DBObject doc = _modelFactory.getDBObject(model);
        _collection.save(doc);
        _modelFactory.mergeIdAndVersionIntoModel(model, doc);
    }

    @Override
    public void delete(T model) {
        if (!model.isNew()) {
            DBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(model.getId()));
            _collection.remove(query);
        }
    }
}
