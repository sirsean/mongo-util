package com.vikinghammer.mongo.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bson.types.ObjectId;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

import com.vikinghammer.mongo.model.MongoModel;

/**
 * A factory builds MongoModel objects based on DBObject documents,
 * as well as DBObject documents from MongoModel objects. You'll use
 * this to construct your user-friendly MongoModel objects from
 * the database-friendly DBObject objects.
 *
 * Subclasses need only worry about the specific fields defined in
 * the MongoModel subclass; administrative fields like id/version are
 * handled automatically in the ModelFactory.
 * 
 * @author Sean Schulte
 */
public abstract class ModelFactory<M extends MongoModel> {

	protected Logger _log = LoggerFactory.getLogger(getClass());

    public ModelFactory() {
        super();
    }

    /**
     * Build a new MongoModel object from a DBObject document that
     * came from the database.
     *
     * @param doc
     * @return a newly-built MongoModel object
     */
    public M build(DBObject doc) {
        if (doc == null) {
            return null;
        }

        M model = doCreateModel(doc);

        mergeIdAndVersionIntoModel(model, doc);

        return model;
    }

    public void mergeIdAndVersionIntoModel(M model, DBObject doc) {
        if (doc.get("_id") != null) {
            Object id = doc.get("_id");
            if (id instanceof ObjectId) {
                model.setId(((ObjectId)id).toString());
            } else if (id instanceof String) {
                model.setId((String)id);
            }
        } else {
            model.setId(null);
        }
        model.setVersion((Integer)doc.get("version"));
    }

    /**
     * Build a MongoModel object from a DBObject document, using
     * only the model-specific fields that you defined (ie, you don't
     * have to worry about the id/version fields.
     *
     * @param doc
     * @return a newly-build MongoModel object
     */
    protected abstract M doCreateModel(final DBObject doc);

    /**
     * Construct a DBObject that can be saved in the database from
     * a MongoModel object that we pass around.
     *
     * @param model
     * @return a document
     */
    public DBObject getDBObject(M model) {
        if (model == null) {
            return null;
        }

        DBObject doc = new BasicDBObject();

        doFillDBObject(doc, model);

        if (!model.isNew()) {
            doc.put("_id", new ObjectId(model.getId()));
        }
        doc.put("version", model.getVersion());

        return doc;
    }

    /**
     * Fill in the fields in the document with the fields in the
     * model.
     *
     * NOTE: This has the side-effect of changing the doc parameter.
     *
     * @param doc - the DBObject to be filled in
     * @param model - the MongoModel to get the fields from
     */
    protected abstract void doFillDBObject(DBObject doc, final M model);
	
}
