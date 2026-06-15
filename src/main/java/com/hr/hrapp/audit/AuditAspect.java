package com.hr.hrapp.audit;

import com.hr.hrapp.entity.AuditLog;
import com.hr.hrapp.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditLogService auditLogService;

    @Around("execution(public * com.hr.hrapp.controller..*(..))")
    public Object auditControllerMethods(ProceedingJoinPoint pjp) throws Throwable {
        String username = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            username = auth.getName();
        }

        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        String endpoint = "unknown";
        if (attrs != null) {
            Object req = attrs.resolveReference(RequestAttributes.REFERENCE_REQUEST);
            if (req instanceof HttpServletRequest) {
                endpoint = ((HttpServletRequest) req).getRequestURI();
            }
        }

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        String action = method.getName().toUpperCase();

        LocalDateTime time = LocalDateTime.now();

        try {
            Object result = pjp.proceed();
            // SUCCESS
            auditLogService.save(new AuditLog(username, action, endpoint, time, "SUCCESS"));
            return result;
        } catch (Throwable ex) {
            // FAILURE
            auditLogService.save(new AuditLog(username, action, endpoint, time, "FAILURE"));
            throw ex;
        }
    }
}
