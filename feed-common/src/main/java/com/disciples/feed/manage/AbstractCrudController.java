package com.disciples.feed.manage;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.disciples.feed.Identifiable;
import com.disciples.feed.Response;

public abstract class AbstractCrudController<DTO extends Identifiable<Integer>, S extends CrudService<DTO, ?, ?>> {
    
    @Autowired
    protected S crudService;
    
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public Object getPage(@RequestParam int page, @RequestParam int size, @RequestBody MultiValueMap<String, Object> params) {
        Page<DTO> pageData = crudService.find(page, size, params.toSingleValueMap());
        return Response.success(pageData.getContent(), pageData.getTotalElements());
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public Object save(@RequestBody DTO dto) {
        return Response.success(crudService.save(dto));
    }
    
    @RequestMapping(value="{id}", method = RequestMethod.PUT)
    public Object update(@RequestBody DTO dto) {
        return Response.success(crudService.save(dto));
    }
    
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public Object delete(@RequestBody List<Map<String, Object>> dtoList) {
        return Response.success(crudService.delete(dtoList));
    }

}
