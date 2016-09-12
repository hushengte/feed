package com.disciples.feed.manage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.disciples.feed.Identifiable;
import com.disciples.feed.Response;

public abstract class AbstractCrudController<DTO extends Identifiable, S extends CrudService<DTO>> {
    
    @Autowired
    protected S crudService;
    
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public Object getPage(@RequestParam int page, @RequestParam int size, @RequestParam(required = false) String filter) {
        Page<DTO> pageData = crudService.getPage(page, size, filter);
        return Response.success(pageData.getContent(), pageData.getTotalElements());
    }
    
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public Object get(@PathVariable("id") Integer id) {
        return Response.success(crudService.get(id));
    }

    /**
     * 如果想校验参数，请打开@Valid注解，并且在相应的DTO对象上添加校验规则
     */
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public Object save(@RequestBody /*@Valid*/DTO dto) {
        return Response.success(crudService.save(dto));
    }
    
    @RequestMapping(value="{id}", method = RequestMethod.PUT)
    public Object update(@RequestBody /*@Valid*/DTO dto) {
        return Response.success(crudService.save(dto));
    }
    
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable("id") Integer id) {
    	crudService.delete(id);
        return Response.success(true);
    }
    
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public Object delete(@RequestBody List<DTO> dtoList) {
        return Response.success(crudService.delete(dtoList));
    }

}
