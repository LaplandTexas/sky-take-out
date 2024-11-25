package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 *
 * 自定义切面，实现公共字段自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     *切入点
     *
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {}

    /**
     *
     * 自定义前置通知，为公共字段赋值
     */
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段填充");

        //1.获取当前被拦截的方法上的数据库操作类型
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //2.获取方法的参数
        Object[] args = joinPoint.getArgs();
        if (args==null || args.length==0) return;

        Object entity = args[0];
        //3.准备赋值数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //4.根据类型反射赋值
        Method setUpdateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
        Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);

        //通过反射赋值
        setUpdateUser.invoke(entity,currentId);
        setUpdateTime.invoke(entity,now);

        if (operationType==OperationType.INSERT) {
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            setCreateUser.invoke(entity,currentId);
            setCreateTime.invoke(entity,now);
        }
    }
}
