package com.sme.erp.audit.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sme.erp.audit.service.ActivityLogInvocationContext;
import com.sme.erp.audit.service.ActivityLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Locale;

@Aspect
@Component
public class ActivityLogAspect {
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    public ActivityLogAspect(ActivityLogService activityLogService, ObjectMapper objectMapper) {
        this.activityLogService = activityLogService;
        this.objectMapper = objectMapper;
    }

    @Around("execution(public * com.sme.erp..service.impl.*ServiceImpl.*(..)) && !within(com.sme.erp.audit..*)")
    public Object logBusinessAction(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String action = actionFor(method.getName());
        if (action == null) {
            return joinPoint.proceed();
        }

        ActivityLogInvocationContext.reset();
        Object result = null;
        Throwable failure = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            failure = ex;
            throw ex;
        } finally {
            try {
                if (failure == null && !ActivityLogInvocationContext.wasLogged()) {
                    String module = moduleFor(joinPoint.getSignature().getDeclaringTypeName());
                    Long entityId = extractId(result, joinPoint.getArgs());
                    String details = "Automatic audit for " + method.getName();
                    activityLogService.log(action, module, tableFor(module), entityId, safeJson(joinPoint.getArgs()), safeJson(result), details);
                }
            } finally {
                ActivityLogInvocationContext.clear();
            }
        }
    }

    private String actionFor(String methodName) {
        String name = methodName.toLowerCase(Locale.ROOT);
        if (name.startsWith("get") || name.startsWith("find") || name.startsWith("list") || name.startsWith("search")
                || name.startsWith("count") || name.startsWith("exists") || name.startsWith("to")) {
            return null;
        }
        if (name.contains("restore")) return "RESTORE";
        if (name.contains("delete") || name.contains("deactivate")) return "DELETE";
        if (name.contains("approve")) return "APPROVE";
        if (name.contains("reject")) return "REJECT";
        if (name.contains("post") || name.contains("submit")) return "POST";
        if (name.contains("reverse")) return "REVERSE";
        if (name.contains("logout")) return "LOGOUT";
        if (name.contains("login")) return "LOGIN";
        if (name.contains("update") || name.contains("edit") || name.contains("change")) return "UPDATE";
        if (name.contains("create") || name.contains("save") || name.contains("add")) return "CREATE";
        if (name.contains("cancel")) return "REJECT";
        return null;
    }

    private String moduleFor(String className) {
        String[] parts = className.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            if ("erp".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1].replace("auth", "user").toUpperCase(Locale.ROOT);
            }
        }
        return "SYSTEM";
    }

    private String tableFor(String module) {
        return module.toLowerCase(Locale.ROOT) + "_activity";
    }

    private Long extractId(Object result, Object[] args) {
        Long fromResult = readId(result);
        if (fromResult != null) {
            return fromResult;
        }
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Long value) {
                    return value;
                }
                Long fromArg = readId(arg);
                if (fromArg != null) {
                    return fromArg;
                }
            }
        }
        return null;
    }

    private Long readId(Object value) {
        if (value == null) {
            return null;
        }
        try {
            Method getter = value.getClass().getMethod("getId");
            Object id = getter.invoke(value);
            return id instanceof Number number ? number.longValue() : null;
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private String safeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException | RuntimeException ex) {
            return String.valueOf(value);
        }
    }
}
