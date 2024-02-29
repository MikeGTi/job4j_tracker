package ru.job4j.action;

import ru.job4j.tracker.*;

import java.util.Objects;

public class FindById implements UserAction {
    private final Output out;

    public FindById(Output out) {
        this.out = out;
    }

    @Override
    public String name() {
        return "Показать заявку по id";
    }

    @Override
    public boolean execute(Input input, Store memTracker) {
        out.println("=== Вывод заявки по id ===");
        int id = input.askInt("Введите id: ");
        Item item = memTracker.findById(id);
        out.println(Objects.requireNonNullElseGet(item, () -> "Заявка с введенным id: " + id + " не найдена."));
        return true;
    }
}
