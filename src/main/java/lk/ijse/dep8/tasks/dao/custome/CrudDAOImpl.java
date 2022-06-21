package lk.ijse.dep8.tasks.dao.custome;

import lk.ijse.dep8.tasks.dao.CrudDAO;
import lk.ijse.dep8.tasks.entity.SuperEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

public class CrudDAOImpl<T extends SuperEntity, ID extends Serializable> implements CrudDAO<T, ID> {
    @PersistenceContext
    protected EntityManager entityManager;

    private Class<T> entityClassObject;

    public CrudDAOImpl() {
        entityClassObject = (Class<T>) (((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0])
    }

    @Override
    public T save(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public void deleteById(ID pk) {
        entityManager.remove(entityManager.getReference(entityClassObject, pk));
    }

    @Override
    public Optional<T> findById(ID pk) {
        T entity = entityManager.getReference(entityClassObject, pk);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    @Override
    public boolean existsById(ID pk) {
        return findById(pk).isPresent();
    }

    @Override
    public List<T> findAll() {
        return entityManager.createQuery("FROM " + entityClassObject.getName() + " u", entityClassObject).getResultList();
    }

    @Override
    public long count() {
        return entityManager.createQuery("SELECT COUNT(u) FROM " + entityClassObject.getName() + " u", Long.class).getSingleResult();
    }
}
