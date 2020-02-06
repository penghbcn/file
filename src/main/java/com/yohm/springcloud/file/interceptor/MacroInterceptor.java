package com.yohm.springcloud.file.interceptor;

import com.yohm.springcloud.file.annotation.Macro;
import com.yohm.springcloud.file.utils.MappedStatementUtil;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 处理{@link Macro}注解，对SQL做字符串替换，将SQL语句中的@xxx替换成{@link Macro#name()}为xxx的注解中的{@link Macro#content()}，例如：
 *
 * <pre>
 * {@code
 * @Macro(name = "columns", content = "id, name, cel_phone")
 * public interface UserMapper {
 *    @Select("select @columns from user where id = #{id})
 *    User queryById(@Param("id")int id);
 * }
 *
 * }
 * </pre>
 * <p>
 * 注意，宏的使用必须要跟着逗号或空白字符，或者是SQL的结尾，否则识别不了
 *
 * @author gaohang
 */
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
//        @Signature(type = XMLLanguageDriver.class, method = "createSqlSource", args = {Configuration.class,String.class,Class.class})
})
public class MacroInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(MacroInterceptor.class);

    /**
     * 记录不需要处理{@link Macro}的映射接口
     */
    private final Set<String> namespaceNotContainsMacro = Collections.synchronizedSet(new HashSet<>());

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Object[] args = invocation.getArgs();
        final MappedStatement ms = (MappedStatement) args[0];
        //这里的sqlSource是一个代理对象
        final SqlSource sqlSource = new SqlSourceDelegate(ms);
        //复制MapedStatement对象
        final MappedStatement mappedStatement = MappedStatementUtil.copyMappedStatement(ms, sqlSource);
        args[0] = mappedStatement;
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    /**
     * 对{@link SqlSource}的代理，{@link #getBoundSql(Object)}方法会返回一个{@link BoundSql}
     * 对象的cglib代理，在代理对象中处理宏的替换
     */
    private final class SqlSourceDelegate implements SqlSource {
        private final MappedStatement ms;

        private SqlSourceDelegate(MappedStatement ms) {
            this.ms = ms;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            //获取原始的BoundSql
            final BoundSql boundSql = ms.getSqlSource().getBoundSql(parameterObject);
            final String namespace = getNamespace(ms);
            if (namespace == null) {
                return boundSql;
            }
            //拦截一次
            if (namespaceNotContainsMacro.contains(namespace)) {
                return boundSql;
            }

            try {
                final Class<?> mapperInterface = Class.forName(namespace);
                final Macro[] macros = mapperInterface.getAnnotationsByType(Macro.class);
                //没有定义Macro，则返回原始的boundSql
                if (macros == null || macros.length == 0) {
                    namespaceNotContainsMacro.add(namespace);
                    return boundSql;
                }
                //对BoundSql做代理，处理getSql()返回的sql语句
                return proxyBoundSql(ms.getConfiguration(), boundSql, macros);
            } catch (ClassNotFoundException e) {
                logger.debug("load namespace class failed, maybe namespace {} is not a class", namespace, e);
                namespaceNotContainsMacro.add(namespace);
            }

            return boundSql;
        }
    }

    /**
     * 创建{@link BoundSql}的代理对象
     */
    private BoundSql proxyBoundSql(Configuration configuration, BoundSql boundSql, Macro[] macros) {
        return getProxy(configuration, boundSql, (proxy, method, args, methodProxy) -> {
            if (!Objects.equals("getSql", method.getName())) {
                return methodProxy.invoke(boundSql, args);
            }
            //处理宏
            return replaceMacroContent(macros, boundSql.getSql());
        });
    }

    private BoundSql getProxy(Configuration configuration, BoundSql target, MethodInterceptor methodInterceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(methodInterceptor);
        final Class<?>[] parameterTypes = {Configuration.class, String.class, List.class, Object.class};
        final Object[] params = new Object[]{
                configuration, target.getSql(), target.getParameterMappings(), target.getParameterObject()
        };
        return (BoundSql) enhancer.create(parameterTypes, params);
    }

    /**
     * 对sql做宏的替换
     */
    private String replaceMacroContent(Macro[] macros, String sql) {
        //识别sql字符串中的@符号，并取出@符号之后的宏名
        int lastAppended = 0;
        final StringBuilder sqlAppender = new StringBuilder();
        for (int i = sql.length(); lastAppended < i; ) {
            final int macroPlaceStart = sql.indexOf('@', lastAppended);
            if (macroPlaceStart < 0) {
                //没有了，结束
                break;
            }
            final int macroNameStart = macroPlaceStart + 1;
            if (macroNameStart == i) {
                break;
            }
            int k = macroNameStart;
            for (; k < i; k++) {
                final char ch = sql.charAt(k);
                //@macroName后面必须要跟着逗号或者空白字符，或者是SQL的结尾
                if (Character.isWhitespace(ch) || ch == ',') {
                    break;
                }
            }
            sqlAppender.append(sql, lastAppended, macroPlaceStart);
            //(macroPlaceStart, k)是宏结束
            final String macroName = sql.substring(macroNameStart, k);
            for (Macro macro : macros) {
                if (Objects.equals(macro.name(), macroName)) {
                    final String[] contents = macro.content();
                    for (String content : contents) {
                        sqlAppender.append(content).append(' ');
                    }
                }
            }
            lastAppended = k;
        }
        if (sqlAppender.length() == 0) {
            return sql;
        }
        if (lastAppended < sql.length()) {
            sqlAppender.append(sql.substring(lastAppended));
        }
        return sqlAppender.toString();
    }

    private String getNamespace(MappedStatement ms) {
        final String statementId = ms.getId();
        final int i = statementId.lastIndexOf('.');
        if (i <= 0) {
            return null;
        }
        return statementId.substring(0, i);
    }

}
