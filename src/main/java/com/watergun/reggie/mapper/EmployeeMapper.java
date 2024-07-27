package com.watergun.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watergun.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author watergun
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
