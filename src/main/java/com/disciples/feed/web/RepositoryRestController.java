package com.disciples.feed.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.disciples.feed.Response;
import com.disciples.feed.rest.RepositoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("${feed.rest.baseUrl:/api/admin}")
public class RepositoryRestController {
	
	private static final String BASE_URL = "{repository}";
    
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ObjectMapper objectMapper;
	private MappingJackson2HttpMessageConverter converter;
	
	@PostConstruct
	public void init() {
		this.converter = new MappingJackson2HttpMessageConverter(objectMapper);
	}
	
    @RequestMapping(value = BASE_URL + "/list", method = {RequestMethod.GET, RequestMethod.POST})
    public Object search(@PathVariable String repository, Integer page, Integer size, @RequestBody(required = false) MultiValueMap<String, Object> params) {
        Page<?> pageData = repositoryService.find(repositoryService.getDomainClass(repository), page, size, params);
        return Response.success(pageData.getContent(), pageData.getTotalElements());
    }
    
    @RequestMapping(value = BASE_URL + "/getKeyValues", method = RequestMethod.GET)
    public Object getKeyValues(@PathVariable String repository, @RequestParam(required = false) String method) {
        List<?> result = repositoryService.getKeyValues(repositoryService.getDomainClass(repository), method);
        return Response.success(result, result.size());
    }
    
    @RequestMapping(value = BASE_URL + "/add", method = RequestMethod.POST)
    public Object save(@PathVariable String repository, HttpServletRequest request) {
    	Class<?> domainClass = repositoryService.getDomainClass(repository);
    	if (domainClass == null) {
    		return Response.error(String.format("领域对象不存在：repository=%s", repository));
    	}
    	try {
			Object obj = converter.read(domainClass, new ServletServerHttpRequest(request));
			return Response.success(repositoryService.save(obj));
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
    	repositoryService.delete(repositoryService.getDomainClass(repository), id);
		return Response.success(true);
    }
    
    @RequestMapping(value = BASE_URL + "/delete", method = RequestMethod.POST)
    public Object deleteList(@PathVariable String repository, @RequestBody List<Map<String, Object>> dtoList) {
    	return Response.success(repositoryService.delete(repositoryService.getDomainClass(repository), dtoList));
    }

}
