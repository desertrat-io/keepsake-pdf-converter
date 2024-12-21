package commands;

import entities.PayloadEntity;

public interface Command<T extends PayloadEntity> {
    void invoke();

    void invoke(T entity);
}
