package com.hll.learn.dynamicdatasource.config;

/**
 * @author hell
 * @date 2018/6/28
 * @since 1.0.0
 */
public class Base {
    private String url;
    private String driverClassName;
    private String userName;
    private String password;
    /**
     * 连接池中允许的最大连接数,上线后建议大小为100-200
     */
    private Integer maximumPoolSize;
    /**
     * 连接池中允许的最小连接数。上线后建议大小为10-50
     */
    private Integer minimumIdle;
    /**
     * 一个连接的生命时长（毫秒），超时而且没被使用则被释放（retired）
     * 缺省:30分钟，建议设置比数据库超时时长少30秒
     */
    private Integer maxLifetime;
    /**
     * 等待连接池分配连接的最大时长（毫秒），超过这个时长还没可用的连接则发生SQLException
     * 缺省:30秒
     */
    private Integer connectionTimeout;
    /**
     * 一个连接idle状态的最大时长（毫秒），超时则被释放（retired），缺省:10分钟
     */
    private Integer idleTimeout;

    public Base() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public Integer getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(Integer minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public Integer getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(Integer maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @Override
    public String toString() {
        return "MasterDbConfig{}";
    }
}
