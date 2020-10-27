package com.lemon1234.mapper;

import java.util.List;
import java.util.Map;

import com.lemon1234.entity.Doubt;

public interface DoubtMapper {

	void addDoubt(Doubt doubt);
	
	void deletebyviewId(Integer id);
	
	void deletebyqId(Integer id);
	
	List<Doubt> list(Map<String, Object> param);
	
	Integer getCount(Map<String, Object> param);
	
	void update(Doubt doubt);
	
	void delete(Integer id);
}
