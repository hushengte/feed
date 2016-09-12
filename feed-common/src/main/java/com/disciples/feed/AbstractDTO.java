package com.disciples.feed;

@SuppressWarnings("serial")
public abstract class AbstractDTO implements Identifiable {
    
    protected Integer id = 0;
    
    protected AbstractDTO() {}
    
    protected AbstractDTO(Integer id) {
        this.setId(id);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return String.format("DTO of type %s with id: %s", this.getClass().getName(), getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        AbstractDTO that = (AbstractDTO) obj;
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += getId().hashCode() * 31;
        return hashCode;
    }

}
