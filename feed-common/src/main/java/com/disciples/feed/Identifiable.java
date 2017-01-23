package com.disciples.feed;

import java.io.Serializable;

public interface Identifiable<ID extends Serializable> extends Serializable {

	ID getId();

    void setId(ID id);
}
