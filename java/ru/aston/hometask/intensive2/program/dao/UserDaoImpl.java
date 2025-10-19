package ru.aston.hometask.intensive2.program.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import ru.aston.hometask.intensive2.program.model.User;
import ru.aston.hometask.intensive2.program.util.HibernateUtil;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);

    @Override
    public Optional<User> findById(Long id) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            User user = session.get(User.class, id);

            if (user != null) {
                logger.info("User found with id: {}", id);
                return Optional.of(user);
            } else {
                logger.warn("User not found with id: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error finding user by id: {}", id, e);
            throw new RuntimeException("Error finding user by id", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<User> findAll() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            List<User> users = session.createQuery("from User", User.class).list();

            logger.info("Found {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw new RuntimeException("Error finding all users", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public User save(User user) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            session.save(user);
            transaction.commit();

            logger.info("User saved successfully with id: {}", user.getId());
            return user;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error saving user: {}", user.getEmail(), e);
            throw new RuntimeException("Error saving user", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public User update(User user) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User updatedUser = (User) session.merge(user);
            transaction.commit();

            logger.info("User updated successfully with id: {}", updatedUser.getId());
            return updatedUser;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error updating user with id: {}", user.getId(), e);
            throw new RuntimeException("Error updating user", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void delete(Long id) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
                logger.info("User deleted successfully with id: {}", id);
            } else {
                logger.warn("Attempt to delete non-existing user with id: {}", id);
                throw new IllegalArgumentException("User not found with id: " + id);
            }
            transaction.commit();
        } catch (IllegalArgumentException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error deleting user with id: {}", id, e);
            throw new RuntimeException("Error deleting user", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<User> query = session.createQuery("from User where email = :email", User.class);
            query.setParameter("email", email);
            User user = query.uniqueResult();

            if (user != null) {
                logger.info("User found with email: {}", email);
                return Optional.of(user);
            } else {
                logger.info("User not found with email: {}", email);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new RuntimeException("Error finding user by email", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public boolean isEmailExistsForOtherUser(String email, Long excludeUserId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Long> query = session.createQuery(
                    "select count(u) from User u where u.email = :email and u.id != :excludeId",
                    Long.class
            );
            query.setParameter("email", email);
            query.setParameter("excludeId", excludeUserId);
            Long count = query.uniqueResult();

            boolean exists = count != null && count > 0;
            logger.info("Email {} exists for other users: {}", email, exists);
            return exists;
        } catch (Exception e) {
            logger.error("Error checking email existence: {}", email, e);
            throw new RuntimeException("Error checking email existence", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}