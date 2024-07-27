package com.watergun.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.reggie.entity.Employee;
import com.watergun.reggie.mapper.EmployeeMapper;
import com.watergun.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @author watergun
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements EmployeeService {

}
