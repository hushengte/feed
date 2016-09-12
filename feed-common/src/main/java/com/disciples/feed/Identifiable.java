package com.disciples.feed;

import java.io.Serializable;

public interface Identifiable extends Serializable {

    Integer getId();

    void setId(Integer id);
}
