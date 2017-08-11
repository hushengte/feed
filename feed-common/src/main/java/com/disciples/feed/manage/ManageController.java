package com.disciples.feed.manage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.disciples.feed.Response;

public class ManageController {
	
	private static final String BASE_URL = "{repository}";
    
	private MappingJackson2HttpMessageConverter converter;
	
	@Autowired ManageService manageService;
	
	public ManageController() {
		Jackson2ObjectMapperFactoryBean factory = new Jackson2ObjectMapperFactoryBean();
		factory.setSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		factory.afterPropertiesSet();
		converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(factory.getObject());
	}
	
    @RequestMapping(value = BASE_URL + "/list", method = {RequestMethod.GET, RequestMethod.POST})
    public Object search(@PathVariable String repository, Integer page, Integer size, @RequestBody MultiValueMap<String, Object> params) {
        Page<?> pageData = manageService.find(manageService.getDomainClass(repository), page, size, params);
        return Response.success(pageData.getContent(), pageData.getTotalElements());
    }
    
    @RequestMapping(value = BASE_URL + "/getKeyValues", method = RequestMethod.GET)
    public Object getKeyValues(@PathVariable String repository) {
        List<?> result = manageService.getKeyValues(manageService.getDomainClass(repository));
        return Response.success(result, result.size());
    }
    
    @RequestMapping(value = BASE_URL + "/add", method = RequestMethod.POST)
    public Object save(@PathVariable String repository, HttpServletRequest request) {
    	Class<?> domainClass = manageService.getDomainClass(repository);
    	if (domainClass == null) {
    		return Response.error(String.format("领域对象不存在：repository=%s", repository));
    	}
    	try {
			Object obj = converter.read(domainClass, new ServletServerHttpRequest(request));
			if (obj == null) {
				return Response.error("请求数据不能为空");
			}
			return Response.success(manageService.save(obj));
		} catch (IOException e) {
			return Response.error(String.format("请求体数据读取失败：%s", e.getMessage()));
		}
    }
    
    @RequestMapping(value= BASE_URL + "/{id}", method = RequestMethod.PUT)
    public Object update(@PathVariable String repository, HttpServletRequest request) {
        return this.save(repository, request);
    }
    
    @RequestMapping(value = BASE_URL + "/{id}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable String repository, @PathVariable Integer id) {
    	manageService.delete(manageService.getDomainClass(repository), id, null);
        return Response.success(true);
    }
    
    @RequestMapping(value = BASE_URL + "/delete", method = RequestMethod.POST)
    public Object deleteList(@PathVariable String repository, @RequestBody List<Map<String, Object>> dtoList) {
        return Response.success(manageService.delete(manageService.getDomainClass(repository), dtoList));
    }

}
