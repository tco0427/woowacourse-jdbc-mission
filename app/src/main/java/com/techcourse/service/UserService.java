package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.service.exception.NotFoundUserException;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(NotFoundUserException::new);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        Connection connection = null;
        try {
            connection = DataSourceConfig.getInstance().getConnection();
            connection.setAutoCommit(false);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    log.error("Transaction is being rolled back");
                    connection.rollback();
                } catch (SQLException excep) {
                    log.error(excep.getMessage(), excep);
                    throw new RuntimeException(excep);
                }
            }
        }
    }
}
