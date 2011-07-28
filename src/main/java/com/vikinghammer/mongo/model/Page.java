package com.vikinghammer.mongo.model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * This is a serializable page for external use by services.
 *
 * You should get an instance of it from a DBPage, then populate its
 * list of items based on the DBPage's cursor.
 * 
 * @author Sean Schulte
 */
public class Page<E> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<E> _items;
    private Long _totalItems;
    private Integer _pageNumber;
    private Integer _pageSize;

    public Page() {
        super();
        _items = new ArrayList<E>();
    }

    public List<E> getItems() {
        return _items;
    }

    public void addItem(E item) {
        _items.add(item);
    }

    public Long getTotalItems() {
        return _totalItems;
    }

    public void setTotalItems(Long totalItems) {
        _totalItems = totalItems;
    }

    public Integer getPageNumber() {
        return _pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        _pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return _pageSize;
    }

    public void setPageSize(Integer pageSize) {
        _pageSize = pageSize;
    }
	
}
