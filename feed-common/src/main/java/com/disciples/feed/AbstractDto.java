package com.disciples.feed;

@SuppressWarnings("serial")
public abstract class AbstractDto implements Identifiable<Integer> {
    
    private Integer id;
    
    protected AbstractDto() {}
    
    public AbstractDto(Integer id) {
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
        AbstractDto that = (AbstractDto) obj;
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += getId().hashCode() * 31;
        return hashCode;
    }

}
