package ru.aston.hometask.intensive2.program.dao;

import ru.aston.hometask.intensive2.program.model.User;
import ru.aston.hometask.intensive2.program.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);

    @Override
    public Optional<User> findById(Long id) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            transaction.commit();

            if (user != null) {
                logger.info("User found with id: {}", id);
                return Optional.of(user);
            } else {
                logger.warn("User not found with id: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
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
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            List<User> users = session.createQuery("from User", User.class).list();
            transaction.commit();

            logger.info("Found {} users", users.size());
            return users;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
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
            if (transaction != null) {
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
            if (transaction != null) {
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
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
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
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            Query<User> query = session.createQuery("from User where email = :email", User.class);
            query.setParameter("email", email);
            User user = query.uniqueResult();
            transaction.commit();

            if (user != null) {
                logger.info("User found with email: {}", email);
                return Optional.of(user);
            } else {
                logger.info("User not found with email: {}", email);
                return Optional.empty();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error finding user by email: {}", email, e);
            throw new RuntimeException("Error finding user by email", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}